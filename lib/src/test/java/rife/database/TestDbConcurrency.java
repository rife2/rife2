/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
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

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testConcurrency(Datasource datasource) {
        Structure structure = new Structure(datasource);
        try {
            structure.install();
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        if (VERBOSE) {
            System.out.println();
        }

        ArrayList<Concurrency> threads = new ArrayList<Concurrency>();
        final Object main_lock = new Object();
        Concurrency concurrency = null;
        for (int i = 1; i <= datasource.getPoolsize() * sConnectionOverload; i++) {
            concurrency = new Concurrency(datasource, main_lock);

            Thread thread = new Thread(concurrency, "example " + i);
            thread.setDaemon(true);
            threads.add(concurrency);
            thread.start();
        }

        boolean thread_alive = true;
        boolean thread_advanced = false;
        synchronized (main_lock) {
            while (thread_alive) {
                try {
                    main_lock.wait(10000);
                } catch (InterruptedException e) {
                    Thread.yield();
                }

                thread_alive = false;
                thread_advanced = false;

                for (Concurrency thread : threads) {
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
                    for (Concurrency thread : threads) {
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
    private Object mMainlock = null;
    private int mErrors = 0;
    private int mCommits = 0;
    private long mLastExecution = -1;
    private boolean mAlive = false;

    public Concurrency(Datasource datasource, Object mainlock) {
        super(datasource);

        mMainlock = mainlock;
    }

    public void terminate() {
        mAlive = false;
    }

    public long getLastExecution() {
        return mLastExecution;
    }

    public boolean hasAdvanced() {
        if (-1 == mLastExecution) {
            return true;
        }

        if (System.currentTimeMillis() < mLastExecution + (100 * 1000)) {
            return true;
        }

        return false;
    }

    public boolean isAlive() {
        if (mErrors >= 10 && mCommits >= 10) {
            return false;
        }

        return mAlive;
    }

    public void run() {
        mAlive = true;

        while (isAlive()) {
            mLastExecution = System.currentTimeMillis();

            try {
                doIt();
                Thread.yield();
                Thread.sleep((int) Math.random() * 200);
                TestDbConcurrency.displayCommit();
                mCommits++;
            } catch (DatabaseException e) {
                TestDbConcurrency.displayError();
                mErrors++;
                if (TestDbConcurrency.DEBUG) {
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        mAlive = false;

        synchronized (mMainlock) {
            mMainlock.notifyAll();
        }
    }

    public void doIt()
    throws DatabaseException {
        if (TestDbConcurrency.DEBUG) {
            System.out.println(Thread.currentThread().getName() + " : begin");
        }

        inTransaction(new DbTransactionUserWithoutResult() {

            public int getTransactionIsolation() {
                if (getDatasource().getAliasedDriver().equals("org.apache.derby.jdbc.EmbeddedDriver")) {
                    return Connection.TRANSACTION_READ_UNCOMMITTED;
                }

                return -1;
            }

            public void useTransactionWithoutResult()
            throws InnerClassException {
                Insert insert = new Insert(getDatasource());
                insert
                    .into("example")
                    .fieldParameter("firstname")
                    .fieldParameter("lastname");
                DbPreparedStatement insert_stmt = getConnection().getPreparedStatement(insert);
                try {
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
                } finally {
                    insert_stmt.close();
                }

                Select select = new Select(getDatasource());
                select
                    .from("example")
                    .orderBy("firstname");
                DbStatement select_stmt = executeQuery(select);
                try {
                    Processor processor = new Processor();
                    while (fetch(select_stmt.getResultSet(), processor) &&
                        processor.wasSuccessful()) {
                        processor.getFirstname();
                        processor.getLastname();
                    }
                } finally {
                    select_stmt.close();
                }
            }
        });

        if ((Math.random() * 100) <= 10) {
            inTransaction(new DbTransactionUserWithoutResult() {

                public int getTransactionIsolation() {
                    if (getDatasource().getAliasedDriver().equals("org.apache.derby.jdbc.EmbeddedDriver")) {
                        return Connection.TRANSACTION_READ_UNCOMMITTED;
                    }

                    return -1;
                }

                public void useTransactionWithoutResult()
                throws InnerClassException {
                    Delete delete = new Delete(getDatasource());
                    delete
                        .from("example");
                    DbPreparedStatement delete_stmt = getConnection().getPreparedStatement(delete);
                    try {
                        delete_stmt.executeUpdate();
                    } finally {
                        delete_stmt.close();
                    }
                    if (TestDbConcurrency.DEBUG) {
                        System.out.println(Thread.currentThread().getName() + " : deleted");
                    }
                }
            });
        }

        if (TestDbConcurrency.DEBUG) {
            System.out.println(Thread.currentThread().getName() + " : comitted");
        }
    }
}

class Processor extends DbRowProcessor {
    private String mFirstname = null;
    private String mLastname = null;

    public String getFirstname() {
        return mFirstname;
    }

    public String getLastname() {
        return mLastname;
    }

    public boolean processRow(ResultSet resultSet)
    throws SQLException {
        mFirstname = resultSet.getString("firstname");
        mLastname = resultSet.getString("lastname");

        return true;
    }
}

class Structure extends DbQueryManager {
    public Structure(Datasource datasource) {
        super(datasource);
    }

    public void install()
    throws DatabaseException {
        CreateTable create = new CreateTable(getDatasource());
        create
            .table("example")
            .column("firstname", String.class, 50, CreateTable.NOTNULL)
            .column("lastname", String.class, 50, CreateTable.NOTNULL);
        executeUpdate(create);
    }

    public void remove()
    throws DatabaseException {
        DropTable drop = new DropTable(getDatasource());
        drop.table("example");
        executeUpdate(drop);
    }
}

