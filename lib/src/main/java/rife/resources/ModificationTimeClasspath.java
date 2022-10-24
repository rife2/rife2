/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.resources;

import rife.resources.exceptions.*;
import rife.tools.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ModificationTimeClasspath {
    public static long getModificationTime(URL resource)
    throws ResourceFinderErrorException {
        if (null == resource) {
            return -1;
        }

        long modification_time = -1;

        var resource_protocol = resource.getProtocol();
        var resource_filename = StringUtils.decodeUrl(resource.getFile());

        // handle Jetty's custom tx protocol
        if (resource_protocol.equals("tx")) {
            resource_protocol = "file";
            resource_filename = StringUtils.stripFromFront(resource_filename, "file:");
        }

        switch (resource_protocol) {
            case "jar" -> {
                var prefix = "file:";
                var jar_filename = resource_filename.substring(prefix.length(), resource_filename.indexOf('!'));
                var jar_entryname = resource_filename.substring(resource_filename.indexOf('!') + 2);
                var jar_regularfile = new File(jar_filename);
                if (jar_regularfile.exists() &&
                    jar_regularfile.canRead()) {
                    try (JarFile jar_file = new JarFile(jar_regularfile)) {
                        JarEntry jar_entry = jar_file.getJarEntry(jar_entryname);
                        if (null != jar_entry) {
                            modification_time = jar_entry.getTime();
                        } else {
                            throw new CantFindResourceJarEntryException(jar_filename, jar_entryname, null);
                        }
                    } catch (IOException e) {
                        throw new CantFindResourceJarEntryException(jar_filename, jar_entryname, e);
                    }
                } else {
                    throw new CouldntAccessResourceJarException(jar_filename, jar_entryname);
                }
            }
            case "file" -> {
                File resource_file = new File(resource_filename);
                if (resource_file.exists() &&
                    resource_file.canRead()) {
                    modification_time = resource_file.lastModified();
                } else {
                    throw new CouldntAccessResourceFileException(resource_filename);
                }
            }
            // support orion's classloader resource url
            // support weblogic's classloader resource url
            case "classloader", "zip" -> {}


            default -> throw new UnsupportedResourceProtocolException(resource_filename, resource_protocol);
        }

        return modification_time;
    }
}
