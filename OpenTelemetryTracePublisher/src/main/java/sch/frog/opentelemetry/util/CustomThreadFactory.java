package sch.frog.opentelemetry.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadFactory implements ThreadFactory {

    private final String prefix;

    private final AtomicInteger increment = new AtomicInteger(0);

    public CustomThreadFactory(String prefix) {
        this.prefix = prefix;
    }


    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName(prefix + "-" + increment.getAndIncrement());
        return t;
    }
}
