package org.jboss.narayana.quickstart.txoj;

import com.arjuna.ats.arjuna.AtomicAction;

public class AtomicOps {

    public static void main(String[] args) throws Exception {
        AtomicAction a = new AtomicAction();
        a.begin();

        org.jboss.narayana.quickstart.txoj.AtomicObject obj = new org.jboss.narayana.quickstart.txoj.AtomicObject();
        obj.set(1234);
        a.commit();

        a = new AtomicAction();
        a.begin();
        try {
            if (obj.get() != 1234) {
                throw new RuntimeException("The object was not set to 1234");
            }
        } finally {
            a.commit();
        }

        a = new AtomicAction();
        a.begin();
        obj.incr(1);
        a.abort();

        a = new AtomicAction();
        a.begin();
        try {
            if (obj.get() != 1234) {
                throw new RuntimeException(
                        "The object was not set to 1234 after abort");
            }
        } finally {
            a.commit();
        }

        a = new AtomicAction();
        a.begin();
        obj.incr(11111);
        a.commit();

        a = new AtomicAction();
        a.begin();
        try {
            if (obj.get() != 12345) {
                throw new RuntimeException(
                        "The object was not set to 12345 after commit");
            }
        } finally {
            a.commit();
        }

        System.out.println("Atomic object operated as expected");
    }
}
