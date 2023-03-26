/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.publish;

public class PomLicense {
    private String name_;
    private String url_;

    public String getName() {
        return name_;
    }

    public PomLicense name(String name) {
        setName(name);
        return this;
    }

    public void setName(String name) {
        this.name_ = name;
    }

    public String getUrl() {
        return url_;
    }

    public PomLicense url(String url) {
        setUrl(url);
        return this;
    }

    public void setUrl(String url) {
        this.url_ = url;
    }
}
