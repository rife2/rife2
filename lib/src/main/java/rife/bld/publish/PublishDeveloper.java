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

    public String getId() {
        return id_;
    }

    public PublishDeveloper id(String id) {
        setId(id);
        return this;
    }

    public void setId(String id) {
        this.id_ = id;
    }

    public String getName() {
        return name_;
    }

    public PublishDeveloper name(String name) {
        setName(name);
        return this;
    }

    public void setName(String name) {
        this.name_ = name;
    }

    public String getEmail() {
        return email_;
    }

    public PublishDeveloper email(String email) {
        setEmail(email);
        return this;
    }

    public void setEmail(String email) {
        this.email_ = email;
    }

    public String getUrl() {
        return url_;
    }

    public PublishDeveloper url(String url) {
        setUrl(url);
        return this;
    }

    public void setUrl(String url) {
        this.url_ = url;
    }
}
