/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import rife.tools.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class TestServerShutdown {
    public static class DestroyRecordingSite extends Site {
        private final File marker_;

        DestroyRecordingSite(File marker) {
            marker_ = marker;
        }

        public void setup() {
            get("/", c -> c.print("up"));
        }

        public void destroy() {
            try {
                FileUtils.writeString("destroyed", marker_);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public static void main(String[] args) {
            var site = new DestroyRecordingSite(new File(args[1]));
            if ("tomcat".equals(args[0])) {
                new TomcatServer().hostname("localhost").port(0).start(site);
            } else {
                new Server().host("localhost").port(0).start(site);
            }
            System.out.println("started");
        }
    }

    // Process.destroy() ends Windows processes without running shutdown hooks
    @DisabledOnOs(OS.WINDOWS)
    @ParameterizedTest
    @ValueSource(strings = {"jetty", "tomcat"})
    @Timeout(value = 120, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void testSigtermDestroysSite(String server)
    throws Exception {
        var marker = new File(Files.createTempDirectory("rife2-shutdown").toFile(), "destroyed");

        var process = new ProcessBuilder(TestJavaCommand.forMain(null, DestroyRecordingSite.class.getName(), server, marker.getAbsolutePath()))
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .start();
        try {
            var output = new BufferedReader(new InputStreamReader(process.getInputStream()));
            assertEquals("started", output.readLine());

            process.destroy();
            assertTrue(process.waitFor(60, TimeUnit.SECONDS));
            assertTrue(marker.exists(), "SIGTERM has to call destroy() on the site");
        } finally {
            process.destroyForcibly();
            FileUtils.deleteDirectory(marker.getParentFile());
        }
    }
}
