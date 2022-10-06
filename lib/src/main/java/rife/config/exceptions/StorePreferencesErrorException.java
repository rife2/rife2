/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.config.exceptions;

import java.io.Serial;
import java.util.prefs.Preferences;

public class StorePreferencesErrorException extends ConfigErrorException {
    @Serial private static final long serialVersionUID = -1781853604693398578L;

    private final Preferences preferences_;

    public StorePreferencesErrorException(Preferences preferences, Throwable cause) {
        super("An error occurred while storing the data to the preferences user node '" + preferences.absolutePath() + "'.", cause);

        preferences_ = preferences;
    }

    public Preferences getPreferences() {
        return preferences_;
    }
}
