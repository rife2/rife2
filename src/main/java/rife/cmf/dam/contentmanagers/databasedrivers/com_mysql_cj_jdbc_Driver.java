/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentmanagers.databasedrivers;

import rife.cmf.Content;
import rife.cmf.ContentRepository;
import rife.cmf.dam.contentmanagers.ContentLocation;
import rife.cmf.dam.contentmanagers.DatabaseContentInfo;
import rife.cmf.dam.contentmanagers.exceptions.InstallContentErrorException;
import rife.cmf.dam.contentmanagers.exceptions.RemoveContentErrorException;
import rife.cmf.dam.contentmanagers.exceptions.UnknownContentRepositoryException;
import rife.cmf.dam.contentmanagers.exceptions.UnsupportedMimeTypeException;
import rife.cmf.dam.exceptions.ContentManagerException;
import rife.cmf.transform.ContentTransformer;
import rife.config.RifeConfig;
import rife.database.*;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.CreateTable;
import rife.database.queries.Insert;
import rife.database.queries.Query;
import rife.tools.InnerClassException;

import java.sql.Statement;
import java.sql.Types;

public class com_mysql_cj_jdbc_Driver extends generic {
    private static final Object sVersionMonitor = new Object();

    public com_mysql_cj_jdbc_Driver(Datasource datasource) {
        super(datasource);

        createTableContentRepository_ = new CreateTable(getDatasource())
            .table(RifeConfig.cmf().getTableContentRepository())
            .columns(ContentRepository.class)
            .column("repositoryId", int.class)
            .customAttribute("repositoryId", "AUTO_INCREMENT")
            .primaryKey("PK_" + RifeConfig.cmf().getTableContentRepository(), "repositoryId");

        createTableContentInfo_ = new CreateTable(getDatasource())
            .table(RifeConfig.cmf().getTableContentInfo())
            .columns(DatabaseContentInfo.class)
            .column("repositoryId", int.class, CreateTable.NOTNULL)
            .customAttribute("contentId", "AUTO_INCREMENT")
            .unique(("UQ_" + RifeConfig.cmf().getTableContentInfo()).toUpperCase(), new String[]{"repositoryId", "path", "version"})
            .foreignKey("FK_" + RifeConfig.cmf().getTableContentInfo() + "_REPOSITORYID", RifeConfig.cmf().getTableContentRepository(), "repositoryId", "repositoryId");

        dropContentInfoPathIndex_ = "DROP INDEX " + RifeConfig.cmf().getTableContentInfo() + "_path ON " + RifeConfig.cmf().getTableContentInfo();

        dropContentInfoPathNameIndex_ = "DROP INDEX " + RifeConfig.cmf().getTableContentInfo() + "_pathname ON " + RifeConfig.cmf().getTableContentInfo();

        storeContentRepository_ = new Insert(getDatasource())
            .into(RifeConfig.cmf().getTableContentRepository())
            .fieldsParameters(ContentRepository.class);

        storeContentInfo_ = new Insert(getDatasource())
            .into(RifeConfig.cmf().getTableContentInfo())
            .fieldsParametersExcluded(DatabaseContentInfo.class, new String[]{"contentId"})
            .fieldParameter("repositoryId")
            .fieldParameter("version")
            .fieldParameter("created");
    }

    public boolean install()
    throws ContentManagerException {
        try {
            executeUpdate(createTableContentRepository_);
            executeUpdate(createTableContentInfo_);
            executeUpdate(createTableContentAttribute_);
            executeUpdate(createTableContentProperty_);

            createRepository(ContentRepository.DEFAULT);

            for (var store : stores_) {
                store.install();
            }

            executeUpdate(createContentInfoPathIndex_);
            executeUpdate(createContentInfoPathNameIndex_);
        } catch (DatabaseException e) {
            throw new InstallContentErrorException(e);
        }

        return true;
    }

    public boolean remove()
    throws ContentManagerException {
        try {
            executeUpdate(dropContentInfoPathNameIndex_);
            executeUpdate(dropContentInfoPathIndex_);

            for (var store : stores_) {
                store.remove();
            }

            executeUpdate(dropTableContentProperties_);
            executeUpdate(dropTableContentAttribute_);
            executeUpdate(dropTableContentInfo_);
            executeUpdate(dropTableContentRepository_);
        } catch (DatabaseException e) {
            throw new RemoveContentErrorException(e);
        }

        return true;
    }

    public boolean createRepository(final String name)
    throws ContentManagerException {
        if (null == name) throw new IllegalArgumentException("name can't be null");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty");

        Boolean result = null;

        try {
            // store the content
            result = inTransaction(() -> executeUpdate(storeContentRepository_, s -> s.setString("name", name)) > 0);
        } catch (InnerClassException e) {
            throw (ContentManagerException) e.getCause();
        }

        return result != null && result;
    }

    public boolean storeContent(String location, final Content content, final ContentTransformer transformer)
    throws ContentManagerException {
        if (null == content) throw new IllegalArgumentException("content can't be null");

        final var split_location = ContentLocation.split(location);

        final var store = mimeMapping_.get(content.getMimeType());
        if (null == store) {
            throw new UnsupportedMimeTypeException(content.getMimeType());
        }

        // ensure that all version number increases are handled in a serial fashion
        // relying on database locks is error-prone and doesn't offer any advantages
        synchronized (sVersionMonitor) {
            // get repository id
            final var repository_id = executeGetFirstInt(getContentRepositoryId_, s ->
                s.setString("repository", split_location.repository()));

            // verify the existance of the repository
            if (-1 == repository_id) {
                throw new UnknownContentRepositoryException(split_location.repository());
            }

            // get version
            final var version = executeGetFirstInt(getVersion_, s ->
                s.setInt("repositoryId", repository_id)
                    .setString("path", split_location.path()));

            // store the content
            final var ids_array = new int[1];
            if (executeUpdate(storeContentInfo_, new DbPreparedStatementHandler<>() {
                public DbPreparedStatement getPreparedStatement(Query query, DbConnection connection) {
                    return connection.getPreparedStatement(query, Statement.RETURN_GENERATED_KEYS);
                }

                public int performUpdate(DbPreparedStatement statement) {
                    statement
                        .setString("path", split_location.path())
                        .setString("mimeType", content.getMimeType().toString())
                        .setBoolean("fragment", content.isFragment())
                        .setDate("created", new java.sql.Date(System.currentTimeMillis()))
                        .setInt("repositoryId", repository_id)
                        .setInt("version", version);
                    if (content.hasName()) {
                        statement
                            .setString("name", content.getName());
                    } else {
                        statement
                            .setNull("name", Types.VARCHAR);
                    }

                    var query_result = statement.executeUpdate();
                    ids_array[0] = statement.getFirstGeneratedIntKey();
                    return query_result;
                }
            }) > 0) {
                // store the attributes if there are some
                if (content.hasAttributes()) {
                    for (var attribute : content.getAttributes().entrySet()) {
                        final var name = attribute.getKey();
                        final var value = attribute.getValue();

                        executeUpdate(storeContentAttribute_, s ->
                            s.setInt("contentId", ids_array[0])
                                .setString("name", name)
                                .setString("attVal", value));
                    }
                }

                // put the actual content data in the content store
                if (!store.storeContentData(ids_array[0], content, transformer)) {
                    return false;
                }

                // store the content data properties if there are some
                if (content.hasProperties()) {
                    for (var property : content.getProperties().entrySet()) {
                        final var name = property.getKey();
                        final var value = property.getValue();

                        executeUpdate(storeContentProperty_, s ->
                            s.setInt("contentId", ids_array[0])
                                .setString("name", name)
                                .setString("propVal", value));
                    }
                }

                return true;
            }

            return false;
        }
    }
}
