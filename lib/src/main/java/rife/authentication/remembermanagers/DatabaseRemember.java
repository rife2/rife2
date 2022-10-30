/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.remembermanagers;

import rife.authentication.remembermanagers.exceptions.*;

import rife.authentication.RememberManager;
import rife.authentication.exceptions.RememberManagerException;
import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.DbPreparedStatement;
import rife.database.DbPreparedStatementHandler;
import rife.database.DbQueryManager;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.CreateTable;
import rife.database.queries.Delete;
import rife.database.queries.DropTable;
import rife.database.queries.Insert;
import rife.database.queries.Select;
import rife.tools.StringEncryptor;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public abstract class DatabaseRemember extends DbQueryManager implements RememberManager {
    private long rememberDuration_ = RifeConfig.authentication().getRememberDuration();

    protected DatabaseRemember(Datasource datasource) {
        super(datasource);
    }

    public long getRememberDuration() {
        return rememberDuration_;
    }

    public void setRememberDuration(long milliseconds) {
        rememberDuration_ = milliseconds;
    }

    public abstract boolean install()
    throws RememberManagerException;

    public abstract boolean remove()
    throws RememberManagerException;

    protected boolean _install(CreateTable createRemember, String createRememberMomentIndex) {
        executeUpdate(createRemember);
        executeUpdate(createRememberMomentIndex);

        return true;
    }

    protected boolean _remove(DropTable removeRemember, String removeRememberMomentIndex) {
        executeUpdate(removeRememberMomentIndex);
        executeUpdate(removeRemember);

        return true;
    }

    protected String _createRememberId(Insert createRememberId, final long userId, String hostIp)
    throws RememberManagerException {
        assert createRememberId != null;

        if (userId < 0) {
            throw new CreateRememberIdErrorException(userId);
        }

        final String remember_id_string = UUID.randomUUID().toString();

        try {
            if (0 == executeUpdate(createRememberId, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("rememberId", remember_id_string)
                        .setLong("userId", userId)
                        .setLong("moment", System.currentTimeMillis());
                }
            })) {
                throw new CreateRememberIdErrorException(userId);
            }
        } catch (DatabaseException e) {
            throw new CreateRememberIdErrorException(userId, e);
        }

        try {
            return StringEncryptor.SHA.encrypt(String.valueOf(userId)) + "|" + remember_id_string;
        } catch (NoSuchAlgorithmException e) {
            throw new CreateRememberIdErrorException(userId, e);
        }
    }

    protected boolean _eraseRememberId(Delete eraseRememberId, final String rememberId)
    throws RememberManagerException {
        assert eraseRememberId != null;

        if (null == rememberId ||
            0 == rememberId.length()) {
            return false;
        }

        final int remember_id_slash = rememberId.indexOf("|");
        if (-1 == remember_id_slash) {
            return false;
        }

        boolean result = false;
        try {
            if (0 != executeUpdate(eraseRememberId, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("rememberId", rememberId.substring(remember_id_slash + 1));
                }
            })) {
                result = true;
            }
        } catch (DatabaseException e) {
            throw new EraseRememberIdErrorException(rememberId, e);
        }

        return result;
    }

    protected boolean _eraseUserRememberIds(Delete eraseUserRememberIds, final long userId)
    throws RememberManagerException {
        assert eraseUserRememberIds != null;

        if (userId < 0) {
            return false;
        }

        boolean result = false;
        try {
            if (0 != executeUpdate(eraseUserRememberIds, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setLong("userId", userId);
                }
            })) {
                result = true;
            }
        } catch (DatabaseException e) {
            throw new EraseUserRememberIdsErrorException(userId, e);
        }

        return result;
    }

    protected void _eraseAllRememberIds(Delete eraseAllRememberIds)
    throws RememberManagerException {
        assert eraseAllRememberIds != null;

        try {
            executeUpdate(eraseAllRememberIds);
        } catch (DatabaseException e) {
            throw new EraseAllRememberIdsErrorException(e);
        }
    }

    protected long _getRememberedUserId(Select getRememberedUserId, final String rememberId)
    throws RememberManagerException {
        assert getRememberedUserId != null;

        if (null == rememberId ||
            0 == rememberId.length()) {
            return -1;
        }

        final int rememberid_slash = rememberId.indexOf("|");
        if (-1 == rememberid_slash) {
            return -1;
        }

        final String encrypted_userid = rememberId.substring(0, rememberid_slash);
        final String real_rememberid = rememberId.substring(rememberid_slash + 1);

        long result = -1;

        try {
            result = executeGetFirstLong(getRememberedUserId, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("rememberId", real_rememberid);
                }
            });
        } catch (DatabaseException e) {
            throw new GetRememberedUserIdErrorException(rememberId, e);
        }

        try {
            if (!encrypted_userid.equals(StringEncryptor.SHA.encrypt(String.valueOf(result)))) {
                return -1;
            }
        } catch (NoSuchAlgorithmException e) {
            throw new GetRememberedUserIdErrorException(rememberId, e);
        }

        return result;
    }

    protected void _purgeRememberIds(Delete purgeRememberIds)
    throws RememberManagerException {
        try {
            executeUpdate(purgeRememberIds, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement.setLong(1, System.currentTimeMillis() - getRememberDuration());
                }
            });
        } catch (DatabaseException e) {
            throw new PurgeRememberIdsErrorException(e);
        }
    }
}

