/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.examples.models;

public class Article {
    private String title_;
    private String email_;
    private String body_;

    public void    setTitle(String title) { title_ = title; }
    public String  getTitle()             { return title_; }
    public void    setEmail(String email) { email_ = email; }
    public String  getEmail()             { return email_; }
    public void    setBody(String body)   { body_ = body; }
    public String  getBody()              { return body_; }
}