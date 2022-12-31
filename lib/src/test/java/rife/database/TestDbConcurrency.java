/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.CreateTable;
import rife.database.queries.Delete;
import rife.database.queries.DropTable;
import rife.database.queries.Insert;
import rife.database.queries.Select;
import rife.tools.InnerClassException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

public class TestDbConcurrency {
    public static boolean VERBOSE = false;
    public static boolean DEBUG = false;

    private static final Object sOutputLock = new Object();
    private static final int sOutputLimit = 60;
    private static int sOutputChars = 0;
    private static int sConnectionOverload = 25;

    private static void display(char display) {
        if (VERBOSE) {
            synchronized (sOutputLock) {
                System.out.print(display);
                sOutputChars++;
                if (sOutputChars == sOutputLimit) {
                    sOutputChars = 0;
                    System.out.println();
                }
            }
        }
    }

    public static void displayCommit() {
        display('v');
    }

    public static void displayError() {
        display('x');
    }

//    @ParameterizedTest
//    @ArgumentsSource(TestDatasources.class)
    void testConcurrency(Datasource datasource) {
        var structure = new Structure(datasource);
        try {
            structure.install();
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        if (VERBOSE) {
            System.out.println();
        }

        var threads = new ArrayList<Concurrency>();
        final var main_lock = new Object();
        Concurrency concurrency = null;
        for (var i = 1; i <= datasource.getPoolSize() * sConnectionOverload; i++) {
            concurrency = new Concurrency(datasource, main_lock);

            var thread = new Thread(concurrency, "example " + i);
            thread.setDaemon(true);
            threads.add(concurrency);
            thread.start();
        }

        var thread_alive = true;
        var thread_advanced = false;
        synchronized (main_lock) {
            while (thread_alive) {
                try {
                    main_lock.wait(10000);
                } catch (InterruptedException e) {
                    Thread.yield();
                }

                thread_alive = false;
                thread_advanced = false;

                for (var thread : threads) {
                    if (thread.isAlive()) {
                        thread_alive = true;

                        if (thread.hasAdvanced()) {
                            thread_advanced = true;
                            break;
                        }
                    }
                }

                // if none of the threads has advanced
                // throw an error and terminate all threads
                if (thread_alive && !thread_advanced) {
                    for (var thread : threads) {
                        thread.terminate();
                    }

                    thread_alive = false;
                    System.out.println();
                    System.out.println("Concurrency deadlock for datasource with driver: '" + datasource.getDriver() + "'.");
                    System.exit(1);
                    break;
                }
            }
        }

        try {
            structure.remove();
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        datasource.cleanup();
    }
}

class Concurrency extends DbQueryManager implements Runnable {
    private final Object mainLock_;
    private int errors_ = 0;
    private int commits_ = 0;
    private long lastExecution_ = -1;
    private boolean alive_ = false;

    public Concurrency(Datasource datasource, Object mainLock) {
        super(datasource);

        mainLock_ = mainLock;
    }

    public void terminate() {
        alive_ = false;
    }

    public long getLastExecution() {
        return lastExecution_;
    }

    public boolean hasAdvanced() {
        if (-1 == lastExecution_) {
            return true;
        }

        return System.currentTimeMillis() < lastExecution_ + (100 * 1000);
    }

    public boolean isAlive() {
        if (errors_ >= 10 && commits_ >= 10) {
            return false;
        }

        return alive_;
    }

    public void run() {
        alive_ = true;

        while (isAlive()) {
            lastExecution_ = System.currentTimeMillis();

            try {
                doIt();
                Thread.yield();
                Thread.sleep((int) (Math.random() * 200));
                TestDbConcurrency.displayCommit();
                commits_++;
            } catch (DatabaseException e) {
                TestDbConcurrency.displayError();
                errors_++;
                if (TestDbConcurrency.DEBUG) {
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        alive_ = false;

        synchronized (mainLock_) {
            mainLock_.notifyAll();
        }
    }

    public void doIt()
    throws DatabaseException {
        if (TestDbConcurrency.DEBUG) {
            System.out.println(Thread.currentThread().getName() + " : begin");
        }

        inTransaction(new DbTransactionUserWithoutResult<>() {

            public int getTransactionIsolation() {
                if (getDatasource().getAliasedDriver().equals("org.apache.derby.jdbc.EmbeddedDriver")) {
                    return Connection.TRANSACTION_READ_UNCOMMITTED;
                }

                return -1;
            }

            public void useTransactionWithoutResult()
            throws InnerClassException {
                var insert = new Insert(getDatasource());
                insert
                    .into("example")
                    .fieldParameter("firstname")
                    .fieldParameter("lastname");
                try (var insert_stmt = getConnection().getPreparedStatement(insert)) {
                    insert_stmt.setString("firstname", "John");
                    if ((Math.random() * 100) <= 30) {
                        insert_stmt.setNull("lastname", Types.VARCHAR);
                    } else {
                        insert_stmt.setString("lastname", "Doe");
                    }
                    insert_stmt.executeUpdate();
                    insert_stmt.clearParameters();
                    insert_stmt.setString("firstname", "Jane");
                    insert_stmt.setString("lastname", "TheLane");
                    insert_stmt.executeUpdate();
                }

                var select = new Select(getDatasource());
                select
                    .from("example")
                    .orderBy("firstname");
                try (var select_stmt = executeQuery(select)) {
                    var processor = new Processor();
                    while (fetch(select_stmt.getResultSet(), processor) &&
                           processor.wasSuccessful()) {
                        processor.getFirstname();
                        processor.getLastname();
                    }
                }
            }
        });

        if ((Math.random() * 100) <= 10) {
            inTransaction(new DbTransactionUserWithoutResult<>() {

                public int getTransactionIsolation() {
                    if (getDatasource().getAliasedDriver().equals("org.apache.derby.jdbc.EmbeddedDriver")) {
                        return Connection.TRANSACTION_READ_UNCOMMITTED;
                    }

                    return -1;
                }

                public void useTransactionWithoutResult()
                throws InnerClassException {
                    var delete = new Delete(getDatasource());
                    delete
                        .from("example");
                    try (var delete_stmt = getConnection().getPreparedStatement(delete)) {
                        delete_stmt.executeUpdate();
                    }
                    if (TestDbConcurrency.DEBUG) {
                        System.out.println(Thread.currentThread().getName() + " : deleted");
                    }
                }
            });
        }

        if (TestDbConcurrency.DEBUG) {
            System.out.println(Thread.currentThread().getName() + " : committed");
        }
    }
}

class Processor extends DbRowProcessor {
    private String firstname_ = null;
    private String lastname_ = null;

    public String getFirstname() {
        return firstname_;
    }

    public String getLastname() {
        return lastname_;
    }

    public boolean processRow(ResultSet resultSet)
    throws SQLException {
        firstname_ = resultSet.getString("firstname");
        lastname_ = resultSet.getString("lastname");

        return true;
    }
}

class Structure extends DbQueryManager {
    public Structure(Datasource datasource) {
        super(datasource);
    }

    public void install()
    throws DatabaseException {
        var create = new CreateTable(getDatasource());
        create
            .table("example")
            .column("firstname", String.class, 50, CreateTable.NOTNULL)
            .column("lastname", String.class, 50, CreateTable.NOTNULL);
        executeUpdate(create);
    }

    public void remove()
    throws DatabaseException {
        var drop = new DropTable(getDatasource());
        drop.table("example");
        executeUpdate(drop);
    }
}

