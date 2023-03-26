/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.publish;

public class PomScm {
    private String connection_;
    private String developerConnection_;
    private String url_;

    public String getConnection() {
        return connection_;
    }

    public PomScm connection(String connection) {
        setConnection(connection);
        return this;
    }

    public void setConnection(String connection) {
        this.connection_ = connection;
    }

    public String getDeveloperConnection() {
        return developerConnection_;
    }

    public PomScm developerConnection(String developerConnection) {
        setDeveloperConnection(developerConnection);
        return this;
    }

    public void setDeveloperConnection(String developerConnection) {
        this.developerConnection_ = developerConnection;
    }

    public String getUrl() {
        return url_;
    }

    public PomScm url(String url) {
        setUrl(url);
        return this;
    }

    public void setUrl(String url) {
        this.url_ = url;
    }
}
