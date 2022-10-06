/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rife.template.exceptions.TemplateException;
import rife.tools.ExceptionUtils;

import static org.junit.jupiter.api.Assertions.*;

public class TestParsed {
    private Parser paser_ = null;

    @BeforeEach
    public void setUp() {
        paser_ = TemplateFactory.HTML.getParser();
    }

    @Test
    public void testInstantiation() {
        Parsed tp = new Parsed(paser_);

        assertNotNull(tp);
        assertNull(tp.getResource());
        assertNull(tp.getClassName());
        assertNull(tp.getFullClassName());
        assertNull(tp.getBlock("test"));
        assertNull(tp.getDefaultValue("test"));
        assertEquals(tp.getBlocks().size(), 0);
        assertEquals(tp.getDefaultValues().size(), 0);
        assertEquals(tp.getDependencies().size(), 0);
        assertNull(tp.getContent());
    }

    @Test
    public void testResource() {
        Parsed tp = new Parsed(paser_);

        assertNull(tp.getResource());
        URL url = null;
        try {
            url = new URL("file:/test/");
        } catch (MalformedURLException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
        tp.setResource(url);
        assertEquals(tp.getResource(), url);
    }

    @Test
    public void testClassname() {
        Parsed tp = new Parsed(paser_);

        assertNull(tp.getClassName());
        assertNull(tp.getFullClassName());
        String classname = "some_template";
        tp.setClassName(classname);
        assertNotNull(tp.getFullClassName());
        assertNotEquals(classname, tp.getFullClassName());
    }

    @Test
    public void testBlocks() {
        Parsed tp = new Parsed(paser_);

        assertEquals(tp.getBlocks().size(), 0);

        ParsedBlockData blockdata1 = new ParsedBlockData();
        ParsedBlockData blockdata2 = new ParsedBlockData();
        tp.setBlock("blockparts1", blockdata1);
        tp.setBlock("blockparts2", blockdata2);

        assertEquals(tp.getBlocks().size(), 2);
        assertSame(tp.getBlock("blockparts1"), blockdata1);
        assertSame(tp.getBlock("blockparts2"), blockdata2);
        assertNull(tp.getBlock("blockparts3"));
        assertNull(tp.getContent());

        ParsedBlockData contentdata = new ParsedBlockData();
        tp.setBlock("", contentdata);
        assertEquals(tp.getBlocks().size(), 3);
        assertSame(tp.getContent(), contentdata);
    }

    @Test
    public void testDefaultValues() {
        Parsed tp = new Parsed(paser_);

        assertEquals(tp.getDefaultValues().size(), 0);

        String defaultvalue1 = new String();
        String defaultvalue2 = new String();
        tp.setDefaultValue("defaultvalue1", defaultvalue1);
        tp.setDefaultValue("defaultvalue2", defaultvalue2);

        assertEquals(tp.getDefaultValues().size(), 2);
        assertSame(tp.getDefaultValue("defaultvalue1"), defaultvalue1);
        assertSame(tp.getDefaultValue("defaultvalue2"), defaultvalue2);
        assertNull(tp.getDefaultValue("defaultvalue3"));
    }

    // TODO
//    @Test
//    public void testIncludes() {
//        Parsed tp = new Parsed(paser_);
//
//        String include1_name = "noblocks_in";
//        Parsed include1 = paser_.prepare(include1_name, paser_.resolve(include1_name));
//        String include2_name = "defaultvalues_in";
//        Parsed include2 = paser_.prepare(include2_name, paser_.resolve(include2_name));
//        String include3_name = "blocks_successive_in";
//        Parsed include3 = paser_.prepare(include3_name, paser_.resolve(include3_name));
//
//        try {
//            tp.addDependency(include1);
//            assertEquals(tp.getDependencies().size(), 1);
//            tp.addDependency(include2);
//            assertEquals(tp.getDependencies().size(), 2);
//            tp.addDependency(include3);
//            assertEquals(tp.getDependencies().size(), 3);
//        } catch (TemplateException e) {
//            fail(ExceptionUtils.getExceptionStackTrace(e));
//        }
//
//        assertEquals(include1_name.indexOf(include1.getClassName()), 0);
//        assertEquals(include2_name.indexOf(include2.getClassName()), 0);
//        assertEquals(include3_name.indexOf(include3.getClassName()), 0);
//    }
}
