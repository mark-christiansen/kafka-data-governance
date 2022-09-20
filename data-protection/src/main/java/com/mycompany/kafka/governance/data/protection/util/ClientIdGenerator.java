package com.mycompany.kafka.governance.data.protection.util;

import java.util.concurrent.atomic.AtomicInteger;

public class ClientIdGenerator {
    private static final AtomicInteger id = new AtomicInteger(0);

    public static int nextClientId() {
        return id.getAndIncrement();
    }
}
