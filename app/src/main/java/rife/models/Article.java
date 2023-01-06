/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.models;

public class Article {
    private Integer id_;
    private String title_;
    private String email_;
    private String body_;

    public void    setId(Integer id)      { id_ = id; }
    public Integer getId()                { return id_; }
    public void    setTitle(String title) { title_ = title; }
    public String  getTitle()             { return title_; }
    public void    setEmail(String email) { email_ = email; }
    public String  getEmail()             { return email_; }
    public void    setBody(String body)   { body_ = body; }
    public String  getBody()              { return body_; }
}