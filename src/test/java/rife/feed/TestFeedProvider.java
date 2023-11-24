/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.feed;

import org.junit.jupiter.api.Test;
import rife.engine.Site;
import rife.feed.elements.FeedProvider;
import rife.test.MockConversation;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestFeedProvider {
    @Test
    public void testFeedProviderRss() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/rss", () -> new FeedProvider(FeedProvider.FeedType.RSS_2_0, new SimpleEntryProvider()));
            }
        });

        var response = conversation.doRequest("http://localhost/rss");
        assertEquals("application/xml; charset=UTF-8", response.getContentType());
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<rss version=\"2.0\">\n" +
                     "    <channel>\n" +
                     "        <title>feed_title</title>\n" +
                     "        <link>feed_link</link>\n" +
                     "        <description>feed_description</description>\n" +
                     "        <language>feed_language</language>\n" +
                     "        <copyright>feed_copyright</copyright>\n" +
                     "        <pubDate>Fri, 24 Nov 2023 02:00:00 -0500</pubDate>\n" +
                     "        <managingEditor>feed_author</managingEditor>\n" +
                     "        <item>\n" +
                     "            <title>entry_title1</title>\n" +
                     "            <link>entry_link1</link>\n" +
                     "            <description>&quot;entry&quot;\n" +
                     "&lt;content&gt;1</description>\n" +
                     "            <pubDate>Fri, 24 Nov 2023 01:00:00 -0500</pubDate>\n" +
                     "            <author>entry_author1</author>\n" +
                     "            <guid>entry_link1</guid>\n" +
                     "        </item>\n" +
                     "        <item>\n" +
                     "            <title>entry_title2</title>\n" +
                     "            <link>entry_link2</link>\n" +
                     "            <description>&quot;entry&quot;\n" +
                     "&lt;content&gt;2</description>\n" +
                     "            <pubDate>Fri, 24 Nov 2023 02:00:00 -0500</pubDate>\n" +
                     "            <author>entry_author2</author>\n" +
                     "            <guid>entry_link2</guid>\n" +
                     "        </item>\n" +
                     "    </channel>\n" +
                     "</rss>", response.getText());
    }

    @Test
    public void testFeedProviderImagesRss() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/images_rss", () -> new FeedProvider(FeedProvider.FeedType.RSS_2_0, new ImageEntryProvider()));
            }
        });

        var response = conversation.doRequest("http://localhost/images_rss");
        assertEquals("application/xml; charset=UTF-8", response.getContentType());
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<rss xmlns:media=\"http://search.yahoo.com/mrss\" version=\"2.0\">\n" +
                     "    <channel>\n" +
                     "        <title>feed_title</title>\n" +
                     "        <link>feed_link</link>\n" +
                     "        <description>feed_description</description>\n" +
                     "        <language>feed_language</language>\n" +
                     "        <copyright>feed_copyright</copyright>\n" +
                     "        <pubDate>Fri, 24 Nov 2023 03:00:00 -0500</pubDate>\n" +
                     "        <managingEditor>feed_author</managingEditor>\n" +
                     "        <item>\n" +
                     "            <title>entry_title1</title>\n" +
                     "            <link>entry_link1</link>\n" +
                     "            <description>&quot;entry&quot;\n" +
                     "&lt;content&gt;1</description>\n" +
                     "            <pubDate>Fri, 24 Nov 2023 01:00:00 -0500</pubDate>\n" +
                     "            <author>entry_author1</author>\n" +
                     "            <guid>entry_link1</guid>\n" +
                     "            <media:thumbnail url=\"https://rife2.com/images/logo.svg?0\" />\n" +
                     "        </item>\n" +
                     "        <item>\n" +
                     "            <title>entry_title2</title>\n" +
                     "            <link>entry_link2</link>\n" +
                     "            <description>&quot;entry&quot;\n" +
                     "&lt;content&gt;2</description>\n" +
                     "            <pubDate>Fri, 24 Nov 2023 02:00:00 -0500</pubDate>\n" +
                     "            <author>entry_author2</author>\n" +
                     "            <guid>entry_link2</guid>\n" +
                     "        </item>\n" +
                     "        <item>\n" +
                     "            <title>entry_title3</title>\n" +
                     "            <link>entry_link3</link>\n" +
                     "            <description>&quot;entry&quot;\n" +
                     "&lt;content&gt;3</description>\n" +
                     "            <pubDate>Fri, 24 Nov 2023 03:00:00 -0500</pubDate>\n" +
                     "            <author>entry_author3</author>\n" +
                     "            <guid>entry_link3</guid>\n" +
                     "            <media:thumbnail url=\"https://rife2.com/images/logo.svg?2\" />\n" +
                     "        </item>\n" +
                     "    </channel>\n" +
                     "</rss>", response.getText());
    }

    @Test
    public void testFeedProviderNamespacesRss() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/namespaces_rss", () -> new FeedProvider(FeedProvider.FeedType.RSS_2_0, new NamespacesEntryProvider()));
            }
        });

        var response = conversation.doRequest("http://localhost/namespaces_rss");
        assertEquals("application/xml; charset=UTF-8", response.getContentType());
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<rss xmlns:doap=\"http://usefulinc.com/ns/doap#\" xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" version=\"2.0\">\n" +
                     "    <channel>\n" +
                     "        <title>feed_title_namespace</title>\n" +
                     "        <link>feed_link_namespace</link>\n" +
                     "        <description>feed_description_namespace</description>\n" +
                     "        <language>feed_language_namespace</language>\n" +
                     "        <copyright>feed_copyright_namespace</copyright>\n" +
                     "        <pubDate>Fri, 24 Nov 2023 02:00:00 -0500</pubDate>\n" +
                     "        <managingEditor>feed_author_namespace</managingEditor>\n" +
                     "        <item>\n" +
                     "            <title>entry_title_namespace1</title>\n" +
                     "            <link>entry_link_namespace1</link>\n" +
                     "            <description><doap:Project>entry_content_namespace1</doap:Project></description>\n" +
                     "            <pubDate>Fri, 24 Nov 2023 01:00:00 -0500</pubDate>\n" +
                     "            <author>entry_author_namespace1</author>\n" +
                     "            <guid>entry_link_namespace1</guid>\n" +
                     "        </item>\n" +
                     "        <item>\n" +
                     "            <title>entry_title_namespace2</title>\n" +
                     "            <link>entry_link_namespace2</link>\n" +
                     "            <description><doap:Project>entry_content_namespace2</doap:Project></description>\n" +
                     "            <pubDate>Fri, 24 Nov 2023 02:00:00 -0500</pubDate>\n" +
                     "            <author>entry_author_namespace2</author>\n" +
                     "            <guid>entry_link_namespace2</guid>\n" +
                     "        </item>\n" +
                     "    </channel>\n" +
                     "</rss>", response.getText());
    }

    @Test
    public void testFeedProviderAtom0_3() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/atom", () -> new FeedProvider(FeedProvider.FeedType.ATOM_0_3, new SimpleEntryProvider()));
            }
        });

        var response = conversation.doRequest("http://localhost/atom");
        assertEquals("application/xml; charset=UTF-8", response.getContentType());
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<feed xmlns=\"http://purl.org/atom/ns#\" xmlns:taxo=\"http://purl.org/rss/1.0/modules/taxonomy/\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:sy=\"http://purl.org/rss/1.0/modules/syndication/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" version=\"0.3\">\n" +
                     "    <title>feed_title</title>\n" +
                     "    <link rel=\"alternate\" href=\"feed_link\" type=\"text/html\" />\n" +
                     "    <author>\n" +
                     "        <name>feed_author</name>\n" +
                     "    </author>\n" +
                     "    <copyright>feed_copyright</copyright>\n" +
                     "    <info>feed_description</info>\n" +
                     "    <modified>2023-11-24T02:00:00-0500</modified>\n" +
                     "    <dc:creator>feed_author</dc:creator>\n" +
                     "    <dc:date>2023-11-24T02:00:00-0500</dc:date>\n" +
                     "    <dc:language>feed_language</dc:language>\n" +
                     "    <dc:rights>feed_copyright</dc:rights>\n" +
                     "    <entry>\n" +
                     "        <title>entry_title1</title>\n" +
                     "        <link rel=\"alternate\" href=\"entry_link1\" type=\"text/html\" />\n" +
                     "        <author>\n" +
                     "            <name>entry_author1</name>\n" +
                     "        </author>\n" +
                     "        <modified>2023-11-24T01:00:00-0500</modified>\n" +
                     "        <content type=\"text/html\" mode=\"escaped\">&quot;entry&quot;\n" +
                     "&lt;content&gt;1</content>\n" +
                     "        <id>entry_link1</id>\n" +
                     "        <issued>2023-11-24T01:00:00-0500</issued>\n" +
                     "        <dc:creator>entry_author1</dc:creator>\n" +
                     "        <dc:date>2023-11-24T01:00:00-0500</dc:date>\n" +
                     "    </entry>\n" +
                     "    <entry>\n" +
                     "        <title>entry_title2</title>\n" +
                     "        <link rel=\"alternate\" href=\"entry_link2\" type=\"text/html\" />\n" +
                     "        <author>\n" +
                     "            <name>entry_author2</name>\n" +
                     "        </author>\n" +
                     "        <modified>2023-11-24T02:00:00-0500</modified>\n" +
                     "        <content type=\"text/html\" mode=\"escaped\">&quot;entry&quot;\n" +
                     "&lt;content&gt;2</content>\n" +
                     "        <id>entry_link2</id>\n" +
                     "        <issued>2023-11-24T02:00:00-0500</issued>\n" +
                     "        <dc:creator>entry_author2</dc:creator>\n" +
                     "        <dc:date>2023-11-24T02:00:00-0500</dc:date>\n" +
                     "    </entry>\n" +
                     "</feed>", response.getText());
    }

    @Test
    public void testFeedProviderNamespacesAtom0_3() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/namespaces_atom", () -> new FeedProvider(FeedProvider.FeedType.ATOM_0_3, new NamespacesEntryProvider()));
            }
        });

        var response = conversation.doRequest("http://localhost/namespaces_atom");
        assertEquals("application/xml; charset=UTF-8", response.getContentType());
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<feed xmlns=\"http://purl.org/atom/ns#\" xmlns:taxo=\"http://purl.org/rss/1.0/modules/taxonomy/\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:sy=\"http://purl.org/rss/1.0/modules/syndication/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:doap=\"http://usefulinc.com/ns/doap#\" xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" version=\"0.3\">\n" +
                     "    <title>feed_title_namespace</title>\n" +
                     "    <link rel=\"alternate\" href=\"feed_link_namespace\" type=\"text/html\" />\n" +
                     "    <author>\n" +
                     "        <name>feed_author_namespace</name>\n" +
                     "    </author>\n" +
                     "    <copyright>feed_copyright_namespace</copyright>\n" +
                     "    <info>feed_description_namespace</info>\n" +
                     "    <modified>2023-11-24T02:00:00-0500</modified>\n" +
                     "    <dc:creator>feed_author_namespace</dc:creator>\n" +
                     "    <dc:date>2023-11-24T02:00:00-0500</dc:date>\n" +
                     "    <dc:language>feed_language_namespace</dc:language>\n" +
                     "    <dc:rights>feed_copyright_namespace</dc:rights>\n" +
                     "    <entry>\n" +
                     "        <title>entry_title_namespace1</title>\n" +
                     "        <link rel=\"alternate\" href=\"entry_link_namespace1\" type=\"text/html\" />\n" +
                     "        <author>\n" +
                     "            <name>entry_author_namespace1</name>\n" +
                     "        </author>\n" +
                     "        <modified>2023-11-24T01:00:00-0500</modified>\n" +
                     "        <content type=\"application/rdf+xml\"><doap:Project>entry_content_namespace1</doap:Project></content>\n" +
                     "        <id>entry_link_namespace1</id>\n" +
                     "        <issued>2023-11-24T01:00:00-0500</issued>\n" +
                     "        <dc:creator>entry_author_namespace1</dc:creator>\n" +
                     "        <dc:date>2023-11-24T01:00:00-0500</dc:date>\n" +
                     "    </entry>\n" +
                     "    <entry>\n" +
                     "        <title>entry_title_namespace2</title>\n" +
                     "        <link rel=\"alternate\" href=\"entry_link_namespace2\" type=\"text/html\" />\n" +
                     "        <author>\n" +
                     "            <name>entry_author_namespace2</name>\n" +
                     "        </author>\n" +
                     "        <modified>2023-11-24T02:00:00-0500</modified>\n" +
                     "        <content type=\"application/rdf+xml\"><doap:Project>entry_content_namespace2</doap:Project></content>\n" +
                     "        <id>entry_link_namespace2</id>\n" +
                     "        <issued>2023-11-24T02:00:00-0500</issued>\n" +
                     "        <dc:creator>entry_author_namespace2</dc:creator>\n" +
                     "        <dc:date>2023-11-24T02:00:00-0500</dc:date>\n" +
                     "    </entry>\n" +
                     "</feed>", response.getText());
    }

    @Test
    public void testFeedProviderAtom1_0() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/atom", () -> new FeedProvider(FeedProvider.FeedType.ATOM_1_0, new SimpleEntryProvider()));
            }
        });

        var response = conversation.doRequest("http://localhost/atom");
        assertEquals("application/xml; charset=UTF-8", response.getContentType());
        assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                     "<feed xmlns=\"http://www.w3.org/2005/Atom\">\n" +
                     "    <title>feed_title</title>\n" +
                     "    <subtitle>feed_description</subtitle>\n" +
                     "    <id>feed_link</id>\n" +
                     "    <updated>2023-11-24T02:00:00-0500</updated>\n" +
                     "    <link href=\"feed_link\" type=\"text/html\" />\n" +
                     "    <rights>feed_copyright</rights>\n" +
                     "    <author>\n" +
                     "        <name>feed_author</name>\n" +
                     "    </author>\n" +
                     "    <entry>\n" +
                     "        <title>entry_title1</title>\n" +
                     "        <link rel=\"alternate\" type=\"text/html\" href=\"entry_link1\" />\n" +
                     "        <id>entry_link1</id>\n" +
                     "        <updated>2023-11-24T01:00:00-0500</updated>\n" +
                     "        <published>2023-11-24T01:00:00-0500</published>\n" +
                     "        <author>\n" +
                     "            <name>entry_author1</name>\n" +
                     "        </author>\n" +
                     "        <content type=\"text/html\">&quot;entry&quot;\n" +
                     "&lt;content&gt;1</content>\n" +
                     "    </entry>\n" +
                     "    <entry>\n" +
                     "        <title>entry_title2</title>\n" +
                     "        <link rel=\"alternate\" type=\"text/html\" href=\"entry_link2\" />\n" +
                     "        <id>entry_link2</id>\n" +
                     "        <updated>2023-11-24T02:00:00-0500</updated>\n" +
                     "        <published>2023-11-24T02:00:00-0500</published>\n" +
                     "        <author>\n" +
                     "            <name>entry_author2</name>\n" +
                     "        </author>\n" +
                     "        <content type=\"text/html\">&quot;entry&quot;\n" +
                     "&lt;content&gt;2</content>\n" +
                     "    </entry>\n" +
                     "</feed>", response.getText());
    }

    @Test
    public void testFeedProviderImagesAtom1_0() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/images_atom", () -> new FeedProvider(FeedProvider.FeedType.ATOM_1_0, new ImageEntryProvider()));
            }
        });

        var response = conversation.doRequest("http://localhost/images_atom");
        assertEquals("application/xml; charset=UTF-8", response.getContentType());
        assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                     "<feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:media=\"http://search.yahoo.com/mrss\">\n" +
                     "    <title>feed_title</title>\n" +
                     "    <subtitle>feed_description</subtitle>\n" +
                     "    <id>feed_link</id>\n" +
                     "    <updated>2023-11-24T03:00:00-0500</updated>\n" +
                     "    <link href=\"feed_link\" type=\"text/html\" />\n" +
                     "    <rights>feed_copyright</rights>\n" +
                     "    <author>\n" +
                     "        <name>feed_author</name>\n" +
                     "    </author>\n" +
                     "    <entry>\n" +
                     "        <title>entry_title1</title>\n" +
                     "        <link rel=\"alternate\" type=\"text/html\" href=\"entry_link1\" />\n" +
                     "        <id>entry_link1</id>\n" +
                     "        <updated>2023-11-24T01:00:00-0500</updated>\n" +
                     "        <published>2023-11-24T01:00:00-0500</published>\n" +
                     "        <author>\n" +
                     "            <name>entry_author1</name>\n" +
                     "        </author>\n" +
                     "        <content type=\"text/html\">&quot;entry&quot;\n" +
                     "&lt;content&gt;1</content>\n" +
                     "        <media:thumbnail url=\"https://rife2.com/images/logo.svg?0\" />\n" +
                     "    </entry>\n" +
                     "    <entry>\n" +
                     "        <title>entry_title2</title>\n" +
                     "        <link rel=\"alternate\" type=\"text/html\" href=\"entry_link2\" />\n" +
                     "        <id>entry_link2</id>\n" +
                     "        <updated>2023-11-24T02:00:00-0500</updated>\n" +
                     "        <published>2023-11-24T02:00:00-0500</published>\n" +
                     "        <author>\n" +
                     "            <name>entry_author2</name>\n" +
                     "        </author>\n" +
                     "        <content type=\"text/html\">&quot;entry&quot;\n" +
                     "&lt;content&gt;2</content>\n" +
                     "    </entry>\n" +
                     "    <entry>\n" +
                     "        <title>entry_title3</title>\n" +
                     "        <link rel=\"alternate\" type=\"text/html\" href=\"entry_link3\" />\n" +
                     "        <id>entry_link3</id>\n" +
                     "        <updated>2023-11-24T03:00:00-0500</updated>\n" +
                     "        <published>2023-11-24T03:00:00-0500</published>\n" +
                     "        <author>\n" +
                     "            <name>entry_author3</name>\n" +
                     "        </author>\n" +
                     "        <content type=\"text/html\">&quot;entry&quot;\n" +
                     "&lt;content&gt;3</content>\n" +
                     "        <media:thumbnail url=\"https://rife2.com/images/logo.svg?2\" />\n" +
                     "    </entry>\n" +
                     "</feed>", response.getText());
    }

    @Test
    public void testFeedProviderNamespacesAtom1_0() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/atom", () -> new FeedProvider(FeedProvider.FeedType.ATOM_1_0, new NamespacesEntryProvider()));
            }
        });

        var response = conversation.doRequest("http://localhost/atom");
        assertEquals("application/xml; charset=UTF-8", response.getContentType());
        assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                     "<feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:doap=\"http://usefulinc.com/ns/doap#\" xmlns:foaf=\"http://xmlns.com/foaf/0.1/\">\n" +
                     "    <title>feed_title_namespace</title>\n" +
                     "    <subtitle>feed_description_namespace</subtitle>\n" +
                     "    <id>feed_link_namespace</id>\n" +
                     "    <updated>2023-11-24T02:00:00-0500</updated>\n" +
                     "    <link href=\"feed_link_namespace\" type=\"text/html\" />\n" +
                     "    <rights>feed_copyright_namespace</rights>\n" +
                     "    <author>\n" +
                     "        <name>feed_author_namespace</name>\n" +
                     "    </author>\n" +
                     "    <entry>\n" +
                     "        <title>entry_title_namespace1</title>\n" +
                     "        <link rel=\"alternate\" type=\"text/html\" href=\"entry_link_namespace1\" />\n" +
                     "        <id>entry_link_namespace1</id>\n" +
                     "        <updated>2023-11-24T01:00:00-0500</updated>\n" +
                     "        <published>2023-11-24T01:00:00-0500</published>\n" +
                     "        <author>\n" +
                     "            <name>entry_author_namespace1</name>\n" +
                     "        </author>\n" +
                     "        <content type=\"application/rdf+xml\"><doap:Project>entry_content_namespace1</doap:Project></content>\n" +
                     "    </entry>\n" +
                     "    <entry>\n" +
                     "        <title>entry_title_namespace2</title>\n" +
                     "        <link rel=\"alternate\" type=\"text/html\" href=\"entry_link_namespace2\" />\n" +
                     "        <id>entry_link_namespace2</id>\n" +
                     "        <updated>2023-11-24T02:00:00-0500</updated>\n" +
                     "        <published>2023-11-24T02:00:00-0500</published>\n" +
                     "        <author>\n" +
                     "            <name>entry_author_namespace2</name>\n" +
                     "        </author>\n" +
                     "        <content type=\"application/rdf+xml\"><doap:Project>entry_content_namespace2</doap:Project></content>\n" +
                     "    </entry>\n" +
                     "</feed>", response.getText());
    }

    @Test
    public void testFeedProviderJson1_1() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/json", () -> new FeedProvider(FeedProvider.FeedType.JSON_1_1, new SimpleEntryProvider()));
            }
        });

        var response = conversation.doRequest("http://localhost/json");
        assertEquals("application/json; charset=UTF-8", response.getContentType());
        assertEquals("{\n" +
                     "    \"version\": \"https://jsonfeed.org/version/1.1\",\n" +
                     "    \"title\": \"feed_title\",\n" +
                     "    \"home_page_url\": \"feed_link\",\n" +
                     "    \"description\": \"feed_description\",\n" +
                     "    \"authors\": [\n" +
                     "        {\n" +
                     "            \"name\": \"feed_author\"\n" +
                     "        }\n" +
                     "    ],\n" +
                     "    \"language\": \"feed_language\",\n" +
                     "    \"items\": [\n" +
                     "        {\n" +
                     "            \"id\": \"entry_link1\",\n" +
                     "            \"url\": \"entry_link1\",\n" +
                     "            \"title\": \"entry_title1\",\n" +
                     "            \"content_html\": \"\\\"entry\\\"\\n<content>1\",\n" +
                     "            \"date_published\": \"2023-11-24T01:00:00-0500\",\n" +
                     "            \"authors\": [\n" +
                     "                {\n" +
                     "                    \"name\": \"entry_author1\"\n" +
                     "                }\n" +
                     "            ]\n" +
                     "        },\n" +
                     "        {\n" +
                     "            \"id\": \"entry_link2\",\n" +
                     "            \"url\": \"entry_link2\",\n" +
                     "            \"title\": \"entry_title2\",\n" +
                     "            \"content_html\": \"\\\"entry\\\"\\n<content>2\",\n" +
                     "            \"date_published\": \"2023-11-24T02:00:00-0500\",\n" +
                     "            \"authors\": [\n" +
                     "                {\n" +
                     "                    \"name\": \"entry_author2\"\n" +
                     "                }\n" +
                     "            ]\n" +
                     "        },\n" +
                     "    ]\n" +
                     "}", response.getText());
    }

    @Test
    public void testFeedProviderImagesJson1_1()
    throws Exception {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/images_json", () -> new FeedProvider(FeedProvider.FeedType.JSON_1_1, new ImageEntryProvider()));
            }
        });

        var response = conversation.doRequest("http://localhost/images_json");
        assertEquals("application/json; charset=UTF-8", response.getContentType());
        assertEquals("{\n" +
                     "    \"version\": \"https://jsonfeed.org/version/1.1\",\n" +
                     "    \"title\": \"feed_title\",\n" +
                     "    \"home_page_url\": \"feed_link\",\n" +
                     "    \"description\": \"feed_description\",\n" +
                     "    \"authors\": [\n" +
                     "        {\n" +
                     "            \"name\": \"feed_author\"\n" +
                     "        }\n" +
                     "    ],\n" +
                     "    \"language\": \"feed_language\",\n" +
                     "    \"items\": [\n" +
                     "        {\n" +
                     "            \"id\": \"entry_link1\",\n" +
                     "            \"url\": \"entry_link1\",\n" +
                     "            \"title\": \"entry_title1\",\n" +
                     "            \"content_html\": \"\\\"entry\\\"\\n<content>1\",\n" +
                     "            \"date_published\": \"2023-11-24T01:00:00-0500\",\n" +
                     "            \"authors\": [\n" +
                     "                {\n" +
                     "                    \"name\": \"entry_author1\"\n" +
                     "                }\n" +
                     "            ],\n" +
                     "            \"image\": \"https://rife2.com/images/logo.svg?0\"\n" +
                     "        },\n" +
                     "        {\n" +
                     "            \"id\": \"entry_link2\",\n" +
                     "            \"url\": \"entry_link2\",\n" +
                     "            \"title\": \"entry_title2\",\n" +
                     "            \"content_html\": \"\\\"entry\\\"\\n<content>2\",\n" +
                     "            \"date_published\": \"2023-11-24T02:00:00-0500\",\n" +
                     "            \"authors\": [\n" +
                     "                {\n" +
                     "                    \"name\": \"entry_author2\"\n" +
                     "                }\n" +
                     "            ]\n" +
                     "        },\n" +
                     "        {\n" +
                     "            \"id\": \"entry_link3\",\n" +
                     "            \"url\": \"entry_link3\",\n" +
                     "            \"title\": \"entry_title3\",\n" +
                     "            \"content_html\": \"\\\"entry\\\"\\n<content>3\",\n" +
                     "            \"date_published\": \"2023-11-24T03:00:00-0500\",\n" +
                     "            \"authors\": [\n" +
                     "                {\n" +
                     "                    \"name\": \"entry_author3\"\n" +
                     "                }\n" +
                     "            ],\n" +
                     "            \"image\": \"https://rife2.com/images/logo.svg?2\"\n" +
                     "        },\n" +
                     "    ]\n" +
                     "}", response.getText());
    }
}
