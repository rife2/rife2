/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionvalidators;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProcessSessionValidityBasic extends ProcessSessionValidity {
    private int mValidity = DatabaseSessionValidator.SESSION_INVALID;

    public boolean processRow(ResultSet resultSet)
    throws SQLException {
        assert resultSet != null;

        mValidity = DatabaseSessionValidator.SESSION_VALID;

        return true;
    }

    public int getValidity() {
        return mValidity;
    }
}

