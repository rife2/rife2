/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

public class Person {
    private Integer id_;
    private String name_;

    public void    setId(Integer id)    { id_ = id; }
    public Integer getId()              { return id_; }
    public void    setName(String name) { name_ = name; }
    public String  getName()            { return name_; }
}