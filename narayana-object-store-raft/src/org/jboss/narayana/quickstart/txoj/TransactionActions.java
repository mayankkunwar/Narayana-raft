package org.jboss.narayana.quickstart.txoj;

import com.arjuna.ats.arjuna.AtomicAction;
import org.jgroups.util.Util;

import java.io.DataOutput;
import java.io.IOException;

public class TransactionActions {

    private AtomicAction atomicAction;
    private AtomicObject atomicObject;
    private int atomicCounter = 0;

    public AtomicObject getAtomicObject() {
        return this.atomicObject;
    }

    public void createAtomicObject(final Object val) throws Exception {
        synchronized (this) {
            if (this.atomicObject == null) {
                this.atomicObject = new AtomicObject();
            }
            atomicObject.set((Integer) val);
            atomicObject.releaselock(atomicObject.get_uid());
        }
    }

    public AtomicObject incNGetAtomicObj(final int val) throws Exception {
        this.atomicObject.incr(val);
        return this.atomicObject;
    }

    public void beginTransaction() {
        try {
            this.atomicAction = (AtomicAction) Class.forName("com.arjuna.ats.arjuna.AtomicAction").newInstance();
            this.atomicAction.begin();
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            System.out.println(e);
        }
    }

    public void commitTransaction() {
        this.atomicAction.commit();
    }

    public void abortTransaction() {
        this.atomicAction.abort();
    }

    public void setAtomicInstances(final Object obj) {
        if (obj instanceof AtomicObject) {
            this.atomicObject = (AtomicObject) obj;
        } else if (obj instanceof AtomicAction) {
            this.atomicAction = (AtomicAction) obj;
        }
    }

    public void writeContentTo(final DataOutput dataOutput) throws IOException {
        Util.objectToStream(this.atomicObject, dataOutput);
        Util.objectToStream(this.atomicAction, dataOutput);
    }

    public String executeUserTx(String commandSequence) {
        String[] cmd = commandSequence.split(" ");
        System.out.println("executeUserTx.UserCommands = " + commandSequence);
        for (String input : cmd) {
            performActions(input.trim().charAt(0));
        }
        return "value of atomic counter is " + getAtomicCounterVal();
    }

    private int getAtomicCounterVal() {
        if (this.atomicObject != null) {
            try {
                this.beginTransaction();
                this.atomicCounter = this.getAtomicObject().get();
                this.commitTransaction();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        return this.atomicCounter;
    }

    private void performActions(char input) {
        switch (input) {
            case '2':
                try {
                    this.beginTransaction();
                    System.out.println("performActions - AtomicAction.begin()");
                } catch (Exception e) {
                    System.out.println("Unable to start AtomicAction" + e);
                }
                break;
            case '3':
                try {
                    this.commitTransaction();
                    System.out.println("AtomicAction.commit()");
                } catch (Exception e) {
                    System.out.println("Unable to commit AtomicAction" + e);
                }
                break;
            case '4':
                try {
                    this.abortTransaction();
                    System.out.println("AtomicAction.abort()");
                } catch (Exception e) {
                    System.out.println("Unable to abort AtomicAction" + e);
                }
                break;
            case '5':
                try {
                    this.createAtomicObject(1234);
                    System.out.println("AtomicObject created with value ; 1234");
                } catch (Exception e) {
                    System.out.println("Unable to create AtomicObject" + e);
                }
                break;
            case '6':
                try {
                    AtomicObject ao = (AtomicObject) this.getAtomicObject();
                    System.out.println("AtomicObject Value set to : " + ao.get());
                } catch (Exception e) {
                    System.out.println("Unable to inc AtomicObject value" + e);
                }
                break;
            case '7':
                try {
                    AtomicObject ao = (AtomicObject) this.incNGetAtomicObj(1);
                    System.out.println("Value after increment : " + ao.get());
                } catch (Exception e) {
                    System.out.println("Unable to inc AtomicObject value" + e);
                }
                break;
            case '8':
                try {
                    AtomicObject ao = (AtomicObject) this.incNGetAtomicObj(-1);
                    System.out.println("Value after decreasing by 1 : " + ao.get());
                } catch (Exception e) {
                    System.out.println("Unable to decrease AtomicObject value" + e);
                }
                break;
            case '9':
                try {
                    AtomicObject ao = (AtomicObject) this.incNGetAtomicObj(11111);
                    System.out.println("Value after increment : " + ao.get());
                } catch (Exception e) {
                    System.out.println("Unable to increase AtomicObject value" + e);
                }
                break;
            default:
                System.out.println("Wrong choice ; " + input);
                break;
        }
    }
}
