/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.remembermanagers;

import rife.authentication.RememberManager;
import rife.authentication.exceptions.RememberManagerException;
import rife.authentication.remembermanagers.exceptions.*;
import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.DbPreparedStatement;
import rife.database.DbPreparedStatementHandler;
import rife.database.DbQueryManager;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.*;
import rife.tools.ExceptionUtils;
import rife.tools.StringEncryptor;
import rife.tools.UniqueIDGenerator;

import java.security.NoSuchAlgorithmException;

public abstract class DatabaseRemember extends DbQueryManager implements RememberManager {
    private long rememberDuration_ = RifeConfig.authentication().getRememberDuration();
    private int rememberPurgeFrequency_ = RifeConfig.authentication().getRememberPurgeFrequency();
    private int rememberPurgeScale_ = RifeConfig.authentication().getRememberPurgeScale();

    protected DatabaseRemember(Datasource datasource) {
        super(datasource);
    }

    @Override
    public long getRememberDuration() {
        return rememberDuration_;
    }

    @Override
    public void setRememberDuration(long milliseconds) {
        rememberDuration_ = milliseconds;
    }

    @Override
    public int getRememberPurgeFrequency() {
        return rememberPurgeFrequency_;
    }

    @Override
    public void setRememberPurgeFrequency(int frequency) {
        rememberPurgeFrequency_ = frequency;
    }

    @Override
    public int getRememberPurgeScale() {
        return rememberPurgeScale_;
    }

    @Override
    public void setRememberPurgeScale(int scale) {
        rememberPurgeScale_ = scale;
    }

    public abstract boolean install()
    throws RememberManagerException;

    public abstract boolean remove()
    throws RememberManagerException;

    protected boolean _install(CreateTable createRemember, String createRememberMomentIndex) {
        assert createRemember != null;
        assert createRememberMomentIndex != null;
        try {
            executeUpdate(createRemember);
            executeUpdate(createRememberMomentIndex);
        } catch (DatabaseException e) {
            final String trace = ExceptionUtils.getExceptionStackTrace(e);
            if (!trace.contains("already exists")) {
                throw new InstallRememberUserErrorException(e);
            }
        }

        return true;
    }

    protected boolean _remove(DropTable removeRemember, String removeRememberMomentIndex) {
        executeUpdate(removeRememberMomentIndex);
        executeUpdate(removeRemember);

        return true;
    }

    protected String _createRememberId(Insert createRememberId, final long userId)
    throws RememberManagerException {
        assert createRememberId != null;

        if (userId < 0) {
            throw new CreateRememberIdErrorException(userId);
        }

        final String remember_id_string = UniqueIDGenerator.generate().toString();

        try {
            if (0 == executeUpdate(createRememberId, s ->
                s.setString("rememberId", remember_id_string)
                    .setLong("userId", userId)
                    .setLong("moment", System.currentTimeMillis()))) {
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
            rememberId.isEmpty()) {
            return false;
        }

        final int remember_id_slash = rememberId.indexOf('|');
        if (-1 == remember_id_slash) {
            return false;
        }

        boolean result = false;
        try {
            if (0 != executeUpdate(eraseRememberId, new DbPreparedStatementHandler<>() {
                @Override
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
                @Override
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
            rememberId.isEmpty()) {
            return -1;
        }

        final int rememberid_slash = rememberId.indexOf('|');
        if (-1 == rememberid_slash) {
            return -1;
        }

        final String encrypted_userid = rememberId.substring(0, rememberid_slash);
        final String real_rememberid = rememberId.substring(rememberid_slash + 1);

        long result = -1;

        try {
            result = executeGetFirstLong(getRememberedUserId, new DbPreparedStatementHandler<>() {
                @Override
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
                @Override
                public void setParameters(DbPreparedStatement statement) {
                    statement.setLong(1, System.currentTimeMillis() - getRememberDuration());
                }
            });
        } catch (DatabaseException e) {
            throw new PurgeRememberIdsErrorException(e);
        }
    }
}

