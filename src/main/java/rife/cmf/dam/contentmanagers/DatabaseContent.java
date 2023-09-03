/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentmanagers;

import rife.cmf.Content;
import rife.cmf.ContentRepository;
import rife.cmf.MimeType;
import rife.cmf.dam.ContentDataUser;
import rife.cmf.dam.ContentDataUserWithoutResult;
import rife.cmf.dam.ContentManager;
import rife.cmf.dam.ContentStore;
import rife.cmf.dam.contentmanagers.exceptions.InstallContentErrorException;
import rife.cmf.dam.contentmanagers.exceptions.RemoveContentErrorException;
import rife.cmf.dam.contentmanagers.exceptions.UnknownContentRepositoryException;
import rife.cmf.dam.contentmanagers.exceptions.UnsupportedMimeTypeException;
import rife.cmf.dam.contentstores.DatabaseImageStoreFactory;
import rife.cmf.dam.contentstores.DatabaseRawStoreFactory;
import rife.cmf.dam.contentstores.DatabaseTextStoreFactory;
import rife.cmf.dam.exceptions.ContentManagerException;
import rife.cmf.transform.ContentTransformer;
import rife.database.Datasource;
import rife.database.DbQueryManager;
import rife.database.DbRowProcessor;
import rife.database.DbTransactionUser;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.*;
import rife.engine.Context;
import rife.engine.Route;
import rife.tools.InnerClassException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class DatabaseContent extends DbQueryManager implements ContentManager {
    protected final ArrayList<ContentStore> stores_;
    protected final HashMap<MimeType, ContentStore> mimeMapping_;

    public DatabaseContent(Datasource datasource) {
        super(datasource);

        stores_ = new ArrayList<>();
        stores_.add(DatabaseTextStoreFactory.instance(getDatasource()));
        stores_.add(DatabaseImageStoreFactory.instance(getDatasource()));
        stores_.add(DatabaseRawStoreFactory.instance(getDatasource()));

        mimeMapping_ = new HashMap<>();
        for (var store : stores_) {
            for (var mime_type : store.getSupportedMimeTypes()) {
                mimeMapping_.put(mime_type, store);
            }
        }
    }

    public abstract DatabaseContentInfo getContentInfo(String location)
    throws ContentManagerException;

    protected boolean _install(CreateSequence createSequenceContentRepository, CreateSequence createSequenceContentInfo,
                               CreateTable createTableContentRepository, CreateTable createTableContentInfo, CreateTable createTableContentAttribute, CreateTable createTableContentProperty)
    throws ContentManagerException {
        assert createSequenceContentRepository != null;
        assert createSequenceContentInfo != null;
        assert createTableContentRepository != null;
        assert createTableContentInfo != null;
        assert createTableContentAttribute != null;
        assert createTableContentProperty != null;

        try {
            executeUpdate(createSequenceContentRepository);
            executeUpdate(createSequenceContentInfo);
            executeUpdate(createTableContentRepository);
            executeUpdate(createTableContentInfo);
            executeUpdate(createTableContentAttribute);
            executeUpdate(createTableContentProperty);

            createRepository(ContentRepository.DEFAULT);

            for (var store : stores_) {
                store.install();
            }
        } catch (DatabaseException e) {
            throw new InstallContentErrorException(e);
        }

        return true;
    }

    protected boolean _remove(DropSequence dropSequenceContentRepository, DropSequence dropSequenceContentInfo,
                              DropTable dropTableContentRepository, DropTable dropTableContentInfo, DropTable dropTableContentAttribute, DropTable dropTableContentProperty)
    throws ContentManagerException {
        assert dropSequenceContentRepository != null;
        assert dropSequenceContentInfo != null;
        assert dropTableContentRepository != null;
        assert dropTableContentInfo != null;
        assert dropTableContentAttribute != null;
        assert dropTableContentProperty != null;

        try {
            for (var store : stores_) {
                store.remove();
            }

            executeUpdate(dropTableContentProperty);
            executeUpdate(dropTableContentAttribute);
            executeUpdate(dropTableContentInfo);
            executeUpdate(dropTableContentRepository);
            executeUpdate(dropSequenceContentInfo);
            executeUpdate(dropSequenceContentRepository);
        } catch (DatabaseException e) {
            throw new RemoveContentErrorException(e);
        }

        return true;
    }

    protected boolean _createRepository(final SequenceValue getContentRepositoryId, final Insert storeContentRepository, final String name)
    throws ContentManagerException {
        if (null == name) throw new IllegalArgumentException("name can't be null");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty");

        assert getContentRepositoryId != null;
        assert storeContentRepository != null;

        Boolean result = null;

        try {
            result = inTransaction(() -> {
                // get new repository id
                final var id = executeGetFirstInt(getContentRepositoryId);

                // store the content
                return executeUpdate(storeContentRepository, s ->
                    s.setInt("repositoryId", id)
                        .setString("name", name)) > 0;
            });
        } catch (InnerClassException e) {
            throw (ContentManagerException) e.getCause();
        }

        return result != null && result;
    }

    protected boolean _containsRepository(final Select containsContentRepository, final String name)
    throws ContentManagerException {
        if (null == name) throw new IllegalArgumentException("name can't be null");

        assert containsContentRepository != null;

        final String repository;
        if (name.isEmpty()) {
            repository = ContentRepository.DEFAULT;
        } else {
            repository = name;
        }

        return executeGetFirstInt(containsContentRepository, s ->
            s.setString("name", repository)) > 0;
    }

    protected boolean _storeContent(final SequenceValue getContentId, final Select getContentRepositoryId, final Insert storeContentInfo, final Insert storeContentAttribute, final Insert storeContentProperty, String location, final Content content, final ContentTransformer transformer)
    throws ContentManagerException {
        if (null == content) throw new IllegalArgumentException("content can't be null");

        final var split_location = ContentLocation.split(location);

        assert getContentId != null;
        assert getContentRepositoryId != null;
        assert storeContentInfo != null;
        assert storeContentAttribute != null;
        assert storeContentProperty != null;

        final var store = mimeMapping_.get(content.getMimeType());
        if (null == store) {
            throw new UnsupportedMimeTypeException(content.getMimeType());
        }

        Boolean result = null;

        try {
            result = inTransaction(new DbTransactionUser<>() {
                public Boolean useTransaction()
                throws InnerClassException {
                    // get new content id
                    final var id = executeGetFirstInt(getContentId);

                    // get repository id
                    final var repository_id = executeGetFirstInt(getContentRepositoryId, s ->
                        s.setString("repository", split_location.repository()));

                    // verify the existence of the repository
                    if (-1 == repository_id) {
                        throwException(new UnknownContentRepositoryException(split_location.repository()));
                    }

                    // store the content
                    if (executeUpdate(storeContentInfo, s -> {
                        s.setInt("contentId", id)
                            .setInt("repositoryId", repository_id)
                            .setString("path", split_location.path())
                            .setString("mimeType", content.getMimeType().toString())
                            .setBoolean("fragment", content.isFragment());
                        if (content.hasName()) {
                            s.setString("name", content.getName());
                        } else {
                            s.setNull("name", Types.VARCHAR);
                        }
                    }) > 0) {
                        // store the attributes if there are some
                        if (content.hasAttributes()) {
                            for (var attribute : content.getAttributes().entrySet()) {
                                final var name = attribute.getKey();
                                final var value = attribute.getValue();

                                executeUpdate(storeContentAttribute, s ->
                                    s.setInt("contentId", id)
                                        .setString("name", name)
                                        .setString("attVal", value));
                            }
                        }

                        // put the actual content data in the content store
                        try {
                            if (!store.storeContentData(id, content, transformer)) {
                                rollback();
                            }
                        } catch (ContentManagerException e) {
                            throwException(e);
                        }

                        // store the content data properties if there are some
                        if (content.hasProperties()) {
                            for (var property : content.getProperties().entrySet()) {
                                final var name = property.getKey();
                                final var value = property.getValue();

                                executeUpdate(storeContentProperty, s ->
                                    s.setInt("contentId", id)
                                        .setString("name", name)
                                        .setString("propVal", value));
                            }
                        }

                        return true;
                    }

                    return false;
                }
            });
        } catch (InnerClassException e) {
            throw (ContentManagerException) e.getCause();
        }

        return result != null && result;
    }

    protected boolean _deleteContent(final Select getContentInfo, final Delete deleteContentInfo, final Delete deleteContentAttributes, final Delete deleteContentProperties, String location)
    throws ContentManagerException {
        final var split_location = ContentLocation.split(location);

        assert getContentInfo != null;
        assert deleteContentInfo != null;
        assert deleteContentAttributes != null;
        assert deleteContentProperties != null;

        Boolean result = null;

        try {
            result = inTransaction(new DbTransactionUser<>() {
                public Boolean useTransaction()
                throws InnerClassException {
                    return executeFetchAll(getContentInfo, new DbRowProcessor() {
                        public boolean processRow(ResultSet resultSet)
                        throws SQLException {
                            final var content_id = resultSet.getInt("contentId");

                            var mimetype = MimeType.getMimeType(resultSet.getString("mimeType"));

                            var store = mimeMapping_.get(mimetype);
                            if (null == store) {
                                throw new UnsupportedMimeTypeException(mimetype);
                            }

                            if (!store.deleteContentData(content_id)) {
                                rollback();
                            }

                            executeUpdate(deleteContentAttributes, s -> s.setInt("contentId", content_id));
                            executeUpdate(deleteContentProperties, s -> s.setInt("contentId", content_id));
                            if (0 == executeUpdate(deleteContentInfo, s -> s.setInt("contentId", content_id))) {
                                rollback();
                            }

                            return true;
                        }
                    }, s -> s.setString("repository", split_location.repository())
                        .setString("path", split_location.path()));
                }
            });
        } catch (InnerClassException e) {
            throw (ContentManagerException) e.getCause();
        }

        return result != null && result;
    }

    record PathParts(String pathPart, String namePart) {
        static PathParts split(String path) {
            assert path != null;

            var slash_index = path.lastIndexOf('/');
            var path_part = path.substring(0, slash_index);
            var name_part = path.substring(slash_index + 1);

            return new PathParts(path_part, name_part);
        }
    }

    protected void _useContentData(Select retrieveContent, String location, ContentDataUserWithoutResult user)
    throws ContentManagerException {
        if (null == user) throw new IllegalArgumentException("user can't be null");

        var content_info = retrieveDatabaseContentInfo(retrieveContent, location);
        if (content_info == null) return;

        var mime_type = MimeType.getMimeType(content_info.getMimeType());
        var store = mimeMapping_.get(mime_type);
        if (null == store) {
            throw new UnsupportedMimeTypeException(mime_type);
        }

        store.useContentData(content_info.getContentId(), user);
    }

    protected <ResultType> ResultType _useContentDataResult(Select retrieveContent, String location, ContentDataUser<ResultType> user)
    throws ContentManagerException {
        if (null == user) throw new IllegalArgumentException("user can't be null");

        var content_info = retrieveDatabaseContentInfo(retrieveContent, location);
        if (content_info == null) return null;

        var mime_type = MimeType.getMimeType(content_info.getMimeType());
        var store = mimeMapping_.get(mime_type);
        if (null == store) {
            throw new UnsupportedMimeTypeException(mime_type);
        }

        return store.useContentDataResult(content_info.getContentId(), user);
    }

    private DatabaseContentInfo retrieveDatabaseContentInfo(Select retrieveContent, String location) {
        final var split_location = ContentLocation.split(location);
        final var path_parts = PathParts.split(split_location.path());

        assert retrieveContent != null;

        return executeFetchFirstBean(retrieveContent, DatabaseContentInfo.class, s ->
            s.setString("repository", split_location.repository())
                .setString("path", split_location.path())
                .setString("pathpart", path_parts.pathPart())
                .setString("namepart", path_parts.namePart()));
    }

    protected boolean _hasContentData(Select retrieveContent, String location)
    throws ContentManagerException {
        final var split_location = ContentLocation.split(location);
        final var path_parts = PathParts.split(split_location.path());

        assert retrieveContent != null;

        var content_info = executeFetchFirstBean(retrieveContent, DatabaseContentInfo.class, s ->
            s.setString("repository", split_location.repository())
                .setString("path", split_location.path())
                .setString("pathpart", path_parts.pathPart())
                .setString("namepart", path_parts.namePart()));

        if (null == content_info) {
            return false;
        }

        var mime_type = MimeType.getMimeType(content_info.getMimeType());
        var store = mimeMapping_.get(mime_type);
        if (null == store) {
            throw new UnsupportedMimeTypeException(mime_type);
        }

        return store.hasContentData(content_info.getContentId());
    }

    protected DatabaseContentInfo _getContentInfo(Select getContentInfo, Select getContentAttributes, Select getContentProperties, String location)
    throws ContentManagerException {
        final var split_location = ContentLocation.split(location);
        final var path_parts = PathParts.split(split_location.path());

        assert getContentInfo != null;
        assert getContentAttributes != null;
        assert getContentProperties != null;

        final var content_info = executeFetchFirstBean(getContentInfo, DatabaseContentInfo.class, s ->
            s.setString("repository", split_location.repository())
                .setString("path", split_location.path())
                .setString("pathpart", path_parts.pathPart())
                .setString("namepart", path_parts.namePart()));

        if (content_info != null) {
            // get the content attributes
            var processor_attributes = new ContentAttributesProcessor();
            executeFetchAll(getContentAttributes, processor_attributes, s ->
                s.setInt("contentId", content_info.getContentId()));
            content_info.setAttributes(processor_attributes.getAttributes());

            // get the content data properties
            var processor_properties = new ContentPropertiesProcessor();
            executeFetchAll(getContentProperties, processor_properties, s ->
                s.setInt("contentId", content_info.getContentId()));
            content_info.setProperties(processor_properties.getProperties());

            // retrieve the content store
            var mime_type = MimeType.getMimeType(content_info.getMimeType());
            var store = mimeMapping_.get(mime_type);
            if (null == store) {
                throw new UnsupportedMimeTypeException(mime_type);
            }

            // retrieve the content size
            content_info.setSize(store.getSize(content_info.getContentId()));
        }

        return content_info;
    }

    protected void _serveContentData(Context context, final String location)
    throws ContentManagerException {
        if (null == context) throw new IllegalArgumentException("context can't be null.");

        try {
            ContentLocation.split(location);
        } catch (IllegalArgumentException e) {
            context.defer();
            return;
        }


        DatabaseContentInfo content_info = null;
        try {
            content_info = getContentInfo(location);
        } catch (IllegalArgumentException e) {
            context.defer();
            return;
        }
        if (null == content_info) {
            context.defer();
            return;
        }

        // retrieve the content store
        var mime_type = MimeType.getMimeType(content_info.getMimeType());
        var store = mimeMapping_.get(mime_type);
        if (null == store) {
            throw new UnsupportedMimeTypeException(mime_type);
        }

        // set cache headers
        long if_modified_since = context.headerDate("If-Modified-Since");
        var last_modified = content_info.getCreated();
        var last_modified_timestamp = (last_modified.getTime() / 1000) * 1000;
        if (if_modified_since > 0 &&
            if_modified_since >= last_modified_timestamp) {
            context.setStatus(Context.SC_NOT_MODIFIED);
            return;
        }

        // set general headers
        context.setContentType(store.getContentType(content_info));
        if (content_info.hasName()) {
            context.addHeader("Content-Disposition", "inline; filename=" + content_info.getName());
        }
        context.addHeader("Cache-Control", "must-revalidate");
        context.addDateHeader("Expires", System.currentTimeMillis() + 60 * 60 * 1000);
        context.addDateHeader("Last-Modified", last_modified_timestamp);

        store.serveContentData(context, content_info.getContentId());
    }

    protected String _getContentForHtml(String location, Context context, Route route)
    throws ContentManagerException {
        DatabaseContentInfo content_info = null;
        try {
            content_info = getContentInfo(location);
        } catch (IllegalArgumentException e) {
            return "";
        }
        if (null == content_info) {
            return "";
        }

        // retrieve the content store
        var mime_type = MimeType.getMimeType(content_info.getMimeType());
        var store = mimeMapping_.get(mime_type);
        if (null == store) {
            throw new UnsupportedMimeTypeException(mime_type);
        }

        return store.getContentForHtml(content_info.getContentId(), content_info, context, route);
    }

    private static class ContentAttributesProcessor extends DbRowProcessor {
        private Map<String, String> attributes_ = null;

        public boolean processRow(ResultSet result)
        throws SQLException {
            if (null == attributes_) {
                attributes_ = new HashMap<>();
            }

            attributes_.put(result.getString("name"), result.getString("attVal"));
            return true;
        }

        public Map<String, String> getAttributes() {
            return attributes_;
        }
    }

    private static class ContentPropertiesProcessor extends DbRowProcessor {
        private Map<String, String> properties_ = null;

        public boolean processRow(ResultSet result)
        throws SQLException {
            if (null == properties_) {
                properties_ = new HashMap<>();
            }

            properties_.put(result.getString("name"), result.getString("propVal"));
            return true;
        }

        public Map<String, String> getProperties() {
            return properties_;
        }
    }
}
