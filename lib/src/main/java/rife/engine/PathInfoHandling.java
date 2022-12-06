/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

public class PathInfoHandling {
    public static final PathInfoHandling NONE = new PathInfoHandling(Type.NONE);
    public static final PathInfoHandling CAPTURE = new PathInfoHandling(Type.CAPTURE);

    public static PathInfoHandling MAP(PathInfoBuilder builder) {
        var mapping = new PathInfoMapping();
        builder.build(mapping);
        return new PathInfoHandling(mapping);
    }

    public enum Type {
        NONE, CAPTURE, MAP
    }

    private final Type type_;
    private final PathInfoMapping mapping_;

    private PathInfoHandling(Type type) {
        if (type == null) {
            type = Type.NONE;
        }
        type_ = type;
        mapping_ = null;
    }

    private PathInfoHandling(PathInfoMapping mapping) {
        mapping.compile();

        type_ = Type.MAP;
        mapping_ = mapping;
    }

    public Type getType() {
        return type_;
    }

    public PathInfoMapping getMapping() {
        return mapping_;
    }
}