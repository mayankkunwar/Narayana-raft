package org.jboss.narayana.quickstart.txoj;

import org.jboss.narayana.quickstart.txoj.statemachine.TransactionStateMachine;
import org.jgroups.util.Util;

public class UserActions {
    private TransactionStateMachine tsm;

    public UserActions() {
    }

    public UserActions(final TransactionStateMachine stateMachine) {
        this.tsm = stateMachine;
    }

    public void setTransactionStateMachine(TransactionStateMachine stateMachine) {
        this.tsm = stateMachine;
    }

    public void loop() {
        boolean looping = true;
        while (looping) {
            int input = Util.keyPress(" [2] beginAtomicAction [3] commitAtomicAction [4] abortAtomicAction [5] createAtomicObject" +
                    " [6] getAtomicObject [7] incrementAndGetAtomicObject [8] decrementAndGetAtomicObject [9] addAndGetAtomicObject [x] exit\n" +
                    "first-applied=" + this.tsm.firstApplied() +
                    ", last-applied=" + this.tsm.lastApplied() +
                    ", commit-index=" + this.tsm.commitIndex() +
                    ", log size=" + Util.printBytes(this.tsm.logSize()) + "\n");
            switch (input) {
                case '2':
                    try {
                        this.tsm.beginAtomicAction();
                        System.out.println("AtomicAction.begin()");
                    } catch (Exception e) {
                        System.out.println("Unable to start AtomicAction" + e);
                    }
                    break;
                case '3':
                    try {
                        this.tsm.commitAtomicAction();
                        System.out.println("AtomicAction.commit()");
                    } catch (Exception e) {
                        System.out.println("Unable to commit AtomicAction" + e);
                    }
                    break;
                case '4':
                    try {
                        this.tsm.abortAtomicAction();
                        System.out.println("AtomicAction.abort()");
                    } catch (Exception e) {
                        System.out.println("Unable to abort AtomicAction" + e);
                    }
                    break;
                case '5':
                    try {
                        this.tsm.createAtomicObject(1234);
                        System.out.println("AtomicObject created with value ; 1234");
                    } catch (Exception e) {
                        System.out.println("Unable to create AtomicObject" + e);
                    }
                    break;
                case '6':
                    try {
                        AtomicObject ao = (AtomicObject) this.tsm.getAtomicObject();
                        System.out.println("AtomicObject Value set to : " + ao.get());
                    } catch (Exception e) {
                        System.out.println("Unable to inc AtomicObject value" + e);
                    }
                    break;
                case '7':
                    try {
                        AtomicObject ao = (AtomicObject) this.tsm.incrementAndGetAtomicObject();
                        System.out.println("Value after increment : " + ao.get());
                    } catch (Exception e) {
                        System.out.println("Unable to inc AtomicObject value" + e);
                    }
                    break;
                case '8':
                    try {
                        AtomicObject ao = (AtomicObject) this.tsm.decrementAndGetAtomicObject();
                        System.out.println("Value after decreasing by 1 : " + ao.get());
                    } catch (Exception e) {
                        System.out.println("Unable to decrease AtomicObject value" + e);
                    }
                    break;
                case '9':
                    try {
                        AtomicObject ao = (AtomicObject) this.tsm.addAndGetAtomicObject(11111);
                        System.out.println("Value after increment : " + ao.get());
                    } catch (Exception e) {
                        System.out.println("Unable to increase AtomicObject value" + e);
                    }
                    break;
                case 'x':
                    looping = false;
                    break;
            }
        }
    }
}
