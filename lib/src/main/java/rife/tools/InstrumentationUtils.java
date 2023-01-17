/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.io.ByteArrayInputStream;
import java.io.File;

import rife.tools.exceptions.FileUtilsErrorException;

public final class InstrumentationUtils {
    public static final String PROPERTY_RIFE_INSTRUMENTATION_DUMP = "rife.instrumentation.dump";

    private InstrumentationUtils() {
        // no-op
    }

    public static void dumpClassBytes(String type, String classname, byte[] bytes) {
        var write_to_disk = (System.getProperty(PROPERTY_RIFE_INSTRUMENTATION_DUMP) != null);
        if (write_to_disk) {
            var user_home = System.getProperty("user.home");
            var file_out_name = user_home + File.separatorChar + "rife_instrumentation_" + type + File.separatorChar + classname.replace('.', File.separatorChar) + ".class";
            var dir_out_name = file_out_name.substring(0, file_out_name.lastIndexOf(File.separatorChar));
            // ensure that all the parent dirs are present
            new File(dir_out_name).mkdirs();
            var file_out = new File(file_out_name);
            try {
                System.out.println("Dumping " + type + " resource: " + file_out.getAbsolutePath());
                FileUtils.copy(new ByteArrayInputStream(bytes), file_out);
            } catch (FileUtilsErrorException e) {
                e.printStackTrace();
            }
        }
    }
}