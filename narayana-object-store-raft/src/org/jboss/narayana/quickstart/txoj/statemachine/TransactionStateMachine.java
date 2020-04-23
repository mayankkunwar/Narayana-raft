package org.jboss.narayana.quickstart.txoj.statemachine;

import org.jboss.narayana.quickstart.txoj.TransactionActions;
import org.jgroups.JChannel;
import org.jgroups.protocols.raft.InternalCommand;
import org.jgroups.protocols.raft.RAFT;
import org.jgroups.protocols.raft.Role;
import org.jgroups.protocols.raft.StateMachine;
import org.jgroups.raft.RaftHandle;
import org.jgroups.util.Bits;
import org.jgroups.util.ByteArrayDataInputStream;
import org.jgroups.util.ByteArrayDataOutputStream;
import org.jgroups.util.Util;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.concurrent.TimeUnit;

public class TransactionStateMachine implements StateMachine, RAFT.RoleChange {

    private TransactionActions txActions;

    protected JChannel ch;
    protected RaftHandle raft;
    protected long repl_timeout = 20000; // timeout (ms) to wait for a majority to ack a write

    /**
     * If true, reads can return the local transactionObject value directly. Else, reads have to go through the leader
     */
    protected boolean allow_dirty_reads = false;
    private Role role;

    public JChannel channel() {
        return this.ch;
    }

    public boolean isLeader() {
        return this.raft.isLeader();
    }

    protected enum TxCommand {
        execUserCommand, createAtomicAction, beginAtomicAction, commitAtomicAction, abortAtomicAction,
        getAtomicObject, createAtomicObject, incrementAndGetAtomicObject, decrementAndGetAtomicObject, addAndGetAtomicObject
    }

    public TransactionStateMachine(JChannel ch) {
        this.txActions = new TransactionActions();
        setChannel(ch);
    }

    private void setChannel(JChannel ch) {
        this.ch = ch;
        this.raft = new RaftHandle(this.ch, this);
        this.raft.addRoleListener(this);
    }

    public void addRoleChangeListener(RAFT.RoleChange listener) {
        raft.addRoleListener(listener);
    }

    public long replTimeout() {
        return repl_timeout;
    }

    public TransactionStateMachine replTimeout(long timeout) {
        this.repl_timeout = timeout;
        return this;
    }

    public boolean allowDirtyReads() {
        return allow_dirty_reads;
    }

    public TransactionStateMachine allowDirtyReads(boolean flag) {
        allow_dirty_reads = flag;
        return this;
    }

    public int lastApplied() {
        return raft.lastApplied();
    }

    public int commitIndex() {
        return raft.commitIndex();
    }

    public void snapshot() throws Exception {
        raft.snapshot();
    }

    public int logSize() {
        return raft.logSizeInBytes();
    }

    public String raftId() {
        return raft.raftId();
    }

    public TransactionStateMachine raftId(String id) {
        raft.raftId(id);
        return this;
    }

    public void dumpLog() {
        raft.logEntries((entry, index) -> {
            StringBuilder sb = new StringBuilder().append(index).append(" (").append(entry.term()).append("): ");
            if (entry.command() == null) {
                sb.append("<marker record>");
                System.out.println(sb);
                return;
            }
            if (entry.internal()) {
                try {
                    InternalCommand cmd = Util.streamableFromByteBuffer(InternalCommand.class,
                            entry.command(), entry.offset(), entry.length());
                    sb.append("[internal] ").append(cmd).append("\n");
                } catch (Exception ex) {
                    sb.append("[failure reading internal cmd] ").append(ex).append("\n");
                }
                System.out.println(sb);
                return;
            }
            ByteArrayDataInputStream in = new ByteArrayDataInputStream(entry.command(), entry.offset(), entry.length());
            try {
                TransactionStateMachine.TxCommand type = TransactionStateMachine.TxCommand.values()[in.readByte()];
                Object val;
                switch (type) {
                    case execUserCommand:
                        val = Util.objectFromStream(in);
                        sb.append("execUserCommand sequence : "+ String.valueOf(val));
                        break;
                    case createAtomicAction:
                        sb.append("createAtomicAction()");
                        break;
                    case beginAtomicAction:
                        sb.append("beginAtomicAction()");
                        break;
                    case commitAtomicAction:
                        sb.append("commitAtomicAction()");
                        break;
                    case abortAtomicAction:
                        sb.append("abortAtomicAction()");
                        break;
                    case getAtomicObject:
                        sb.append("getAtomicObject()");
                        break;
                    case createAtomicObject:
                        sb.append("createAtomicObject()");
                        break;
                    case incrementAndGetAtomicObject:
                        sb.append("incrementAndGetAtomicObject()");
                        break;
                    case decrementAndGetAtomicObject:
                        val = Util.objectFromStream(in);
                        sb.append("decrementAndGetAtomicObject()");
                        break;
                    case addAndGetAtomicObject:
                        val = Util.objectFromStream(in);
                        sb.append("addAndGetAtomicObject()");
                        break;
                    default:
                        sb.append("type " + type + " is unknown");
                }
            } catch (Throwable t) {
                sb.append(t);
            }
            System.out.println(sb);
        });
    }

    @Override
    public void roleChanged(Role role) {
        this.role = role;
        System.out.println(this.raftId()+"'s ROLE changed to "+role.name());
    }

    ///////////////////////////////////////// StateMachine callbacks /////////////////////////////////////

    @Override
    public byte[] apply(byte[] data, int offset, int length) throws Exception {
        ByteArrayDataInputStream in = new ByteArrayDataInputStream(data, offset, length);
        TransactionStateMachine.TxCommand command = TransactionStateMachine.TxCommand.values()[in.readByte()];
        Object val;
        switch (command) {
            case execUserCommand:
                val = Util.objectFromStream(in);
                return Util.objectToByteBuffer(this.txActions.executeUserTx((String) val));
            case commitAtomicAction:
                this.txActions.commitTransaction();
                break;
            case abortAtomicAction:
                this.txActions.abortTransaction();
                break;
            case getAtomicObject:
                return Util.objectToByteBuffer(this.txActions.getAtomicObject());
            case createAtomicObject:
                val = Util.objectFromStream(in);
                this.txActions.createAtomicObject(val);
                break;
            case incrementAndGetAtomicObject:
                return Util.objectToByteBuffer(this.txActions.incNGetAtomicObj(1));
            case decrementAndGetAtomicObject:
                return Util.objectToByteBuffer(this.txActions.incNGetAtomicObj(-1));
            case addAndGetAtomicObject:
                int v1 = Bits.readInt(in);
                return Util.objectToByteBuffer(this.txActions.incNGetAtomicObj(v1));
            default:
                throw new IllegalArgumentException("command " + command + " is unknown");
        }
        return Util.objectToByteBuffer(null);
    }

    @Override
    public void readContentFrom(DataInput dataInput) throws Exception {
        Object obj = Util.objectFromStream(dataInput);
        this.txActions.setAtomicInstances(obj);
    }

    @Override
    public void writeContentTo(DataOutput dataOutput) throws Exception {
        this.txActions.writeContentTo(dataOutput);
    }

    ///////////////////////////////////// End of StateMachine callbacks ///////////////////////////////////


    public Object execUserCommands(final String input) throws Exception {
        return invoke(TransactionStateMachine.TxCommand.execUserCommand, input, false);
    }

    protected Object invoke(TransactionStateMachine.TxCommand command, Object val, boolean ignore_return_value) throws Exception {
        ByteArrayDataOutputStream out = new ByteArrayDataOutputStream(2048);
        try {
            out.writeByte(command.ordinal());
            Util.objectToStream(val, out);
        } catch (Exception ex) {
            throw new Exception("serialization failure ( val=" + val + ")", ex);
        }
        byte[] buf = out.buffer();
        byte[] rsp = raft.set(buf, 0, out.position(), repl_timeout, TimeUnit.MILLISECONDS);
        return ignore_return_value ? null : (Object) Util.objectFromByteBuffer(rsp);
    }

    public Role getRole() {
        return this.role;
    }

    private Object getAtomicObjectDirectly() {
        return this.txActions.getAtomicObject();
    }

    public int firstApplied() {
        RAFT raft = this.channel().getProtocolStack().findProtocol(RAFT.class);
        return raft.log().firstAppended();
    }
}
