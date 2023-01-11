/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlFileInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestAnnotations {
    @Test
    void testDefaultValuesIn()
    throws IOException {
        try (final var server = new TestServerRunner(new AnnotationInSite())) {
            try (final var webClient = new WebClient()) {
                assertEquals("""
                                        
                    0
                    defaultCookie
                    -2
                    defaultCookie2
                    -3
                    null
                    null
                    null
                    null
                    null
                    null
                    defaultParam
                    -4
                    defaultParam2
                    -5
                    defaultPathInfo
                    -6
                    defaultRequestAttribute
                    -7
                    defaultRequestAttribute2
                    -8
                    defaultSessionAttribute
                    -9
                    defaultSessionAttribute2
                    -10
                    defaultHeader
                    -11
                    defaultHeader2
                    -12
                    propval1
                    defaultProp2
                    propval1
                    """, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get"), HttpMethod.GET)).getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testAnnotationInCookiesParamsAttributesHeaders()
    throws IOException {
        try (final var server = new TestServerRunner(new AnnotationInSite())) {
            try (final var webClient = new WebClient()) {
                webClient.getCookieManager().addCookie(new Cookie("localhost", "stringCookie", "cookie1"));
                webClient.getCookieManager().addCookie(new Cookie("localhost", "intCookie", "2"));
                webClient.getCookieManager().addCookie(new Cookie("localhost", "cookie2", "cookie3"));
                webClient.getCookieManager().addCookie(new Cookie("localhost", "cookie3", "4"));
                var request = new WebRequest(new URL("http://localhost:8181/get/info/27"), HttpMethod.GET);
                request.setRequestParameters(List.of(
                    new NameValuePair("generate", "true"),
                    new NameValuePair("stringParam", "value5"),
                    new NameValuePair("intParam", "6"),
                    new NameValuePair("param2", "value7"),
                    new NameValuePair("param3", "8")));
                request.setAdditionalHeader("stringHeader", "value17");
                request.setAdditionalHeader("intHeader", "18");
                request.setAdditionalHeader("header2", "value19");
                request.setAdditionalHeader("header3", "20");
                assertEquals("""
                                        
                    0
                    cookie1
                    2
                    cookie3
                    4
                    null
                    null
                    null
                    null
                    null
                    null
                    value5
                    6
                    value7
                    8
                    27
                    27
                    value9
                    10
                    value11
                    12
                    value13
                    14
                    value15
                    16
                    value17
                    18
                    value19
                    20
                    propval1
                    defaultProp2
                    propval1
                    """, webClient.getPage(request).getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testAnnotationBodyIn()
    throws IOException {
        try (final var server = new TestServerRunner(new AnnotationInSite())) {
            try (final var webClient = new WebClient()) {
                var request = new WebRequest(new URL("http://localhost:8181/post"), HttpMethod.POST);
                request.setHttpMethod(HttpMethod.POST);
                request.setRequestBody("theBody");
                request.setAdditionalHeader("Content-Type", "text/plain");
                assertEquals("""
                    theBody
                    0
                    defaultCookie
                    -2
                    defaultCookie2
                    -3
                    null
                    null
                    null
                    null
                    null
                    null
                    defaultParam
                    -4
                    defaultParam2
                    -5
                    defaultPathInfo
                    -6
                    defaultRequestAttribute
                    -7
                    defaultRequestAttribute2
                    -8
                    defaultSessionAttribute
                    -9
                    defaultSessionAttribute2
                    -10
                    defaultHeader
                    -11
                    defaultHeader2
                    -12
                    propval1
                    defaultProp2
                    propval1
                    """, webClient.getPage(request).getWebResponse().getContentAsString());

                request.setRequestBody("836");
                assertEquals("""
                    836                   
                    836
                    defaultCookie
                    -2
                    defaultCookie2
                    -3
                    null
                    null
                    null
                    null
                    null
                    null
                    defaultParam
                    -4
                    defaultParam2
                    -5
                    defaultPathInfo
                    -6
                    defaultRequestAttribute
                    -7
                    defaultRequestAttribute2
                    -8
                    defaultSessionAttribute
                    -9
                    defaultSessionAttribute2
                    -10
                    defaultHeader
                    -11
                    defaultHeader2
                    -12
                    propval1
                    defaultProp2
                    propval1
                    """, webClient.getPage(request).getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testFileUploadIn()
    throws IOException {
        try (final var server = new TestServerRunner(new AnnotationInSite())) {
            try (final var webClient = new WebClient()) {
                var request = new WebRequest(new URL("http://localhost:8181/form"), HttpMethod.GET);
                HtmlPage page = webClient.getPage(request);
                var form = page.getForms().get(0);

                HtmlFileInput file1 = form.getInputByName("uploadedFile");
                file1.setValueAttribute("file1.txt");
                file1.setContentType("text/plain");
                file1.setData("File 1 Data".getBytes());

                HtmlFileInput file2 = form.getInputByName("file");
                file2.setValueAttribute("file2.txt");
                file2.setContentType("text/plain");
                file2.setData("File 2 Data".getBytes());

                HtmlFileInput file3 = form.getInputByName("fileString");
                file3.setValueAttribute("file3.txt");
                file3.setContentType("text/plain");
                file3.setData("File 3 Data".getBytes());

                HtmlFileInput file4 = form.getInputByName("file2");
                file4.setValueAttribute("file4.txt");
                file4.setContentType("text/plain");
                file4.setData("File 4 Data".getBytes());

                HtmlFileInput file5 = form.getInputByName("file3");
                file5.setValueAttribute("file5.txt");
                file5.setContentType("text/plain");
                file5.setData("File 5 Data".getBytes());

                HtmlFileInput file6 = form.getInputByName("file4");
                file6.setValueAttribute("file6.txt");
                file6.setContentType("text/plain");
                file6.setData("File 6 Data".getBytes());

                page = page.getHtmlElementById("submit").click();
                assertEquals("""
                                        
                    0
                    defaultCookie
                    -2
                    defaultCookie2
                    -3
                    file1.txt, text/plain, File 1 Data
                    File 2 Data
                    File 3 Data
                    file4.txt, text/plain, File 4 Data
                    File 5 Data
                    File 6 Data
                    defaultParam
                    -4
                    defaultParam2
                    -5
                    defaultPathInfo
                    -6
                    defaultRequestAttribute
                    -7
                    defaultRequestAttribute2
                    -8
                    defaultSessionAttribute
                    -9
                    defaultSessionAttribute2
                    -10
                    defaultHeader
                    -11
                    defaultHeader2
                    -12
                    propval1
                    defaultProp2
                    propval1
                    """, page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testDefaultValuesOut()
    throws IOException {
        try (final var server = new TestServerRunner(new AnnotationOutSite())) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage(new WebRequest(new URL("http://localhost:8181/get"), HttpMethod.GET));
                var cookie_manager = webClient.getCookieManager();
                assertEquals("defaultCookie", cookie_manager.getCookie("stringCookie").getValue());
                assertEquals("-2", cookie_manager.getCookie("intCookie").getValue());
                assertEquals("defaultCookie2", cookie_manager.getCookie("cookie2").getValue());
                assertEquals("-3", cookie_manager.getCookie("cookie3").getValue());
                assertEquals("defaultHeader", page.getWebResponse().getResponseHeaderValue("stringHeader"));
                assertEquals("-8", page.getWebResponse().getResponseHeaderValue("intHeader"));
                assertEquals("defaultHeader2", page.getWebResponse().getResponseHeaderValue("header2"));
                assertEquals("-9", page.getWebResponse().getResponseHeaderValue("header3"));
                assertEquals("defaultBody" +
                             "-1" +
                             "defaultRequestAttribute" +
                             "-4" +
                             "defaultRequestAttribute2" +
                             "-5" +
                             "defaultSessionAttribute" +
                             "-6" +
                             "defaultSessionAttribute2" +
                             "-7", page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testGeneratedValuesOut()
    throws IOException {
        try (final var server = new TestServerRunner(new AnnotationOutSite())) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage(new WebRequest(new URL("http://localhost:8181/get?generate=true"), HttpMethod.GET));
                var cookie_manager = webClient.getCookieManager();
                assertEquals("value3", cookie_manager.getCookie("stringCookie").getValue());
                assertEquals("4", cookie_manager.getCookie("intCookie").getValue());
                assertEquals("value5", cookie_manager.getCookie("cookie2").getValue());
                assertEquals("6", cookie_manager.getCookie("cookie3").getValue());
                assertEquals("value15", page.getWebResponse().getResponseHeaderValue("stringHeader"));
                assertEquals("16", page.getWebResponse().getResponseHeaderValue("intHeader"));
                assertEquals("value17", page.getWebResponse().getResponseHeaderValue("header2"));
                assertEquals("18", page.getWebResponse().getResponseHeaderValue("header3"));
                assertEquals("value1" +
                             "2" +
                             "value7" +
                             "8" +
                             "value9" +
                             "10" +
                             "value11" +
                             "12" +
                             "value13" +
                             "14", page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testDefaultValuesInOut()
    throws IOException {
        try (final var server = new TestServerRunner(new AnnotationInOutSite())) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage(new WebRequest(new URL("http://localhost:8181/get"), HttpMethod.GET));
                var cookie_manager = webClient.getCookieManager();
                assertEquals("defaultCookie", cookie_manager.getCookie("stringCookie").getValue());
                assertEquals("-2", cookie_manager.getCookie("intCookie").getValue());
                assertEquals("defaultCookie2", cookie_manager.getCookie("cookie2").getValue());
                assertEquals("-3", cookie_manager.getCookie("cookie3").getValue());
                assertEquals("defaultHeader", page.getWebResponse().getResponseHeaderValue("stringHeader"));
                assertEquals("-8", page.getWebResponse().getResponseHeaderValue("intHeader"));
                assertEquals("defaultHeader2", page.getWebResponse().getResponseHeaderValue("header2"));
                assertEquals("-9", page.getWebResponse().getResponseHeaderValue("header3"));
                assertEquals("""
                                        
                    0
                    defaultCookie
                    -2
                    defaultCookie2
                    -3
                    defaultRequestAttribute
                    -4
                    defaultRequestAttribute2
                    -5
                    defaultSessionAttribute
                    -6
                    defaultSessionAttribute2
                    -7
                    defaultHeader
                    -8
                    defaultHeader2
                    -9
                    0defaultRequestAttribute
                    -4
                    defaultRequestAttribute2
                    -5
                    defaultSessionAttribute
                    -6
                    defaultSessionAttribute2
                    -7
                    """, page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testGeneratedInValuesInOut()
    throws IOException {
        try (final var server = new TestServerRunner(new AnnotationInOutSite())) {
            try (final var webClient = new WebClient()) {
                webClient.getCookieManager().addCookie(new Cookie("localhost", "stringCookie", "cookie1"));
                webClient.getCookieManager().addCookie(new Cookie("localhost", "intCookie", "2"));
                webClient.getCookieManager().addCookie(new Cookie("localhost", "cookie2", "cookie3"));
                webClient.getCookieManager().addCookie(new Cookie("localhost", "cookie3", "4"));
                var request = new WebRequest(new URL("http://localhost:8181/post?generateIn=true"), HttpMethod.POST);
                request.setHttpMethod(HttpMethod.POST);
                request.setRequestBody("theBody");
                request.setAdditionalHeader("Content-Type", "text/plain");
                request.setAdditionalHeader("stringHeader", "value15");
                request.setAdditionalHeader("intHeader", "16");
                request.setAdditionalHeader("header2", "value17");
                request.setAdditionalHeader("header3", "18");
                HtmlPage page = webClient.getPage(request);
                assertEquals("""
                    theBody
                    0
                    cookie1
                    2
                    cookie3
                    4
                    inValue7
                    1008
                    inValue11
                    1012
                    inValue13
                    1014
                    inValue15
                    1016
                    value15
                    16
                    value17
                    18
                    theBody0inValue7
                    1008
                    inValue11
                    1012
                    inValue13
                    1014
                    inValue15
                    1016
                    """, page.getWebResponse().getContentAsString());
           }
        }
    }

    @Test
    void testGeneratedOutValuesInOut()
    throws IOException {
        try (final var server = new TestServerRunner(new AnnotationInOutSite())) {
            try (final var webClient = new WebClient()) {
                webClient.getCookieManager().addCookie(new Cookie("localhost", "stringCookie", "cookie1"));
                webClient.getCookieManager().addCookie(new Cookie("localhost", "intCookie", "2"));
                webClient.getCookieManager().addCookie(new Cookie("localhost", "cookie2", "cookie3"));
                webClient.getCookieManager().addCookie(new Cookie("localhost", "cookie3", "4"));
                var request = new WebRequest(new URL("http://localhost:8181/post?generateOut=true"), HttpMethod.POST);
                request.setHttpMethod(HttpMethod.POST);
                request.setRequestBody("theBody");
                request.setAdditionalHeader("Content-Type", "text/plain");
                request.setAdditionalHeader("stringHeader", "value15");
                request.setAdditionalHeader("intHeader", "16");
                request.setAdditionalHeader("header2", "value17");
                request.setAdditionalHeader("header3", "18");
                HtmlPage page = webClient.getPage(request);
                assertEquals("""
                    theBody
                    0
                    cookie1
                    2
                    cookie3
                    4
                    defaultRequestAttribute
                    -4
                    defaultRequestAttribute2
                    -5
                    defaultSessionAttribute
                    -6
                    defaultSessionAttribute2
                    -7
                    value15
                    16
                    value17
                    18
                    outValue12002outValue7
                    2008
                    outValue9
                    2010
                    outValue11
                    2012
                    outValue13
                    2014
                    """, page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testParametersSite()
    throws IOException {
        try (final var server = new TestServerRunner(new AnnotationParametersSite())) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/out?switchRoute=1");
                assertEquals("http://localhost:8181/in?stringParam=value1&intParam=222&param3=444&param2=value3", page.getWebResponse().getContentAsString());

                page = webClient.getPage(page.getWebResponse().getContentAsString());
                assertEquals("""
                    value1
                    222
                    value3
                    444
                    """, page.getWebResponse().getContentAsString());

                page = webClient.getPage("http://localhost:8181/out?switchRoute=2");
                assertEquals("http://localhost:8181/pathinfo/some/222/444?stringParam=value1&param2=value3", page.getWebResponse().getContentAsString());

                page = webClient.getPage(page.getWebResponse().getContentAsString());
                assertEquals("""
                    value1
                    222
                    value3
                    444
                    """, page.getWebResponse().getContentAsString());
            }
        }
    }
}
