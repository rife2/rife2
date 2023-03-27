/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.publish;

public class PublishLicense {
    private String name_;
    private String url_;

    public String name() {
        return name_;
    }

    public PublishLicense name(String name) {
        name_ = name;
        return this;
    }

    public String url() {
        return url_;
    }

    public PublishLicense url(String url) {
        url_ = url;
        return this;
    }
}
