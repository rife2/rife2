/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.examples;

import rife.engine.Server;
import rife.engine.Site;
import rife.workflow.Event;
import rife.workflow.Work;
import rife.workflow.Workflow;

import static rife.examples.HelloWorkflow.WorkType.*;

public class HelloWorkflow extends Site {
    enum WorkType {
        NOTIFICATION, PAYMENT, CONFIRMATION
    }

    final Workflow workflow = createWorkflow();

    public class NotificationWork implements Work {
        public void execute(Workflow workflow) {
            while (true) {
                var event = pauseForEvent(NOTIFICATION);

                System.out.println("Sending notification email " + event.getData());
            }
        }
    }

    public class PaymentWork implements Work {
        public void execute(Workflow workflow) {
            while (true) {
                var event = pauseForEvent(PAYMENT);

                System.out.println("Waiting for confirmation " + event.getData());
                workflow.start(new ConfirmationWork(event.getData()));
                workflow.trigger(NOTIFICATION, event.getData());
            }
        }
    }

    public class ConfirmationWork implements Work {
        private final Object expectedAccount_;
        public ConfirmationWork(Object account) {
            expectedAccount_ = account;
        }
        public void execute(Workflow workflow) {
            Event event;
            do {
                event = pauseForEvent(CONFIRMATION);
            } while (event.getData() == null ||
                     !event.getData().equals(expectedAccount_));

            System.out.println("Sending payment " + event.getData());
        }
    }

    public void setup() {
        workflow.start(new NotificationWork()).start(new PaymentWork());

        get("/notification", c -> workflow.trigger(NOTIFICATION, c.parameter("account")));
        get("/payment", c -> workflow.trigger(PAYMENT, c.parameter("account")));
        get("/confirm", c -> workflow.inform(CONFIRMATION, c.parameter("account")));
    }

    public static void main(String[] args) {
        new Server().start(new HelloWorkflow());
    }
}
