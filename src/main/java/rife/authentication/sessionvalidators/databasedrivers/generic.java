/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionvalidators.databasedrivers;

import rife.authentication.SessionAttributes;
import rife.authentication.exceptions.SessionValidatorException;
import rife.authentication.sessionvalidators.DatabaseSessionValidator;
import rife.authentication.sessionvalidators.ProcessSessionValidityBasic;
import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.queries.Select;

public class generic extends DatabaseSessionValidator {
    protected Select checkValidityNoRole_;
    protected Select checkValidityNoRoleRestrictAuthData_;
    protected Select checkValidityRole_;
    protected Select checkValidityRoleRestrictAuthData_;

    public generic(Datasource datasource) {
        super(datasource);

        checkValidityNoRole_ = new Select(getDatasource())
            .from(RifeConfig.authentication().getTableAuthentication())
            .field(RifeConfig.authentication().getTableAuthentication() + ".userId")
            .whereParameter(RifeConfig.authentication().getTableAuthentication() + ".authId", "=")
            .whereParameterAnd(RifeConfig.authentication().getTableAuthentication() + ".sessStart", ">");

        checkValidityNoRoleRestrictAuthData_ = checkValidityNoRole_.clone()
            .whereParameterAnd(RifeConfig.authentication().getTableAuthentication() + ".authData", "=");

        checkValidityRole_ = new Select(getDatasource())
            .from(RifeConfig.authentication().getTableAuthentication())
            .join(RifeConfig.authentication().getTableRoleLink())
            .join(RifeConfig.authentication().getTableRole())
            .field(RifeConfig.authentication().getTableAuthentication() + ".userId")
            .whereParameter(RifeConfig.authentication().getTableAuthentication() + ".authId", "=")
            .whereParameterAnd(RifeConfig.authentication().getTableAuthentication() + ".sessStart", ">")
            .whereAnd(RifeConfig.authentication().getTableAuthentication() + ".userId = " + RifeConfig.authentication().getTableRoleLink() + ".userId")
            .whereParameterAnd(RifeConfig.authentication().getTableRole() + ".name", "role", "=")
            .whereAnd(RifeConfig.authentication().getTableRole() + ".roleId = " + RifeConfig.authentication().getTableRoleLink() + ".roleId");

        checkValidityRoleRestrictAuthData_ = checkValidityRole_.clone()
            .whereParameterAnd(RifeConfig.authentication().getTableAuthentication() + ".authData", "=");
    }

    @Override
    public int validateSession(String authId, String authData, SessionAttributes attributes)
    throws SessionValidatorException {
        return _validateSession(checkValidityNoRole_, checkValidityNoRoleRestrictAuthData_, checkValidityRole_, checkValidityRoleRestrictAuthData_, new ProcessSessionValidityBasic(), authId, authData, attributes);
    }
}


