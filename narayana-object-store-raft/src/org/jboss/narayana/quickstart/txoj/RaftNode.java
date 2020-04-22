package org.jboss.narayana.quickstart.txoj;

import org.jgroups.JChannel;
import org.jgroups.protocols.raft.StateMachine;
import org.jgroups.raft.RaftHandle;

public class RaftNode {
    private String raftId;
    private StateMachine rsm;
    private RaftHandle raftHandle;

    public RaftNode(final String raftId, final String cluster, final StateMachine sm) throws Exception {
        this.raftId = raftId;
        JChannel channel = new JChannel("raft.xml").name(raftId);
        this.rsm = sm;
        raftHandle = new RaftHandle(channel, sm).raftId(raftId);
        channel.connect(cluster);
    }

    public String getRaftId(){
        return this.raftId;
    }

    public RaftHandle getRaftHandle(){
        return this.raftHandle;
    }
}
