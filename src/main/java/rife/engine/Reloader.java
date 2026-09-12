/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import com.sun.management.HotSpotDiagnosticMXBean;
import com.sun.management.VMOption;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Runs a web application in a separate JVM and restarts it whenever its
 * compiled classes change, so that code changes are picked up during
 * development without restarting the application by hand.
 * <p>
 * Launch it with the JVM options, the classpath and the module path of your
 * application, followed by the application's main class and its arguments:
 * <pre>java -cp [classpath] rife.engine.Reloader com.example.AppSite [arguments]</pre>
 * <p>
 * The directories of the classpath and the module path are checked for class
 * files that are modified, added or removed. Once they stop changing, the
 * application is stopped and started again. RIFE2's servers tear their site
 * down when the JVM stops, which is what calls {@code destroy()} on it. On
 * Windows the application is ended forcibly, so its {@code destroy()} methods
 * don't run there.
 * <p>
 * The application receives the same JVM options as the reloader, apart from
 * the ones in the {@code JDK_JAVA_OPTIONS}, {@code JAVA_TOOL_OPTIONS} and
 * {@code _JAVA_OPTIONS} environment variables, which it picks up by itself,
 * apart from the options that the JVM's launcher added on its own, and apart
 * from the debugging options, which can only be used by one JVM at a time.
 * <p>
 * Changes to other files don't restart the application, since templates and
 * resource bundles are reloaded while it runs, as long as their automatic
 * reloading is active and they aren't precompiled. The browser is told to
 * fetch the page again instead, also for the files of a static resource base.
 * <p>
 * The pages that the application renders itself get a small script that
 * reloads the browser as soon as the application has restarted. The script is
 * added before the closing {@code </body>} tag, or at the very end of a page
 * without one, unless a template already placed it with the
 * {@code webapp:reloadScript} value. Files that the
 * servlet container serves directly, for instance from a static resource
 * base, don't go through the engine and don't get the script.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.11
 */
public class Reloader {
    /**
     * The system property that identifies the running instance of an
     * application under the reloader, it's absent everywhere else.
     *
     * @since 1.11
     */
    public static final String INSTANCE_PROPERTY = "rife.reload.instance";

    static final String SUPERVISOR_PROPERTY = "rife.reload.supervisor";
    static final List<String> INHERITED_OPTIONS = List.of("JDK_JAVA_OPTIONS", "JAVA_TOOL_OPTIONS", "_JAVA_OPTIONS");
    static final List<String> DEBUG_OPTIONS = List.of("-agentlib:jdwp", "-Xrunjdwp");

    private static final long STOP_TIMEOUT_MS = 10000;

    private final String mainClass_;
    private final List<String> arguments_;
    private final List<Path> directories_;
    private final List<String> jvmArguments_;
    private final ReloadScanner scanner_;
    private Process process_ = null;
    private boolean stopped_ = false;

    Reloader(String mainClass, List<String> arguments, List<Path> directories) {
        mainClass_ = mainClass;
        arguments_ = arguments;
        directories_ = directories;
        jvmArguments_ = launcherOptionsRemoved(debuggingRemoved(ManagementFactory.getRuntimeMXBean().getInputArguments()), addedByLauncher());
        scanner_ = new ReloadScanner(directories, ReloadScanner.CLASS_FILES);
    }

    /**
     * Starts the application and restarts it each time its classes change.
     *
     * @param args the main class of the application, followed by its
     *             arguments
     * @since 1.11
     */
    public static void main(String[] args)
    throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: java -cp [classpath] " + Reloader.class.getName() + " [main class] [arguments]");
            System.exit(1);
        }

        var directories = ReloadScanner.directories(System.getProperty("java.class.path"), System.getProperty("jdk.module.path"));
        new Reloader(args[0], Arrays.asList(args).subList(1, args.length), directories).run();
    }

    void run()
    throws InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        var watched = directories_.stream().filter(Files::isDirectory).toList();
        if (watched.isEmpty()) {
            System.out.println("The classpath has no directories with classes to watch, so the application " +
                "won't be restarted. Run it from compiled classes rather than from an archive to reload it.");
        } else {
            System.out.println("Watching for class changes in " +
                watched.stream().map(Path::toString).collect(Collectors.joining(", ")));
        }

        launch();

        var exit_reported = false;
        while (true) {
            if (scanner_.settled()) {
                exit_reported = false;
                System.out.println("Restarting the application after class changes.");
                terminate();
                launch();
            } else if (!exit_reported) {
                var exit_code = exitCode();
                if (exit_code != null) {
                    System.out.println("The application exited with code " + exit_code + ", it will be restarted after class changes.");
                    exit_reported = true;
                }
            }

            Thread.sleep(ReloadScanner.POLL_INTERVAL_MS);
        }
    }

    // a failure to launch shouldn't end the session, the next change tries again
    private void launch() {
        try {
            start();
        } catch (IOException e) {
            System.out.println("Couldn't start the application: " + e.getMessage());
        }
    }

    private synchronized void start()
    throws IOException {
        if (stopped_) {
            return;
        }

        process_ = null;
        var command = command(mainClass_, arguments_, UUID.randomUUID().toString(), jvmArguments_, System.getProperty("java.class.path"));
        var builder = new ProcessBuilder(command).inheritIO();
        clearInheritedOptions(builder.environment());
        process_ = builder.start();
    }

    private synchronized void stop() {
        stopped_ = true;
        terminate();
    }

    private synchronized Integer exitCode() {
        if (stopped_ ||
            null == process_ ||
            process_.isAlive()) {
            return null;
        }
        return process_.exitValue();
    }

    private synchronized void terminate() {
        if (null == process_) {
            return;
        }

        process_.destroy();
        try {
            if (!process_.waitFor(STOP_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                System.out.println("The application didn't stop within " + STOP_TIMEOUT_MS / 1000 +
                    " seconds and is being killed, its site was not destroyed.");
                process_.destroyForcibly().waitFor();
            }
        } catch (InterruptedException e) {
            process_.destroyForcibly();
            Thread.currentThread().interrupt();
        }
    }

    static List<String> command(String mainClass, List<String> arguments, String instance, List<String> jvmArguments, String classpath) {
        var command = new ArrayList<String>();
        command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        command.addAll(jvmArguments);
        command.add("-D" + INSTANCE_PROPERTY + "=" + instance);
        command.add("-D" + SUPERVISOR_PROPERTY + "=" + ProcessHandle.current().pid());
        command.add("-cp");
        command.add(classpath);
        command.add(mainClass);
        command.addAll(arguments);
        return command;
    }

    // only one JVM at a time can listen on the debugger's address, handing the
    // options to the application would keep it from starting at all
    static List<String> debuggingRemoved(List<String> jvmArguments) {
        var result = new ArrayList<String>();
        var removed = false;
        for (var argument : jvmArguments) {
            if (DEBUG_OPTIONS.stream().anyMatch(argument::startsWith)) {
                removed = true;
            } else {
                result.add(argument);
            }
        }
        if (removed) {
            System.out.println("The debugging options are left out of the application, only the reloader itself uses them.");
        }
        return result;
    }

    // some launchers add -XX options of their own, which the JVM warns about
    // once they're passed to it explicitly
    static List<String> launcherOptionsRemoved(List<String> jvmArguments, Predicate<String> addedByLauncher) {
        var result = new ArrayList<String>();
        for (var argument : jvmArguments) {
            if (!argument.startsWith("-XX:") ||
                !addedByLauncher.test(argument)) {
                result.add(argument);
            }
        }
        return result;
    }

    private static Predicate<String> addedByLauncher() {
        try {
            var diagnostics = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
            if (null == diagnostics) {
                return argument -> false;
            }
            return argument -> {
                var name = argument.substring(4).replaceFirst("^[+-]", "").replaceFirst("=.*", "");
                try {
                    return diagnostics.getVMOption(name).getOrigin() == VMOption.Origin.OTHER;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            };
        } catch (Throwable e) {
            // not a HotSpot JVM
            return argument -> false;
        }
    }

    // the JVM reports these options as its own arguments, so they're passed on
    // explicitly; inheriting them as well would for instance load an agent twice
    static void clearInheritedOptions(Map<String, String> environment) {
        INHERITED_OPTIONS.forEach(environment::remove);
    }
}
