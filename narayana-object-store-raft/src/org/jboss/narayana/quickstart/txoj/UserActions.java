package org.jboss.narayana.quickstart.txoj;

import org.jboss.narayana.quickstart.txoj.statemachine.TransactionStateMachine;
import org.jboss.narayana.quickstart.txoj.util.Utility;
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
        boolean userInput=true;
        while (looping) {
            String input = Utility.readConsoleInput("Select sequence of commands with one space in between(' ') (Input Example: 2 5 3)\n [2] beginAtomicAction [3] commitAtomicAction [4] abortAtomicAction [5] createAtomicObject" +
                    " [6] getAtomicObject [7] incrementAndGetAtomicObject [8] decrementAndGetAtomicObject [9] addAndGetAtomicObject [x] exit\n" +
                    "first-applied=" + this.tsm.firstApplied() +
                    ", last-applied=" + this.tsm.lastApplied() +
                    ", commit-index=" + this.tsm.commitIndex() +
                    ", log size=" + Util.printBytes(this.tsm.logSize()) + "\n", userInput);
            if (input.equals('x')) {
                looping = false;
                userInput = true;
            } else if (!input.trim().isEmpty()){
                try {
                    System.out.println("Started Executing the commands in given sequence..");
                    String output = (String) this.tsm.execUserCommands(input);
                    System.out.println("Output " + String.valueOf(output));
                } catch (Exception e) {
                    System.out.println("Unable to start AtomicAction" + e);
                }
                userInput = true;
            } else{
                userInput = false;
            }
        }
    }
}
