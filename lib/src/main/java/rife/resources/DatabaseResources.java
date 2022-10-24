/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.resources;

import rife.database.Datasource;
import rife.database.DbPreparedStatement;
import rife.database.DbPreparedStatementHandler;
import rife.database.DbQueryManager;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.*;
import rife.resources.exceptions.*;
import rife.tools.ExceptionUtils;
import rife.tools.InnerClassException;
import rife.tools.InputStreamUser;
import rife.tools.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * This class offers <code>ResourceFinder</code> and <code>ResourceWriter</code>
 * capabilities for resources that are stored in a database. The relevant database
 * is specified through a <code>Datasource/code> instance at construction.
 * <p>
 * While the table can be configured through the <code>TABLE_RESOURCES</code>
 * configuration setting, the structure of the table is fixed. It can be
 * installed with the <code>install()</code> method and removed with the
 * <code>remove()</code> method. The latter will implicitly erase all the
 * resources that have been stored in the database table.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see rife.resources.ResourceFinder
 * @since 1.0
 */
public abstract class DatabaseResources extends DbQueryManager implements ResourceFinder, ResourceWriter {
    protected static final String PROTOCOL = "file";

    protected static final String COLUMN_NAME = "name";
    protected static final String COLUMN_CONTENT = "content";
    protected static final String COLUMN_MODIFIED = "modified";

    /**
     * Creates a new instance according to the provided datasource.
     *
     * @param datasource the <code>Datasource</code> instance that defines the
     *                   database that will be used as resources storage.
     * @since 1.0
     */
    protected DatabaseResources(Datasource datasource) {
        super(datasource);
    }

    /**
     * Installs the database structure that's needed to store and retrieve
     * resources in and from a database.
     *
     * @throws ResourceWriterErrorException when an error occurred during the
     *                                      installation
     */
    public abstract boolean install()
    throws ResourceWriterErrorException;

    /**
     * Removes the database structure that's needed to store and retrieve
     * resources in and from a database.
     *
     * @throws ResourceWriterErrorException when an error occurred during the
     *                                      removal
     */
    public abstract boolean remove()
    throws ResourceWriterErrorException;

    protected boolean _install(CreateTable createTable)
    throws ResourceWriterErrorException {
        try {
            executeUpdate(createTable);
        } catch (DatabaseException e) {
            throw new ResourceStructureInstallationException(e);
        }

        return true;
    }

    protected boolean _remove(DropTable dropTable)
    throws ResourceWriterErrorException {
        try {
            executeUpdate(dropTable);
        } catch (DatabaseException e) {
            throw new ResourceStructureRemovalException(e);
        }

        return true;
    }

    protected void _addResource(Insert addResource, final String name, final String content)
    throws ResourceWriterErrorException {
        assert addResource != null;

        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");
        if (null == content) throw new IllegalArgumentException("content can't be null.");

        try {
            executeUpdate(addResource, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString(COLUMN_NAME, name)
                        .setString(COLUMN_CONTENT, content)
                        .setTimestamp(COLUMN_MODIFIED, new java.sql.Timestamp(System.currentTimeMillis()));
                }
            });
        } catch (DatabaseException e) {
            throw new ResourceAdditionErrorException(name, content, e);
        }
    }

    protected boolean _updateResource(Update updateResource, final String name, final String content)
    throws ResourceWriterErrorException {
        assert updateResource != null;

        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");
        if (null == content) throw new IllegalArgumentException("content can't be null.");

        var result = false;

        try {
            if (0 != executeUpdate(updateResource, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString(COLUMN_CONTENT, content)
                        .setTimestamp(COLUMN_MODIFIED, new java.sql.Timestamp(System.currentTimeMillis()))
                        .setString(COLUMN_NAME, name);
                }
            })) {
                result = true;
            }
        } catch (DatabaseException e) {
            throw new ResourceUpdateErrorException(name, content, e);
        }

        return result;
    }

    protected boolean _removeResource(Delete removeResource, final String name)
    throws ResourceWriterErrorException {
        assert removeResource != null;

        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        var result = false;

        try {
            if (0 != executeUpdate(removeResource, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString(COLUMN_NAME, name);
                }
            })) {
                result = true;
            }
        } catch (DatabaseException e) {
            throw new ResourceRemovalErrorException(name, e);
        }

        return result;
    }

    protected URL _getResource(Select hasResource, final String name) {
        assert hasResource != null;

        if (null == name) {
            return null;
        }

        URL resource = null;

        try {
            if (executeHasResultRows(hasResource, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString(COLUMN_NAME, name);
                }
            })) {
                resource = new URL(PROTOCOL, "", name);
            }
        } catch (MalformedURLException e) {
            return null;
        } catch (DatabaseException e) {
            Logger.getLogger("rife.resources").severe("Error while retrieving the resource with name '" + name + "' :\n" + ExceptionUtils.getExceptionStackTrace(e));
            return null;
        }

        return resource;
    }

    protected <ResultType> ResultType _useStream(Select getResourceContent, final URL resource, InputStreamUser<ResultType, ?> user)
    throws ResourceFinderErrorException, InnerClassException {
        assert getResourceContent != null;

        if (null == resource ||
            null == user) {
            return null;
        }

        if (!PROTOCOL.equals(resource.getProtocol())) {
            return null;
        }

        try {
            return executeUseFirstBinaryStream(getResourceContent, user, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString(COLUMN_NAME, StringUtils.decodeUrl(resource.getFile()));
                }
            });
        } catch (DatabaseException e) {
            throw new CantOpenResourceStreamException(resource, e);
        }
    }

    protected String _getContent(Select getResourceContent, final URL resource, String encoding)
    throws ResourceFinderErrorException {
        assert getResourceContent != null;

        if (null == resource) {
            return null;
        }

        if (!PROTOCOL.equals(resource.getProtocol())) {
            return null;
        }

        String result = null;
        try {
            result = executeGetFirstString(getResourceContent, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString(COLUMN_NAME, StringUtils.decodeUrl(resource.getFile()));
                }
            });
        } catch (DatabaseException e) {
            throw new CantRetrieveResourceContentException(resource, encoding, e);
        }

        return result;
    }

    protected long _getModificationTime(Select getResourceModified, final URL resource) {
        assert getResourceModified != null;

        if (null == resource) {
            return -1;
        }

        if (!PROTOCOL.equals(resource.getProtocol())) {
            return -1;
        }

        try {
            long result = -1;

            var timestamp = executeGetFirstTimestamp(getResourceModified, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString(COLUMN_NAME, StringUtils.decodeUrl(resource.getFile()));
                }
            });
            if (null == timestamp) {
                return -1;
            }
            result = timestamp.getTime();

            return result;
        } catch (DatabaseException e) {
            return -1;
        }
    }

    public <ResultType> ResultType useStream(String name, InputStreamUser<ResultType, ?> user)
    throws ResourceFinderErrorException, InnerClassException {
        if (null == name ||
            null == user) {
            return null;
        }

        var resource = getResource(name);
        if (null == resource) {
            return null;
        }

        return useStream(resource, user);
    }

    public String getContent(String name)
    throws ResourceFinderErrorException {
        return getContent(name, null);
    }

    public String getContent(String name, String encoding)
    throws ResourceFinderErrorException {
        if (null == name) {
            return null;
        }

        var resource = getResource(name);
        if (null == resource) {
            return null;
        }

        return getContent(resource, encoding);
    }

    public String getContent(URL resource)
    throws ResourceFinderErrorException {
        return getContent(resource, null);
    }

    public long getModificationTime(String name)
    throws ResourceFinderErrorException {
        if (null == name) {
            return -1;
        }

        var resource = getResource(name);
        if (null == resource) {
            return -1;
        }

        return getModificationTime(resource);
    }
}
