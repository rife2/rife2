/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.instrument;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import rife.tools.FileUtils;
import rife.validation.Validated;
import rife.workflow.Work;
import rife.workflow.Workflow;
import rifeinstrumenttests.Note;
import rifeworkflowtests.CountdownWork;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class TestInstrumentationDeployer {
    private static byte[] classBytes(Class<?> klass)
    throws Exception {
        try (var stream = klass.getClassLoader().getResourceAsStream(klass.getName().replace('.', '/') + ".class")) {
            return stream.readAllBytes();
        }
    }

    @Test
    @Timeout(60)
    void testInstrumentAndRun()
    throws Throwable {
        var in_dir = Files.createTempDirectory("deployer-in").toFile();
        var out_dir = Files.createTempDirectory("deployer-out").toFile();
        try {
            var class_file = new File(in_dir, "rifeworkflowtests/CountdownWork.class");
            class_file.getParentFile().mkdirs();
            FileUtils.writeBytes(classBytes(CountdownWork.class), class_file);
            FileUtils.writeBytes(classBytes(CountdownWork.Types.class),
                new File(in_dir, "rifeworkflowtests/CountdownWork$Types.class"));

            // the work class is instrumented, its enum is left untouched
            var instrumented = InstrumentationDeployer.instrument(List.of(in_dir), out_dir, false);
            assertEquals(List.of("rifeworkflowtests.CountdownWork"), instrumented);
            var transformed = new File(out_dir, "rifeworkflowtests/CountdownWork.class");
            assertTrue(transformed.exists());
            assertFalse(new File(out_dir, "rifeworkflowtests/CountdownWork$Types.class").exists());

            // the reflection configuration covers the entry method
            var reflect_config = FileUtils.readString(
                new File(out_dir, "META-INF/native-image/rife-instrumented/reflect-config.json"));
            assertTrue(reflect_config.contains("\"rifeworkflowtests.CountdownWork\""));
            assertTrue(reflect_config.contains("\"execute\""));
            assertTrue(reflect_config.contains("\"rife.workflow.Workflow\""));

            // instrumenting already instrumented classes makes no changes
            var second_out = Files.createTempDirectory("deployer-out2").toFile();
            try {
                assertTrue(InstrumentationDeployer.instrument(List.of(out_dir), second_out, false).isEmpty());
            } finally {
                FileUtils.deleteDirectory(second_out);
            }

            // the instrumented class runs a complete pause and resume cycle
            var bytes = FileUtils.readBytes(transformed);
            var loader = new ClassLoader(getClass().getClassLoader()) {
                protected Class<?> findClass(String name)
                throws ClassNotFoundException {
                    if (name.equals("rifeworkflowtests.CountdownWork")) {
                        return defineClass(name, bytes, 0, bytes.length);
                    }
                    throw new ClassNotFoundException(name);
                }

                protected Class<?> loadClass(String name, boolean resolve)
                throws ClassNotFoundException {
                    if (name.equals("rifeworkflowtests.CountdownWork")) {
                        var loaded = findLoadedClass(name);
                        if (loaded == null) {
                            loaded = findClass(name);
                        }
                        return loaded;
                    }
                    return super.loadClass(name, resolve);
                }
            };

            var work = (Work) loader.loadClass("rifeworkflowtests.CountdownWork")
                .getDeclaredConstructor().newInstance();
            var workflow = new Workflow();
            var ticks = new AtomicInteger();
            workflow.addListener(event -> {
                if (String.valueOf(event.getType()).equals("TICK")) {
                    ticks.incrementAndGet();
                }
            });

            workflow.start(work);
            assertTrue(workflow.waitForPausedWork());
            workflow.trigger(CountdownWork.Types.START, 3);
            workflow.waitForNoWork();
            assertEquals(3, ticks.get());
        } finally {
            FileUtils.deleteDirectory(in_dir);
            FileUtils.deleteDirectory(out_dir);
        }
    }

    @Test
    void testInstrumentMetaDataBeanTwice()
    throws Throwable {
        var in_dir = Files.createTempDirectory("deployer-in").toFile();
        var out_dir = Files.createTempDirectory("deployer-out").toFile();
        var second_out = Files.createTempDirectory("deployer-out2").toFile();
        try {
            var class_file = new File(in_dir, "rifeinstrumenttests/Note.class");
            class_file.getParentFile().mkdirs();
            FileUtils.writeBytes(classBytes(Note.class), class_file);

            assertEquals(List.of("rifeinstrumenttests.Note"), InstrumentationDeployer.instrument(List.of(in_dir), out_dir, false));

            // the merged metadata makes the bean constrained, so it also
            // gets lazy loading, neither is added a second time
            assertEquals(List.of(), InstrumentationDeployer.instrument(List.of(out_dir), second_out, false));

            var bytes = FileUtils.readBytes(new File(out_dir, "rifeinstrumenttests/Note.class"));
            var loader = new ClassLoader(getClass().getClassLoader()) {
                protected Class<?> loadClass(String name, boolean resolve)
                throws ClassNotFoundException {
                    if (name.equals("rifeinstrumenttests.Note")) {
                        var loaded = findLoadedClass(name);
                        return loaded != null ? loaded : defineClass(name, bytes, 0, bytes.length);
                    }
                    return super.loadClass(name, resolve);
                }
            };
            var note = loader.loadClass("rifeinstrumenttests.Note").getDeclaredConstructor().newInstance();
            assertInstanceOf(Validated.class, note);
            assertFalse(((Validated) note).validate());
        } finally {
            FileUtils.deleteDirectory(in_dir);
            FileUtils.deleteDirectory(out_dir);
            FileUtils.deleteDirectory(second_out);
        }
    }
}
