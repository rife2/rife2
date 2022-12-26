/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentstores;

import rife.cmf.Content;
import rife.cmf.ContentInfo;
import rife.cmf.MimeType;
import rife.cmf.dam.ContentDataUser;
import rife.cmf.dam.ContentDataUserWithoutResult;
import rife.cmf.dam.contentstores.exceptions.StoreContentDataErrorException;
import rife.cmf.dam.contentstores.exceptions.UseContentDataErrorException;
import rife.cmf.dam.exceptions.ContentManagerException;
import rife.cmf.format.Formatter;
import rife.cmf.format.exceptions.FormatException;
import rife.cmf.transform.ContentTransformer;
import rife.database.Datasource;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.Insert;
import rife.database.queries.Select;
import rife.engine.Context;
import rife.engine.Route;
import rife.tools.Convert;
import rife.tools.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public abstract class DatabaseTextStore extends DatabaseContentStore {
    public DatabaseTextStore(Datasource datasource) {
        super(datasource);

        addMimeType(MimeType.APPLICATION_XHTML);
        addMimeType(MimeType.TEXT_PLAIN);
        addMimeType(MimeType.TEXT_XML);
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

        return content_type + "; charset=UTF-8";
    }

    public Formatter getFormatter(MimeType mimeType, boolean fragment) {
        if (!getSupportedMimeTypes().contains(mimeType)) {
            return null;
        }
        return mimeType.getFormatter();
    }

    protected boolean _storeContentData(Insert storeContent, final int id, Content content, ContentTransformer transformer)
    throws ContentManagerException {
        if (id < 0) throw new IllegalArgumentException("id must be positive");
        if (content != null &&
            content.getData() != null &&
            !(content.getData() instanceof String)) throw new IllegalArgumentException("the content data must be of type String");

        assert storeContent != null;

        final String typed_data;

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
                    typed_data = (String) formatter.format(content, transformer);
                } catch (FormatException e) {
                    throw new StoreContentDataErrorException(id, e);
                }
            } else {
                typed_data = (String) content.getData();
            }
        }

        return storeContent(storeContent, id, typed_data);
    }

    protected boolean storeContent(Insert storeContent, final int id, final String data)
    throws ContentManagerException {
        try {
            var result = executeUpdate(storeContent, s -> {
                s.setInt("contentId", id);
                if (null == data) {
                    s.setNull("content", Types.CLOB)
                        .setInt("contentSize", 0);
                } else {
                    byte[] bytes = null;
                    bytes = data.getBytes(StandardCharsets.UTF_8);

                    s.setInt("contentSize", bytes.length)
                        .setCharacterStream("content", new StringReader(data), data.length());
                }
            });

            return result > 0;
        } catch (DatabaseException e) {
            throw new StoreContentDataErrorException(id, e);
        }
    }

    protected void _useContentData(Select retrieveContent, final int id, ContentDataUserWithoutResult user)
    throws ContentManagerException {
        if (id < 0) throw new IllegalArgumentException("id must be positive");
        if (null == user) throw new IllegalArgumentException("user can't be null");

        assert retrieveContent != null;

        try {
            user.useContentData(executeGetFirstString(retrieveContent, s -> s.setInt("contentId", id)));
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
            return user.useContentData(executeGetFirstString(retrieveContent, s -> s.setInt("contentId", id)));
        } catch (DatabaseException e) {
            throw new UseContentDataErrorException(id, e);
        }
    }

    public String getContentForHtml(int id, final ContentInfo info, Context context, Route route)
    throws ContentManagerException {
        return useContentDataResult(id, contentData -> {
            if (null == contentData) {
                return "";
            }

            if (MimeType.APPLICATION_XHTML.toString().equals(info.getMimeType())) {
                return contentData.toString();
            } else if (MimeType.TEXT_PLAIN.toString().equals(info.getMimeType())) {
                return StringUtils.encodeHtml(contentData.toString());
            }

            return "";
        });
    }

    protected void outputContentColumn(ResultSet resultSet, OutputStream os)
    throws SQLException {
        var text_reader = resultSet.getCharacterStream("content");
        var buffer = new char[512];
        var size = 0;
        try {
            while ((size = text_reader.read(buffer)) != -1) {
                var string_buffer = new String(buffer, 0, size);
                os.write(string_buffer.getBytes(StandardCharsets.UTF_8));
            }

            os.flush();
        } catch (IOException e) {
            // don't do anything, the client has probably disconnected
        }
    }
}
