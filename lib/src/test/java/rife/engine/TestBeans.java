/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlFileInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Test;
import rife.resources.ResourceFinderClasspath;
import rife.tools.FileUtils;
import rife.tools.InnerClassException;
import rife.tools.InputStreamUser;
import rife.tools.SerializationUtils;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestBeans {
    @Test
    void testParametersBean()
    throws Exception {
        try (final var server = new TestServerRunner(new GetBeanSite())) {
            try (final var webClient = new WebClient()) {
                HtmlPage page;
                HtmlForm form;

                page = webClient.getPage("http://localhost:8181/bean/get");
                form = page.getFormByName("submissionform");
                form.getInputByName("enum").setValueAttribute("WEDNESDAY");
                form.getInputByName("string").setValueAttribute("the string");
                form.getInputByName("stringbuffer").setValueAttribute("the stringbuffer");
                form.getInputByName("int").setValueAttribute("23154");
                form.getInputByName("integer").setValueAttribute("893749");
                form.getInputByName("char").setValueAttribute("u");
                form.getInputByName("character").setValueAttribute("R");
                form.getInputByName("boolean").setValueAttribute("y");
                form.getInputByName("booleanObject").setValueAttribute("no");
                form.getInputByName("byte").setValueAttribute("120");
                form.getInputByName("byteObject").setValueAttribute("21");
                form.getInputByName("double").setValueAttribute("34878.34");
                form.getInputByName("doubleObject").setValueAttribute("25435.98");
                form.getInputByName("float").setValueAttribute("3434.76");
                form.getInputByName("floatObject").setValueAttribute("6534.8");
                form.getInputByName("long").setValueAttribute("34347897");
                form.getInputByName("longObject").setValueAttribute("2335454");
                form.getInputByName("short").setValueAttribute("32");
                form.getInputByName("shortObject").setValueAttribute("12");

                form.getInputByName("date").setValueAttribute("2005-08-20 09:44");
                form.getInputByName("dateFormatted").setValueAttribute("Sat 20 Aug 2005 09:44:00");
                var dates_formatted_inputs = form.getInputsByName("datesFormatted");
                dates_formatted_inputs.get(0).setValueAttribute("Sun 21 Aug 2005 11:06:14");
                dates_formatted_inputs.get(1).setValueAttribute("Mon 17 Jul 2006 16:05:31");

                form.getInputByName("instant").setValueAttribute("2006-08-20 08:44");
                form.getInputByName("instantFormatted").setValueAttribute("Sun 20 Aug 2006 08:44:00");
                var instants_formatted_inputs = form.getInputsByName("instantsFormatted");
                instants_formatted_inputs.get(0).setValueAttribute("Tue 21 Aug 2007 10:06:14");
                instants_formatted_inputs.get(1).setValueAttribute("Thu 17 Jul 2008 15:05:31");

                form.getInputByName("serializableParam").setValueAttribute(SerializationUtils.serializeToString(new BeanImpl.SerializableParam(13, "Thirteen")));
                var serializable_params_inputs = form.getInputsByName("serializableParams");
                serializable_params_inputs.get(0).setValueAttribute(SerializationUtils.serializeToString(new BeanImpl.SerializableParam(9, "Nine")));
                serializable_params_inputs.get(1).setValueAttribute(SerializationUtils.serializeToString(new BeanImpl.SerializableParam(91, "NinetyOne")));

                HtmlFileInput string_file = form.getInputByName("stringFile");
                string_file.setValueAttribute("somedesign.html");
                string_file.setContentType("text/html");
                string_file.setData("this is some html content".getBytes(StandardCharsets.UTF_8));

                byte[] image_bytes = ResourceFinderClasspath.instance().useStream("uwyn.png", new InputStreamUser<>() {
                    public byte[] useInputStream(InputStream stream)
                    throws InnerClassException {
                        try {
                            return FileUtils.readBytes(stream);
                        } catch (FileUtilsErrorException e) {
                            throwException(e);
                        }

                        return null;
                    }
                });
                HtmlFileInput bytesFile = form.getInputByName("bytesFile");
                bytesFile.setValueAttribute("someimage.png");
                bytesFile.setContentType("image/png");
                bytesFile.setData(image_bytes);

                HtmlFileInput streamFile = form.getInputByName("streamFile");
                streamFile.setValueAttribute("somefile.png");
                streamFile.setData(image_bytes);

                page = page.getHtmlElementById("beanSubmit").click();

                assertEquals("WEDNESDAY,the string,the stringbuffer,23154,893749,u,null,true,false,0,21,34878.34,25435.98,3434.76,6534.8,34347897,2335454,32,12,this is some html content,true,someimage.png,true," +
                             "Sat 20 Aug 2005 09:44:00,Sun 21 Aug 2005 11:06:14,Mon 17 Jul 2006 16:05:31," +
                             "Sun 20 Aug 2006 08:44:00,Tue 21 Aug 2007 10:06:14,Thu 17 Jul 2008 15:05:31," +
                             "13:Thirteen,9:Nine,91:NinetyOne", page.getWebResponse().getContentAsString());

                page = webClient.getPage("http://localhost:8181/bean/get");
                form = page.getFormByName("submissionform");
                form.getInputByName("enum").setValueAttribute("invalid");
                form.getInputByName("string").setValueAttribute("the string");
                form.getInputByName("stringbuffer").setValueAttribute("the stringbuffer");
                form.getInputByName("int").setValueAttribute("23fd33");
                form.getInputByName("char").setValueAttribute("u");
                form.getInputByName("character").setValueAttribute("R");
                form.getInputByName("boolean").setValueAttribute("y");
                form.getInputByName("booleanObject").setValueAttribute("no");
                form.getInputByName("byte").setValueAttribute("120");
                form.getInputByName("byteObject").setValueAttribute("21");
                form.getInputByName("double").setValueAttribute("zef.34");
                form.getInputByName("doubleObject").setValueAttribute("25435.98");
                form.getInputByName("float").setValueAttribute("3434.76");
                form.getInputByName("floatObject").setValueAttribute("6534.8");
                form.getInputByName("long").setValueAttribute("34347897");
                form.getInputByName("longObject").setValueAttribute("233f5454");
                form.getInputByName("short").setValueAttribute("32");
                form.getInputByName("shortObject").setValueAttribute("");

                dates_formatted_inputs = form.getInputsByName("datesFormatted");
                dates_formatted_inputs.get(0).setValueAttribute("Sun 21 Aug 2005 11:06:14");
                dates_formatted_inputs.get(1).setValueAttribute("Mon 18 Jul 2006 16:05:31");

                instants_formatted_inputs = form.getInputsByName("instantsFormatted");
                instants_formatted_inputs.get(0).setValueAttribute("Tue 21 Aug 2007 10:06:14");
                instants_formatted_inputs.get(1).setValueAttribute("Thu 16 Jul 2008 15:05:31");

                form.getInputByName("serializableParam").setValueAttribute("invalid");
                serializable_params_inputs = form.getInputsByName("serializableParams");
                serializable_params_inputs.get(0).setValueAttribute("invalid");
                serializable_params_inputs.get(1).setValueAttribute(SerializationUtils.serializeToString(new BeanImpl.SerializableParam(91, "NinetyOne")));

                page = page.getHtmlElementById("beanSubmit").click();

                assertEquals("""
                    invalid : enum
                    notNumeric : int
                    notNumeric : double
                    notNumeric : longObject
                    invalid : datesFormatted
                    invalid : instantsFormatted
                    invalid : serializableParam
                    invalid : serializableParams
                    null,the string,the stringbuffer,0,null,u,null,true,false,0,21,0.0,25435.98,3434.76,6534.8,34347897,null,32,null,,false,null,false,null,Sun 21 Aug 2005 11:06:14,null,null,Tue 21 Aug 2007 10:06:14,null,null,null,91:NinetyOne""", page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testParametersBeanPrefix()
    throws Exception {
        try (final var server = new TestServerRunner(new GetBeanSite("prefix_"))) {
            try (final var webClient = new WebClient()) {
                HtmlPage page;
                HtmlForm form;

                page = webClient.getPage("http://localhost:8181/bean/get");
                form = page.getFormByName("submissionform");
                form.getInputByName("prefix_enum").setValueAttribute("WEDNESDAY");
                form.getInputByName("prefix_string").setValueAttribute("the string");
                form.getInputByName("prefix_stringbuffer").setValueAttribute("the stringbuffer");
                form.getInputByName("prefix_int").setValueAttribute("23154");
                form.getInputByName("prefix_integer").setValueAttribute("893749");
                form.getInputByName("prefix_char").setValueAttribute("u");
                form.getInputByName("prefix_character").setValueAttribute("R");
                form.getInputByName("prefix_boolean").setValueAttribute("y");
                form.getInputByName("prefix_booleanObject").setValueAttribute("no");
                form.getInputByName("prefix_byte").setValueAttribute("120");
                form.getInputByName("prefix_byteObject").setValueAttribute("21");
                form.getInputByName("prefix_double").setValueAttribute("34878.34");
                form.getInputByName("prefix_doubleObject").setValueAttribute("25435.98");
                form.getInputByName("prefix_float").setValueAttribute("3434.76");
                form.getInputByName("prefix_floatObject").setValueAttribute("6534.8");
                form.getInputByName("prefix_long").setValueAttribute("34347897");
                form.getInputByName("prefix_longObject").setValueAttribute("2335454");
                form.getInputByName("prefix_short").setValueAttribute("32");
                form.getInputByName("prefix_shortObject").setValueAttribute("12");
                form.getInputByName("prefix_date").setValueAttribute("2005-08-20 09:44");
                form.getInputByName("prefix_dateFormatted").setValueAttribute("Sat 20 Aug 2005 09:44:00");
                var dates_formatted_inputs = form.getInputsByName("prefix_datesFormatted");
                dates_formatted_inputs.get(0).setValueAttribute("Sun 21 Aug 2005 11:06:14");
                dates_formatted_inputs.get(1).setValueAttribute("Mon 17 Jul 2006 16:05:31");
                form.getInputByName("prefix_instant").setValueAttribute("2006-08-20 08:44");
                form.getInputByName("prefix_instantFormatted").setValueAttribute("Sun 20 Aug 2006 08:44:00");
                var instants_formatted_inputs = form.getInputsByName("prefix_instantsFormatted");
                instants_formatted_inputs.get(0).setValueAttribute("Tue 21 Aug 2007 10:06:14");
                instants_formatted_inputs.get(1).setValueAttribute("Thu 17 Jul 2008 15:05:31");
                form.getInputByName("prefix_serializableParam").setValueAttribute(SerializationUtils.serializeToString(new BeanImpl.SerializableParam(13, "Thirteen")));
                var serializable_params_inputs = form.getInputsByName("prefix_serializableParams");
                serializable_params_inputs.get(0).setValueAttribute(SerializationUtils.serializeToString(new BeanImpl.SerializableParam(9, "Nine")));
                serializable_params_inputs.get(1).setValueAttribute(SerializationUtils.serializeToString(new BeanImpl.SerializableParam(91, "NinetyOne")));

                HtmlFileInput string_file = form.getInputByName("prefix_stringFile");
                string_file.setValueAttribute("somedesign.html");
                string_file.setContentType("text/html");
                string_file.setData("this is some html content".getBytes(StandardCharsets.UTF_8));

                byte[] image_bytes = ResourceFinderClasspath.instance().useStream("uwyn.png", new InputStreamUser<>() {
                    public byte[] useInputStream(InputStream stream)
                    throws InnerClassException {
                        try {
                            return FileUtils.readBytes(stream);
                        } catch (FileUtilsErrorException e) {
                            throwException(e);
                        }

                        return null;
                    }
                });
                HtmlFileInput bytesFile = form.getInputByName("prefix_bytesFile");
                bytesFile.setValueAttribute("someimage.png");
                bytesFile.setContentType("image/png");
                bytesFile.setData(image_bytes);

                HtmlFileInput streamFile = form.getInputByName("prefix_streamFile");
                streamFile.setValueAttribute("somefile.png");
                streamFile.setData(image_bytes);

                page = page.getHtmlElementById("beanSubmit").click();

                assertEquals("WEDNESDAY,the string,the stringbuffer,23154,893749,u,null,true,false,0,21,34878.34,25435.98,3434.76,6534.8,34347897,2335454,32,12,this is some html content,true,someimage.png,true," +
                             "Sat 20 Aug 2005 09:44:00,Sun 21 Aug 2005 11:06:14,Mon 17 Jul 2006 16:05:31," +
                             "Sun 20 Aug 2006 08:44:00,Tue 21 Aug 2007 10:06:14,Thu 17 Jul 2008 15:05:31," +
                             "13:Thirteen,9:Nine,91:NinetyOne", page.getWebResponse().getContentAsString());

                page = webClient.getPage("http://localhost:8181/bean/get");
                form = page.getFormByName("submissionform");
                form.getInputByName("prefix_enum").setValueAttribute("invalid");
                form.getInputByName("prefix_string").setValueAttribute("the string");
                form.getInputByName("prefix_stringbuffer").setValueAttribute("the stringbuffer");
                form.getInputByName("prefix_int").setValueAttribute("23fd33");
                form.getInputByName("prefix_char").setValueAttribute("u");
                form.getInputByName("prefix_character").setValueAttribute("R");
                form.getInputByName("prefix_boolean").setValueAttribute("y");
                form.getInputByName("prefix_booleanObject").setValueAttribute("no");
                form.getInputByName("prefix_byte").setValueAttribute("120");
                form.getInputByName("prefix_byteObject").setValueAttribute("21");
                form.getInputByName("prefix_double").setValueAttribute("zef.34");
                form.getInputByName("prefix_doubleObject").setValueAttribute("25435.98");
                form.getInputByName("prefix_float").setValueAttribute("3434.76");
                form.getInputByName("prefix_floatObject").setValueAttribute("6534.8");
                form.getInputByName("prefix_long").setValueAttribute("34347897");
                form.getInputByName("prefix_longObject").setValueAttribute("233f5454");
                form.getInputByName("prefix_short").setValueAttribute("32");
                form.getInputByName("prefix_shortObject").setValueAttribute("");

                dates_formatted_inputs = form.getInputsByName("prefix_datesFormatted");
                dates_formatted_inputs.get(0).setValueAttribute("Sun 21 Aug 2005 11:06:14");
                dates_formatted_inputs.get(1).setValueAttribute("Mon 18 Jul 2006 16:05:31");

                instants_formatted_inputs = form.getInputsByName("prefix_instantsFormatted");
                instants_formatted_inputs.get(0).setValueAttribute("Tue 21 Aug 2007 10:06:14");
                instants_formatted_inputs.get(1).setValueAttribute("Thu 16 Jul 2008 15:05:31");

                form.getInputByName("prefix_serializableParam").setValueAttribute("invalid");
                serializable_params_inputs = form.getInputsByName("prefix_serializableParams");
                serializable_params_inputs.get(0).setValueAttribute("invalid");
                serializable_params_inputs.get(1).setValueAttribute(SerializationUtils.serializeToString(new BeanImpl.SerializableParam(91, "NinetyOne")));

                page = page.getHtmlElementById("beanSubmit").click();

                assertEquals("""
                    invalid : enum
                    notNumeric : int
                    notNumeric : double
                    notNumeric : longObject
                    invalid : datesFormatted
                    invalid : instantsFormatted
                    invalid : serializableParam
                    invalid : serializableParams
                    null,the string,the stringbuffer,0,null,u,null,true,false,0,21,0.0,25435.98,3434.76,6534.8,34347897,null,32,null,,false,null,false,null,Sun 21 Aug 2005 11:06:14,null,null,Tue 21 Aug 2007 10:06:14,null,null,null,91:NinetyOne""", page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testParametersBeanFill()
    throws Exception {
        try (final var server = new TestServerRunner(new FillBeanSite())) {
            try (final var webClient = new WebClient()) {
                HtmlPage page;
                HtmlForm form;

                page = webClient.getPage("http://localhost:8181/bean/fill");
                form = page.getFormByName("submissionform");

                form.getInputByName("enum").setValueAttribute("MONDAY");
                form.getInputByName("string").setValueAttribute("the string");
                form.getInputByName("stringbuffer").setValueAttribute("the stringbuffer");
                form.getInputByName("int").setValueAttribute("23154");
                form.getInputByName("integer").setValueAttribute("893749");
                form.getInputByName("char").setValueAttribute("u");
                form.getInputByName("character").setValueAttribute("R");
                form.getInputByName("boolean").setValueAttribute("y");
                form.getInputByName("booleanObject").setValueAttribute("no");
                form.getInputByName("byte").setValueAttribute("120");
                form.getInputByName("byteObject").setValueAttribute("21");
                form.getInputByName("double").setValueAttribute("34878.34");
                form.getInputByName("doubleObject").setValueAttribute("25435.98");
                form.getInputByName("float").setValueAttribute("3434.76");
                form.getInputByName("floatObject").setValueAttribute("6534.8");
                form.getInputByName("long").setValueAttribute("34347897");
                form.getInputByName("longObject").setValueAttribute("2335454");
                form.getInputByName("short").setValueAttribute("32");
                form.getInputByName("shortObject").setValueAttribute("12");

                form.getInputByName("date").setValueAttribute("2005-08-20 09:44");
                form.getInputByName("dateFormatted").setValueAttribute("Sat 20 Aug 2005 09:44:00");
                var dates_formatted_inputs = form.getInputsByName("datesFormatted");
                dates_formatted_inputs.get(0).setValueAttribute("Sun 21 Aug 2005 11:06:14");
                dates_formatted_inputs.get(1).setValueAttribute("Mon 17 Jul 2006 16:05:31");

                form.getInputByName("instant").setValueAttribute("2006-08-20 08:44");
                form.getInputByName("instantFormatted").setValueAttribute("Sun 20 Aug 2006 08:44:00");
                var instants_formatted_inputs = form.getInputsByName("instantsFormatted");
                instants_formatted_inputs.get(0).setValueAttribute("Tue 21 Aug 2007 10:06:14");
                instants_formatted_inputs.get(1).setValueAttribute("Thu 17 Jul 2008 15:05:31");

                form.getInputByName("serializableParam").setValueAttribute(SerializationUtils.serializeToString(new BeanImpl.SerializableParam(13, "Thirteen")));
                var serializable_params_inputs = form.getInputsByName("serializableParams");
                serializable_params_inputs.get(0).setValueAttribute(SerializationUtils.serializeToString(new BeanImpl.SerializableParam(9, "Nine")));
                serializable_params_inputs.get(1).setValueAttribute(SerializationUtils.serializeToString(new BeanImpl.SerializableParam(91, "NinetyOne")));

                HtmlFileInput string_file = form.getInputByName("stringFile");
                string_file.setValueAttribute("somedesign.html");
                string_file.setContentType("text/html");
                string_file.setData("this is some html content".getBytes(StandardCharsets.UTF_8));

                byte[] image_bytes = ResourceFinderClasspath.instance().useStream("uwyn.png", new InputStreamUser<>() {
                    public byte[] useInputStream(InputStream stream)
                    throws InnerClassException {
                        try {
                            return FileUtils.readBytes(stream);
                        } catch (FileUtilsErrorException e) {
                            throwException(e);
                        }

                        return null;
                    }
                });
                HtmlFileInput bytesFile = form.getInputByName("bytesFile");
                bytesFile.setValueAttribute("someimage.png");
                bytesFile.setContentType("image/png");
                bytesFile.setData(image_bytes);

                HtmlFileInput streamFile = form.getInputByName("streamFile");
                streamFile.setValueAttribute("somefile.png");
                streamFile.setData(image_bytes);

                page = page.getHtmlElementById("beanSubmit").click();

                assertEquals("MONDAY,the string,the stringbuffer,23154,893749,u,b,true,false,22,21,34878.34,25435.98,3434.76,6534.8,34347897,2335454,32,12,this is some html content,true,someimage.png,true," +
                    "Sat 20 Aug 2005 09:44:00,Sun 21 Aug 2005 11:06:14,Mon 17 Jul 2006 16:05:31," +
                    "Sun 20 Aug 2006 08:44:00,Tue 21 Aug 2007 10:06:14,Thu 17 Jul 2008 15:05:31," +
                    "13:Thirteen,9:Nine,91:NinetyOne", page.getWebResponse().getContentAsString());

                page = webClient.getPage("http://localhost:8181/bean/fill");
                form = page.getFormByName("submissionform");
                form.getInputByName("enum").setValueAttribute("invalid");
                form.getInputByName("string").setValueAttribute("the string");
                form.getInputByName("boolean").setValueAttribute("y");
                form.getInputByName("string").setValueAttribute("the string");
                form.getInputByName("stringbuffer").setValueAttribute("the stringbuffer");
                form.getInputByName("int").setValueAttribute("23fd33");
                form.getInputByName("char").setValueAttribute("u");
                form.getInputByName("character").setValueAttribute("R");
                form.getInputByName("boolean").setValueAttribute("y");
                form.getInputByName("booleanObject").setValueAttribute("no");
                form.getInputByName("byte").setValueAttribute("120");
                form.getInputByName("byteObject").setValueAttribute("21");
                form.getInputByName("double").setValueAttribute("zef.34");
                form.getInputByName("doubleObject").setValueAttribute("25435.98");
                form.getInputByName("float").setValueAttribute("3434.76");
                form.getInputByName("floatObject").setValueAttribute("6534.8");
                form.getInputByName("long").setValueAttribute("34347897");
                form.getInputByName("longObject").setValueAttribute("233f5454");
                form.getInputByName("short").setValueAttribute("32");
                form.getInputByName("shortObject").setValueAttribute("");

                dates_formatted_inputs = form.getInputsByName("datesFormatted");
                dates_formatted_inputs.get(0).setValueAttribute("Sun 21 Aug 2005 11:06:14");
                dates_formatted_inputs.get(1).setValueAttribute("Mon 18 Jul 2006 16:05:31");

                instants_formatted_inputs = form.getInputsByName("instantsFormatted");
                instants_formatted_inputs.get(0).setValueAttribute("Tue 21 Aug 2007 10:06:14");
                instants_formatted_inputs.get(1).setValueAttribute("Thu 16 Jul 2008 15:05:31");

                form.getInputByName("serializableParam").setValueAttribute("invalid");
                serializable_params_inputs = form.getInputsByName("serializableParams");
                serializable_params_inputs.get(0).setValueAttribute("invalid");
                serializable_params_inputs.get(1).setValueAttribute(SerializationUtils.serializeToString(new BeanImpl.SerializableParam(91, "NinetyOne")));

                page = page.getHtmlElementById("beanSubmit").click();

                assertEquals("""
                    invalid : enum
                    notNumeric : int
                    notNumeric : double
                    notNumeric : longObject
                    invalid : datesFormatted
                    invalid : instantsFormatted
                    invalid : serializableParam
                    invalid : serializableParams
                    FRIDAY,the string,the stringbuffer,999,null,u,b,true,false,22,21,123.45,25435.98,3434.76,6534.8,34347897,55,32,null,,false,null,false,null,Sun 21 Aug 2005 11:06:14,null,null,Tue 21 Aug 2007 10:06:14,null,null,null,91:NinetyOne""", page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testParametersBeanFillPrefix()
    throws Exception {
        try (final var server = new TestServerRunner(new FillBeanSite("prefix_"))) {
            try (final var webClient = new WebClient()) {
                HtmlPage page;
                HtmlForm form;

                page = webClient.getPage("http://localhost:8181/bean/fill");
                form = page.getFormByName("submissionform");

                form.getInputByName("prefix_enum").setValueAttribute("MONDAY");
                form.getInputByName("prefix_string").setValueAttribute("the string");
                form.getInputByName("prefix_stringbuffer").setValueAttribute("the stringbuffer");
                form.getInputByName("prefix_int").setValueAttribute("23154");
                form.getInputByName("prefix_integer").setValueAttribute("893749");
                form.getInputByName("prefix_char").setValueAttribute("u");
                form.getInputByName("prefix_character").setValueAttribute("R");
                form.getInputByName("prefix_boolean").setValueAttribute("y");
                form.getInputByName("prefix_booleanObject").setValueAttribute("no");
                form.getInputByName("prefix_byte").setValueAttribute("120");
                form.getInputByName("prefix_byteObject").setValueAttribute("21");
                form.getInputByName("prefix_double").setValueAttribute("34878.34");
                form.getInputByName("prefix_doubleObject").setValueAttribute("25435.98");
                form.getInputByName("prefix_float").setValueAttribute("3434.76");
                form.getInputByName("prefix_floatObject").setValueAttribute("6534.8");
                form.getInputByName("prefix_long").setValueAttribute("34347897");
                form.getInputByName("prefix_longObject").setValueAttribute("2335454");
                form.getInputByName("prefix_short").setValueAttribute("32");
                form.getInputByName("prefix_shortObject").setValueAttribute("12");

                form.getInputByName("prefix_date").setValueAttribute("2005-08-20 09:44");
                form.getInputByName("prefix_dateFormatted").setValueAttribute("Sat 20 Aug 2005 09:44:00");
                var dates_formatted_inputs = form.getInputsByName("prefix_datesFormatted");
                dates_formatted_inputs.get(0).setValueAttribute("Sun 21 Aug 2005 11:06:14");
                dates_formatted_inputs.get(1).setValueAttribute("Mon 17 Jul 2006 16:05:31");

                form.getInputByName("prefix_instant").setValueAttribute("2006-08-20 08:44");
                form.getInputByName("prefix_instantFormatted").setValueAttribute("Sun 20 Aug 2006 08:44:00");
                var instants_formatted_inputs = form.getInputsByName("prefix_instantsFormatted");
                instants_formatted_inputs.get(0).setValueAttribute("Tue 21 Aug 2007 10:06:14");
                instants_formatted_inputs.get(1).setValueAttribute("Thu 17 Jul 2008 15:05:31");

                form.getInputByName("prefix_serializableParam").setValueAttribute(SerializationUtils.serializeToString(new BeanImpl.SerializableParam(13, "Thirteen")));
                var serializable_params_inputs = form.getInputsByName("prefix_serializableParams");
                serializable_params_inputs.get(0).setValueAttribute(SerializationUtils.serializeToString(new BeanImpl.SerializableParam(9, "Nine")));
                serializable_params_inputs.get(1).setValueAttribute(SerializationUtils.serializeToString(new BeanImpl.SerializableParam(91, "NinetyOne")));

                HtmlFileInput string_file = form.getInputByName("prefix_stringFile");
                string_file.setValueAttribute("somedesign.html");
                string_file.setContentType("text/html");
                string_file.setData("this is some html content".getBytes(StandardCharsets.UTF_8));

                byte[] image_bytes = ResourceFinderClasspath.instance().useStream("uwyn.png", new InputStreamUser<>() {
                    public byte[] useInputStream(InputStream stream)
                    throws InnerClassException {
                        try {
                            return FileUtils.readBytes(stream);
                        } catch (FileUtilsErrorException e) {
                            throwException(e);
                        }

                        return null;
                    }
                });
                HtmlFileInput bytesFile = form.getInputByName("prefix_bytesFile");
                bytesFile.setValueAttribute("someimage.png");
                bytesFile.setContentType("image/png");
                bytesFile.setData(image_bytes);

                HtmlFileInput streamFile = form.getInputByName("prefix_streamFile");
                streamFile.setValueAttribute("somefile.png");
                streamFile.setData(image_bytes);

                page = page.getHtmlElementById("beanSubmit").click();

                assertEquals("MONDAY,the string,the stringbuffer,23154,893749,u,b,true,false,22,21,34878.34,25435.98,3434.76,6534.8,34347897,2335454,32,12,this is some html content,true,someimage.png,true," +
                             "Sat 20 Aug 2005 09:44:00,Sun 21 Aug 2005 11:06:14,Mon 17 Jul 2006 16:05:31," +
                             "Sun 20 Aug 2006 08:44:00,Tue 21 Aug 2007 10:06:14,Thu 17 Jul 2008 15:05:31," +
                             "13:Thirteen,9:Nine,91:NinetyOne", page.getWebResponse().getContentAsString());

                page = webClient.getPage("http://localhost:8181/bean/fill");
                form = page.getFormByName("submissionform");
                form.getInputByName("prefix_enum").setValueAttribute("invalid");
                form.getInputByName("prefix_string").setValueAttribute("the string");
                form.getInputByName("prefix_boolean").setValueAttribute("y");
                form.getInputByName("prefix_string").setValueAttribute("the string");
                form.getInputByName("prefix_stringbuffer").setValueAttribute("the stringbuffer");
                form.getInputByName("prefix_int").setValueAttribute("23fd33");
                form.getInputByName("prefix_char").setValueAttribute("u");
                form.getInputByName("prefix_character").setValueAttribute("R");
                form.getInputByName("prefix_boolean").setValueAttribute("y");
                form.getInputByName("prefix_booleanObject").setValueAttribute("no");
                form.getInputByName("prefix_byte").setValueAttribute("120");
                form.getInputByName("prefix_byteObject").setValueAttribute("21");
                form.getInputByName("prefix_double").setValueAttribute("zef.34");
                form.getInputByName("prefix_doubleObject").setValueAttribute("25435.98");
                form.getInputByName("prefix_float").setValueAttribute("3434.76");
                form.getInputByName("prefix_floatObject").setValueAttribute("6534.8");
                form.getInputByName("prefix_long").setValueAttribute("34347897");
                form.getInputByName("prefix_longObject").setValueAttribute("233f5454");
                form.getInputByName("prefix_short").setValueAttribute("32");
                form.getInputByName("prefix_shortObject").setValueAttribute("");

                dates_formatted_inputs = form.getInputsByName("prefix_datesFormatted");
                dates_formatted_inputs.get(0).setValueAttribute("Sun 21 Aug 2005 11:06:14");
                dates_formatted_inputs.get(1).setValueAttribute("Mon 18 Jul 2006 16:05:31");

                instants_formatted_inputs = form.getInputsByName("prefix_instantsFormatted");
                instants_formatted_inputs.get(0).setValueAttribute("Tue 21 Aug 2007 10:06:14");
                instants_formatted_inputs.get(1).setValueAttribute("Thu 16 Jul 2008 15:05:31");

                form.getInputByName("prefix_serializableParam").setValueAttribute("invalid");
                serializable_params_inputs = form.getInputsByName("prefix_serializableParams");
                serializable_params_inputs.get(0).setValueAttribute("invalid");
                serializable_params_inputs.get(1).setValueAttribute(SerializationUtils.serializeToString(new BeanImpl.SerializableParam(91, "NinetyOne")));

                page = page.getHtmlElementById("beanSubmit").click();

                assertEquals("""
                    invalid : enum
                    notNumeric : int
                    notNumeric : double
                    notNumeric : longObject
                    invalid : datesFormatted
                    invalid : instantsFormatted
                    invalid : serializableParam
                    invalid : serializableParams
                    FRIDAY,the string,the stringbuffer,999,null,u,b,true,false,22,21,123.45,25435.98,3434.76,6534.8,34347897,55,32,null,,false,null,false,null,Sun 21 Aug 2005 11:06:14,null,null,Tue 21 Aug 2007 10:06:14,null,null,null,91:NinetyOne""", page.getWebResponse().getContentAsString());
            }
        }
    }

}