/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.elements;

import rife.cmf.dam.ContentManager;
import rife.cmf.dam.contentmanagers.DatabaseContentFactory;
import rife.cmf.dam.exceptions.ContentManagerException;
import rife.database.Datasource;
import rife.engine.Context;
import rife.engine.Element;
import rife.tools.ExceptionUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class ServeContent implements Element {
    private final Datasource datasource_;
    private final String repositoryName_;

    public ServeContent(Datasource datasource) {
        if (null == datasource) throw new IllegalArgumentException("datasource can't be null");

        datasource_ = datasource;
        repositoryName_ = null;
    }

    public ServeContent(Datasource datasource, String repositoryName) {
        if (null == datasource) throw new IllegalArgumentException("datasource can't be null");

        datasource_ = datasource;
        repositoryName_ = repositoryName;
    }

    public void process(Context c)
    throws Exception {
        // retrieve and output the content that corresponds to the path info
        ContentManager manager = DatabaseContentFactory.instance(datasource_);

        // get the content path
        String content_path;
        try {
            // get the content path from the path info
            content_path = c.pathInfo();
            if (content_path != null) {
                content_path = URLDecoder.decode(content_path, StandardCharsets.UTF_8);
            }

            // filter the content path
            content_path = filterPath(content_path);

            // prepend a slack to the content path if needed
            if (content_path != null && !content_path.startsWith("/")) {
                content_path = "/" + content_path;
            }

            // prepend the repository name if it was provided
            if (repositoryName_ != null) {
                content_path = repositoryName_ + ":" + content_path;
            }

            // serve the content for the path, if the path is valid
            if (content_path != null &&
                !content_path.equals("/")) {
                manager.serveContentData(c, content_path);
                return;
            }
        } catch (ContentManagerException e) {
            Logger.getLogger("rife.cmf").severe(ExceptionUtils.getExceptionStackTrace(e));
            c.setStatus(Context.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        c.defer();
    }

    public String filterPath(String path) {
        return path;
    }
}
