/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifeworkflowtests;

import rife.workflow.Work;
import rife.workflow.Workflow;

import java.util.List;

/** Test fixtures for durable event hand-offs on a multi-thread executor. */
public class DurableHandoffWork {
    private static final Object ITEM = new Object();
    private static final Object TAKEN = new Object();

    public static class Producer implements Work {
        private final int rounds_;

        public Producer(int rounds) {
            rounds_ = rounds;
        }

        public void execute(Workflow workflow) {
            for (var i = 1; i <= rounds_; ++i) {
                workflow.trigger(ITEM, i);
                var taken = pauseForEvent(TAKEN);
                if (!Integer.valueOf(i).equals(taken.getData())) {
                    throw new IllegalStateException("Unexpected acknowledgement " + taken.getData());
                }
            }
        }
    }

    public static class Consumer implements Work {
        private final int rounds_;
        private final List<Integer> received_;

        public Consumer(int rounds, List<Integer> received) {
            rounds_ = rounds;
            received_ = received;
        }

        public void execute(Workflow workflow) {
            for (var i = 1; i <= rounds_; ++i) {
                var item = pauseForEvent(ITEM);
                received_.add((Integer) item.getData());
                workflow.trigger(TAKEN, item.getData());
            }
        }
    }
}
