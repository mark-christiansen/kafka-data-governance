package com.mycompany.kafka.governance.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.Super;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class AgentMain {

    public static void premain(String agentOps, Instrumentation inst) {
        instrument(agentOps, inst);
    }

    public static void agentmain(String agentOps, Instrumentation inst) {
        instrument(agentOps, inst);
    }

    public static void instrument(String agentOps, Instrumentation inst) {

        new AgentBuilder.Default()
                .with(new AgentBuilder.InitializationStrategy.SelfInjection.Eager())
                .type(named("com.mycompany.kafka.governance.agent.AgentTest"))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.method(
                                isPublic()
                                        .and(named("test"))
                                        .and(takesArgument(0, named("java.lang.String")))
                                        .and(takesArgument(1, named("java.lang.Integer")))
                                        .and(takesArgument(2, named("java.lang.Double")))
                        ).intercept(MethodDelegation.to(MyInterceptor.class)))
                .installOn(inst);

        /*
        new AgentBuilder.Default()
                .with(new AgentBuilder.InitializationStrategy.SelfInjection.Eager())
                .type(named("org.apache.kafka.clients.producer.KafkaProducer"))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.method(
                            isPublic()
                                .and(named("send"))
                                .and(takesArgument(0, named("org.apache.kafka.clients.producer.ProducerRecord")))
                                .and(takesArgument(1, named("org.apache.kafka.clients.producer.Callback")))
                        ).intercept(MethodDelegation.to(MyInterceptor.class)))
                .installOn(inst);
         */
    }

    public static class MyInterceptor {
        public static String test(@Super AgentTest test,
                                  @AllArguments Object[] args) {
            System.out.println("intercepted! " + AgentTest.class.getName());
            System.out.println("args=" + args);
            if (!args[0].equals("skip")) {
                try {
                    return test.test((String)args[0], (Integer)args[1], (Double)args[2]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return test.test("skipped", 0, 0d);
        }
    }

    /*
    public static class MyInterceptor {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static Future<RecordMetadata> send(@SuperCall Callable<Future<RecordMetadata>> superClass,
                                                  @Origin final Method method,
                                                  @AllArguments Object[] args) {
            System.out.println("intercepted! " + AgentTest.class.getName());
            try {
                return superClass.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
     */
}
