package org.jboss.narayana.quickstart.txoj.util;

import java.util.Scanner;

public class Utility {
    public static String readConsoleInput(String msg, boolean userInput) {
        if (userInput){
            System.out.println(msg);
        }
        Scanner in = new Scanner(System.in);
        return in.nextLine();
    }
}
