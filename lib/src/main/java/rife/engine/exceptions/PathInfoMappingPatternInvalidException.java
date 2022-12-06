/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import java.io.Serial;

public class PathInfoMappingPatternInvalidException extends EngineException {
	@Serial private static final long serialVersionUID = -7798386202044922044L;

    private final String parameterName_;
    private final String pattern_;

    public PathInfoMappingPatternInvalidException(String parameterName, String pattern, Throwable cause) {
        super("The matching pattern '" + pattern + "' for parameter '" + parameterName + "' is not a valid regular expression.", cause);

        parameterName_ = parameterName;
        pattern_ = pattern;
    }

    public String getParameterName() {
        return parameterName_;
    }

    public String getPattern() {
        return pattern_;
    }
}
