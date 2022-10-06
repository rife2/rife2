/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

public class TestDatasources implements ArgumentsProvider {
    // TODO
    public static Datasource mPgsql = new Datasource("org.postgresql.Driver", "jdbc:postgresql://localhost:5432/unittests", "unittests", "password", 5);
    public static Datasource mOracle = null;
    public static Datasource mHsqldb = new Datasource("org.hsqldb.jdbcDriver", "jdbc:hsqldb:.", "sa", "", 5);
    public static Datasource mH2 = new Datasource("org.h2.Driver", "jdbc:h2:./embedded_dbs/h2/unittests", "sa", "", 5);
    public static Datasource mMysql = new Datasource("com.mysql.cj.jdbc.Driver", "jdbc:mysql://localhost:3306/unittests", "unittests", "password", 5);
    public static Datasource mDerby = new Datasource("org.apache.derby.jdbc.EmbeddedDriver", "jdbc:derby:./embedded_dbs/derby;create=true", "", "", 5);

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Stream.of(
            arguments(TestDatasources.mPgsql),
            arguments(TestDatasources.mDerby),
            arguments(TestDatasources.mMysql)
// TODO : needs more testing with recent versions
//            arguments(TestDatasources.mHsqldb),
//            arguments(TestDatasources.mH2),
        );
    }
}