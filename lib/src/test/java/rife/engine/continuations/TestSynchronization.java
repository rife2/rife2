/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.continuations;

import rife.engine.Context;
import rife.engine.Element;

public class TestSynchronization implements Element {
    private final Object monitorMember_ = new Object();
//    private static Object sMonitorStatic = new Object();

    public void process(Context c) {
//        synchronized (this) {
//        }
//
        synchronized (this) {
            c.print("monitor this");
        }
//        c.print("\n" + c.continuationId());
//
//        c.pause();

        synchronized (monitorMember_) {
            c.print("monitor member");
        }
//        c.print("\n" + c.continuationId());

        c.pause();
//
//        synchronized (sMonitorStatic) {
//            c.print("monitor static");
//        }
//        c.print("\n" + c.continuationId());
//
//        c.pause();
//
//        c.print("done");
    }
}
