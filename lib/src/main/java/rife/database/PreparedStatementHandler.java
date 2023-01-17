/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

/**
 * By implementing this class it's possible to easily customize the behaviour of
 * a large number of methods in the {@link DbQueryManager} class.
 * <p>
 * Implementing the {@link #setParameters(DbPreparedStatement) setParameters}
 * method allows you to set the parameters of a {@link DbPreparedStatement}
 * before the actual execution of any logic.
 * <p>
 * More extensive customizations are possible by extending the
 * {@link DbPreparedStatementHandler} class instead.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see DbPreparedStatement
 * @see DbQueryManager
 * @since 1.0
 */
@FunctionalInterface
public interface PreparedStatementHandler {
    void setParameters(DbPreparedStatement statement);
}