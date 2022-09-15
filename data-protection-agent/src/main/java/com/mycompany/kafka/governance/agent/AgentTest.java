package com.mycompany.kafka.governance.agent;

public class AgentTest {
    public static void main(String[] args) {
        new AgentTest().test(args[0], Integer.parseInt(args[1]), Double.parseDouble(args[2]));
    }

    public String test(String arg1, Integer arg2, Double arg3) {
        System.out.println("arg1=" + arg1);
        System.out.println("arg2=" + arg2);
        System.out.println("arg3=" + arg3);
        return arg1 + "|" + arg2 + "|" + arg3;
    }
}
