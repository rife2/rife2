/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import org.junit.jupiter.api.Test;
import rife.continuations.exceptions.CallTargetNotFoundException;
import rife.continuations.exceptions.ContinuationsNotActiveException;
import rife.continuations.exceptions.MissingActiveContinuationConfigRuntimeException;
import rife.continuations.exceptions.NoContinuableInstanceAvailableException;
import rife.database.Datasource;
import rife.database.DbQueryManager;
import rife.database.TestDatasources;
import rife.database.queries.CreateTable;
import rife.database.queries.DropTable;
import rife.database.queries.Insert;
import rife.database.queries.Select;
import rife.test.MockConversation;
import rife.tools.exceptions.ControlFlowRuntimeException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises what the control flow of the engine does to a transaction that
 * it jumps out of.
 */
public class TestEngineTransactions {
    private final Datasource datasource_ = TestDatasources.H2;

    @Test
    void testARedirectInsideATransactionCommits() {
        var manager = new DbQueryManager(datasource_);
        manager.executeUpdate(new CreateTable(datasource_)
            .table("txredirect")
            .column("id", int.class, CreateTable.NOTNULL));
        try {
            var conversation = new MockConversation(new Site() {
                public void setup() {
                    var thanks = get("/thanks", c -> c.print("thanks"));
                    post("/subscribe", c -> manager.inTransaction(() -> {
                        // storing and then sending the browser on is the
                        // natural way to write a submission, so the jump
                        // commits the work that was done before it
                        manager.executeUpdate(new Insert(datasource_)
                            .into("txredirect").field("id", 1));
                        c.redirect(thanks);
                    }));
                }
            });

            var response = conversation.doRequest("/subscribe",
                new rife.test.MockRequest().method(RequestMethod.POST));

            assertTrue(response.getHeader("Location").endsWith("/thanks"));
            assertEquals(1, manager.executeGetFirstInt(new Select(datasource_)
                .field("count(*)").from("txredirect")));
        } finally {
            manager.executeUpdate(new DropTable(datasource_).table("txredirect"));
        }
    }

    @Test
    void testTheContinuationsFailuresArentControlFlow() {
        // these report that something is broken, which is nothing that a
        // transaction should commit on or that a catch block may not see
        assertFalse(ControlFlowRuntimeException.class.isAssignableFrom(ContinuationsNotActiveException.class));
        assertFalse(ControlFlowRuntimeException.class.isAssignableFrom(CallTargetNotFoundException.class));
        assertFalse(ControlFlowRuntimeException.class.isAssignableFrom(MissingActiveContinuationConfigRuntimeException.class));
        assertFalse(ControlFlowRuntimeException.class.isAssignableFrom(NoContinuableInstanceAvailableException.class));
    }

    @Test
    void testAContinuationsFailureInsideATransactionRollsBack() {
        var manager = new DbQueryManager(datasource_);
        manager.executeUpdate(new CreateTable(datasource_)
            .table("txfailure")
            .column("id", int.class, CreateTable.NOTNULL));
        try {
            try {
                manager.inTransaction(() -> {
                    manager.executeUpdate(new Insert(datasource_)
                        .into("txfailure").field("id", 1));
                    throw new ContinuationsNotActiveException();
                });
                fail("expected the failure to propagate");
            } catch (ContinuationsNotActiveException e) {
                // reported like any other failure
            }

            assertEquals(0, manager.executeGetFirstInt(new Select(datasource_)
                .field("count(*)").from("txfailure")));
        } finally {
            manager.executeUpdate(new DropTable(datasource_).table("txfailure"));
        }
    }
}
