/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rife.template.exceptions.*;
import rife.tools.ExceptionUtils;

import java.io.File;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class TestParser {
    private Parser parser_ = null;

    @BeforeEach
    public void setup() {
        parser_ = TemplateFactory.HTML.getParser();
    }

    @Test
    void testClone() {
        var parser_clone = parser_.clone();
        assertNotNull(parser_clone);
        assertNotSame(parser_, parser_clone);
        assertEquals(parser_, parser_clone);
    }

    @Test
    void testTemplatePackage() {
        try {
            var template_parsed = parser_.parse("test_package.noblocks_in", null);
            assertEquals(template_parsed.getBlocks().size(), 1);
            assertNotNull(template_parsed.getContent());
            assertEquals(template_parsed.getContent().countParts(), 1);
            assertEquals(template_parsed.getContent().getPart(0).getData(), parser_.getTemplateContent("test_package" + File.separator + "noblocks_out_content"));
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testParseDefaultValues() {
        try {
            var template_parsed = parser_.parse("defaultvalues_in", null);
            assertEquals(template_parsed.getBlocks().size(), 2);
            assertNotNull(template_parsed.getContent());
            assertNotNull(template_parsed.getBlock("BLOCK1"));
            var content = template_parsed.getContent();
            assertEquals(content.countParts(), 6);
            assertEquals(template_parsed.getBlock("BLOCK1").countParts(), 1);
            assertEquals(template_parsed.getDefaultValue("VALUE1"), parser_.getTemplateContent("defaultvalues_out_default1"));
            assertEquals(template_parsed.getDefaultValue("VALUE2"), parser_.getTemplateContent("defaultvalues_out_default2"));
            assertEquals(template_parsed.getDefaultValue("VALUE3"), parser_.getTemplateContent("defaultvalues_out_default3"));
            assertEquals(template_parsed.getContent().getPart(0).getData(), parser_.getTemplateContent("defaultvalues_out_content_0"));
            assertEquals(template_parsed.getContent().getPart(1).getData(), parser_.getTemplateContent("defaultvalues_out_content_1"));
            assertEquals(template_parsed.getContent().getPart(2).getData(), parser_.getTemplateContent("defaultvalues_out_content_2"));
            assertEquals(template_parsed.getContent().getPart(3).getData(), parser_.getTemplateContent("defaultvalues_out_content_3"));
            assertEquals(template_parsed.getContent().getPart(4).getData(), parser_.getTemplateContent("defaultvalues_out_content_4"));
            assertEquals(template_parsed.getContent().getPart(5).getData(), parser_.getTemplateContent("defaultvalues_out_content_5"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(0).getData(), parser_.getTemplateContent("defaultvalues_out_block1"));
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testParseComments() {
        try {
            var template_parsed = parser_.parse("comments_in", null);
            assertEquals(template_parsed.getBlocks().size(), 2);
            assertNotNull(template_parsed.getContent());
            assertNotNull(template_parsed.getBlock("BLOCK1"));
            assertEquals(template_parsed.getContent().countParts(), 3);
            assertEquals(template_parsed.getBlock("BLOCK1").countParts(), 1);
            assertEquals(template_parsed.getDefaultValue("VALUE1"), parser_.getTemplateContent("comments_out_default1"));
            assertNull(template_parsed.getDefaultValue("VALUE2"));
            assertEquals(template_parsed.getContent().getPart(0).getData(), parser_.getTemplateContent("comments_out_content_0"));
            assertEquals(template_parsed.getContent().getPart(1).getData(), parser_.getTemplateContent("comments_out_content_1"));
            assertEquals(template_parsed.getContent().getPart(2).getData(), parser_.getTemplateContent("comments_out_content_2"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(0).getData(), parser_.getTemplateContent("comments_out_block1"));
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testParseCommentsSuccessiveEscaped() {
        try {
            var template_parsed = parser_.parse("comments_successive_escaped_in", null);
            assertEquals(template_parsed.getBlocks().size(), 1);
            assertNotNull(template_parsed.getContent());
            assertEquals(template_parsed.getContent().countParts(), 1);
            assertEquals(template_parsed.getContent().getPart(0).getData(), parser_.getTemplateContent("comments_successive_escaped_out"));
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testParseNoBlocks() {
        try {
            var template_parsed = parser_.parse("noblocks_in", null);
            assertEquals(template_parsed.getBlocks().size(), 1);
            assertNotNull(template_parsed.getContent());
            assertEquals(template_parsed.getContent().countParts(), 1);
            assertEquals(template_parsed.getContent().getPart(0).getData(), parser_.getTemplateContent("noblocks_out_content"));
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testParseBlocksSuccessive() {
        try {
            var template_parsed = parser_.parse("blocks_successive_in", null);
            assertEquals(template_parsed.getBlocks().size(), 4);
            assertNotNull(template_parsed.getContent());
            assertNotNull(template_parsed.getBlock("BLOCK1"));
            assertNotNull(template_parsed.getBlock("BLOCK2"));
            assertNotNull(template_parsed.getBlock("BLOCK3"));
            assertEquals(template_parsed.getContent().countParts(), 1);
            assertEquals(template_parsed.getBlock("BLOCK1").countParts(), 1);
            assertEquals(template_parsed.getBlock("BLOCK2").countParts(), 1);
            assertEquals(template_parsed.getBlock("BLOCK3").countParts(), 1);
            assertEquals(template_parsed.getContent().getPart(0).getData(), parser_.getTemplateContent("blocks_successive_out_content"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(0).getData(), parser_.getTemplateContent("blocks_successive_out_block1"));
            assertEquals(template_parsed.getBlock("BLOCK2").getPart(0).getData(), parser_.getTemplateContent("blocks_successive_out_block2"));
            assertEquals(template_parsed.getBlock("BLOCK3").getPart(0).getData(), parser_.getTemplateContent("blocks_successive_out_block3"));
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testParseBlocksSuccessiveEscaped() {
        try {
            var template_parsed = parser_.parse("blocks_successive_escaped_in", null);
            assertEquals(template_parsed.getBlocks().size(), 2);
            assertNotNull(template_parsed.getContent());
            assertNotNull(template_parsed.getBlock("BLOCK2"));
            assertEquals(template_parsed.getContent().countParts(), 1);
            assertEquals(template_parsed.getBlock("BLOCK2").countParts(), 1);
            assertEquals(template_parsed.getContent().getPart(0).getData(), parser_.getTemplateContent("blocks_successive_escaped_out_content"));
            assertEquals(template_parsed.getBlock("BLOCK2").getPart(0).getData(), parser_.getTemplateContent("blocks_successive_escaped_out_block2"));
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testParseBlocksSpaced() {
        try {
            var template_parsed = parser_.parse("blocks_spaced_in", null);
            assertEquals(template_parsed.getBlocks().size(), 4);
            assertNotNull(template_parsed.getContent());
            assertNotNull(template_parsed.getBlock("BLOCK1"));
            assertNotNull(template_parsed.getBlock("BLOCK2"));
            assertNotNull(template_parsed.getBlock("BLOCK3"));
            assertEquals(template_parsed.getContent().countParts(), 1);
            assertEquals(template_parsed.getBlock("BLOCK1").countParts(), 1);
            assertEquals(template_parsed.getBlock("BLOCK2").countParts(), 1);
            assertEquals(template_parsed.getBlock("BLOCK3").countParts(), 1);
            assertEquals(template_parsed.getContent().getPart(0).getData(), parser_.getTemplateContent("blocks_spaced_out_content"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(0).getData(), parser_.getTemplateContent("blocks_spaced_out_block1"));
            assertEquals(template_parsed.getBlock("BLOCK2").getPart(0).getData(), parser_.getTemplateContent("blocks_spaced_out_block2"));
            assertEquals(template_parsed.getBlock("BLOCK3").getPart(0).getData(), parser_.getTemplateContent("blocks_spaced_out_block3"));
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testParseBlocksSpacedEscaped() {
        try {
            var template_parsed = parser_.parse("blocks_spaced_escaped_in", null);
            assertEquals(template_parsed.getBlocks().size(), 2);
            assertNotNull(template_parsed.getContent());
            assertNotNull(template_parsed.getBlock("BLOCK1"));
            assertEquals(template_parsed.getContent().countParts(), 1);
            assertEquals(template_parsed.getBlock("BLOCK1").countParts(), 1);
            assertEquals(template_parsed.getContent().getPart(0).getData(), parser_.getTemplateContent("blocks_spaced_escaped_out_content"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(0).getData(), parser_.getTemplateContent("blocks_spaced_escaped_out_block1"));
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testParseBlocksExtremities() {
        try {
            var template_parsed = parser_.parse("blocks_extremities_in", null);
            assertEquals(template_parsed.getBlocks().size(), 4);
            assertNotNull(template_parsed.getContent());
            assertNotNull(template_parsed.getBlock("BLOCK1"));
            assertNotNull(template_parsed.getBlock("BLOCK2"));
            assertNotNull(template_parsed.getBlock("BLOCK3"));
            assertEquals(template_parsed.getContent().countParts(), 1);
            assertEquals(template_parsed.getBlock("BLOCK1").countParts(), 1);
            assertEquals(template_parsed.getBlock("BLOCK2").countParts(), 1);
            assertEquals(template_parsed.getBlock("BLOCK3").countParts(), 1);
            assertEquals(template_parsed.getContent().getPart(0).getData(), parser_.getTemplateContent("blocks_extremities_out_content"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(0).getData(), parser_.getTemplateContent("blocks_extremities_out_block1"));
            assertEquals(template_parsed.getBlock("BLOCK2").getPart(0).getData(), parser_.getTemplateContent("blocks_extremities_out_block2"));
            assertEquals(template_parsed.getBlock("BLOCK3").getPart(0).getData(), parser_.getTemplateContent("blocks_extremities_out_block3"));
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testParseBlocksExtremitiesEscaped() {
        try {
            var template_parsed = parser_.parse("blocks_extremities_escaped_in", null);
            assertEquals(template_parsed.getBlocks().size(), 2);
            assertNotNull(template_parsed.getContent());
            assertNotNull(template_parsed.getBlock("BLOCK2"));
            assertEquals(template_parsed.getContent().countParts(), 1);
            assertEquals(template_parsed.getBlock("BLOCK2").countParts(), 1);
            assertEquals(template_parsed.getContent().getPart(0).getData(), parser_.getTemplateContent("blocks_extremities_escaped_out_content"));
            assertEquals(template_parsed.getBlock("BLOCK2").getPart(0).getData(), parser_.getTemplateContent("blocks_extremities_escaped_out_block2"));
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testParseBlockvalues() {
        try {
            var template_parsed = parser_.parse("blockvalues_in", null);
            assertEquals(template_parsed.getBlocks().size(), 4);
            assertNotNull(template_parsed.getContent());
            assertNotNull(template_parsed.getBlock("BLOCK1"));
            assertNotNull(template_parsed.getBlock("BLOCK2"));
            assertNotNull(template_parsed.getBlock("BLOCK3"));
            assertEquals(template_parsed.getContent().countParts(), 1);
            assertEquals(template_parsed.getBlock("BLOCK1").countParts(), 1);
            assertEquals(template_parsed.getBlock("BLOCK2").countParts(), 1);
            assertEquals(template_parsed.getBlock("BLOCK3").countParts(), 1);
            assertTrue(template_parsed.hasBlockvalue("BLOCK1"));
            assertTrue(template_parsed.hasBlockvalue("BLOCK2"));
            assertTrue(template_parsed.hasBlockvalue("BLOCK3"));
            assertEquals(template_parsed.getContent().getPart(0).getData(), parser_.getTemplateContent("blockvalues_out_content"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(0).getData(), parser_.getTemplateContent("blockvalues_out_block1"));
            assertEquals(template_parsed.getBlock("BLOCK2").getPart(0).getData(), parser_.getTemplateContent("blockvalues_out_block2"));
            assertEquals(template_parsed.getBlock("BLOCK3").getPart(0).getData(), parser_.getTemplateContent("blockvalues_out_block3"));
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testParseBlockvaluesEscaped() {
        try {
            var template_parsed = parser_.parse("blockvalues_escaped_in", null);
            assertEquals(template_parsed.getBlocks().size(), 3);
            assertNotNull(template_parsed.getContent());
            assertNotNull(template_parsed.getBlock("BLOCK1"));
            assertNotNull(template_parsed.getBlock("BLOCK3"));
            assertEquals(template_parsed.getContent().countParts(), 1);
            assertEquals(template_parsed.getBlock("BLOCK1").countParts(), 1);
            assertEquals(template_parsed.getBlock("BLOCK3").countParts(), 1);
            assertTrue(template_parsed.hasBlockvalue("BLOCK1"));
            assertTrue(template_parsed.hasBlockvalue("BLOCK3"));
            assertEquals(template_parsed.getContent().getPart(0).getData(), parser_.getTemplateContent("blockvalues_escaped_out_content"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(0).getData(), parser_.getTemplateContent("blockvalues_escaped_out_block1"));
            assertEquals(template_parsed.getBlock("BLOCK3").getPart(0).getData(), parser_.getTemplateContent("blockvalues_escaped_out_block3"));
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testParseBlockappends() {
        try {
            var template_parsed = parser_.parse("blockappends_in", null);
            assertEquals(template_parsed.getBlocks().size(), 2);
            assertNotNull(template_parsed.getContent());
            assertNotNull(template_parsed.getBlock("BLOCK"));
            assertEquals(template_parsed.getContent().countParts(), 3);
            assertEquals(template_parsed.getBlock("BLOCK").countParts(), 5);
            assertTrue(template_parsed.hasBlockvalue("BLOCK"));
            assertEquals(template_parsed.getContent().getPart(0).getData(), parser_.getTemplateContent("blockappends_out_content_0"));
            assertEquals(template_parsed.getContent().getPart(1).getType(), ParsedBlockPart.Type.VALUE);
            assertEquals(template_parsed.getContent().getPart(1).getData(), "BLOCK");
            assertEquals(template_parsed.getContent().getPart(2).getData(), parser_.getTemplateContent("blockappends_out_content_2"));
            assertEquals(template_parsed.getBlock("BLOCK").getPart(0).getData(), parser_.getTemplateContent("blockappends_out_block_0"));
            assertEquals(template_parsed.getBlock("BLOCK").getPart(1).getType(), ParsedBlockPart.Type.VALUE);
            assertEquals(template_parsed.getBlock("BLOCK").getPart(1).getData(), "value2");
            assertEquals(template_parsed.getBlock("BLOCK").getPart(2).getData(), parser_.getTemplateContent("blockappends_out_block_2"));
            assertEquals(template_parsed.getBlock("BLOCK").getPart(3).getType(), ParsedBlockPart.Type.VALUE);
            assertEquals(template_parsed.getBlock("BLOCK").getPart(3).getData(), "value3");
            assertEquals(template_parsed.getDefaultValue("value3"), parser_.getTemplateContent("blockappends_out_block_3-defaultvalue"));
            assertEquals(template_parsed.getBlock("BLOCK").getPart(4).getData(), parser_.getTemplateContent("blockappends_out_block_4"));
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testParseBlockappendsEscaped() {
        try {
            var template_parsed = parser_.parse("blockappends_escaped_in", null);
            assertEquals(template_parsed.getBlocks().size(), 3);
            assertNotNull(template_parsed.getContent());
            assertNotNull(template_parsed.getBlock("BLOCK1"));
            assertNotNull(template_parsed.getBlock("BLOCK2"));
            assertEquals(template_parsed.getContent().countParts(), 1);
            assertEquals(template_parsed.getBlock("BLOCK1").countParts(), 1);
            assertEquals(template_parsed.getBlock("BLOCK2").countParts(), 1);
            assertTrue(template_parsed.hasBlockvalue("BLOCK1"));
            assertTrue(template_parsed.hasBlockvalue("BLOCK2"));
            assertEquals(template_parsed.getContent().getPart(0).getData(), parser_.getTemplateContent("blockappends_escaped_out_content"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(0).getData(), parser_.getTemplateContent("blockappends_escaped_out_block1"));
            assertEquals(template_parsed.getBlock("BLOCK2").getPart(0).getData(), parser_.getTemplateContent("blockappends_escaped_out_block2"));
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testParseBlockoverrides() {
        try {
            var template_parsed = parser_.parse("blockoverrides_in", null);
            assertEquals(template_parsed.getBlocks().size(), 3);
            assertNotNull(template_parsed.getContent());
            assertNotNull(template_parsed.getBlock("BLOCK1"));
            assertNotNull(template_parsed.getBlock("BLOCK2"));
            assertEquals(template_parsed.getContent().countParts(), 4);
            assertEquals(template_parsed.getBlock("BLOCK1").countParts(), 1);
            assertFalse(template_parsed.hasBlockvalue("BLOCK1"));
            assertEquals(template_parsed.getBlock("BLOCK2").countParts(), 1);
            assertFalse(template_parsed.hasBlockvalue("BLOCK2"));
            assertEquals(template_parsed.getContent().getPart(0).getData(), parser_.getTemplateContent("blockoverrides_out_content_0"));
            assertEquals(template_parsed.getContent().getPart(1).getType(), ParsedBlockPart.Type.VALUE);
            assertEquals(template_parsed.getContent().getPart(1).getData(), "BLOCK1");
            assertEquals(template_parsed.getContent().getPart(2).getType(), ParsedBlockPart.Type.VALUE);
            assertEquals(template_parsed.getContent().getPart(2).getData(), "BLOCK2");
            assertEquals(template_parsed.getContent().getPart(3).getData(), parser_.getTemplateContent("blockoverrides_out_content_3"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(0).getData(), "block1");

            assertEquals(parser_.getTemplateFactory().get("blockoverrides_in").getContent(), parser_.getTemplateContent("blockoverrides_content"));
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testParseBlocksNested() {
        try {
            var template_parsed = parser_.parse("blocks_nested_in", null);
            assertEquals(template_parsed.getBlocks().size(), 4);
            assertNotNull(template_parsed.getContent());
            assertNotNull(template_parsed.getBlock("BLOCK1"));
            assertNotNull(template_parsed.getBlock("BLOCK2"));
            assertNotNull(template_parsed.getBlock("BLOCK3"));
            assertEquals(template_parsed.getContent().countParts(), 1);
            assertEquals(template_parsed.getBlock("BLOCK1").countParts(), 1);
            assertEquals(template_parsed.getBlock("BLOCK2").countParts(), 1);
            assertEquals(template_parsed.getBlock("BLOCK3").countParts(), 1);
            assertEquals(template_parsed.getContent().getPart(0).getData(), parser_.getTemplateContent("blocks_nested_out_content"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(0).getData(), parser_.getTemplateContent("blocks_nested_out_block1"));
            assertEquals(template_parsed.getBlock("BLOCK2").getPart(0).getData(), parser_.getTemplateContent("blocks_nested_out_block2"));
            assertEquals(template_parsed.getBlock("BLOCK3").getPart(0).getData(), parser_.getTemplateContent("blocks_nested_out_block3"));
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testParseBlocksNestedEscaped() {
        try {
            var template_parsed = parser_.parse("blocks_nested_escaped_in", null);
            assertEquals(template_parsed.getBlocks().size(), 3);
            assertNotNull(template_parsed.getContent());
            assertNotNull(template_parsed.getBlock("BLOCK1"));
            assertNotNull(template_parsed.getBlock("BLOCK3"));
            assertEquals(template_parsed.getContent().countParts(), 1);
            assertEquals(template_parsed.getBlock("BLOCK1").countParts(), 1);
            assertEquals(template_parsed.getBlock("BLOCK3").countParts(), 1);
            assertEquals(template_parsed.getContent().getPart(0).getData(), parser_.getTemplateContent("blocks_nested_escaped_out_content"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(0).getData(), parser_.getTemplateContent("blocks_nested_escaped_out_block1"));
            assertEquals(template_parsed.getBlock("BLOCK3").getPart(0).getData(), parser_.getTemplateContent("blocks_nested_escaped_out_block3"));
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testParseBlocksNameHashcodeConflicts() {
        try {
            assertEquals("DMn0".hashCode(), "Cln0".hashCode());
            assertEquals("DMn0".hashCode(), "DNNO".hashCode());
            assertEquals("FMmO".hashCode(), "EmMn".hashCode());
            assertTrue("DMn0".hashCode() != "FMmO".hashCode());
            assertTrue("DMn0".hashCode() != "HNMn".hashCode());
            assertTrue("FMmO".hashCode() != "HNMn".hashCode());
            var template_parsed = parser_.parse("blocks_stringconflicts_in", null);
            assertEquals(template_parsed.getBlocks().size(), 7);
            assertNotNull(template_parsed.getContent());
            assertNotNull(template_parsed.getBlock("DMn0"));
            assertNotNull(template_parsed.getBlock("Cln0"));
            assertNotNull(template_parsed.getBlock("DNNO"));
            assertNotNull(template_parsed.getBlock("FMmO"));
            assertNotNull(template_parsed.getBlock("EmMn"));
            assertNotNull(template_parsed.getBlock("HNMn"));
            assertEquals(template_parsed.getContent().countParts(), 3);
            assertEquals(template_parsed.getBlock("DMn0").countParts(), 1);
            assertEquals(template_parsed.getBlock("Cln0").countParts(), 1);
            assertEquals(template_parsed.getBlock("DNNO").countParts(), 1);
            assertEquals(template_parsed.getBlock("FMmO").countParts(), 1);
            assertEquals(template_parsed.getBlock("EmMn").countParts(), 1);
            assertEquals(template_parsed.getBlock("HNMn").countParts(), 1);
            assertEquals(template_parsed.getContent().getPart(0).getData(), parser_.getTemplateContent("blocks_stringconflicts_out_content"));
            assertEquals(template_parsed.getBlock("DMn0").getPart(0).getData(), parser_.getTemplateContent("blocks_stringconflicts_out_block1"));
            assertEquals(template_parsed.getBlock("Cln0").getPart(0).getData(), parser_.getTemplateContent("blocks_stringconflicts_out_block2"));
            assertEquals(template_parsed.getBlock("DNNO").getPart(0).getData(), parser_.getTemplateContent("blocks_stringconflicts_out_block3"));
            assertEquals(template_parsed.getBlock("FMmO").getPart(0).getData(), parser_.getTemplateContent("blocks_stringconflicts_out_block4"));
            assertEquals(template_parsed.getBlock("EmMn").getPart(0).getData(), parser_.getTemplateContent("blocks_stringconflicts_out_block5"));
            assertEquals(template_parsed.getBlock("HNMn").getPart(0).getData(), parser_.getTemplateContent("blocks_stringconflicts_out_block6"));
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testParseValuesLong() {
        try {
            var template_parsed = parser_.parse("values_long_in", null);
            assertEquals(template_parsed.getBlocks().size(), 4);
            assertNotNull(template_parsed.getContent());
            assertNotNull(template_parsed.getBlock("BLOCK1"));
            assertNotNull(template_parsed.getBlock("BLOCK2"));
            assertNotNull(template_parsed.getBlock("BLOCK3"));
            assertEquals(template_parsed.getContent().countParts(), 5);
            assertEquals(template_parsed.getBlock("BLOCK1").countParts(), 5);
            assertEquals(template_parsed.getBlock("BLOCK2").countParts(), 3);
            assertEquals(template_parsed.getBlock("BLOCK3").countParts(), 1);
            assertEquals(template_parsed.getContent().getPart(0).getData(), parser_.getTemplateContent("values_long_out_content_0"));
            assertEquals(template_parsed.getContent().getPart(1).getData(), parser_.getTemplateContent("values_long_out_content_1"));
            assertEquals(template_parsed.getContent().getPart(2).getData(), parser_.getTemplateContent("values_long_out_content_2"));
            assertEquals(template_parsed.getContent().getPart(3).getData(), parser_.getTemplateContent("values_long_out_content_3"));
            assertEquals(template_parsed.getContent().getPart(4).getData(), parser_.getTemplateContent("values_long_out_content_4"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(0).getData(), parser_.getTemplateContent("values_long_out_block1_0"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(1).getData(), parser_.getTemplateContent("values_long_out_block1_1"));
            assertEquals(template_parsed.getDefaultValue(template_parsed.getBlock("BLOCK1").getPart(1).getData()), parser_.getTemplateContent("values_long_out_block1_1-defaultvalue"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(2).getData(), parser_.getTemplateContent("values_long_out_block1_2"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(3).getData(), parser_.getTemplateContent("values_long_out_block1_3"));
            assertEquals(template_parsed.getDefaultValue(template_parsed.getBlock("BLOCK1").getPart(3).getData()), parser_.getTemplateContent("values_long_out_block1_3-defaultvalue"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(4).getData(), parser_.getTemplateContent("values_long_out_block1_4"));
            assertEquals(template_parsed.getBlock("BLOCK2").getPart(0).getData(), parser_.getTemplateContent("values_long_out_block2_0"));
            assertEquals(template_parsed.getBlock("BLOCK2").getPart(1).getData(), parser_.getTemplateContent("values_long_out_block2_1"));
            assertEquals(template_parsed.getDefaultValue(template_parsed.getBlock("BLOCK2").getPart(1).getData()), parser_.getTemplateContent("values_long_out_block2_1-defaultvalue"));
            assertEquals(template_parsed.getBlock("BLOCK2").getPart(2).getData(), parser_.getTemplateContent("values_long_out_block2_2"));
            assertEquals(template_parsed.getDefaultValue(template_parsed.getBlock("BLOCK2").getPart(2).getData()), parser_.getTemplateContent("values_long_out_block2_2-defaultvalue"));
            assertEquals(template_parsed.getBlock("BLOCK3").getPart(0).getData(), parser_.getTemplateContent("values_long_out_block3_0"));
            assertEquals(template_parsed.getDefaultValue(template_parsed.getBlock("BLOCK3").getPart(0).getData()), parser_.getTemplateContent("values_long_out_block3_0-defaultvalue"));
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testParseValuesLongEscaped() {
        try {
            var template_parsed = parser_.parse("values_long_escaped_in", null);
            assertEquals(template_parsed.getBlocks().size(), 2);
            assertNotNull(template_parsed.getContent());
            assertNotNull(template_parsed.getBlock("BLOCK1"));
            assertEquals(template_parsed.getContent().countParts(), 3);
            assertEquals(template_parsed.getBlock("BLOCK1").countParts(), 6);
            assertEquals(template_parsed.getContent().getPart(0).getData(), parser_.getTemplateContent("values_long_escaped_out_content_0"));
            assertEquals(template_parsed.getContent().getPart(1).getData(), parser_.getTemplateContent("values_long_escaped_out_content_1"));
            assertEquals(template_parsed.getContent().getPart(2).getData(), parser_.getTemplateContent("values_long_escaped_out_content_2"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(0).getData(), parser_.getTemplateContent("values_long_escaped_out_block1_0"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(1).getData(), parser_.getTemplateContent("values_long_escaped_out_block1_1"));
            assertEquals(template_parsed.getDefaultValue(template_parsed.getBlock("BLOCK1").getPart(1).getData()), parser_.getTemplateContent("values_long_escaped_out_block1_1-defaultvalue"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(2).getData(), parser_.getTemplateContent("values_long_escaped_out_block1_2"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(3).getData(), parser_.getTemplateContent("values_long_escaped_out_block1_3"));
            assertEquals(template_parsed.getDefaultValue(template_parsed.getBlock("BLOCK1").getPart(3).getData()), parser_.getTemplateContent("values_long_escaped_out_block1_3-defaultvalue"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(4).getData(), parser_.getTemplateContent("values_long_escaped_out_block1_4"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(5).getData(), parser_.getTemplateContent("values_long_escaped_out_block1_5"));
            assertEquals(template_parsed.getDefaultValue(template_parsed.getBlock("BLOCK1").getPart(5).getData()), parser_.getTemplateContent("values_long_escaped_out_block1_5-defaultvalue"));
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testParseValuesShort() {
        try {
            var template_parsed = parser_.parse("values_short_in", null);
            assertEquals(template_parsed.getBlocks().size(), 4);
            assertNotNull(template_parsed.getContent());
            assertNotNull(template_parsed.getBlock("BLOCK1"));
            assertNotNull(template_parsed.getBlock("BLOCK2"));
            assertNotNull(template_parsed.getBlock("BLOCK3"));
            assertEquals(template_parsed.getContent().countParts(), 5);
            assertEquals(template_parsed.getBlock("BLOCK1").countParts(), 5);
            assertEquals(template_parsed.getBlock("BLOCK2").countParts(), 3);
            assertEquals(template_parsed.getBlock("BLOCK3").countParts(), 1);
            assertEquals(template_parsed.getContent().getPart(0).getData(), parser_.getTemplateContent("values_short_out_content_0"));
            assertEquals(template_parsed.getContent().getPart(1).getData(), parser_.getTemplateContent("values_short_out_content_1"));
            assertEquals(template_parsed.getContent().getPart(2).getData(), parser_.getTemplateContent("values_short_out_content_2"));
            assertEquals(template_parsed.getContent().getPart(3).getData(), parser_.getTemplateContent("values_short_out_content_3"));
            assertEquals(template_parsed.getContent().getPart(4).getData(), parser_.getTemplateContent("values_short_out_content_4"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(0).getData(), parser_.getTemplateContent("values_short_out_block1_0"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(1).getData(), parser_.getTemplateContent("values_short_out_block1_1"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(2).getData(), parser_.getTemplateContent("values_short_out_block1_2"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(3).getData(), parser_.getTemplateContent("values_short_out_block1_3"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(4).getData(), parser_.getTemplateContent("values_short_out_block1_4"));
            assertEquals(template_parsed.getBlock("BLOCK2").getPart(0).getData(), parser_.getTemplateContent("values_short_out_block2_0"));
            assertEquals(template_parsed.getBlock("BLOCK2").getPart(1).getData(), parser_.getTemplateContent("values_short_out_block2_1"));
            assertEquals(template_parsed.getBlock("BLOCK2").getPart(2).getData(), parser_.getTemplateContent("values_short_out_block2_2"));
            assertEquals(template_parsed.getBlock("BLOCK3").getPart(0).getData(), parser_.getTemplateContent("values_short_out_block3_0"));
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testParseValuesShortEscaped() {
        try {
            var template_parsed = parser_.parse("values_short_escaped_in", null);
            assertEquals(template_parsed.getBlocks().size(), 2);
            assertNotNull(template_parsed.getContent());
            assertNotNull(template_parsed.getBlock("BLOCK1"));
            assertEquals(template_parsed.getContent().countParts(), 5);
            assertEquals(template_parsed.getBlock("BLOCK1").countParts(), 3);
            assertEquals(template_parsed.getContent().getPart(0).getData(), parser_.getTemplateContent("values_short_escaped_out_content_0"));
            assertEquals(template_parsed.getContent().getPart(1).getData(), parser_.getTemplateContent("values_short_escaped_out_content_1"));
            assertEquals(template_parsed.getContent().getPart(2).getData(), parser_.getTemplateContent("values_short_escaped_out_content_2"));
            assertEquals(template_parsed.getContent().getPart(3).getData(), parser_.getTemplateContent("values_short_escaped_out_content_3"));
            assertEquals(template_parsed.getContent().getPart(4).getData(), parser_.getTemplateContent("values_short_escaped_out_content_4"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(0).getData(), parser_.getTemplateContent("values_short_escaped_out_block1_0"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(1).getData(), parser_.getTemplateContent("values_short_escaped_out_block1_1"));
            assertEquals(template_parsed.getBlock("BLOCK1").getPart(2).getData(), parser_.getTemplateContent("values_short_escaped_out_block1_2"));
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testParseIncludes() {
        try {
            var template_parsed = parser_.parse("includes_master_in", null);
            assertEquals(template_parsed.getBlocks().size(), 1);
            assertNotNull(template_parsed.getContent());
            assertEquals(template_parsed.getContent().countParts(), 1);
            assertEquals(template_parsed.getContent().getPart(0).getData(), parser_.getTemplateContent("includes_out_content"));
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testParseIncludesMultiple() {
        try {
            var template_parsed = parser_.parse("includes_multiple_in", null);
            assertEquals(template_parsed.getBlocks().size(), 1);
            assertNotNull(template_parsed.getContent());
            assertEquals(template_parsed.getContent().countParts(), 1);
            assertEquals(template_parsed.getContent().getPart(0).getData(), parser_.getTemplateContent("includes_multiple_out_content"));
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    /*
           @Test void testParseIncludesOtherType()
            {
                try
                {
                    Parsed template_parsed = parser_.parse("includes_othertype_in", null, null);
                    assertEquals(template_parsed.getBlocks().size(), 2);
                    assertNotNull(template_parsed.getContent());
                    assertEquals(template_parsed.getContent().countParts(), 5);
                    assertEquals(template_parsed.getContent().getPart(0).getData(), parser_.getTemplateContent("includes_othertype_out_content_0"));
                    assertEquals(template_parsed.getContent().getPart(1).getData(), parser_.getTemplateContent("includes_othertype_out_content_1"));
                    assertEquals(template_parsed.getContent().getPart(2).getData(), parser_.getTemplateContent("includes_othertype_out_content_2"));
                    assertEquals(template_parsed.getContent().getPart(3).getData(), parser_.getTemplateContent("includes_othertype_out_content_3"));
                    assertEquals(template_parsed.getContent().getPart(4).getData(), parser_.getTemplateContent("includes_othertype_out_content_4"));
                }
                catch (TemplateException e)
                {
                    fail(ExceptionUtils.getExceptionStackTrace(e));
                }
            }
*/

    @Test
    void testParseIncludesEscaped() {
        try {
            var template_parsed = parser_.parse("includes_escaped_master_in", null);
            assertEquals(template_parsed.getBlocks().size(), 1);
            assertNotNull(template_parsed.getContent());
            assertEquals(template_parsed.getContent().countParts(), 1);
            assertEquals(template_parsed.getContent().getPart(0).getData(), parser_.getTemplateContent("includes_escaped_out_content"));
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testEncodingLatinHtml() {
        try {
            var template_ascii = TemplateFactory.HTML.getParser().parse("encoding_latin_ascii", "US-ASCII");
            var template_utf_16le = TemplateFactory.HTML.getParser().parse("encoding_latin_utf16le", "UTF-16LE");
            var template_ascii_wrong = TemplateFactory.HTML.getParser().parse("encoding_latin_ascii", "UTF-16LE");
            var template_utf_16le_wrong = TemplateFactory.HTML.getParser().parse("encoding_latin_utf16le", "US-ASCII");

            assertEquals(template_ascii.getContent().getPart(0).getData(), template_utf_16le.getContent().getPart(0).getData());
            assertNotEquals(template_utf_16le.getContent().getPart(0).getData(), template_utf_16le_wrong.getContent().getPart(0).getData());
            assertNotEquals(template_ascii.getContent().getPart(0).getData(), template_ascii_wrong.getContent().getPart(0).getData());
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testEncodingLatinTxt() {
        try {
            var template_ascii = TemplateFactory.TXT.getParser().parse("encoding_latin_ascii", "US-ASCII");
            var template_utf_16le = TemplateFactory.TXT.getParser().parse("encoding_latin_utf16le", "UTF-16LE");
            var template_ascii_wrong = TemplateFactory.TXT.getParser().parse("encoding_latin_ascii", "UTF-16LE");
            var template_utf_16le_wrong = TemplateFactory.TXT.getParser().parse("encoding_latin_utf16le", "US-ASCII");

            assertEquals(template_ascii.getContent().getPart(0).getData(), template_utf_16le.getContent().getPart(0).getData());
            assertNotEquals(template_utf_16le.getContent().getPart(0).getData(), template_utf_16le_wrong.getContent().getPart(0).getData());
            assertNotEquals(template_ascii.getContent().getPart(0).getData(), template_ascii_wrong.getContent().getPart(0).getData());
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testEncodingLatin1Html() {
        try {
            var template_iso8859_1 = TemplateFactory.HTML.getParser().parse("encoding_latin1_iso88591", "ISO8859-1");
            var template_utf_8 = TemplateFactory.HTML.getParser().parse("encoding_latin1_utf8", "UTF-8");
            var template_iso8859_1_wrong = TemplateFactory.HTML.getParser().parse("encoding_latin1_iso88591", "UTF-8");
            var template_utf_8_wrong = TemplateFactory.HTML.getParser().parse("encoding_latin1_utf8", "ISO8859-1");

            assertEquals(template_iso8859_1.getContent().getPart(0).getData(), template_utf_8.getContent().getPart(0).getData());
            assertNotEquals(template_iso8859_1.getContent().getPart(0).getData(), template_iso8859_1_wrong.getContent().getPart(0).getData());
            assertNotEquals(template_utf_8.getContent().getPart(0).getData(), template_utf_8_wrong.getContent().getPart(0).getData());
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testEncodingLatin1Txt() {
        try {
            var template_iso8859_1 = TemplateFactory.TXT.getParser().parse("encoding_latin1_iso88591", "ISO8859-1");
            var template_utf_8 = TemplateFactory.TXT.getParser().parse("encoding_latin1_utf8", "UTF-8");
            var template_iso8859_1_wrong = TemplateFactory.TXT.getParser().parse("encoding_latin1_iso88591", "UTF-8");
            var template_utf_8_wrong = TemplateFactory.TXT.getParser().parse("encoding_latin1_utf8", "ISO8859-1");

            assertEquals(template_iso8859_1.getContent().getPart(0).getData(), template_utf_8.getContent().getPart(0).getData());
            assertNotEquals(template_iso8859_1.getContent().getPart(0).getData(), template_iso8859_1_wrong.getContent().getPart(0).getData());
            assertNotEquals(template_utf_8.getContent().getPart(0).getData(), template_utf_8_wrong.getContent().getPart(0).getData());
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testEncodingNonLatinHtml() {
        try {
            var template_utf_8 = TemplateFactory.HTML.getParser().parse("encoding_nonlatin_utf8", "UTF-8");
            var template_utf_8_wrong = TemplateFactory.HTML.getParser().parse("encoding_nonlatin_utf8", "ISO8859-1");
            var template_utf_16be = TemplateFactory.HTML.getParser().parse("encoding_nonlatin_utf16be", "UTF-16BE");
            var template_utf_16be_wrong = TemplateFactory.HTML.getParser().parse("encoding_nonlatin_utf16be", "UTF-16LE");

            assertEquals(template_utf_8.getContent().getPart(0).getData(), template_utf_16be.getContent().getPart(0).getData());
            assertNotEquals(template_utf_8.getContent().getPart(0).getData(), template_utf_8_wrong.getContent().getPart(0).getData());
            assertNotEquals(template_utf_16be.getContent().getPart(0).getData(), template_utf_16be_wrong.getContent().getPart(0).getData());
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testEncodingNonLatinTxt() {
        try {
            var template_utf_8 = TemplateFactory.TXT.getParser().parse("encoding_nonlatin_utf8", "UTF-8");
            var template_utf_8_wrong = TemplateFactory.TXT.getParser().parse("encoding_nonlatin_utf8", "ISO8859-1");
            var template_utf_16be = TemplateFactory.TXT.getParser().parse("encoding_nonlatin_utf16be", "UTF-16BE");
            var template_utf_16be_wrong = TemplateFactory.TXT.getParser().parse("encoding_nonlatin_utf16be", "UTF-16LE");

            assertEquals(template_utf_8.getContent().getPart(0).getData(), template_utf_16be.getContent().getPart(0).getData());
            assertNotEquals(template_utf_8.getContent().getPart(0).getData(), template_utf_8_wrong.getContent().getPart(0).getData());
            assertNotEquals(template_utf_16be.getContent().getPart(0).getData(), template_utf_16be_wrong.getContent().getPart(0).getData());
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testParseFilteredBlocks() {
        try {
            Parser parser;
            Parsed template_parsed;
            FilteredTagsMap filtered_blocks_map;

            var filter1 = "^FILTER1:(\\w+):CONST:(\\w+)$";
            var filter2 = "^FILTER2:(\\w+)$";
            var filter3 = "^CONST-FILTER3:(\\w+)$";
            var filter4 = "(\\w+)";
            FilteredTags filtered_blocks = null;

            parser = new Parser(TemplateFactory.HTML, "html", ".html", (Pattern[]) null, (Pattern[]) null);
            template_parsed = parser.parse("blocks_filtered_in", null);
            filtered_blocks_map = template_parsed.getFilteredBlocksMap();
            assertNull(filtered_blocks_map);

            parser = new Parser(TemplateFactory.HTML, "html", ".html", new Pattern[]{Pattern.compile(filter1), Pattern.compile(filter2), Pattern.compile(filter3), Pattern.compile(filter4)}, null);
            template_parsed = parser.parse("blocks_filtered_in", null);
            filtered_blocks_map = template_parsed.getFilteredBlocksMap();
            assertNotNull(filtered_blocks_map);

            assertTrue(filtered_blocks_map.containsFilter(filter1));
            assertTrue(filtered_blocks_map.containsFilter(filter2));
            assertTrue(filtered_blocks_map.containsFilter(filter3));
            assertFalse(filtered_blocks_map.containsFilter(filter4));

            filtered_blocks = filtered_blocks_map.getFilteredTag(filter1);
            assertEquals(3, filtered_blocks.size());

            var filter1_got_block1 = false;
            var filter1_got_block2 = false;
            var filter1_got_block3 = false;
            for (var entry : filtered_blocks.entrySet()) {
                var block_groups = entry.getValue();
                assertEquals(3, block_groups.length);
                if (entry.getKey().equals("FILTER1:BLOCK1a:CONST:BLOCK1b") &&
                    block_groups[0].equals("FILTER1:BLOCK1a:CONST:BLOCK1b") &&
                    block_groups[1].equals("BLOCK1a") &&
                    block_groups[2].equals("BLOCK1b")) {
                    filter1_got_block1 = true;
                } else if (entry.getKey().equals("FILTER1:BLOCK2a:CONST:BLOCK2b") &&
                    block_groups[0].equals("FILTER1:BLOCK2a:CONST:BLOCK2b") &&
                    block_groups[1].equals("BLOCK2a") &&
                    block_groups[2].equals("BLOCK2b")) {
                    filter1_got_block2 = true;
                } else if (entry.getKey().equals("FILTER1:BLOCK3a:CONST:BLOCK3b") &&
                    block_groups[0].equals("FILTER1:BLOCK3a:CONST:BLOCK3b") &&
                    block_groups[1].equals("BLOCK3a") &&
                    block_groups[2].equals("BLOCK3b")) {
                    filter1_got_block3 = true;
                }
            }
            assertTrue(filter1_got_block1 && filter1_got_block2 && filter1_got_block3);

            filtered_blocks = filtered_blocks_map.getFilteredTag(filter2);
            assertEquals(2, filtered_blocks.size());

            var filter2_got_block1 = false;
            var filter2_got_block2 = false;
            for (var entry : filtered_blocks.entrySet()) {
                var block_groups = entry.getValue();
                assertEquals(2, block_groups.length);
                if (entry.getKey().equals("FILTER2:BLOCK1") &&
                    block_groups[0].equals("FILTER2:BLOCK1") &&
                    block_groups[1].equals("BLOCK1")) {
                    filter2_got_block1 = true;
                } else if (entry.getKey().equals("FILTER2:BLOCK2") &&
                    block_groups[0].equals("FILTER2:BLOCK2") &&
                    block_groups[1].equals("BLOCK2")) {
                    filter2_got_block2 = true;
                }
            }
            assertTrue(filter2_got_block1 && filter2_got_block2);

            filtered_blocks = filtered_blocks_map.getFilteredTag(filter3);
            assertEquals(2, filtered_blocks.size());

            var filter3_got_block1 = false;
            var filter3_got_block2 = false;
            for (var entry : filtered_blocks.entrySet()) {
                var block_groups = entry.getValue();
                assertEquals(2, block_groups.length);
                if (entry.getKey().equals("CONST-FILTER3:BLOCK1") &&
                    block_groups[0].equals("CONST-FILTER3:BLOCK1") &&
                    block_groups[1].equals("BLOCK1")) {
                    filter3_got_block1 = true;
                } else if (entry.getKey().equals("CONST-FILTER3:BLOCK2") &&
                    block_groups[0].equals("CONST-FILTER3:BLOCK2") &&
                    block_groups[1].equals("BLOCK2")) {
                    filter3_got_block2 = true;
                }
            }
            assertTrue(filter3_got_block1 && filter3_got_block2);

            parser = new Parser(TemplateFactory.HTML, "html", ".html", new Pattern[]{Pattern.compile(filter4), Pattern.compile(filter1), Pattern.compile(filter2), Pattern.compile(filter3)}, null);
            template_parsed = parser.parse("blocks_filtered_in", null);
            filtered_blocks_map = template_parsed.getFilteredBlocksMap();
            assertNotNull(filtered_blocks_map);

            assertFalse(filtered_blocks_map.containsFilter(filter1));
            assertFalse(filtered_blocks_map.containsFilter(filter2));
            assertFalse(filtered_blocks_map.containsFilter(filter3));
            assertTrue(filtered_blocks_map.containsFilter(filter4));

            filtered_blocks = filtered_blocks_map.getFilteredTag(filter4);
            assertEquals(7, filtered_blocks.size());

            var filter4_got_block1 = false;
            var filter4_got_block2 = false;
            var filter4_got_block3 = false;
            var filter4_got_block4 = false;
            var filter4_got_block5 = false;
            var filter4_got_block6 = false;
            var filter4_got_block7 = false;
            for (var entry : filtered_blocks.entrySet()) {
                var block_groups = entry.getValue();
                if (entry.getKey().equals("FILTER1:BLOCK1a:CONST:BLOCK1b") &&
                    block_groups[0].equals("FILTER1:BLOCK1a:CONST:BLOCK1b") &&
                    block_groups[1].equals("FILTER1") &&
                    block_groups[2].equals("BLOCK1a") &&
                    block_groups[3].equals("CONST") &&
                    block_groups[4].equals("BLOCK1b")) {
                    assertEquals(5, block_groups.length);
                    filter4_got_block1 = true;
                    continue;
                }
                if (entry.getKey().equals("FILTER1:BLOCK2a:CONST:BLOCK2b") &&
                    block_groups[0].equals("FILTER1:BLOCK2a:CONST:BLOCK2b") &&
                    block_groups[1].equals("FILTER1") &&
                    block_groups[2].equals("BLOCK2a") &&
                    block_groups[3].equals("CONST") &&
                    block_groups[4].equals("BLOCK2b")) {
                    assertEquals(5, block_groups.length);
                    filter4_got_block2 = true;
                    continue;
                }
                if (entry.getKey().equals("FILTER1:BLOCK3a:CONST:BLOCK3b") &&
                    block_groups[0].equals("FILTER1:BLOCK3a:CONST:BLOCK3b") &&
                    block_groups[1].equals("FILTER1") &&
                    block_groups[2].equals("BLOCK3a") &&
                    block_groups[3].equals("CONST") &&
                    block_groups[4].equals("BLOCK3b")) {
                    assertEquals(5, block_groups.length);
                    filter4_got_block3 = true;
                    continue;
                }
                if (entry.getKey().equals("FILTER2:BLOCK1") &&
                    block_groups[0].equals("FILTER2:BLOCK1") &&
                    block_groups[1].equals("FILTER2") &&
                    block_groups[2].equals("BLOCK1")) {
                    assertEquals(3, block_groups.length);
                    filter4_got_block4 = true;
                    continue;
                }
                if (entry.getKey().equals("FILTER2:BLOCK2") &&
                    block_groups[0].equals("FILTER2:BLOCK2") &&
                    block_groups[1].equals("FILTER2") &&
                    block_groups[2].equals("BLOCK2")) {
                    assertEquals(3, block_groups.length);
                    filter4_got_block5 = true;
                    continue;
                }
                if (entry.getKey().equals("CONST-FILTER3:BLOCK1") &&
                    block_groups[0].equals("CONST-FILTER3:BLOCK1") &&
                    block_groups[1].equals("CONST") &&
                    block_groups[2].equals("FILTER3") &&
                    block_groups[3].equals("BLOCK1")) {
                    assertEquals(4, block_groups.length);
                    filter4_got_block6 = true;
                    continue;
                }
                if (entry.getKey().equals("CONST-FILTER3:BLOCK2") &&
                    block_groups[0].equals("CONST-FILTER3:BLOCK2") &&
                    block_groups[1].equals("CONST") &&
                    block_groups[2].equals("FILTER3") &&
                    block_groups[3].equals("BLOCK2")) {
                    assertEquals(4, block_groups.length);
                    filter4_got_block7 = true;
                    continue;
                }
            }
            assertTrue(filter4_got_block1 && filter4_got_block2 && filter4_got_block3 &&
                filter4_got_block4 && filter4_got_block5 && filter4_got_block6 &&
                filter4_got_block7);
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testParseFilteredValues() {
        try {
            Parser parser;
            Parsed template_parsed;
            FilteredTagsMap filtered_values_map;

            var filter1 = "^FILTER1:(\\w+):CONST:(\\w+)$";
            var filter2 = "^FILTER2:(\\w+)$";
            var filter3 = "^CONST-FILTER3:(\\w+)$";
            var filter4 = "(\\w+)";
            FilteredTags filtered_values = null;

            parser = new Parser(TemplateFactory.HTML, "html", ".html", (Pattern[]) null, (Pattern[]) null);
            template_parsed = parser.parse("values_filtered_in", null);
            filtered_values_map = template_parsed.getFilteredValuesMap();
            assertNull(filtered_values_map);

            parser = new Parser(TemplateFactory.HTML, "html", ".html", null, new Pattern[]{Pattern.compile(filter1), Pattern.compile(filter2), Pattern.compile(filter3), Pattern.compile(filter4)});
            template_parsed = parser.parse("values_filtered_in", null);
            filtered_values_map = template_parsed.getFilteredValuesMap();
            assertNotNull(filtered_values_map);

            assertTrue(filtered_values_map.containsFilter(filter1));
            assertTrue(filtered_values_map.containsFilter(filter2));
            assertTrue(filtered_values_map.containsFilter(filter3));
            assertFalse(filtered_values_map.containsFilter(filter4));

            filtered_values = filtered_values_map.getFilteredTag(filter1);
            assertEquals(3, filtered_values.size());

            var filter1_got_value1 = false;
            var filter1_got_value2 = false;
            var filter1_got_value3 = false;
            for (var entry : filtered_values.entrySet()) {
                var value_groups = entry.getValue();
                assertEquals(3, value_groups.length);
                if (entry.getKey().equals("FILTER1:VALUE1a:CONST:VALUE1b") &&
                    value_groups[0].equals("FILTER1:VALUE1a:CONST:VALUE1b") &&
                    value_groups[1].equals("VALUE1a") &&
                    value_groups[2].equals("VALUE1b")) {
                    filter1_got_value1 = true;
                } else if (entry.getKey().equals("FILTER1:VALUE2a:CONST:VALUE2b") &&
                    value_groups[0].equals("FILTER1:VALUE2a:CONST:VALUE2b") &&
                    value_groups[1].equals("VALUE2a") &&
                    value_groups[2].equals("VALUE2b")) {
                    filter1_got_value2 = true;
                } else if (entry.getKey().equals("FILTER1:VALUE3a:CONST:VALUE3b") &&
                    value_groups[0].equals("FILTER1:VALUE3a:CONST:VALUE3b") &&
                    value_groups[1].equals("VALUE3a") &&
                    value_groups[2].equals("VALUE3b")) {
                    filter1_got_value3 = true;
                }
            }
            assertTrue(filter1_got_value1 && filter1_got_value2 && filter1_got_value3);

            filtered_values = filtered_values_map.getFilteredTag(filter2);
            assertEquals(2, filtered_values.size());

            var filter2_got_value1 = false;
            var filter2_got_value2 = false;
            for (var entry : filtered_values.entrySet()) {
                var value_groups = entry.getValue();
                assertEquals(2, value_groups.length);
                if (entry.getKey().equals("FILTER2:VALUE1") &&
                    value_groups[0].equals("FILTER2:VALUE1") &&
                    value_groups[1].equals("VALUE1")) {
                    filter2_got_value1 = true;
                } else if (entry.getKey().equals("FILTER2:VALUE2") &&
                    value_groups[0].equals("FILTER2:VALUE2") &&
                    value_groups[1].equals("VALUE2")) {
                    filter2_got_value2 = true;
                }
            }
            assertTrue(filter2_got_value1 && filter2_got_value2);

            filtered_values = filtered_values_map.getFilteredTag(filter3);
            assertEquals(2, filtered_values.size());

            var filter3_got_value1 = false;
            var filter3_got_value2 = false;
            for (var entry : filtered_values.entrySet()) {
                var value_groups = entry.getValue();
                assertEquals(2, value_groups.length);
                if (entry.getKey().equals("CONST-FILTER3:VALUE1") &&
                    value_groups[0].equals("CONST-FILTER3:VALUE1") &&
                    value_groups[1].equals("VALUE1")) {
                    filter3_got_value1 = true;
                } else if (entry.getKey().equals("CONST-FILTER3:VALUE2") &&
                    value_groups[0].equals("CONST-FILTER3:VALUE2") &&
                    value_groups[1].equals("VALUE2")) {
                    filter3_got_value2 = true;
                }
            }
            assertTrue(filter3_got_value1 && filter3_got_value2);

            parser = new Parser(TemplateFactory.HTML, "html", ".html", null, new Pattern[]{Pattern.compile(filter4), Pattern.compile(filter1), Pattern.compile(filter2), Pattern.compile(filter3)});
            template_parsed = parser.parse("values_filtered_in", null);
            filtered_values_map = template_parsed.getFilteredValuesMap();
            assertNotNull(filtered_values_map);

            assertFalse(filtered_values_map.containsFilter(filter1));
            assertFalse(filtered_values_map.containsFilter(filter2));
            assertFalse(filtered_values_map.containsFilter(filter3));
            assertTrue(filtered_values_map.containsFilter(filter4));

            filtered_values = filtered_values_map.getFilteredTag(filter4);
            assertEquals(7, filtered_values.size());

            var filter4_got_value1 = false;
            var filter4_got_value2 = false;
            var filter4_got_value3 = false;
            var filter4_got_value4 = false;
            var filter4_got_value5 = false;
            var filter4_got_value6 = false;
            var filter4_got_value7 = false;
            for (var entry : filtered_values.entrySet()) {
                var value_groups = entry.getValue();
                if (entry.getKey().equals("FILTER1:VALUE1a:CONST:VALUE1b") &&
                    value_groups[0].equals("FILTER1:VALUE1a:CONST:VALUE1b") &&
                    value_groups[1].equals("FILTER1") &&
                    value_groups[2].equals("VALUE1a") &&
                    value_groups[3].equals("CONST") &&
                    value_groups[4].equals("VALUE1b")) {
                    assertEquals(5, value_groups.length);
                    filter4_got_value1 = true;
                    continue;
                }
                if (entry.getKey().equals("FILTER1:VALUE2a:CONST:VALUE2b") &&
                    value_groups[0].equals("FILTER1:VALUE2a:CONST:VALUE2b") &&
                    value_groups[1].equals("FILTER1") &&
                    value_groups[2].equals("VALUE2a") &&
                    value_groups[3].equals("CONST") &&
                    value_groups[4].equals("VALUE2b")) {
                    assertEquals(5, value_groups.length);
                    filter4_got_value2 = true;
                    continue;
                }
                if (entry.getKey().equals("FILTER1:VALUE3a:CONST:VALUE3b") &&
                    value_groups[0].equals("FILTER1:VALUE3a:CONST:VALUE3b") &&
                    value_groups[1].equals("FILTER1") &&
                    value_groups[2].equals("VALUE3a") &&
                    value_groups[3].equals("CONST") &&
                    value_groups[4].equals("VALUE3b")) {
                    assertEquals(5, value_groups.length);
                    filter4_got_value3 = true;
                    continue;
                }
                if (entry.getKey().equals("FILTER2:VALUE1") &&
                    value_groups[0].equals("FILTER2:VALUE1") &&
                    value_groups[1].equals("FILTER2") &&
                    value_groups[2].equals("VALUE1")) {
                    assertEquals(3, value_groups.length);
                    filter4_got_value4 = true;
                    continue;
                }
                if (entry.getKey().equals("FILTER2:VALUE2") &&
                    value_groups[0].equals("FILTER2:VALUE2") &&
                    value_groups[1].equals("FILTER2") &&
                    value_groups[2].equals("VALUE2")) {
                    assertEquals(3, value_groups.length);
                    filter4_got_value5 = true;
                    continue;
                }
                if (entry.getKey().equals("CONST-FILTER3:VALUE1") &&
                    value_groups[0].equals("CONST-FILTER3:VALUE1") &&
                    value_groups[1].equals("CONST") &&
                    value_groups[2].equals("FILTER3") &&
                    value_groups[3].equals("VALUE1")) {
                    assertEquals(4, value_groups.length);
                    filter4_got_value6 = true;
                    continue;
                }
                if (entry.getKey().equals("CONST-FILTER3:VALUE2") &&
                    value_groups[0].equals("CONST-FILTER3:VALUE2") &&
                    value_groups[1].equals("CONST") &&
                    value_groups[2].equals("FILTER3") &&
                    value_groups[3].equals("VALUE2")) {
                    assertEquals(4, value_groups.length);
                    filter4_got_value7 = true;
                    continue;
                }
            }
            assertTrue(filter4_got_value1 && filter4_got_value2 && filter4_got_value3 &&
                filter4_got_value4 && filter4_got_value5 && filter4_got_value6 &&
                filter4_got_value7);
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testErrorTerminatingUnopenedValue() {
        try {
            parser_.parse("error_terminating_unopened_value", null);
            fail();
        } catch (SyntaxErrorException e) {
            assertEquals("<!--v avalue/--><!--/v-->", e.getErrorLocation().lineContent());
            assertEquals(1, e.getErrorLocation().line());
            assertEquals(16, e.getErrorLocation().column());
            assertEquals(9, e.getErrorLocation().marking());
            assertEquals(e.getTemplateName(), "error_terminating_unopened_value");
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testErrorTerminatingUnopenedBlock() {
        try {
            parser_.parse("error_terminating_unopened_block", null);
            fail();
        } catch (SyntaxErrorException e) {
            assertEquals("<!--/b-->", e.getErrorLocation().lineContent());
            assertEquals(1, e.getErrorLocation().line());
            assertEquals(0, e.getErrorLocation().column());
            assertEquals(9, e.getErrorLocation().marking());
            assertEquals(e.getTemplateName(), "error_terminating_unopened_block");
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testErrorTerminatingUnopenedBlockvalue() {
        try {
            parser_.parse("error_terminating_unopened_blockvalue", null);
            fail();
        } catch (SyntaxErrorException e) {
            assertEquals("<!--/bv-->", e.getErrorLocation().lineContent());
            assertEquals(1, e.getErrorLocation().line());
            assertEquals(0, e.getErrorLocation().column());
            assertEquals(10, e.getErrorLocation().marking());
            assertEquals(e.getTemplateName(), "error_terminating_unopened_blockvalue");
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testErrorIncludeNotFound() {
        try {
            parser_.parse("error_include_not_found", null);
            fail();
        } catch (IncludeNotFoundException e) {
            assertEquals("\t\t<!--i error_missing_include/-->", e.getErrorLocation().lineContent());
            assertEquals(8, e.getErrorLocation().line());
            assertEquals(2, e.getErrorLocation().column());
            assertEquals(5, e.getErrorLocation().marking());
            assertEquals(e.getTemplateName(), "error_include_not_found");
            assertEquals(e.getIncluded(), "error_missing_include");
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testErrorCircularIncludes() {
        try {
            parser_.parse("error_circular_includes_master", null);
            fail();
        } catch (CircularIncludesException e) {
            assertEquals("<!--i error_circular_includes_master/-->", e.getErrorLocation().lineContent());
            assertEquals(1, e.getErrorLocation().line());
            assertEquals(0, e.getErrorLocation().column());
            assertEquals(5, e.getErrorLocation().marking());
            assertTrue(e.getPreviousIncludes().contains(parser_.getPackage() + "error_circular_includes_master"));
            assertTrue(e.getPreviousIncludes().contains(parser_.getPackage() + "error_circular_includes_included"));
            assertEquals(e.getIncluded(), "error_circular_includes_master");
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testErrorIncludeBadlyTerminated() {
        try {
            parser_.parse("error_include_badly_terminated", null);
            fail();
        } catch (SyntaxErrorException e) {
            assertEquals("		<!--i error_badly_terminated_include' erzer /-->", e.getErrorLocation().lineContent());
            assertEquals(8, e.getErrorLocation().line());
            assertEquals(40, e.getErrorLocation().column());
            assertEquals(5, e.getErrorLocation().marking());
            assertEquals(e.getTemplateName(), "error_include_badly_terminated");
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testErrorValueTagNotTerminated() {
        try {
            parser_.parse("error_value_tag_not_terminated", null);
            fail();
        } catch (SyntaxErrorException e) {
            assertEquals("</html>", e.getErrorLocation().lineContent());
            assertEquals(10, e.getErrorLocation().line());
            assertEquals(7, e.getErrorLocation().column());
            assertEquals(1, e.getErrorLocation().marking());
            assertEquals(e.getTemplateName(), "error_value_tag_not_terminated");
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testErrorValueShortBeginTagBadlyTerminated() {
        try {
            parser_.parse("error_valueshort_begin_badly_terminated", null);
            fail();
        } catch (SyntaxErrorException e) {
            assertEquals("		<!--v VALUE1'   eff  /-->", e.getErrorLocation().lineContent());
            assertEquals(8, e.getErrorLocation().line());
            assertEquals(18, e.getErrorLocation().column());
            assertEquals(3, e.getErrorLocation().marking());
            assertEquals(e.getTemplateName(), "error_valueshort_begin_badly_terminated");
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testErrorValueLongBeginTagBadlyTerminated() {
        try {
            parser_.parse("error_valuelong_begin_badly_terminated", null);
            fail();
        } catch (SyntaxErrorException e) {
            assertEquals("		<!--v VALUE1'   eff  --><!--/v-->", e.getErrorLocation().lineContent());
            assertEquals(8, e.getErrorLocation().line());
            assertEquals(18, e.getErrorLocation().column());
            assertEquals(3, e.getErrorLocation().marking());
            assertEquals(e.getTemplateName(), "error_valuelong_begin_badly_terminated");
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testErrorUnsupportedNestedValueTag() {
        try {
            parser_.parse("error_unsupported_nested_value_tag", null);
            fail();
        } catch (SyntaxErrorException e) {
            assertEquals("		<!--v VALUE2/-->", e.getErrorLocation().lineContent());
            assertEquals(9, e.getErrorLocation().line());
            assertEquals(2, e.getErrorLocation().column());
            assertEquals(5, e.getErrorLocation().marking());
            assertEquals(e.getTemplateName(), "error_unsupported_nested_value_tag");
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testErrorValueBeginTagNotEnded() {
        try {
            parser_.parse("error_value_begin_tag_not_ended", null);
            fail();
        } catch (SyntaxErrorException e) {
            assertEquals("" +
                "\t</body>", e.getErrorLocation().lineContent());
            assertEquals(9, e.getErrorLocation().line());
            assertEquals(4, e.getErrorLocation().column());
            assertEquals(3, e.getErrorLocation().marking());
            assertEquals(e.getTemplateName(), "error_value_begin_tag_not_ended");
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testErrorCommentMissingTerminationTag() {
        try {
            parser_.parse("error_comment_missing_termination_tag", null);
            fail();
        } catch (SyntaxErrorException e) {
            assertEquals("</html>", e.getErrorLocation().lineContent());
            assertEquals(10, e.getErrorLocation().line());
            assertEquals(7, e.getErrorLocation().column());
            assertEquals(1, e.getErrorLocation().marking());
            assertEquals(e.getTemplateName(), "error_comment_missing_termination_tag");
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testErrorBlockBeginTagNotEnded() {
        try {
            parser_.parse("error_block_begin_tag_not_ended", null);
            fail();
        } catch (SyntaxErrorException e) {
            assertEquals("\t</body>", e.getErrorLocation().lineContent());
            assertEquals(9, e.getErrorLocation().line());
            assertEquals(4, e.getErrorLocation().column());
            assertEquals(3, e.getErrorLocation().marking());
            assertEquals(e.getTemplateName(), "error_block_begin_tag_not_ended");
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testErrorBlockMissingTerminationTag() {
        try {
            parser_.parse("error_block_missing_termination_tag", null);
            fail();
        } catch (SyntaxErrorException e) {
            assertEquals("</html>", e.getErrorLocation().lineContent());
            assertEquals(10, e.getErrorLocation().line());
            assertEquals(7, e.getErrorLocation().column());
            assertEquals(1, e.getErrorLocation().marking());
            assertEquals(e.getTemplateName(), "error_block_missing_termination_tag");
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testErrorBlockMismatchedTerminationTag1() {
        try {
            parser_.parse("error_block_mismatched_termination_tag1", null);
            fail();
        } catch (SyntaxErrorException e) {
            assertEquals("		<!--b BLOCK1--><!--/bv-->", e.getErrorLocation().lineContent());
            assertEquals(8, e.getErrorLocation().line());
            assertEquals(17, e.getErrorLocation().column());
            assertEquals(10, e.getErrorLocation().marking());
            assertEquals(e.getTemplateName(), "error_block_mismatched_termination_tag1");
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testErrorBlockMismatchedTerminationTag2() {
        try {
            parser_.parse("error_block_mismatched_termination_tag2", null);
            fail();
        } catch (SyntaxErrorException e) {
            assertEquals("		<!--bv BLOCK2--><!--/b-->", e.getErrorLocation().lineContent());
            assertEquals(8, e.getErrorLocation().line());
            assertEquals(18, e.getErrorLocation().column());
            assertEquals(9, e.getErrorLocation().marking());
            assertEquals(e.getTemplateName(), "error_block_mismatched_termination_tag2");
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testErrorBlockMismatchedTerminationTag3() {
        try {
            parser_.parse("error_block_mismatched_termination_tag3", null);
            fail();
        } catch (SyntaxErrorException e) {
            assertEquals("		<!--bv BLOCK2-->", e.getErrorLocation().lineContent());
            assertEquals(8, e.getErrorLocation().line());
            assertEquals(18, e.getErrorLocation().column());
            assertEquals(1, e.getErrorLocation().marking());
            assertEquals(e.getTemplateName(), "error_block_mismatched_termination_tag3");
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testErrorBlockBeginTagBadlyTerminated() {
        try {
            parser_.parse("error_block_begin_tag_badly_terminated", null);
            fail();
        } catch (SyntaxErrorException e) {
            assertEquals("		<!--b BLOCK1' dfsdf -->  <!--/b-->", e.getErrorLocation().lineContent());
            assertEquals(8, e.getErrorLocation().line());
            assertEquals(16, e.getErrorLocation().column());
            assertEquals(5, e.getErrorLocation().marking());
            assertEquals(e.getTemplateName(), "error_block_begin_tag_badly_terminated");
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testErrorBlockValueBeginTagBadlyTerminated() {
        try {
            parser_.parse("error_blockvalue_begin_tag_badly_terminated", null);
            fail();
        } catch (SyntaxErrorException e) {
            assertEquals("		<!--bv BLOCK1' dfsdf -->  <!--/b-->", e.getErrorLocation().lineContent());
            assertEquals(8, e.getErrorLocation().line());
            assertEquals(17, e.getErrorLocation().column());
            assertEquals(5, e.getErrorLocation().marking());
            assertEquals(e.getTemplateName(), "error_blockvalue_begin_tag_badly_terminated");
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testErrorUnsupportedEncoding() {
        try {
            TemplateFactory.TXT.getParser().parse("encoding_nonlatin_utf8", "THIS_ENCODING_DOESNT_EXIST");
            fail();
        } catch (GetContentErrorException e) {
            assertTrue(e.getCause() instanceof rife.resources.exceptions.ResourceFinderErrorException);
            assertTrue(e.getCause().getCause() instanceof rife.tools.exceptions.FileUtilsErrorException);
            assertTrue(e.getCause().getCause().getCause() instanceof java.io.UnsupportedEncodingException);
        }
    }
}
