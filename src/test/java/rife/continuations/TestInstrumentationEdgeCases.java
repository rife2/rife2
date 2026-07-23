/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

import org.junit.jupiter.api.Test;
import rife.asm.ClassReader;
import rife.asm.ClassWriter;
import rife.continuations.basic.BasicContinuableClassLoader;
import rife.continuations.basic.BasicContinuableRunner;
import rife.continuations.instrument.ContinuationsBytecodeTransformer;
import rife.continuations.instrument.ContinuationsTransformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class TestInstrumentationEdgeCases {
    private static final ParameterConfig PARAMETER_CONFIG = new ParameterConfig();

    @Test
    void testCurrentClassFrameMerge()
    throws Throwable {
        var runner = new ContinuableRunnerTest();
        var id = runner.start(TestPauseCurrentClassFrameMerge.class.getName());
        assertNotNull(id);
        assertNull(runner.resume(id));

        var continuable = runner.getCurrentContinuable();
        assertNotNull(continuable);
        assertEquals("Peer", continuable.getClass().getMethod("getResult").invoke(continuable));
    }

    @Test
    void testOperandTypesAcrossCall()
    throws Throwable {
        var runner = new ContinuableRunnerTest();
        assertNull(runner.start(TestCallOperandTypesSource.class.getName()));

        var continuable = runner.getCurrentContinuable();
        assertNotNull(continuable);
        assertEquals("null:100000:10000000000:100000.5:100000.25:true:constant:java.lang.String:answer",
            continuable.getClass().getMethod("getResult").invoke(continuable));
    }

    @Test
    void testParameterRestoreFollowsRuntimePath()
    throws Throwable {
        assertEquals("resume", runParameterCase(0));
        assertEquals("assigned", runParameterCase(1));
        assertNull(runParameterCase(2));
    }

    private Object runParameterCase(int assignment)
    throws Throwable {
        var runner = new ParameterRunner();
        runner.setArguments("start", assignment);
        var id = runner.start(TestParameterFrameMerge.class.getName());
        assertNotNull(id);

        runner.setArguments("resume", assignment);
        assertNull(runner.resume(id));

        var continuable = runner.getCurrentContinuable();
        assertNotNull(continuable);
        return continuable.getClass().getMethod("getResult").invoke(continuable);
    }

    @Test
    void testFramelessJoinRejected()
    throws Exception {
        byte[] frameless_bytes;
        try (var input = getClass().getResourceAsStream("/rife/continuations/TestPauseCurrentClassFrameMerge.class")) {
            assertNotNull(input);
            var reader = new ClassReader(input);
            var writer = new ClassWriter(0);
            reader.accept(writer, ClassReader.SKIP_FRAMES);
            frameless_bytes = writer.toByteArray();
        }

        var exception = assertThrows(ClassNotFoundException.class, () ->
            ContinuationsBytecodeTransformer.transformIntoResumableBytes(
                new ContinuationConfigInstrumentTests(),
                frameless_bytes,
                TestPauseCurrentClassFrameMerge.class.getName(),
                getClass().getClassLoader()));
        assertInstanceOf(IllegalStateException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("missing a stack-map frame"));
    }

    @Test
    void testAgentReportsInstrumentationFailure()
    throws Exception {
        var records = new ArrayList<LogRecord>();
        var handler = new Handler() {
            public void publish(LogRecord record) {
                records.add(record);
            }

            public void flush() {
            }

            public void close() {
            }
        };
        handler.setLevel(Level.ALL);

        var logger = Logger.getLogger(ContinuationsTransformer.class.getName());
        var previous_level = logger.getLevel();
        var previous_parent_handlers = logger.getUseParentHandlers();
        var handler_levels = new HashMap<Handler, Level>();
        for (var existing_handler : logger.getHandlers()) {
            handler_levels.put(existing_handler, existing_handler.getLevel());
            existing_handler.setLevel(Level.OFF);
        }
        var property = "rife.test.continuations.transformer";
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        try {
            var original = new byte[]{0};
            var transformer = new ContinuationsTransformer(new ContinuationConfigInstrumentTests(), property);
            assertSame(original, transformer.transform(getClass().getClassLoader(), "example/Broken", null, null, original));

            assertTrue(records.stream().anyMatch(record ->
                record.getLevel() == Level.SEVERE &&
                record.getMessage().contains("example.Broken") &&
                record.getThrown() instanceof ClassNotFoundException));
        } finally {
            logger.removeHandler(handler);
            logger.setLevel(previous_level);
            logger.setUseParentHandlers(previous_parent_handlers);
            handler_levels.forEach(Handler::setLevel);
            System.clearProperty(property);
        }
    }

    private static class ParameterConfig extends ContinuationConfigInstrumentTests {
        public String getEntryMethodDescriptor() {
            return "(Ljava/lang/String;I)V";
        }
    }

    private static class ParameterRunner extends BasicContinuableRunner {
        private String value_;
        private int assignment_;

        private ParameterRunner() {
            super(PARAMETER_CONFIG,
                new Class[]{String.class, Integer.TYPE},
                new BasicContinuableClassLoader(PARAMETER_CONFIG));
        }

        private void setArguments(String value, int assignment) {
            value_ = value;
            assignment_ = assignment;
        }

        public void executeContinuable(Object object)
        throws Throwable {
            var method = object.getClass().getMethod(
                getConfigInstrumentation().getEntryMethodName(),
                getEntryMethodArgumentTypes());
            method.setAccessible(true);
            method.invoke(object, value_, assignment_);
        }
    }
}
