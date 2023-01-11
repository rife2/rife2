/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentstores;

import rife.cmf.dam.*;
import rife.cmf.dam.contentstores.exceptions.*;
import rife.database.*;
import rife.database.queries.*;

import rife.cmf.Content;
import rife.cmf.ContentInfo;
import rife.cmf.MimeType;
import rife.cmf.dam.exceptions.ContentManagerException;
import rife.cmf.format.Formatter;
import rife.cmf.format.exceptions.FormatException;
import rife.cmf.transform.ContentTransformer;
import rife.config.RifeConfig;
import rife.database.exceptions.DatabaseException;
import rife.engine.Context;
import rife.engine.Route;
import rife.tools.Convert;
import rife.tools.ExceptionUtils;
import rife.tools.FileUtils;
import rife.tools.InnerClassException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

public abstract class DatabaseRawStore extends DbQueryManager implements ContentStore {
    private final List<MimeType> mimeTypes_ = new ArrayList<>();

    public DatabaseRawStore(Datasource datasource) {
        super(datasource);

        addMimeType(MimeType.RAW);
    }

    protected void addMimeType(MimeType mimeType) {
        mimeTypes_.add(mimeType);
    }

    public Collection<MimeType> getSupportedMimeTypes() {
        return mimeTypes_;
    }

    public String getContentType(ContentInfo contentInfo) {
        var mimeType = MimeType.getMimeType(contentInfo.getMimeType());
        if (!getSupportedMimeTypes().contains(mimeType)) {
            return null;
        }

        var attributes = contentInfo.getAttributes();
        if (attributes != null) {
            if (attributes.containsKey("content-type")) {
                return attributes.get("content-type");
            }
        }
        if (contentInfo.hasName()) {
            return RifeConfig.Mime.getMimeType(FileUtils.getExtension(contentInfo.getName()));
        }

        return null;
    }

    public Formatter getFormatter(MimeType mimeType, boolean fragment) {
        if (!getSupportedMimeTypes().contains(mimeType)) {
            return null;
        }
        return mimeType.getFormatter();
    }

    public String getContentForHtml(int id, ContentInfo info, Context context, Route route)
    throws ContentManagerException {
        return "";
    }

    protected boolean _install(CreateTable createTableContentInfo, CreateTable createTableContentChunk)
    throws ContentManagerException {
        assert createTableContentInfo != null;
        assert createTableContentChunk != null;

        try {
            executeUpdate(createTableContentInfo);
            executeUpdate(createTableContentChunk);
        } catch (DatabaseException e) {
            throw new InstallContentStoreErrorException(e);
        }

        return true;
    }

    protected boolean _remove(DropTable dropTableContentInfo, DropTable dropTableContentChunk)
    throws ContentManagerException {
        assert dropTableContentInfo != null;

        try {
            executeUpdate(dropTableContentChunk);
            executeUpdate(dropTableContentInfo);
        } catch (DatabaseException e) {
            throw new RemoveContentStoreErrorException(e);
        }

        return true;
    }

    protected boolean _deleteContentData(final Delete deleteContentInfo, final Delete deleteContentChunk, final int id)
    throws ContentManagerException {
        if (id < 0) throw new IllegalArgumentException("id must be positive");

        assert deleteContentInfo != null;
        assert deleteContentChunk != null;

        Boolean result = null;

        try {
            result = inTransaction(() -> {
                if (executeUpdate(deleteContentChunk, s -> s.setInt("contentId", id)) == 0) {
                    return false;
                }

                return executeUpdate(deleteContentInfo, s -> s.setInt("contentId", id)) != 0;
            });
        } catch (DatabaseException e) {
            throw new DeleteContentDataErrorException(id, e);
        }

        return result != null && result;
    }

    protected int _getSize(Select retrieveSize, final int id)
    throws ContentManagerException {
        if (id < 0) throw new IllegalArgumentException("id must be positive");

        assert retrieveSize != null;

        try {
            return executeGetFirstInt(retrieveSize, s -> s.setInt("contentId", id));
        } catch (DatabaseException e) {
            throw new RetrieveSizeErrorException(id, e);
        }
    }

    protected boolean _hasContentData(Select hasContentData, final int id)
    throws ContentManagerException {
        if (id < 0) throw new IllegalArgumentException("id must be positive");

        assert hasContentData != null;

        try {
            return executeHasResultRows(hasContentData, s -> s.setInt("contentId", id));
        } catch (DatabaseException e) {
            throw new HasContentDataErrorException(id, e);
        }
    }

    protected boolean _storeContentData(final Insert storeContentInfo, final Insert storeContentChunk, final int id, Content content, ContentTransformer transformer)
    throws ContentManagerException {
        if (id < 0) throw new IllegalArgumentException("id must be positive");
        if (content != null &&
            content.getData() != null &&
            !(content.getData() instanceof InputStream) &&
            !(content.getData() instanceof byte[])) throw new IllegalArgumentException("the content data must be of type InputStream or byte[]");

        assert storeContentInfo != null;
        assert storeContentChunk != null;

        final InputStream typed_data;

        if (null == content ||
            null == content.getData()) {
            typed_data = null;
        } else {
            if (content.getData() instanceof byte[]) {
                var cloned_content = content.clone();
                cloned_content.setData(new ByteArrayInputStream((byte[]) content.getData()));
                cloned_content.setCachedLoadedData(null);
                content = cloned_content;
            }

            Formatter formatter = null;
            if (!Convert.toBoolean(content.getAttribute("unformatted"), false)) {
                formatter = getFormatter(content.getMimeType(), content.isFragment());
            }

            if (formatter != null) {
                try {
                    typed_data = (InputStream) formatter.format(content, transformer);
                } catch (FormatException e) {
                    throw new StoreContentDataErrorException(id, e);
                }
            } else {
                typed_data = (InputStream) content.getData();
            }
        }

        // store the data
        try {
            Boolean success = inTransaction(new DbTransactionUser<>() {
                public Object useTransaction()
                throws InnerClassException {
                    try {
                        final var size = storeChunks(storeContentChunk, id, typed_data);
                        if (size < 0) {
                            rollback();
                        }

                        if (executeUpdate(storeContentInfo, s ->
                            s.setInt("contentId", id)
                                .setInt("contentSize", size)) <= 0) {
                            rollback();
                        }
                    } catch (IOException e) {
                        throwException(e);
                    }

                    return true;
                }
            });

            return null != success && success;
        } catch (InnerClassException e) {
            throw new StoreContentDataErrorException(id, e.getCause());
        } catch (DatabaseException e) {
            throw new StoreContentDataErrorException(id, e);
        }
    }

    protected int storeChunks(Insert storeContentChunk, final int id, InputStream data)
    throws IOException {
        class Scope {
            int size = 0;
            int length = -1;
            int ordinal = 0;
            byte[] buffer = null;
        }
        final var scope = new Scope();

        if (data != null) {
            scope.buffer = new byte[65535];
            while ((scope.length = data.read(scope.buffer)) != -1) {
                scope.size += scope.length;

                if (executeUpdate(storeContentChunk, s ->
                    s.setInt("contentId", id)
                        .setInt("ordinal", scope.ordinal)
                        .setBinaryStream("chunk", new ByteArrayInputStream(scope.buffer), scope.length)) <= 0) {
                    return -1;
                }

                scope.ordinal++;
            }
        }

        return scope.size;
    }

    protected int storeChunksNoStream(Insert storeContentChunk, final int id, InputStream data)
    throws IOException {
        class Scope {
            int size = 0;
            int length = -1;
            int ordinal = 0;
            byte[] buffer = null;
            byte[] buffer_swp = null;
        }
        final var scope = new Scope();

        if (data != null) {
            scope.buffer = new byte[65535];
            while ((scope.length = data.read(scope.buffer)) != -1) {
                scope.size += scope.length;

                if (scope.length < scope.buffer.length) {
                    var new_buffer = new byte[scope.length];
                    System.arraycopy(scope.buffer, 0, new_buffer, 0, scope.length);
                    scope.buffer_swp = scope.buffer;
                    scope.buffer = new_buffer;
                }

                if (executeUpdate(storeContentChunk, s ->
                    s.setInt("contentId", id)
                        .setInt("ordinal", scope.ordinal)
                        .setBytes("chunk", scope.buffer)) <= 0) {
                    return -1;
                }

                if (scope.buffer_swp != null) {
                    scope.buffer = scope.buffer_swp;
                    scope.buffer_swp = null;
                }

                scope.ordinal++;
            }
        }

        return scope.size;
    }

    protected void _useContentData(Select retrieveContentChunks, final int id, ContentDataUserWithoutResult user)
    throws ContentManagerException {
        if (id < 0) throw new IllegalArgumentException("id must be positive");
        if (null == user) throw new IllegalArgumentException("user can't be null");

        assert retrieveContentChunks != null;

        try {
            InputStream data = RawContentStream.instance(this, retrieveContentChunks, id);
            try {
                user.useContentData(data);
            } finally {
                if (data != null) {
                    try {
                        data.close();
                    } catch (IOException e) {
                        throw new UseContentDataErrorException(id, e);
                    }
                }
            }
        } catch (DatabaseException e) {
            throw new UseContentDataErrorException(id, e);
        }
    }

    protected <ResultType> ResultType _useContentDataResult(Select retrieveContentChunks, final int id, ContentDataUser<ResultType> user)
    throws ContentManagerException {
        if (id < 0) throw new IllegalArgumentException("id must be positive");
        if (null == user) throw new IllegalArgumentException("user can't be null");

        assert retrieveContentChunks != null;

        try {
            InputStream data = RawContentStream.instance(this, retrieveContentChunks, id);
            try {
                return user.useContentData(data);
            } finally {
                if (data != null) {
                    try {
                        data.close();
                    } catch (IOException e) {
                        throw new UseContentDataErrorException(id, e);
                    }
                }
            }
        } catch (DatabaseException e) {
            throw new UseContentDataErrorException(id, e);
        }
    }

    protected DbPreparedStatement getStreamPreparedStatement(Query query, DbConnection connection) {
        var statement = connection.getPreparedStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
        statement.setFetchDirection(ResultSet.FETCH_FORWARD);
        statement.setFetchSize(1);
        return statement;
    }

    protected void _serveContentData(final Select retrieveContentChunks, final Context context, final int id)
    throws ContentManagerException {
        if (null == context) throw new IllegalArgumentException("element can't be null");

        if (id < 0) {
            context.defer();
            return;
        }

        assert retrieveContentChunks != null;

        // set the content length header
        final var size = getSize(id);
        if (size < 0) {
            context.defer();
            return;
        }
        context.setContentLength(size);

        try {
            Boolean success = executeQuery(retrieveContentChunks, new DbPreparedStatementHandler<>() {
                public DbPreparedStatement getPreparedStatement(Query query, DbConnection connection) {
                    return getStreamPreparedStatement(query, connection);
                }

                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setInt("contentId", id);
                }

                public Object concludeResults(DbResultSet resultSet)
                throws SQLException {
                    if (!resultSet.next()) {
                        return false;
                    }

                    // output the content
                    var os = context.outputStream();
                    try {
                        serveChunks(resultSet, os, size);

                        os.flush();
                    } catch (IOException e) {
                        // don't do anything, the client has probably disconnected
                    }

                    return true;
                }
            });

            if (null == success || !success) {
                context.defer();
            }
        } catch (DatabaseException e) {
            Logger.getLogger("rife.cmf").severe(ExceptionUtils.getExceptionStackTrace(e));
            context.setStatus(Context.SC_INTERNAL_SERVER_ERROR);
        }
    }

    protected void serveChunks(DbResultSet resultset, OutputStream os, int size)
    throws SQLException, IOException {
        var buffer = new byte[512];
        do {
            var is = resultset.getBinaryStream("chunk");
            var buffered_raw_is = new BufferedInputStream(is, 512);
            var buffer_size = 0;
            try {
                while ((buffer_size = buffered_raw_is.read(buffer)) != -1) {
                    os.write(buffer, 0, buffer_size);
                }
            } catch (IOException e) {
                // don't do anything, the client has probably disconnected
            }
        }
        while (resultset.next());
    }
}
