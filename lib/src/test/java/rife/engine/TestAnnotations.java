/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
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
    public void testDefaultValues()
    throws IOException {
        try (final var server = new TestServerRunner(new AnnotationSite())) {
            try (final WebClient webClient = new WebClient()) {
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
                    """, webClient.getPage(new WebRequest(new URL("http://localhost:8181/get"), HttpMethod.GET)).getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testAnnotationCookiesParamsAttributesHeaders()
    throws IOException {
        try (final var server = new TestServerRunner(new AnnotationSite())) {
            try (final WebClient webClient = new WebClient()) {
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
                    """, webClient.getPage(request).getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testAnnotationBody()
    throws IOException {
        try (final var server = new TestServerRunner(new AnnotationSite())) {
            try (final WebClient webClient = new WebClient()) {
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
                    """, webClient.getPage(request).getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testFileUpload()
    throws IOException {
        try (final var server = new TestServerRunner(new AnnotationSite())) {
            try (final WebClient webClient = new WebClient()) {
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
                    """, page.getWebResponse().getContentAsString());
            }
        }
    }
}
