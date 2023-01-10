/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.*;
import java.util.stream.Stream;

public class TestDatasources implements ArgumentsProvider {
    public static Datasource PGSQL = new Datasource("org.postgresql.Driver", "jdbc:postgresql://localhost:5432/unittests", "unittests", "password", 5);
    // Oracle can be started up on macOS with:
    // docker run --name oracle-xe-slim -p 1521:1521 -e ORACLE_RANDOM_PASSWORD=true -e APP_USER=unittests -e APP_USER_PASSWORD=password gvenzl/oracle-xe:18-slim
    // on Apple Silicon, first install colima (https://github.com/abiosoft/colima#installation) and run it with:
    // colima start --arch x86_64 --memory 4
    // see blog post about this: https://blog.jdriven.com/2022/07/running-oracle-xe-on-apple-silicon/
    public static Datasource ORACLE = new Datasource("oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@localhost:1521/XEPDB1", "unittests", "password", 5);
    public static Datasource MYSQL = new Datasource("com.mysql.cj.jdbc.Driver", "jdbc:mysql://localhost:3306/unittests", "unittests", "password", 5);
    public static Datasource HSQLDB = new Datasource("org.hsqldb.jdbcDriver", "jdbc:hsqldb:.", "sa", "", 5);
    public static Datasource H2 = new Datasource("org.h2.Driver", "jdbc:h2:./embedded_dbs/h2/unittests", "sa", "", 5);
    public static Datasource DERBY = new Datasource("org.apache.derby.jdbc.EmbeddedDriver", "jdbc:derby:./embedded_dbs/derby;create=true", "", "", 5);

    public static Map<TestDatasourceIdentifier, Datasource> ACTIVE_DATASOURCES;
    static {
        ACTIVE_DATASOURCES = new HashMap<>();
        if (Boolean.parseBoolean(System.getProperty("test.postgres")))       ACTIVE_DATASOURCES.put(TestDatasourceIdentifier.PGSQL, TestDatasources.PGSQL);
        if (Boolean.parseBoolean(System.getProperty("test.mysql")))          ACTIVE_DATASOURCES.put(TestDatasourceIdentifier.MYSQL, TestDatasources.MYSQL);
        if (Boolean.parseBoolean(System.getProperty("test.oracle")))         ACTIVE_DATASOURCES.put(TestDatasourceIdentifier.ORACLE, TestDatasources.ORACLE);
        if (Boolean.parseBoolean(System.getProperty("test.derby", "true")))  ACTIVE_DATASOURCES.put(TestDatasourceIdentifier.DERBY, TestDatasources.DERBY);
        if (Boolean.parseBoolean(System.getProperty("test.hsqldb", "true"))) ACTIVE_DATASOURCES.put(TestDatasourceIdentifier.HSQLDB, TestDatasources.HSQLDB);
        if (Boolean.parseBoolean(System.getProperty("test.h2", "true")))     ACTIVE_DATASOURCES.put(TestDatasourceIdentifier.H2, TestDatasources.H2);
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return ACTIVE_DATASOURCES.values().stream().map(Arguments::arguments).toList().stream();
    }
}