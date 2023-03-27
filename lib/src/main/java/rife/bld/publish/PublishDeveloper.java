/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.publish;

public class PublishDeveloper {
    private String id_;
    private String name_;
    private String email_;
    private String url_;

    public String id() {
        return id_;
    }

    public PublishDeveloper id(String id) {
        id_ = id;
        return this;
    }

    public String name() {
        return name_;
    }

    public PublishDeveloper name(String name) {
        name_ = name;
        return this;
    }

    public String email() {
        return email_;
    }

    public PublishDeveloper email(String email) {
        email_ = email;
        return this;
    }

    public String url() {
        return url_;
    }

    public PublishDeveloper url(String url) {
        url_ = url;
        return this;
    }
}
