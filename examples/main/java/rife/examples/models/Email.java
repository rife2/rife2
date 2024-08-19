/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.examples.models;

import rife.validation.*;

public class Email extends MetaData {
    private Integer id_;
    private String email_;
    private Friend friend_;

    public void activateMetaData() {
        addConstraint(new ConstrainedProperty("id")
            .identifier(true));
        addConstraint(new ConstrainedProperty("email")
            .notNull(true)
            .maxLength(50)
            .email(true));
        addConstraint(new ConstrainedProperty("friend")
            .manyToOne());
    }

    public void    setId(Integer id)        { id_ = id; }
    public Integer getId()                  { return id_; }
    public void    setEmail(String email)   { email_ = email; }
    public String  getEmail()               { return email_; }
    public void    setFriend(Friend friend) { friend_ = friend; }
    public Friend  getFriend()              { return friend_; }
}
