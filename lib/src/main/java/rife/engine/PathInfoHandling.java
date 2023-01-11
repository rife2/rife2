/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides the different pathinfo handling options for a
 * route definition.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class PathInfoHandling {
    /**
     * No pathinfo will be handled.
     *
     * @since 1.0
     */
    public static final PathInfoHandling NONE = new PathInfoHandling(PathInfoType.NONE);

    /**
     * The whole pathinfo after the route's path will be captured
     *
     * @since 1.0
     */
    public static final PathInfoHandling CAPTURE = new PathInfoHandling(PathInfoType.CAPTURE);

    /**
     * The pathinfo has to match a pattern and will be mapped to parameters.
     *
     * @param builder the builder that creates the mapping specification
     * @return the defined pathinfo handling
     * @since 1.0
     */
    public static PathInfoHandling MAP(PathInfoBuilder... builder) {
        List<PathInfoMapping> mappings = new ArrayList<>();
        for (var b : builder) {
            var mapping = new PathInfoMapping();
            b.build(mapping);
            mappings.add(mapping);
        }
        return new PathInfoHandling(mappings);
    }

    private final PathInfoType type_;
    private final List<PathInfoMapping> mappings_;

    private PathInfoHandling(PathInfoType type) {
        if (type == null) {
            type = PathInfoType.NONE;
        }
        type_ = type;
        mappings_ = null;
    }

    private PathInfoHandling(List<PathInfoMapping> mapping) {
        type_ = PathInfoType.MAP;
        mappings_ = mapping;
    }

    PathInfoType type() {
        return type_;
    }

    List<PathInfoMapping> mappings() {
        return mappings_;
    }
}