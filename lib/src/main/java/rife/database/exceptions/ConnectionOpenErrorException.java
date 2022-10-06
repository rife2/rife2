/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import java.io.Serial;

public class ConnectionOpenErrorException extends DatabaseException {
    @Serial private static final long serialVersionUID = -8963881858111262119L;

    private final String url_;
    private final String user_;
    private final String password_;

    public ConnectionOpenErrorException(String url, Throwable cause) {
        super("Couldn't connect to the database with connection url '" + url + "'.", cause);
        url_ = url;
        user_ = null;
        password_ = null;
    }

    public ConnectionOpenErrorException(String url, String user, String password, Throwable cause) {
        super("Couldn't connect to the database with connection url '" + url + "'.", cause);
        url_ = url;
        user_ = user;
        password_ = password;
    }

    public String getUrl() {
        return url_;
    }

    public String getUser() {
        return user_;
    }

    public String getPassword() {
        return password_;
    }
}
