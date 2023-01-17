/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.resources;

import rife.resources.exceptions.ResourceFinderErrorException;
import rife.tools.*;

import java.io.ByteArrayInputStream;
import java.io.StringBufferInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * This class offers {@code ResourceFinder} and {@code ResourceWriter}
 * capabilities for resources that are stored in a memory.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see rife.resources.ResourceFinder
 * @since 1.0
 */
public class MemoryResources extends AbstractResourceFinder implements ResourceWriter {
    record MemoryResource(long modification, String content) {
    }

    private static final String PROTOCOL = "file";
    private final Map<String, MemoryResource> resources_ = new HashMap<>();

    /**
     * Creates a new instance.
     * @since 1.0
     */
    public MemoryResources() {
    }

    public void addResource(String name, String content) {
        resources_.put(name, new MemoryResource(System.currentTimeMillis(), content));
    }

    public boolean updateResource(String name, String content) {
        if (!resources_.containsKey(name)) {
            return false;
        }
        resources_.put(name, new MemoryResource(System.currentTimeMillis(), content));
        return true;
    }

    public boolean removeResource(String name) {
        return resources_.remove(name) != null;
    }

    public URL getResource(String name) {
        if (!resources_.containsKey(name)) {
            return null;
        }
        try {
            return new URL(PROTOCOL, "", name);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public <ResultType> ResultType useStream(URL resource, InputStreamUser<ResultType, ?> user)
    throws ResourceFinderErrorException, InnerClassException {
        var content = getContent(resource);
        if (content != null) {
            var stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            return user.useInputStream(stream);
        }
        return null;
    }

    public String getContent(URL resource, String encoding) {
        if (resource == null ||
            !resource.getProtocol().equals(PROTOCOL)) {
            return null;
        }
        var item = resources_.get(StringUtils.decodeUrl(resource.getFile()));
        if (item != null) {
            return item.content();
        }
        return null;
    }

    public long getModificationTime(URL resource) {
        if (resource == null ||
            !resource.getProtocol().equals(PROTOCOL)) {
            return -1;
        }
        var item = resources_.get(StringUtils.decodeUrl(resource.getFile()));
        if (item != null) {
            return item.modification();
        }
        return -1;
    }
}
