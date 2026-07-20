/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.examples.models;

public class Task {
    private Integer id_;
    private String description_;
    private boolean done_;

    public void    setId(Integer id)                    { id_ = id; }
    public Integer getId()                              { return id_; }
    public void    setDescription(String description)   { description_ = description; }
    public String  getDescription()                     { return description_; }
    public void    setDone(boolean done)                { done_ = done; }
    public boolean isDone()                             { return done_; }
}
