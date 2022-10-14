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
    public static Datasource PGSQL = new Datasource("org.postgresql.Driver", "jdbc:postgresql://localhost:5432/unittests", "unittests", "password", 5);
    // TODO : oracle database
    public static Datasource ORACLE = null;
    public static Datasource HSQLDB = new Datasource("org.hsqldb.jdbcDriver", "jdbc:hsqldb:.", "sa", "", 5);
    public static Datasource H2 = new Datasource("org.h2.Driver", "jdbc:h2:./embedded_dbs/h2/unittests", "sa", "", 5);
    public static Datasource MYSQL = new Datasource("com.mysql.cj.jdbc.Driver", "jdbc:mysql://localhost:3306/unittests", "unittests", "password", 5);
    public static Datasource DERBY = new Datasource("org.apache.derby.jdbc.EmbeddedDriver", "jdbc:derby:./embedded_dbs/derby;create=true", "", "", 5);

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Stream.of(
            arguments(TestDatasources.PGSQL),
            arguments(TestDatasources.DERBY),
            arguments(TestDatasources.MYSQL),
            arguments(TestDatasources.HSQLDB),
            arguments(TestDatasources.H2)
        );
    }
}