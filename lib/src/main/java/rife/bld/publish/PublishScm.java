/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.publish;

public class PublishScm {
    private String connection_;
    private String developerConnection_;
    private String url_;

    public String connection() {
        return connection_;
    }

    public PublishScm connection(String connection) {
        connection_ = connection;
        return this;
    }

    public String developerConnection() {
        return developerConnection_;
    }

    public PublishScm developerConnection(String developerConnection) {
        developerConnection_ = developerConnection;
        return this;
    }

    public String url() {
        return url_;
    }

    public PublishScm url(String url) {
        url_ = url;
        return this;
    }
}
