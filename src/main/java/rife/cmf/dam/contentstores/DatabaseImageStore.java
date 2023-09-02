/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentstores;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import rife.cmf.Content;
import rife.cmf.ContentInfo;
import rife.cmf.MimeType;
import rife.cmf.dam.ContentDataUser;
import rife.cmf.dam.ContentDataUserWithoutResult;
import rife.cmf.dam.contentstores.exceptions.StoreContentDataErrorException;
import rife.cmf.dam.contentstores.exceptions.UseContentDataErrorException;
import rife.cmf.dam.exceptions.ContentManagerException;
import rife.cmf.format.Formatter;
import rife.cmf.format.ImageFormatter;
import rife.cmf.format.exceptions.FormatException;
import rife.cmf.transform.ContentTransformer;
import rife.database.Datasource;
import rife.database.DbPreparedStatement;
import rife.database.DbPreparedStatementHandler;
import rife.database.DbResultSet;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.Insert;
import rife.database.queries.Select;
import rife.engine.Context;
import rife.engine.Route;
import rife.tools.Convert;
import rife.tools.StringUtils;
import rife.tools.exceptions.ConversionException;

public abstract class DatabaseImageStore extends DatabaseContentStore {
    public DatabaseImageStore(Datasource datasource) {
        super(datasource);

        addMimeType(MimeType.IMAGE_GIF);
        addMimeType(MimeType.IMAGE_JPEG);
        addMimeType(MimeType.IMAGE_PNG);
    }

    public String getContentType(ContentInfo contentInfo) {
        var mimeType = MimeType.getMimeType(contentInfo.getMimeType());
        if (!getSupportedMimeTypes().contains(mimeType)) {
            return null;
        }

        var content_type = mimeType.toString();

        var attributes = contentInfo.getAttributes();
        if (attributes != null) {
            if (attributes.containsKey("content-type")) {
                content_type = attributes.get("content-type");
            }
        }

        return content_type;
    }

    public String getContentForHtml(int id, ContentInfo info, Context context, Route route)
    throws ContentManagerException {
        if (null == context) throw new IllegalArgumentException("context can't be null.");
        if (null == route) throw new IllegalArgumentException("route can't be null.");

        var result = new StringBuilder();
        result.append("<img src=\"")
            .append(StringUtils.encodeHtml(context.urlFor(route).pathInfo(info.getPath()).toString()))
            .append('"');
        var properties = info.getProperties();
        if (properties != null) {
            var width = properties.get(ImageFormatter.CmfProperty.WIDTH);
            var height = properties.get(ImageFormatter.CmfProperty.HEIGHT);
            var hidpi = Convert.toBoolean(properties.get(ImageFormatter.CmfProperty.HIDPI), true);
            if (width != null) {
                try {
                    var width_attribute = Convert.toInt(width);
                    if (hidpi) {
                        width_attribute = width_attribute / 2;
                    }
                    result.append(" width=\"");
                    result.append(width_attribute);
                    result.append('"');
                } catch (ConversionException e) {
                    throw new RuntimeException(e);
                }
            }
            if (height != null) {
                try {
                    var height_attribute = Convert.toInt(height);
                    if (hidpi) {
                        height_attribute = height_attribute / 2;
                    }
                    result.append(" height=\"");
                    result.append(height_attribute);
                    result.append('"');
                } catch (ConversionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        result.append(" alt=\"\" />");

        return result.toString();
    }

    protected boolean _storeContentData(final Insert storeContent, final int id, Content content, ContentTransformer transformer)
    throws ContentManagerException {
        if (id < 0) throw new IllegalArgumentException("id must be positive");
        if (content != null &&
            content.getData() != null &&
            !(content.getData() instanceof byte[])) throw new IllegalArgumentException("the content data must be of type byte[]");

        assert storeContent != null;

        final byte[] typed_data;

        if (null == content ||
            null == content.getData()) {
            typed_data = null;
        } else {
            Formatter formatter = null;
            if (!Convert.toBoolean(content.getAttribute("unformatted"), false)) {
                formatter = getFormatter(content.getMimeType(), content.isFragment());
            }

            if (formatter != null) {
                try {
                    typed_data = (byte[]) formatter.format(content, transformer);
                } catch (FormatException e) {
                    throw new StoreContentDataErrorException(id, e);
                }
            } else {
                typed_data = (byte[]) content.getData();
            }
        }

        return storeTypedData(storeContent, id, typed_data);
    }

    protected boolean storeTypedData(Insert storeContent, final int id, final byte[] data)
    throws ContentManagerException {
        try {
            var result = executeUpdate(storeContent, s -> {
                s.setInt("contentId", id);
                if (null == data) {
                    s.setNull("content", getNullSqlType())
                        .setInt("contentSize", 0);
                } else {
                    s.setBinaryStream("content", new ByteArrayInputStream(data), data.length)
                        .setInt("contentSize", data.length);
                }
            });

            return result != -1;
        } catch (DatabaseException e) {
            throw new StoreContentDataErrorException(id, e);
        }
    }

    protected int getNullSqlType() {
        return Types.BLOB;
    }

    protected void _useContentData(Select retrieveContent, final int id, ContentDataUserWithoutResult user)
    throws ContentManagerException {
        if (id < 0) throw new IllegalArgumentException("id must be positive");
        if (null == user) throw new IllegalArgumentException("user can't be null");

        assert retrieveContent != null;

        try {
            user.useContentData(executeQuery(retrieveContent, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setInt("contentId", id);
                }

                public Object concludeResults(DbResultSet resultSet)
                throws SQLException {
                    if (!resultSet.next()) {
                        return null;
                    }

                    return resultSet.getBytes("content");
                }
            }));
        } catch (DatabaseException e) {
            throw new UseContentDataErrorException(id, e);
        }
    }

    protected <ResultType> ResultType _useContentDataResult(Select retrieveContent, final int id, ContentDataUser<ResultType> user)
    throws ContentManagerException {
        if (id < 0) throw new IllegalArgumentException("id must be positive");
        if (null == user) throw new IllegalArgumentException("user can't be null");

        assert retrieveContent != null;

        try {
            return user.useContentData(executeQuery(retrieveContent, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setInt("contentId", id);
                }

                public Object concludeResults(DbResultSet resultSet)
                throws SQLException {
                    if (!resultSet.next()) {
                        return null;
                    }

                    return resultSet.getBytes("content");
                }
            }));
        } catch (DatabaseException e) {
            throw new UseContentDataErrorException(id, e);
        }
    }

    protected void outputContentColumn(ResultSet resultSet, OutputStream os)
    throws SQLException {
        var is = resultSet.getBinaryStream("content");
        var buffer = new byte[512];
        var buffered_raw_is = new BufferedInputStream(is, 512);
        var size = 0;
        try {
            while ((size = buffered_raw_is.read(buffer)) != -1) {
                os.write(buffer, 0, size);
            }

            os.flush();
        } catch (IOException e) {
            // don't do anything, the client has probably disconnected
        }
    }
}
