/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.engine.exceptions.PathInfoMappingPatternInvalidException;
import rife.tools.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Micro DSL to describe the format of how a pathinfo should
 * be structure and mapped to parameters.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class PathInfoMapping {
    private final StringBuilder mappingRegexp_ = new StringBuilder();
    private final List<String> parameters_ = new ArrayList<>();
    private final List<PathInfoSegment> segments_ = new ArrayList<>();
    private Pattern regexp_ = null;

    /**
     * Indicates that a slash is expected.
     *
     * @return the instance of the DSL
     * @since 1.0
     */
    public PathInfoMapping s() {
        return t("/");
    }

    /**
     * Indicates that a text literal is expected
     * @param literal the expected text literal
     *
     * @return the instance of the DSL
     * @since 1.0
     */
    public PathInfoMapping t(String literal) {
        if (literal.length() > 0) {
            mappingRegexp_.append(StringUtils.encodeRegexp(literal));
            segments_.add(PathInfoSegment.createTextSegment(literal));
        }

        regexp_ = null;

        return this;
    }

    /**
     * Indicates that a match for regex [\w-._~]+ is expected
     * and will be mapped to a named parameter.
     *
     * @param name the parameter name the pathinfo segment will be mapped to
     * @return the instance of the DSL
     * @since 1.0
     */
    public PathInfoMapping p(String name) {
        return p(name, "[\\w-._~]+");
    }

    /**
     * Indicates that a match for a provided regex is expected
     * and will be mapped to a named parameter.
     *
     * @param name the parameter name the pathinfo segment will be mapped to
     * @param regex the regex that will be matched
     * @return the instance of the DSL
     * @since 1.0
     */
    public PathInfoMapping p(String name, String regex) {
        Pattern pattern;
        try {
            pattern = Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            throw new PathInfoMappingPatternInvalidException(name, regex, e);
        }

        parameters_.add(name);

        mappingRegexp_.append("(");
        mappingRegexp_.append(regex);
        mappingRegexp_.append(")");
        segments_.add(PathInfoSegment.createRegexpSegment(pattern));

        regexp_ = null;

        return this;
    }

    List<String> parameters() {
        return parameters_;
    }

    List<PathInfoSegment> segments() {
        return segments_;
    }

    Pattern regexp() {
        if (regexp_ == null) {
            regexp_ = Pattern.compile(mappingRegexp_.toString());
        }
        return regexp_;
    }
}