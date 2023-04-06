/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.resources;

import rife.resources.exceptions.CantOpenResourceStreamException;
import rife.resources.exceptions.CantRetrieveResourceContentException;
import rife.resources.exceptions.ResourceFinderErrorException;
import rife.tools.InputStreamUser;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows a group of resource finders to acts as if they are one single
 * resource finders. They will be consecutively used in their order of addition
 * to the group.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see rife.resources.ResourceFinder
 * @since 1.0
 */
public class ResourceFinderGroup extends AbstractResourceFinder {
    private final List<ResourceFinder> resourceFinders_ = new ArrayList<>();

    public ResourceFinderGroup add(ResourceFinder resourceFinder) {
        resourceFinders_.add(resourceFinder);

        return this;
    }

    public URL getResource(String name) {
        URL result;
        for (var resource_finder : resourceFinders_) {
            result = resource_finder.getResource(name);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    public <ResultType> ResultType useStream(URL resource, InputStreamUser<ResultType, ?> user)
    throws ResourceFinderErrorException {
        ResultType result = null;
        for (ResourceFinder resource_finder : resourceFinders_) {
            try {
                result = resource_finder.useStream(resource, user);
            } catch (CantOpenResourceStreamException e) {
                continue;
            }

            return result;
        }

        throw new CantOpenResourceStreamException(resource, null);
    }

    public String getContent(URL resource, String encoding)
    throws ResourceFinderErrorException {
        String result = null;
        for (ResourceFinder resource_finder : resourceFinders_) {
            try {
                result = resource_finder.getContent(resource, encoding);
                if (result != null) {
                    return result;
                }
            } catch (ResourceFinderErrorException e) {
                // ignore
            }
        }

        throw new CantRetrieveResourceContentException(resource, encoding, null);
    }

    public long getModificationTime(URL resource)
    throws ResourceFinderErrorException {
        long result = -1;
        for (ResourceFinder resource_finder : resourceFinders_) {
            try {
                result = resource_finder.getModificationTime(resource);
                if (result != -1) {
                    return result;
                }
            } catch (ResourceFinderErrorException e) {
                // ignore
            }
        }

        return -1;
    }
}
