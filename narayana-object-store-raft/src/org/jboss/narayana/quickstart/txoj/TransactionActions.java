package org.jboss.narayana.quickstart.txoj;

import com.arjuna.ats.arjuna.AtomicAction;
import org.jgroups.util.Util;

import java.io.DataOutput;
import java.io.IOException;

public class TransactionActions {

    private AtomicAction atomicAction;
    private AtomicObject atomicObject;

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
}
