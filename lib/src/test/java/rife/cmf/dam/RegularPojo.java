/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam;

public class RegularPojo {
    private int id_ = -1;
    private String name_ = null;

    public RegularPojo() {
    }

    public void setId(int id) {
        id_ = id;
    }

    public int getId() {
        return id_;
    }

    public void setName(String name) {
        name_ = name;
    }

    public String getName() {
        return name_;
    }

    public RegularPojo name(String name) {
        name_ = name;
        return this;
    }
}
