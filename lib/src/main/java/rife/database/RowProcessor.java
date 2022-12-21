package rife.database;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This interface can be implemented to process one row in a database
 * query result set. The <code>fetch</code> method of a <code>DbQueryManager</code>
 * requires an instance of a <code>RowProcessor</code> and calls its
 * <code>processRow</code> method each time it is called.
 * <p>
 * The <code>RowProcessor</code> instance can then work with the result set
 * and extract all needed data. It is free to implement any logic to be
 * able to return the retrieved data in an acceptable form to the user.
 * <p>
 * A class that implements <code>DbRowProcessor</code> can for example take a
 * <code>Template</code> instance as the argument of its constructor and
 * progressively fill in each resulting row in an HTML table. This, without
 * having to maintain the query results in memory to be able to provide it to a
 * separate method which is responsible for the handling of the output. Using a
 * <code>RowProcessor</code> thus allows for perfect separation and
 * abstraction of result processing without having to be burdened with possible
 * large memory usage or large object allocation.
 * <p>
 * More extensive processing is possible by extending the {@link DbRowProcessor}
 * class instead.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see #processRow(ResultSet resultSet)
 * @see DbQueryManager
 * @see DbRowProcessor
 * @since 1.0
 */
@FunctionalInterface
public interface RowProcessor {
    /**
     * This method has to contain all the logic that should be executed for each
     * row of a result set.
     *
     * @param resultSet the <code>ResultSet</code> instance that was provided to
     *                  the <code>DbQueryManager</code>'s <code>fetch</code> method.
     * @throws SQLException when a database error occurs, it's thus not
     *                      necessary to catch all the possible <code>SQLException</code>s inside
     *                      this method. They'll be caught higher up and be transformed in
     *                      <code>DatabaseException</code>s.
     * @see DbQueryManager#fetch(ResultSet, RowProcessor)
     * @since 1.0
     */
    void processRow(ResultSet resultSet) throws SQLException;
}