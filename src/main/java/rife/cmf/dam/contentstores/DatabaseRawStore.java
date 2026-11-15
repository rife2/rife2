/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentstores;

import rife.cmf.Content;
import rife.cmf.ContentInfo;
import rife.cmf.MimeType;
import rife.cmf.dam.ContentDataUser;
import rife.cmf.dam.ContentDataUserWithoutResult;
import rife.cmf.dam.ContentStore;
import rife.cmf.dam.contentstores.exceptions.*;
import rife.cmf.dam.exceptions.ContentManagerException;
import rife.cmf.format.Formatter;
import rife.cmf.format.exceptions.FormatException;
import rife.cmf.transform.ContentTransformer;
import rife.config.RifeConfig;
import rife.database.*;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.*;
import rife.engine.Context;
import rife.engine.Route;
import rife.tools.Convert;
import rife.tools.ExceptionUtils;
import rife.tools.FileUtils;
import rife.tools.InnerClassException;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
            !(content.getData() instanceof byte[]))
            throw new IllegalArgumentException("the content data must be of type InputStream or byte[]");

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

    /**
     * Serves a range of the content data for a certain content id through
     * the provided context.
     * <p>This takes over the complete handling of the request, like
     * {@link #serveContentData(Context, int)}, apart from the status and the
     * {@code Content-Range} header.
     *
     * <p>An {@code end} beyond the last byte serves the data up to its last
     * byte, as does a negative {@code end}.
     *
     * @param context an active web engine context
     * @param id      the id of the content whose data will be served
     * @param start   the position of the first byte that will be served
     * @param end     the position of the last byte that will be served, inclusive
     * @throws IllegalArgumentException if {@code start} is negative, or
     *                                  {@code end} isn't negative and lies before {@code start}
     * @throws ContentManagerException  if an unexpected error occurred
     * @since 1.11.0
     */
    public abstract void serveContentData(Context context, int id, long start, long end)
    throws ContentManagerException;

    protected void _serveContentData(final Select retrieveContentChunks, final Context context, final int id)
    throws ContentManagerException {
        _serveContentData(retrieveContentChunks, context, id, 0, -1);
    }

    // an end before zero serves the data up to its last byte
    protected void _serveContentData(final Select retrieveContentChunks, final Context context, final int id, final long start, final long end)
    throws ContentManagerException {
        if (null == context) throw new IllegalArgumentException("element can't be null");
        if (start < 0) throw new IllegalArgumentException("start can't be negative");
        if (end >= 0 && end < start) throw new IllegalArgumentException("end can't lie before start");

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
        final var last = end < 0 ? size - 1 : Math.min(end, size - 1);
        final var count = Math.max(0, last - start + 1);
        context.setContentLength((int) count);
        // empty data is stored without any chunks
        if (0 == size) {
            return;
        }

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
                        serveChunks(resultSet, os, start, count);

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

    // the chunks don't have a fixed size, so the ones before the range are
    // read and skipped, and no chunks are read after it
    protected void serveChunks(DbResultSet resultset, OutputStream os, long skip, long count)
    throws SQLException {
        var buffer = new byte[8192];
        var remaining_skip = skip;
        var remaining = count;
        do {
            try (var is = resultset.getBinaryStream("chunk")) {
                int read;
                while (remaining > 0 && (read = is.read(buffer)) != -1) {
                    var offset = (int) Math.min(remaining_skip, read);
                    remaining_skip -= offset;
                    var length = (int) Math.min(read - offset, remaining);
                    if (length > 0) {
                        os.write(buffer, offset, length);
                        remaining -= length;
                    }
                }
            } catch (IOException e) {
                // the client has probably disconnected
                return;
            }
        }
        while (remaining > 0 && resultset.next());
    }
}
