/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.engine.Server;
import rife.engine.Site;
import rife.workflow.*;

import java.util.concurrent.atomic.AtomicBoolean;

public class HelloWorkflow extends Site {
    enum WorkType {
        EMAIL, PAYMENT, CONFIRMATION
    }

    final Workflow workflow = createWorkflow();

    public class EmailWork implements Work {
        public void execute(Workflow workflow) {
            while (true) {
                var event = pauseForEvent(WorkType.EMAIL);

                System.out.println("Sending email " + event.getData());
            }
        }
    }

    public class PaymentWork implements Work {
        public void execute(Workflow workflow) {
            while (true) {
                var event = pauseForEvent(WorkType.PAYMENT);

                System.out.println("Waiting for confirmation " + event.getData());
                workflow.start(new ConfirmationWork((String)event.getData()));
                workflow.trigger(WorkType.EMAIL, event.getData());
            }
        }
    }

    public class ConfirmationWork implements Work {
        private final String expectedEmail_;
        public ConfirmationWork(String email) {
            expectedEmail_ = email;
        }
        public void execute(Workflow workflow) {
            Event event;
            do {
                event = pauseForEvent(WorkType.CONFIRMATION);
            } while (!event.getData().equals(expectedEmail_));

            System.out.println("Sending payment " + event.getData());
        }
    }

    public void setup() {
        workflow.start(new EmailWork()).start(new PaymentWork());

        get("/email", c -> workflow.trigger(WorkType.EMAIL, c.parameter("account")));
        get("/payment", c -> workflow.trigger(WorkType.PAYMENT, c.parameter("account")));
        get("/confirm", c -> workflow.trigger(WorkType.CONFIRMATION, c.parameter("account")));
    }

    public static void main(String[] args) {
        new Server().start(new HelloWorkflow());
    }
}
