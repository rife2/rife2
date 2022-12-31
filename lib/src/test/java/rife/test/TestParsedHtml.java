/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.test;

import org.junit.jupiter.api.Test;
import rife.tools.FileUtils;
import rife.tools.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestParsedHtml {
    @Test
    void testTitle()
    throws Exception {
        String text = FileUtils.readString(getClass().getClassLoader().getResource("parsed_html.html"), "UTF-8");
        ParsedHtml parsed = ParsedHtml.parse(null, text);
        assertEquals("This is the title", parsed.getTitle());
    }

    @Test
    void testLinks()
    throws Exception {
        String text = FileUtils.readString(getClass().getClassLoader().getResource("parsed_html.html"), "UTF-8");
        ParsedHtml parsed = ParsedHtml.parse(null, text);
        List<MockLink> links = parsed.getLinks();
        assertEquals(2, links.size());

        MockLink link1 = links.get(0);
        assertEquals(0, link1.getParameters().size());
        assertEquals(0, link1.getParameterNames().size());
        assertEquals("link1", link1.getText());
        assertEquals("/link/url/1", link1.getHref());
        assertEquals("linkid1", link1.getId());
        assertEquals("linkclass1", link1.getClassName());
        assertEquals("linktitle1", link1.getTitle());
        assertEquals("linktarget1", link1.getTarget());
        assertEquals("linkname1", link1.getName());

        MockLink link2 = links.get(1);
        assertEquals(2, link2.getParameters().size());
        assertEquals(2, link2.getParameterNames().size());
        assertTrue(link2.hasParameter("param1"));
        assertTrue(link2.hasParameter("param2"));
        assertEquals("value1", link2.getParameterValue("param1"));
        assertEquals("value2", link2.getParameterValue("param2"));
        assertNull(link2.getParameterValue("unknown"));
        assertEquals("value1,value3", StringUtils.join(link2.getParameterValues("param1"), ","));
        assertEquals("value2", StringUtils.join(link2.getParameterValues("param2"), ","));
        assertNull(link2.getParameterValues("unknown"));
        assertEquals("", link2.getText());
        assertEquals("/link/url/2?param1=value1&param2=value2&param1=value3", link2.getHref());
        assertEquals("linkid2", link2.getId());
        assertEquals("linkclass2", link2.getClassName());
        assertEquals("linktitle2", link2.getTitle());
        assertEquals("linktarget2", link2.getTarget());
        assertEquals("linkname2", link2.getName());

        assertSame(link1, parsed.getLinkWithId("linkid1"));
        assertSame(link1, parsed.getLinkWithName("linkname1"));
        assertSame(link1, parsed.getLinkWithText("link1"));

        assertSame(link2, parsed.getLinkWithId("linkid2"));
        assertSame(link2, parsed.getLinkWithName("linkname2"));
        assertSame(link2, parsed.getLinkWithText(""));
        assertSame(link2, parsed.getLinkWithImageAlt("imagealt2"));
        assertSame(link2, parsed.getLinkWithImageName("imagename2"));
    }

    @Test
    void testUnknownLinks()
    throws Exception {
        String text = FileUtils.readString(getClass().getClassLoader().getResource("parsed_html.html"), "UTF-8");
        ParsedHtml parsed = ParsedHtml.parse(null, text);

        assertNull(parsed.getLinkWithId("unknown"));
        assertNull(parsed.getLinkWithName("unknown"));
        assertNull(parsed.getLinkWithText("unknown"));
        assertNull(parsed.getLinkWithImageAlt("unknown"));
        assertNull(parsed.getLinkWithImageName("unknown"));
    }

    @Test
    void testForms()
    throws Exception {
        String text = FileUtils.readString(getClass().getClassLoader().getResource("parsed_html.html"), "UTF-8");
        ParsedHtml parsed = ParsedHtml.parse(null, text);
        List<MockForm> forms = parsed.getForms();
        assertEquals(2, forms.size());

        MockForm form1 = forms.get(0);
        assertEquals("/form/url/1", form1.getAction());
        assertEquals("POST", form1.getMethod());
        assertEquals("formid1", form1.getId());
        assertEquals("formclass1", form1.getClassName());
        assertEquals("formtitle1", form1.getTitle());
        assertEquals("formname1", form1.getName());

        MockForm form2 = forms.get(1);
        assertEquals("/form/url/2", form2.getAction());
        assertEquals("GET", form2.getMethod());
        assertEquals("formid2", form2.getId());
        assertEquals("formclass2", form2.getClassName());
        assertEquals("formtitle2", form2.getTitle());
        assertEquals("formname2", form2.getName());

        assertSame(form1, parsed.getFormWithId("formid1"));
        assertSame(form1, parsed.getFormWithName("formname1"));

        assertSame(form2, parsed.getFormWithId("formid2"));
        assertSame(form2, parsed.getFormWithName("formname2"));
    }

    @Test
    void testFormParameters()
    throws Exception {
        String text = FileUtils.readString(getClass().getClassLoader().getResource("parsed_html.html"), "UTF-8");
        ParsedHtml parsed = ParsedHtml.parse(null, text);
        List<MockForm> forms = parsed.getForms();
        assertEquals(2, forms.size());

        MockForm form1 = forms.get(0);

        Map<String, String[]> params = form1.getParameters();
        assertEquals(16, params.size());
        assertTrue(params.containsKey("paramhidden1"));
        assertTrue(params.containsKey("paramhidden2"));
        assertTrue(params.containsKey("paramtext1"));
        assertTrue(params.containsKey("paramtext2"));
        assertTrue(params.containsKey("parampassword1"));
        assertTrue(params.containsKey("parampassword2"));
        assertTrue(params.containsKey("paramcheckbox2"));
        assertTrue(params.containsKey("paramcheckbox3"));
        assertTrue(params.containsKey("paramcheckbox4"));
        assertTrue(params.containsKey("paramradio2"));
        assertTrue(params.containsKey("paramradio3"));
        assertTrue(params.containsKey("textarea1"));
        assertTrue(params.containsKey("textarea2"));
        assertTrue(params.containsKey("select1"));
        assertTrue(params.containsKey("select2"));
        assertTrue(params.containsKey("select3"));
    }

    @Test
    void testFormParameterNames()
    throws Exception {
        String text = FileUtils.readString(getClass().getClassLoader().getResource("parsed_html.html"), "UTF-8");
        ParsedHtml parsed = ParsedHtml.parse(null, text);
        List<MockForm> forms = parsed.getForms();
        assertEquals(2, forms.size());

        MockForm form1 = forms.get(0);

        Collection<String> paramvalues = form1.getParameterNames();
        assertEquals(16, paramvalues.size());
        assertTrue(paramvalues.contains("paramhidden1"));
        assertTrue(paramvalues.contains("paramhidden2"));
        assertTrue(paramvalues.contains("paramtext1"));
        assertTrue(paramvalues.contains("paramtext2"));
        assertTrue(paramvalues.contains("parampassword1"));
        assertTrue(paramvalues.contains("parampassword2"));
        assertTrue(paramvalues.contains("paramcheckbox2"));
        assertTrue(paramvalues.contains("paramcheckbox3"));
        assertTrue(paramvalues.contains("paramcheckbox4"));
        assertTrue(paramvalues.contains("paramradio2"));
        assertTrue(paramvalues.contains("paramradio3"));
        assertTrue(paramvalues.contains("textarea1"));
        assertTrue(paramvalues.contains("textarea2"));
        assertTrue(paramvalues.contains("select1"));
        assertTrue(paramvalues.contains("select2"));
        assertTrue(paramvalues.contains("select3"));
    }

    @Test
    void testFormDefaultValues()
    throws Exception {
        String text = FileUtils.readString(getClass().getClassLoader().getResource("parsed_html.html"), "UTF-8");
        ParsedHtml parsed = ParsedHtml.parse(null, text);
        List<MockForm> forms = parsed.getForms();
        assertEquals(2, forms.size());

        MockForm form1 = forms.get(0);

        Map<String, String[]> params = form1.getParameters();

        assertEquals("", StringUtils.join(params.get("paramhidden1"), ","));
        assertEquals("paramhidden2avalue,paramhidden2bvalue", StringUtils.join(params.get("paramhidden2"), ","));
        assertEquals("", StringUtils.join(params.get("paramtext1"), ","));
        assertEquals("paramtext2avalue,paramtext2bvalue", StringUtils.join(params.get("paramtext2"), ","));
        assertEquals("", StringUtils.join(params.get("parampassword1"), ","));
        assertEquals("parampassword2avalue,parampassword2bvalue", StringUtils.join(params.get("parampassword2"), ","));
        assertEquals("on", StringUtils.join(params.get("paramcheckbox2"), ","));
        assertEquals("paramcheckbox3value", StringUtils.join(params.get("paramcheckbox3"), ","));
        assertEquals("on,paramcheckbox4value", StringUtils.join(params.get("paramcheckbox4"), ","));
        assertEquals("on", StringUtils.join(params.get("paramradio2"), ","));
        assertEquals("paramradio3bvalue", StringUtils.join(params.get("paramradio3"), ","));
        assertEquals("", StringUtils.join(params.get("textarea1"), ","));
        assertEquals("paramtextarea2avalue,paramtextarea2bvalue", StringUtils.join(params.get("textarea2"), ","));
        assertEquals("select1option1value,select1option3,select1option2", StringUtils.join(params.get("select1"), ","));
        assertEquals("select2option3", StringUtils.join(params.get("select2"), ","));
        assertEquals("select3option1", StringUtils.join(params.get("select3"), ","));
    }

    @Test
    void testFormParameterValue()
    throws Exception {
        String text = FileUtils.readString(getClass().getClassLoader().getResource("parsed_html.html"), "UTF-8");
        ParsedHtml parsed = ParsedHtml.parse(null, text);
        List<MockForm> forms = parsed.getForms();
        assertEquals(2, forms.size());

        MockForm form1 = forms.get(0);

        assertEquals("", form1.getParameterValue("paramhidden1"));
        assertEquals("paramhidden2avalue", form1.getParameterValue("paramhidden2"));
        assertEquals("", form1.getParameterValue("paramtext1"));
        assertEquals("paramtext2avalue", form1.getParameterValue("paramtext2"));
        assertEquals("", form1.getParameterValue("parampassword1"));
        assertEquals("parampassword2avalue", form1.getParameterValue("parampassword2"));
        assertEquals("on", form1.getParameterValue("paramcheckbox2"));
        assertEquals("paramcheckbox3value", form1.getParameterValue("paramcheckbox3"));
        assertEquals("on", form1.getParameterValue("paramcheckbox4"));
        assertEquals("on", form1.getParameterValue("paramradio2"));
        assertEquals("paramradio3bvalue", form1.getParameterValue("paramradio3"));
        assertEquals("", form1.getParameterValue("textarea1"));
        assertEquals("paramtextarea2avalue", form1.getParameterValue("textarea2"));
        assertEquals("select1option1value", form1.getParameterValue("select1"));
        assertEquals("select2option3", form1.getParameterValue("select2"));
        assertEquals("select3option1", form1.getParameterValue("select3"));
    }

    @Test
    void testFormParameterValues()
    throws Exception {
        String text = FileUtils.readString(getClass().getClassLoader().getResource("parsed_html.html"), "UTF-8");
        ParsedHtml parsed = ParsedHtml.parse(null, text);
        List<MockForm> forms = parsed.getForms();
        assertEquals(2, forms.size());

        MockForm form1 = forms.get(0);

        assertEquals("", StringUtils.join(form1.getParameterValues("paramhidden1"), ","));
        assertEquals("paramhidden2avalue,paramhidden2bvalue", StringUtils.join(form1.getParameterValues("paramhidden2"), ","));
        assertEquals("", StringUtils.join(form1.getParameterValues("paramtext1"), ","));
        assertEquals("paramtext2avalue,paramtext2bvalue", StringUtils.join(form1.getParameterValues("paramtext2"), ","));
        assertEquals("", StringUtils.join(form1.getParameterValues("parampassword1"), ","));
        assertEquals("parampassword2avalue,parampassword2bvalue", StringUtils.join(form1.getParameterValues("parampassword2"), ","));
        assertEquals("on", StringUtils.join(form1.getParameterValues("paramcheckbox2"), ","));
        assertEquals("paramcheckbox3value", StringUtils.join(form1.getParameterValues("paramcheckbox3"), ","));
        assertEquals("on,paramcheckbox4value", StringUtils.join(form1.getParameterValues("paramcheckbox4"), ","));
        assertEquals("on", StringUtils.join(form1.getParameterValues("paramradio2"), ","));
        assertEquals("paramradio3bvalue", StringUtils.join(form1.getParameterValues("paramradio3"), ","));
        assertEquals("", StringUtils.join(form1.getParameterValues("textarea1"), ","));
        assertEquals("paramtextarea2avalue,paramtextarea2bvalue", StringUtils.join(form1.getParameterValues("textarea2"), ","));
        assertEquals("select1option1value,select1option3,select1option2", StringUtils.join(form1.getParameterValues("select1"), ","));
        assertEquals("select2option3", StringUtils.join(form1.getParameterValues("select2"), ","));
        assertEquals("select3option1", StringUtils.join(form1.getParameterValues("select3"), ","));
    }

    @Test
    void testUnknownForms()
    throws Exception {
        String text = FileUtils.readString(getClass().getClassLoader().getResource("parsed_html.html"), "UTF-8");
        ParsedHtml parsed = ParsedHtml.parse(null, text);

        assertNull(parsed.getFormWithId("unknown"));
        assertNull(parsed.getFormWithName("unknown"));
    }
}
