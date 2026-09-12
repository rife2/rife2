/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class TestReloader {
    static final int PORT = 8189;
    static final Pattern INSTANCE = Pattern.compile("e\\.data!==\"([^\"]+)\"");

    public static class ReloadedSite extends Site {
        public void setup() {
            get("/", c -> c.print("<html><body>reloaded</body></html>"));
        }

        public static void main(String[] args) {
            if (args.length > 0 && "exit".equals(args[0])) {
                System.exit(3);
            }
            new Server().port(PORT).host("localhost").start(new ReloadedSite());
        }
    }

    @Test
    void testCommand() {
        var java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        assertEquals(List.of(java, "-Xmx1g",
                "-D" + Reloader.INSTANCE_PROPERTY + "=instance",
                "-D" + Reloader.SUPERVISOR_PROPERTY + "=" + ProcessHandle.current().pid(),
                "-cp", "classes", "com.example.Main", "one", "two"),
            Reloader.command("com.example.Main", List.of("one", "two"), "instance", List.of("-Xmx1g"), "classes"));
    }

    // the options are passed on the command line, inheriting them as well
    // would apply them twice
    @Test
    void testInheritedOptionsAreClearedForTheApplication() {
        var environment = new HashMap<String, String>();
        environment.put("JDK_JAVA_OPTIONS", "-javaagent:agent.jar");
        environment.put("JAVA_TOOL_OPTIONS", "-Dtool=on");
        environment.put("_JAVA_OPTIONS", "-Xmx2g");
        environment.put("PATH", "/usr/bin");

        Reloader.clearInheritedOptions(environment);
        assertEquals(Map.of("PATH", "/usr/bin"), environment);
    }

    // only one JVM at a time can listen on the debugger's address
    @Test
    void testDebuggingOptionsArentPassedOn() {
        var arguments = List.of("-Xmx1g",
            "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005",
            "-Xrunjdwp:transport=dt_socket,address=5006",
            "-Dname=value");
        assertEquals(List.of("-Xmx1g", "-Dname=value"), Reloader.debuggingRemoved(arguments));
    }

    // a launcher's own options come back as the JVM's arguments, the JVM
    // then warns about them when they're passed explicitly
    @Test
    void testLauncherOptionsArentPassedOn() {
        var arguments = List.of("-Xmx1g", "-XX:+EnableJVMCI", "-XX:+UseZGC", "-Dname=value");
        assertEquals(List.of("-Xmx1g", "-XX:+UseZGC", "-Dname=value"),
            Reloader.launcherOptionsRemoved(arguments, "-XX:+EnableJVMCI"::equals));
    }

    static Process startReloader(Path classes, Path output, String... arguments)
    throws Exception {
        var command = new java.util.ArrayList<>(TestJavaCommand.forMain(classes.toString(), Reloader.class.getName(), ReloadedSite.class.getName()));
        command.addAll(List.of(arguments));
        return new ProcessBuilder(command)
            .redirectErrorStream(true)
            .redirectOutput(output.toFile())
            .start();
    }

    static void awaitOutput(Path output, String expected)
    throws Exception {
        var deadline = System.currentTimeMillis() + 60000;
        while (System.currentTimeMillis() < deadline) {
            if (Files.exists(output) && Files.readString(output).contains(expected)) {
                return;
            }
            Thread.sleep(100);
        }
        throw new AssertionError("the reloader never reported '" + expected + "', it said:\n" +
            (Files.exists(output) ? Files.readString(output) : ""));
    }

    static String awaitInstanceOtherThan(String previous)
    throws Exception {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + PORT + "/"))
            .timeout(Duration.ofSeconds(5))
            .build();
        var deadline = System.currentTimeMillis() + 60000;
        while (System.currentTimeMillis() < deadline) {
            try {
                var matcher = INSTANCE.matcher(client.send(request, HttpResponse.BodyHandlers.ofString()).body());
                if (matcher.find() && !matcher.group(1).equals(previous)) {
                    return matcher.group(1);
                }
            } catch (Exception e) {
                // the application is still starting
            }
            Thread.sleep(100);
        }
        throw new AssertionError("no application instance other than " + previous + " responded");
    }

    static Path copySiteClass(Path classes)
    throws Exception {
        var resource = ReloadedSite.class.getName().replace('.', '/') + ".class";
        var class_file = classes.resolve(resource);
        Files.createDirectories(class_file.getParent());
        try (var in = ReloadedSite.class.getClassLoader().getResourceAsStream(resource)) {
            Files.copy(in, class_file);
        }
        return class_file;
    }

    // the processes hold the log file open, Windows won't let the temporary
    // directory be removed until they're really gone
    static void stopReloader(Process reloader, ProcessHandle application)
    throws Exception {
        var descendants = reloader.toHandle().descendants().toList();
        descendants.forEach(ProcessHandle::destroyForcibly);
        reloader.destroyForcibly();
        if (application != null) {
            application.destroyForcibly();
        }

        reloader.waitFor(30, TimeUnit.SECONDS);
        for (var descendant : descendants) {
            descendant.onExit().get(30, TimeUnit.SECONDS);
        }
        if (application != null) {
            application.onExit().get(30, TimeUnit.SECONDS);
        }
    }

    @Test
    @Timeout(value = 180, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void testRestartsOnClassChanges(@TempDir Path classes, @TempDir Path logs)
    throws Exception {
        var class_file = copySiteClass(classes);
        var output = logs.resolve("reloader.log");

        var reloader = startReloader(classes, output);
        ProcessHandle application = null;
        try {
            var first = awaitInstanceOtherThan(null);

            // the size stays the same, so this only moves the modification time
            Files.setLastModifiedTime(class_file, FileTime.fromMillis(System.currentTimeMillis() + 5000));
            awaitInstanceOtherThan(first);
            awaitOutput(output, "Restarting the application after class changes.");

            application = reloader.toHandle().children().findFirst().orElseThrow();
            reloader.destroyForcibly();
            application.onExit().get(30, TimeUnit.SECONDS);
        } finally {
            stopReloader(reloader, application);
        }
    }

    @Test
    @Timeout(value = 180, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void testReportsAnExitAndKeepsWatching(@TempDir Path classes, @TempDir Path logs)
    throws Exception {
        var class_file = copySiteClass(classes);
        var output = logs.resolve("reloader.log");

        var reloader = startReloader(classes, output, "exit");
        try {
            awaitOutput(output, "The application exited with code 3");

            Files.setLastModifiedTime(class_file, FileTime.fromMillis(System.currentTimeMillis() + 5000));
            awaitOutput(output, "Restarting the application after class changes.");
        } finally {
            stopReloader(reloader, null);
        }
    }
}
