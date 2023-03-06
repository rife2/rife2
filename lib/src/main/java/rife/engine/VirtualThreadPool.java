/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jetty.util.thread.ThreadPool;
import rife.engine.exceptions.VirtualThreadsNotAvailableException;

class VirtualThreadPool implements ThreadPool {
    private final ExecutorService executorService_;

    static boolean areVirtualThreadsAvailable() {
        if (Float.parseFloat(System.getProperty("java.specification.version")) < 19) {
            return false;
        }

        try {
            var klass = Thread.class;
            var method = klass.getDeclaredMethod("ofVirtual");
            if (method != null) {
                try {
                    return method.invoke(klass) != null;
                } catch (Throwable e) {
                    return false;
                }
            }
        } catch (Throwable e) {
            return false;
        }
        return false;
    }

    VirtualThreadPool() {
        try {
            var klass = Executors.class;
            var method = klass.getDeclaredMethod("newVirtualThreadPerTaskExecutor");
            executorService_ = (ExecutorService) method.invoke(klass);
            System.out.println("Using virtual threads");
        } catch (Throwable e) {
            throw new VirtualThreadsNotAvailableException(e);
        }
    }

    @Override
    public void join()
    throws InterruptedException {
    }

    @Override
    public int getThreads() {
        return 1;
    }

    @Override
    public int getIdleThreads() {
        return 1;
    }

    @Override
    public boolean isLowOnThreads() {
        return false;
    }

    @Override
    public void execute(Runnable command) {
        executorService_.submit(command);
    }
}
