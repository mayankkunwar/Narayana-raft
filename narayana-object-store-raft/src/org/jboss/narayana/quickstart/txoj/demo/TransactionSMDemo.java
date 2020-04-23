package org.jboss.narayana.quickstart.txoj.demo;

import org.jboss.narayana.quickstart.txoj.UserActions;
import org.jboss.narayana.quickstart.txoj.statemachine.TransactionStateMachine;
import org.jgroups.JChannel;
import org.jgroups.jmx.JmxConfigurator;
import org.jgroups.protocols.raft.ELECTION;
import org.jgroups.util.Util;

import java.util.ArrayList;
import java.util.List;

public class TransactionSMDemo {
    private static UserActions userActions = new UserActions();
    private static List<TransactionStateMachine> nodeList = new ArrayList<>();

    protected static TransactionStateMachine start(String props, final String name) throws Exception {
        JChannel ch = new JChannel(props).name(name);
        TransactionStateMachine tsm = new TransactionStateMachine(ch).raftId(name);
//        if (follower)
//            disableElections(ch);
        tsm.channel().connect("rsm");

        /*if (follower)
            disableElections(ch);
        ch.setReceiver(this);
        try {
            tsm.channel().connect("rsm");
            tsm.addRoleChangeListener(this);
        } finally {
            Util.close(ch);
        }*/
        return tsm;
    }

    protected static void disableElections(JChannel ch) {
        ELECTION election = ch.getProtocolStack().findProtocol(ELECTION.class);
        if (election != null)
            election.noElections(true);
    }


    public static void main(String[] args) throws Exception {
        String props = "narayana-raft.xml";
        addNewStateMachine(props, args[0]);
        String cluster = "rsm";
//        registerNodes(cluster);

        Thread.sleep(2000);
//        checkRoles();

//        chooseNodeNOperate();
//        unregisterNodes(cluster);
        TransactionStateMachine tsm = nodeList.get(0);
        if (tsm !=null){
            userActions.setTransactionStateMachine(nodeList.get(0));
            userActions.loop();
        } else {
            throw new Exception("Cannot start state machine. Try to restart JVM.");
        }
    }

    private static void addNewStateMachine(String props, String name) throws Exception {
        TransactionStateMachine sm1 = start(props, name);
        if (sm1 == null) {
            System.out.println(name + "state machine is null");
        }
        nodeList.add(sm1);
    }

    private static void chooseNodeNOperate() {
        boolean looping = true;
        while (looping) {
            System.out.println("Choose any one :");
            System.out.println("[1] Leader");
            System.out.println("[2] Follower");
            System.out.println("[x] Exit");
            int input = Util.keyPress("ROLE:");
            switch (input) {
                case '1':
                    TransactionStateMachine leader = getLeader();
                    performOps(leader);
                    break;
                case '2':
                    TransactionStateMachine follower = getFollower();
                    performOps(follower);
                    break;
                case 'x':
                    looping = false;
                    break;
                default:
                    System.out.println("Wrong choice. TRY again");
                    break;
            }
        }
    }

    private static void performOps(TransactionStateMachine tsm) {
        if (tsm != null) {
            System.out.println(tsm.raftId() + " started looping..");
            userActions.setTransactionStateMachine(tsm);
            userActions.loop();
        } else {
            System.out.println("No nodes available.");
        }
    }

    private static TransactionStateMachine getFollower() {
        for (TransactionStateMachine tsm : nodeList) {
            if (!tsm.isLeader()) {
                return tsm;
            }
        }
        return null;
    }

    private static TransactionStateMachine getLeader() {
        for (TransactionStateMachine tsm : nodeList) {
            if (tsm.isLeader()) {
                return tsm;
            }
        }
        return null;
    }

    private static void checkRoles() {
        for (TransactionStateMachine tsm : nodeList) {
            System.out.println(tsm.raftId() + "'s ROLE is " + ((null == tsm.getRole()) ? "null" : tsm.getRole().name()));
        }
    }

    private static void unregisterNodes(String cluster) throws Exception {
        for (TransactionStateMachine tsm : nodeList) {
            JmxConfigurator.unregisterChannel(tsm.channel(), Util.getMBeanServer(), cluster);
        }
    }

    private static void registerNodes(String cluster) {
        for (TransactionStateMachine tsm : nodeList) {
            Util.registerChannel(tsm.channel(), cluster);
        }
    }
}
