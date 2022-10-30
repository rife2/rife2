/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.test;

import org.junit.jupiter.api.Test;
import rife.engine.BeanImpl;
import rife.engine.FillBeanSite;
import rife.engine.GetBeanSite;
import rife.resources.ResourceFinderClasspath;
import rife.tools.SerializationUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestMocksBeans {
    @Test
    public void testParametersBean()
    throws Exception {
        var conversation = new MockConversation(new GetBeanSite());

        var response = conversation.doRequest("/bean/get");
        var parsed = response.getParsedHtml();
        var form = parsed.getFormWithName("submissionform");

        form.setParameter("enum", "WEDNESDAY");
        form.setParameter("string", "the string");
        form.setParameter("stringbuffer", "the stringbuffer");
        form.setParameter("int", "23154");
        form.setParameter("integer", "893749");
        form.setParameter("char", "u");
        form.setParameter("character", "R");
        form.setParameter("boolean", "y");
        form.setParameter("booleanObject", "no");
        form.setParameter("byte", "120");
        form.setParameter("byteObject", "21");
        form.setParameter("double", "34878.34");
        form.setParameter("doubleObject", "25435.98");
        form.setParameter("float", "3434.76");
        form.setParameter("floatObject", "6534.8");
        form.setParameter("long", "34347897");
        form.setParameter("longObject", "2335454");
        form.setParameter("short", "32");
        form.setParameter("shortObject", "12");
        form.setParameter("date", "2005-08-20 09:44");
        form.setParameter("dateFormatted", "Sat 20 Aug 2005 09:44:00");
        form.setParameter("datesFormatted", new String[]{"Sun 21 Aug 2005 11:06:14", "Mon 17 Jul 2006 16:05:31"});
        form.setParameter("serializableParam", SerializationUtils.serializeToString(new BeanImpl.SerializableParam(13, "Thirteen")));
        form.setParameter("serializableParams", new String[]{SerializationUtils.serializeToString(new BeanImpl.SerializableParam(9, "Nine")), SerializationUtils.serializeToString(new BeanImpl.SerializableParam(91, "NinetyOne"))});
        form.file("stringFile", new MockFileUpload("somedesign.html", new ByteArrayInputStream("this is some html content".getBytes(StandardCharsets.UTF_8)), "text/html"));
        form.file("streamFile", new MockFileUpload("uwyn.png", ResourceFinderClasspath.instance().getResource("uwyn.png").openStream(), "image/png"));
        form.file("bytesFile", new MockFileUpload("someimage.png", ResourceFinderClasspath.instance().getResource("uwyn.png").openStream(), "image/png"));

        assertEquals("WEDNESDAY,the string,the stringbuffer,23154,893749,u,null,true,false,0,21,34878.34,25435.98,3434.76,6534.8,34347897,2335454,32,12,this is some html content,true,someimage.png,true,Sat 20 Aug 2005 09:44:00,Sun 21 Aug 2005 11:06:14,Mon 17 Jul 2006 16:05:31,13:Thirteen,9:Nine,91:NinetyOne", form.submit().getText());

        response = conversation.doRequest("/bean/get");
        parsed = response.getParsedHtml();
        form = parsed.getFormWithName("submissionform");

        form.setParameter("enum", "invalid");
        form.setParameter("string", "the string");
        form.setParameter("stringbuffer", "the stringbuffer");
        form.setParameter("int", "23fd33");
        form.setParameter("char", "u");
        form.setParameter("character", "R");
        form.setParameter("boolean", "y");
        form.setParameter("booleanObject", "no");
        form.setParameter("byte", "120");
        form.setParameter("byteObject", "21");
        form.setParameter("double", "zef.34");
        form.setParameter("doubleObject", "25435.98");
        form.setParameter("float", "3434.76");
        form.setParameter("floatObject", "6534.8");
        form.setParameter("long", "34347897");
        form.setParameter("longObject", "233f5454");
        form.setParameter("short", "32");
        form.setParameter("shortObject", "");
        form.setParameter("datesFormatted", new String[]{"Sun 21 Aug 2005 11:06:14", "Mon 18 Jul 2006 16:05:31"});
        form.setParameter("serializableParam", "invalid");
        form.setParameter("serializableParams", new String[]{"invalid", SerializationUtils.serializeToString(new BeanImpl.SerializableParam(91, "NinetyOne"))});

        assertEquals("""
            INVALID : enum
            NOTNUMERIC : int
            NOTNUMERIC : double
            NOTNUMERIC : longObject
            INVALID : datesFormatted
            INVALID : serializableParam
            INVALID : serializableParams
            null,the string,the stringbuffer,0,null,u,null,true,false,0,21,0.0,25435.98,3434.76,6534.8,34347897,null,32,null,null,null,null,null,null,Sun 21 Aug 2005 11:06:14,null,null,null,91:NinetyOne""", form.submit().getText());
    }

    @Test
    public void testParametersBeanPrefix()
    throws Exception {
        var conversation = new MockConversation(new GetBeanSite("prefix_"));

        var response = conversation.doRequest("/bean/get");
        var parsed = response.getParsedHtml();
        var form = parsed.getFormWithName("submissionform");

        form.setParameter("prefix_enum", "WEDNESDAY");
        form.setParameter("prefix_string", "the string");
        form.setParameter("prefix_stringbuffer", "the stringbuffer");
        form.setParameter("prefix_int", "23154");
        form.setParameter("prefix_integer", "893749");
        form.setParameter("prefix_char", "u");
        form.setParameter("prefix_character", "R");
        form.setParameter("prefix_boolean", "y");
        form.setParameter("prefix_booleanObject", "no");
        form.setParameter("prefix_byte", "120");
        form.setParameter("prefix_byteObject", "21");
        form.setParameter("prefix_double", "34878.34");
        form.setParameter("prefix_doubleObject", "25435.98");
        form.setParameter("prefix_float", "3434.76");
        form.setParameter("prefix_floatObject", "6534.8");
        form.setParameter("prefix_long", "34347897");
        form.setParameter("prefix_longObject", "2335454");
        form.setParameter("prefix_short", "32");
        form.setParameter("prefix_shortObject", "12");
        form.setParameter("prefix_date", "2005-08-20 09:44");
        form.setParameter("prefix_dateFormatted", "Sat 20 Aug 2005 09:44:00");
        form.setParameter("prefix_datesFormatted", new String[]{"Sun 21 Aug 2005 11:06:14", "Mon 17 Jul 2006 16:05:31"});
        form.setParameter("prefix_serializableParam", SerializationUtils.serializeToString(new BeanImpl.SerializableParam(13, "Thirteen")));
        form.setParameter("prefix_serializableParams", new String[]{SerializationUtils.serializeToString(new BeanImpl.SerializableParam(9, "Nine")), SerializationUtils.serializeToString(new BeanImpl.SerializableParam(91, "NinetyOne"))});
        form.file("prefix_stringFile", new MockFileUpload("somedesign.html", new ByteArrayInputStream("this is some html content".getBytes(StandardCharsets.UTF_8)), "text/html"));
        form.file("prefix_streamFile", new MockFileUpload("uwyn.png", ResourceFinderClasspath.instance().getResource("uwyn.png").openStream(), "image/png"));
        form.file("prefix_bytesFile", new MockFileUpload("someimage.png", ResourceFinderClasspath.instance().getResource("uwyn.png").openStream(), "image/png"));

        assertEquals("WEDNESDAY,the string,the stringbuffer,23154,893749,u,null,true,false,0,21,34878.34,25435.98,3434.76,6534.8,34347897,2335454,32,12,this is some html content,true,someimage.png,true,Sat 20 Aug 2005 09:44:00,Sun 21 Aug 2005 11:06:14,Mon 17 Jul 2006 16:05:31,13:Thirteen,9:Nine,91:NinetyOne", form.submit().getText());

        response = conversation.doRequest("/bean/get");
        parsed = response.getParsedHtml();
        form = parsed.getFormWithName("submissionform");

        form.setParameter("prefix_enum", "invalid");
        form.setParameter("prefix_string", "the string");
        form.setParameter("prefix_stringbuffer", "the stringbuffer");
        form.setParameter("prefix_int", "23fd33");
        form.setParameter("prefix_char", "u");
        form.setParameter("prefix_character", "R");
        form.setParameter("prefix_boolean", "y");
        form.setParameter("prefix_booleanObject", "no");
        form.setParameter("prefix_byte", "120");
        form.setParameter("prefix_byteObject", "21");
        form.setParameter("prefix_double", "zef.34");
        form.setParameter("prefix_doubleObject", "25435.98");
        form.setParameter("prefix_float", "3434.76");
        form.setParameter("prefix_floatObject", "6534.8");
        form.setParameter("prefix_long", "34347897");
        form.setParameter("prefix_longObject", "233f5454");
        form.setParameter("prefix_short", "32");
        form.setParameter("prefix_shortObject", "");
        form.setParameter("prefix_datesFormatted", new String[]{"Sun 21 Aug 2005 11:06:14", "Mon 18 Jul 2006 16:05:31"});
        form.setParameter("prefix_serializableParam", "invalid");
        form.setParameter("prefix_serializableParams", new String[]{"invalid", SerializationUtils.serializeToString(new BeanImpl.SerializableParam(91, "NinetyOne"))});

        assertEquals("""
            INVALID : enum
            NOTNUMERIC : int
            NOTNUMERIC : double
            NOTNUMERIC : longObject
            INVALID : datesFormatted
            INVALID : serializableParam
            INVALID : serializableParams
            null,the string,the stringbuffer,0,null,u,null,true,false,0,21,0.0,25435.98,3434.76,6534.8,34347897,null,32,null,null,null,null,null,null,Sun 21 Aug 2005 11:06:14,null,null,null,91:NinetyOne""", form.submit().getText());
    }

    @Test
    public void testParametersBeanFill()
    throws Exception {
        var conversation = new MockConversation(new FillBeanSite());

        var response = conversation.doRequest("/bean/fill");
        var parsed = response.getParsedHtml();
        var form = parsed.getFormWithName("submissionform");

        form.setParameter("enum", "MONDAY");
        form.setParameter("string", "the string");
        form.setParameter("stringbuffer", "the stringbuffer");
        form.setParameter("int", "23154");
        form.setParameter("integer", "893749");
        form.setParameter("char", "u");
        form.setParameter("character", "R");
        form.setParameter("boolean", "y");
        form.setParameter("booleanObject", "no");
        form.setParameter("byte", "120");
        form.setParameter("byteObject", "21");
        form.setParameter("double", "34878.34");
        form.setParameter("doubleObject", "25435.98");
        form.setParameter("float", "3434.76");
        form.setParameter("floatObject", "6534.8");
        form.setParameter("long", "34347897");
        form.setParameter("longObject", "2335454");
        form.setParameter("short", "32");
        form.setParameter("shortObject", "12");
        form.setParameter("date", "2005-08-20 09:44");
        form.setParameter("dateFormatted", "Sat 20 Aug 2005 09:44:00");
        form.setParameter("datesFormatted", new String[]{"Sun 21 Aug 2005 11:06:14", "Mon 17 Jul 2006 16:05:31"});
        form.setParameter("serializableParam", SerializationUtils.serializeToString(new BeanImpl.SerializableParam(13, "Thirteen")));
        form.setParameter("serializableParams", new String[]{SerializationUtils.serializeToString(new BeanImpl.SerializableParam(9, "Nine")), SerializationUtils.serializeToString(new BeanImpl.SerializableParam(91, "NinetyOne"))});
        form.file("stringFile", new MockFileUpload("somedesign.html", new ByteArrayInputStream("this is some html content".getBytes(StandardCharsets.UTF_8)), "text/html"));
        form.file("streamFile", new MockFileUpload("uwyn.png", ResourceFinderClasspath.instance().getResource("uwyn.png").openStream(), "image/png"));
        form.file("bytesFile", new MockFileUpload("someimage.png", ResourceFinderClasspath.instance().getResource("uwyn.png").openStream(), "image/png"));

        assertEquals("MONDAY,the string,the stringbuffer,23154,893749,u,b,true,false,22,21,34878.34,25435.98,3434.76,6534.8,34347897,2335454,32,12,this is some html content,true,someimage.png,true,Sat 20 Aug 2005 09:44:00,Sun 21 Aug 2005 11:06:14,Mon 17 Jul 2006 16:05:31,13:Thirteen,9:Nine,91:NinetyOne", form.submit().getText());

        response = conversation.doRequest("/bean/fill");
        parsed = response.getParsedHtml();
        form = parsed.getFormWithName("submissionform");

        form.setParameter("enum", "invalid");
        form.setParameter("string", "the string");
        form.setParameter("stringbuffer", "the stringbuffer");
        form.setParameter("int", "23fd33");
        form.setParameter("char", "u");
        form.setParameter("character", "R");
        form.setParameter("boolean", "y");
        form.setParameter("booleanObject", "no");
        form.setParameter("byte", "120");
        form.setParameter("byteObject", "21");
        form.setParameter("double", "zef.34");
        form.setParameter("doubleObject", "25435.98");
        form.setParameter("float", "3434.76");
        form.setParameter("floatObject", "6534.8");
        form.setParameter("long", "34347897");
        form.setParameter("longObject", "233f5454");
        form.setParameter("short", "32");
        form.setParameter("shortObject", "");
        form.setParameter("datesFormatted", new String[]{"Sun 21 Aug 2005 11:06:14", "Mon 18 Jul 2006 16:05:31"});
        form.setParameter("serializableParam", "invalid");
        form.setParameter("serializableParams", new String[]{"invalid", SerializationUtils.serializeToString(new BeanImpl.SerializableParam(91, "NinetyOne"))});

        assertEquals("""
            INVALID : enum
            NOTNUMERIC : int
            NOTNUMERIC : double
            NOTNUMERIC : longObject
            INVALID : datesFormatted
            INVALID : serializableParam
            INVALID : serializableParams
            FRIDAY,the string,the stringbuffer,999,null,u,b,true,false,22,21,123.45,25435.98,3434.76,6534.8,34347897,55,32,null,stringFile,false,null,false,null,Sun 21 Aug 2005 11:06:14,null,null,null,91:NinetyOne""", form.submit().getText());
    }

    @Test
    public void testParametersBeanFillPrefix()
    throws Exception {
        var conversation = new MockConversation(new FillBeanSite("prefix_"));

        var response = conversation.doRequest("/bean/fill");
        var parsed = response.getParsedHtml();
        var form = parsed.getFormWithName("submissionform");

        form.setParameter("prefix_enum", "MONDAY");
        form.setParameter("prefix_string", "the string");
        form.setParameter("prefix_stringbuffer", "the stringbuffer");
        form.setParameter("prefix_int", "23154");
        form.setParameter("prefix_integer", "893749");
        form.setParameter("prefix_char", "u");
        form.setParameter("prefix_character", "R");
        form.setParameter("prefix_boolean", "y");
        form.setParameter("prefix_booleanObject", "no");
        form.setParameter("prefix_byte", "120");
        form.setParameter("prefix_byteObject", "21");
        form.setParameter("prefix_double", "34878.34");
        form.setParameter("prefix_doubleObject", "25435.98");
        form.setParameter("prefix_float", "3434.76");
        form.setParameter("prefix_floatObject", "6534.8");
        form.setParameter("prefix_long", "34347897");
        form.setParameter("prefix_longObject", "2335454");
        form.setParameter("prefix_short", "32");
        form.setParameter("prefix_shortObject", "12");
        form.setParameter("prefix_date", "2005-08-20 09:44");
        form.setParameter("prefix_dateFormatted", "Sat 20 Aug 2005 09:44:00");
        form.setParameter("prefix_datesFormatted", new String[]{"Sun 21 Aug 2005 11:06:14", "Mon 17 Jul 2006 16:05:31"});
        form.setParameter("prefix_serializableParam", SerializationUtils.serializeToString(new BeanImpl.SerializableParam(13, "Thirteen")));
        form.setParameter("prefix_serializableParams", new String[]{SerializationUtils.serializeToString(new BeanImpl.SerializableParam(9, "Nine")), SerializationUtils.serializeToString(new BeanImpl.SerializableParam(91, "NinetyOne"))});
        form.file("prefix_stringFile", new MockFileUpload("somedesign.html", new ByteArrayInputStream("this is some html content".getBytes(StandardCharsets.UTF_8)), "text/html"));
        form.file("prefix_streamFile", new MockFileUpload("uwyn.png", ResourceFinderClasspath.instance().getResource("uwyn.png").openStream(), "image/png"));
        form.file("prefix_bytesFile", new MockFileUpload("someimage.png", ResourceFinderClasspath.instance().getResource("uwyn.png").openStream(), "image/png"));

        assertEquals("MONDAY,the string,the stringbuffer,23154,893749,u,b,true,false,22,21,34878.34,25435.98,3434.76,6534.8,34347897,2335454,32,12,this is some html content,true,someimage.png,true,Sat 20 Aug 2005 09:44:00,Sun 21 Aug 2005 11:06:14,Mon 17 Jul 2006 16:05:31,13:Thirteen,9:Nine,91:NinetyOne", form.submit().getText());

        response = conversation.doRequest("/bean/fill");
        parsed = response.getParsedHtml();
        form = parsed.getFormWithName("submissionform");

        form.setParameter("prefix_enum", "invalid");
        form.setParameter("prefix_string", "the string");
        form.setParameter("prefix_stringbuffer", "the stringbuffer");
        form.setParameter("prefix_int", "23fd33");
        form.setParameter("prefix_char", "u");
        form.setParameter("prefix_character", "R");
        form.setParameter("prefix_boolean", "y");
        form.setParameter("prefix_booleanObject", "no");
        form.setParameter("prefix_byte", "120");
        form.setParameter("prefix_byteObject", "21");
        form.setParameter("prefix_double", "zef.34");
        form.setParameter("prefix_doubleObject", "25435.98");
        form.setParameter("prefix_float", "3434.76");
        form.setParameter("prefix_floatObject", "6534.8");
        form.setParameter("prefix_long", "34347897");
        form.setParameter("prefix_longObject", "233f5454");
        form.setParameter("prefix_short", "32");
        form.setParameter("prefix_shortObject", "");
        form.setParameter("prefix_datesFormatted", new String[]{"Sun 21 Aug 2005 11:06:14", "Mon 18 Jul 2006 16:05:31"});
        form.setParameter("prefix_serializableParam", "invalid");
        form.setParameter("prefix_serializableParams", new String[]{"invalid", SerializationUtils.serializeToString(new BeanImpl.SerializableParam(91, "NinetyOne"))});

        assertEquals("""
            INVALID : enum
            NOTNUMERIC : int
            NOTNUMERIC : double
            NOTNUMERIC : longObject
            INVALID : datesFormatted
            INVALID : serializableParam
            INVALID : serializableParams
            FRIDAY,the string,the stringbuffer,999,null,u,b,true,false,22,21,123.45,25435.98,3434.76,6534.8,34347897,55,32,null,stringFile,false,null,false,null,Sun 21 Aug 2005 11:06:14,null,null,null,91:NinetyOne""", form.submit().getText());
    }
}
