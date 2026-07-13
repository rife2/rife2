/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import org.junit.jupiter.api.Test;
import rife.tools.FileUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestMultipartUpload {
    private static final String BOUNDARY = "rife2multipartboundary";

    private static Site createUploadSite() {
        return new Site() {
            public void setup() {
                post("/upload", c -> {
                    var content = "";
                    if (c.hasFile("doc")) {
                        content = FileUtils.readString(c.file("doc").getFile());
                    }
                    c.print(String.join(",", c.parameterValues("query")) + ";" + c.parameter("text") + ";" + content);
                });
            }
        };
    }

    private static String postMultipart(int port)
    throws Exception {
        var body = "--" + BOUNDARY + "\r\n" +
            "Content-Disposition: form-data; name=\"text\"\r\n" +
            "\r\n" +
            "the text param\r\n" +
            "--" + BOUNDARY + "\r\n" +
            "Content-Disposition: form-data; name=\"doc\"; filename=\"doc.txt\"\r\n" +
            "Content-Type: text/plain\r\n" +
            "\r\n" +
            "the file content\r\n" +
            "--" + BOUNDARY + "--\r\n";
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
            .uri(new URI("http://localhost:" + port + "/upload?query=the%20query+param&query=again"))
            .header("Content-Type", "multipart/form-data; boundary=" + BOUNDARY)
            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
            .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() + ":" + response.body();
    }

    @Test
    void testMultipartUpload()
    throws Exception {
        try (final var server = new TestServerRunner(createUploadSite())) {
            assertEquals("200:the query param,again;the text param;the file content", postMultipart(8181));
        }
    }

    @Test
    void testTomcatMultipartUpload()
    throws Exception {
        try (final var server = new TestTomcatRunner(createUploadSite())) {
            assertEquals("200:the query param,again;the text param;the file content", postMultipart(8282));
        }
    }
}
