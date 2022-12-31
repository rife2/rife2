/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.validation.*;

public class PersonWithEmail extends MetaData {
    private Integer id_;
    private String name_;
    private String email_;

    public void activateMetaData() {
        addConstraint(new ConstrainedBean()
            .defaultOrder("name"));

        addConstraint(new ConstrainedProperty("id")
            .identifier(true));
        addConstraint(new ConstrainedProperty("name")
            .notNull(true)
            .maxLength(20));
        addConstraint(new ConstrainedProperty("email")
            .notNull(true)
            .maxLength(50)
            .email(true));
    }

    public void    setId(Integer id)      { id_ = id; }
    public Integer getId()                { return id_; }
    public void    setName(String name)   { name_ = name; }
    public String  getName()              { return name_; }
    public void    setEmail(String email) { email_ = email; }
    public String  getEmail()             { return email_; }
}