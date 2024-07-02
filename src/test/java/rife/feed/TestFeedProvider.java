/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.feed;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rife.config.RifeConfig;
import rife.engine.Site;
import rife.feed.elements.FeedProvider;
import rife.test.MockConversation;

import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestFeedProvider {
    @BeforeEach
    public void setup() {
        RifeConfig.tools().setDefaultTimeZone(TimeZone.getTimeZone("EST"));
    }

    @AfterEach
    public void tearDown() {
        RifeConfig.tools().setDefaultTimeZone(null);
    }

    @Test
    public void testFeedProviderRss() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/rss", () -> new FeedProvider(FeedProvider.FeedType.RSS_2_0, new SimpleEntryProvider()));
            }
        });

        var response = conversation.doRequest("http://localhost/rss");
        assertEquals("application/xml; charset=UTF-8", response.getContentType());
        assertEquals("""
            <?xml version="1.0" encoding="UTF-8"?>
            <rss xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:atom="http://www.w3.org/2005/Atom" version="2.0">
                <channel>
                    <title>feed_title</title>
                    <link>feed_link</link>
                    <atom:link href="http://localhost/rss" rel="self" type="application/rss+xml" />
                    <category>feed_category1</category>
                    <dc:subject>feed_category1</dc:subject>
                    <category>feed_category2</category>
                    <dc:subject>feed_category2</dc:subject>
                    <description>feed_description</description>
                    <language>feed_language</language>
                    <copyright>feed_copyright</copyright>
                    <pubDate>Fri, 24 Nov 2023 02:00:00 -0500</pubDate>
                    <managingEditor>feed_author</managingEditor>
                    <dc:language>feed_language</dc:language>
                    <dc:rights>feed_copyright</dc:rights>
                    <item>
                        <title>entry_title1</title>
                        <link>entry_link1</link>
                        <category>entry_category1_1</category>
                        <category>entry_category1_2</category>
                        <description>&quot;entry&quot;
            &lt;content&gt;1</description>
                        <pubDate>Fri, 24 Nov 2023 01:00:00 -0500</pubDate>
                        <author>entry_author1</author>
                        <dc:creator>entry_author1</dc:creator>
                        <guid>entry_link1</guid>
                    </item>
                    <item>
                        <title>entry_title2</title>
                        <link>entry_link2</link>
                        <category>entry_category2_1</category>
                        <category>entry_category2_2</category>
                        <description>&quot;entry&quot;
            &lt;content&gt;2</description>
                        <pubDate>Fri, 24 Nov 2023 02:00:00 -0500</pubDate>
                        <author>entry_author2</author>
                        <dc:creator>entry_author2</dc:creator>
                        <guid>entry_link2</guid>
                    </item>
                </channel>
            </rss>""", response.getText());
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
        assertEquals("""
            <?xml version="1.0" encoding="UTF-8"?>
            <rss xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:atom="http://www.w3.org/2005/Atom" xmlns:media="http://search.yahoo.com/mrss" version="2.0">
                <channel>
                    <title>feed_title</title>
                    <link>feed_link</link>
                    <atom:link href="http://localhost/images_rss" rel="self" type="application/rss+xml" />
                    <category>feed_category</category>
                    <dc:subject>feed_category</dc:subject>
                    <description>feed_description</description>
                    <language>feed_language</language>
                    <copyright>feed_copyright</copyright>
                    <pubDate>Fri, 24 Nov 2023 03:00:00 -0500</pubDate>
                    <managingEditor>feed_author</managingEditor>
                    <dc:language>feed_language</dc:language>
                    <dc:rights>feed_copyright</dc:rights>
                    <item>
                        <title>entry_title1</title>
                        <link>entry_link1</link>
                        <description>&quot;entry&quot;
            &lt;content&gt;1</description>
                        <pubDate>Fri, 24 Nov 2023 01:00:00 -0500</pubDate>
                        <author>entry_author1</author>
                        <dc:creator>entry_author1</dc:creator>
                        <guid>entry_link1</guid>
                        <media:thumbnail url="https://rife2.com/images/logo.svg?0" />
                    </item>
                    <item>
                        <title>entry_title2</title>
                        <link>entry_link2</link>
                        <description>&quot;entry&quot;
            &lt;content&gt;2</description>
                        <pubDate>Fri, 24 Nov 2023 02:00:00 -0500</pubDate>
                        <author>entry_author2</author>
                        <dc:creator>entry_author2</dc:creator>
                        <guid>entry_link2</guid>
                    </item>
                    <item>
                        <title>entry_title3</title>
                        <link>entry_link3</link>
                        <description>&quot;entry&quot;
            &lt;content&gt;3</description>
                        <pubDate>Fri, 24 Nov 2023 03:00:00 -0500</pubDate>
                        <author>entry_author3</author>
                        <dc:creator>entry_author3</dc:creator>
                        <guid>entry_link3</guid>
                        <media:thumbnail url="https://rife2.com/images/logo.svg?2" />
                    </item>
                </channel>
            </rss>""", response.getText());
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
        assertEquals("""
            <?xml version="1.0" encoding="UTF-8"?>
            <rss xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:atom="http://www.w3.org/2005/Atom" xmlns:doap="http://usefulinc.com/ns/doap#" xmlns:foaf="http://xmlns.com/foaf/0.1/" version="2.0">
                <channel>
                    <title>feed_title_namespace</title>
                    <link>feed_link_namespace</link>
                    <atom:link href="http://localhost/namespaces_rss" rel="self" type="application/rss+xml" />
                    <description>feed_description_namespace</description>
                    <language>feed_language_namespace</language>
                    <copyright>feed_copyright_namespace</copyright>
                    <pubDate>Fri, 24 Nov 2023 02:00:00 -0500</pubDate>
                    <managingEditor>feed_author_namespace</managingEditor>
                    <dc:language>feed_language_namespace</dc:language>
                    <dc:rights>feed_copyright_namespace</dc:rights>
                    <item>
                        <title>entry_title_namespace1</title>
                        <link>entry_link_namespace1</link>
                        <category>entry_category1</category>
                        <description><doap:Project>entry_content_namespace1</doap:Project></description>
                        <pubDate>Fri, 24 Nov 2023 01:00:00 -0500</pubDate>
                        <author>entry_author_namespace1</author>
                        <dc:creator>entry_author_namespace1</dc:creator>
                        <guid>entry_link_namespace1</guid>
                    </item>
                    <item>
                        <title>entry_title_namespace2</title>
                        <link>entry_link_namespace2</link>
                        <category>entry_category2</category>
                        <description><doap:Project>entry_content_namespace2</doap:Project></description>
                        <pubDate>Fri, 24 Nov 2023 02:00:00 -0500</pubDate>
                        <author>entry_author_namespace2</author>
                        <dc:creator>entry_author_namespace2</dc:creator>
                        <guid>entry_link_namespace2</guid>
                    </item>
                </channel>
            </rss>""", response.getText());
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
        assertEquals("""
            <?xml version="1.0" encoding="utf-8"?>
            <feed xmlns="http://www.w3.org/2005/Atom" xmlns:dc="http://purl.org/dc/elements/1.1/">
                <title>feed_title</title>
                <subtitle>feed_description</subtitle>
                <id>feed_link</id>
                <updated>2023-11-24T02:00:00-0500</updated>
                <link href="feed_link" type="text/html" />
                <link href="http://localhost/atom" rel="self" />
                <category term="feed_category1"></category>
                <dc:subject>feed_category1</dc:subject>
                <category term="feed_category2"></category>
                <dc:subject>feed_category2</dc:subject>
                <rights>feed_copyright</rights>
                <author>
                    <name>feed_author</name>
                </author>
                <dc:language>feed_language</dc:language>
                <dc:rights>feed_copyright</dc:rights>
                <entry>
                    <title>entry_title1</title>
                    <link rel="alternate" type="text/html" href="entry_link1" />
                    <category term="entry_category1_1"></category>
                    <category term="entry_category1_2"></category>
                    <id>entry_link1</id>
                    <updated>2023-11-24T01:00:00-0500</updated>
                    <published>2023-11-24T01:00:00-0500</published>
                    <author>
                        <name>entry_author1</name>
                    </author>
                    <dc:creator>entry_author1</dc:creator>
                    <content type="text/html">&quot;entry&quot;
            &lt;content&gt;1</content>
                </entry>
                <entry>
                    <title>entry_title2</title>
                    <link rel="alternate" type="text/html" href="entry_link2" />
                    <category term="entry_category2_1"></category>
                    <category term="entry_category2_2"></category>
                    <id>entry_link2</id>
                    <updated>2023-11-24T02:00:00-0500</updated>
                    <published>2023-11-24T02:00:00-0500</published>
                    <author>
                        <name>entry_author2</name>
                    </author>
                    <dc:creator>entry_author2</dc:creator>
                    <content type="text/html">&quot;entry&quot;
            &lt;content&gt;2</content>
                </entry>
            </feed>""", response.getText());
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
        assertEquals("""
            <?xml version="1.0" encoding="utf-8"?>
            <feed xmlns="http://www.w3.org/2005/Atom" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:media="http://search.yahoo.com/mrss">
                <title>feed_title</title>
                <subtitle>feed_description</subtitle>
                <id>feed_link</id>
                <updated>2023-11-24T03:00:00-0500</updated>
                <link href="feed_link" type="text/html" />
                <link href="http://localhost/images_atom" rel="self" />
                <category term="feed_category"></category>
                <dc:subject>feed_category</dc:subject>
                <rights>feed_copyright</rights>
                <author>
                    <name>feed_author</name>
                </author>
                <dc:language>feed_language</dc:language>
                <dc:rights>feed_copyright</dc:rights>
                <entry>
                    <title>entry_title1</title>
                    <link rel="alternate" type="text/html" href="entry_link1" />
                    <id>entry_link1</id>
                    <updated>2023-11-24T01:00:00-0500</updated>
                    <published>2023-11-24T01:00:00-0500</published>
                    <author>
                        <name>entry_author1</name>
                    </author>
                    <dc:creator>entry_author1</dc:creator>
                    <content type="text/html">&quot;entry&quot;
            &lt;content&gt;1</content>
                    <media:thumbnail url="https://rife2.com/images/logo.svg?0" />
                </entry>
                <entry>
                    <title>entry_title2</title>
                    <link rel="alternate" type="text/html" href="entry_link2" />
                    <id>entry_link2</id>
                    <updated>2023-11-24T02:00:00-0500</updated>
                    <published>2023-11-24T02:00:00-0500</published>
                    <author>
                        <name>entry_author2</name>
                    </author>
                    <dc:creator>entry_author2</dc:creator>
                    <content type="text/html">&quot;entry&quot;
            &lt;content&gt;2</content>
                </entry>
                <entry>
                    <title>entry_title3</title>
                    <link rel="alternate" type="text/html" href="entry_link3" />
                    <id>entry_link3</id>
                    <updated>2023-11-24T03:00:00-0500</updated>
                    <published>2023-11-24T03:00:00-0500</published>
                    <author>
                        <name>entry_author3</name>
                    </author>
                    <dc:creator>entry_author3</dc:creator>
                    <content type="text/html">&quot;entry&quot;
            &lt;content&gt;3</content>
                    <media:thumbnail url="https://rife2.com/images/logo.svg?2" />
                </entry>
            </feed>""", response.getText());
    }

    @Test
    public void testFeedProviderNamespacesAtom1_0() {
        var conversation = new MockConversation(new Site() {
            public void setup() {
                get("/namespaces_atom", () -> new FeedProvider(FeedProvider.FeedType.ATOM_1_0, new NamespacesEntryProvider()));
            }
        });

        var response = conversation.doRequest("http://localhost/namespaces_atom");
        assertEquals("application/xml; charset=UTF-8", response.getContentType());
        assertEquals("""
            <?xml version="1.0" encoding="utf-8"?>
            <feed xmlns="http://www.w3.org/2005/Atom" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:doap="http://usefulinc.com/ns/doap#" xmlns:foaf="http://xmlns.com/foaf/0.1/">
                <title>feed_title_namespace</title>
                <subtitle>feed_description_namespace</subtitle>
                <id>feed_link_namespace</id>
                <updated>2023-11-24T02:00:00-0500</updated>
                <link href="feed_link_namespace" type="text/html" />
                <link href="http://localhost/namespaces_atom" rel="self" />
                <rights>feed_copyright_namespace</rights>
                <author>
                    <name>feed_author_namespace</name>
                </author>
                <dc:language>feed_language_namespace</dc:language>
                <dc:rights>feed_copyright_namespace</dc:rights>
                <entry>
                    <title>entry_title_namespace1</title>
                    <link rel="alternate" type="text/html" href="entry_link_namespace1" />
                    <category term="entry_category1"></category>
                    <id>entry_link_namespace1</id>
                    <updated>2023-11-24T01:00:00-0500</updated>
                    <published>2023-11-24T01:00:00-0500</published>
                    <author>
                        <name>entry_author_namespace1</name>
                    </author>
                    <dc:creator>entry_author_namespace1</dc:creator>
                    <content type="application/rdf+xml"><doap:Project>entry_content_namespace1</doap:Project></content>
                </entry>
                <entry>
                    <title>entry_title_namespace2</title>
                    <link rel="alternate" type="text/html" href="entry_link_namespace2" />
                    <category term="entry_category2"></category>
                    <id>entry_link_namespace2</id>
                    <updated>2023-11-24T02:00:00-0500</updated>
                    <published>2023-11-24T02:00:00-0500</published>
                    <author>
                        <name>entry_author_namespace2</name>
                    </author>
                    <dc:creator>entry_author_namespace2</dc:creator>
                    <content type="application/rdf+xml"><doap:Project>entry_content_namespace2</doap:Project></content>
                </entry>
            </feed>""", response.getText());
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
        assertEquals("""
            {
                "version": "https://jsonfeed.org/version/1.1",
                "title": "feed_title",
                "home_page_url": "feed_link",
                "description": "feed_description",
                "authors": [
                    {
                        "name": "feed_author"
                    }
                ],
                "language": "feed_language",
                "items": [
                    {
                        "id": "entry_link1",
                        "url": "entry_link1",
                        "tags": ["entry_category1_1", "entry_category1_2"],
                        "title": "entry_title1",
                        "content_html": "\\"entry\\"\\n<content>1",
                        "date_published": "2023-11-24T01:00:00-0500",
                        "authors": [
                            {
                                "name": "entry_author1"
                            }
                        ]
                    },
                    {
                        "id": "entry_link2",
                        "url": "entry_link2",
                        "tags": ["entry_category2_1", "entry_category2_2"],
                        "title": "entry_title2",
                        "content_html": "\\"entry\\"\\n<content>2",
                        "date_published": "2023-11-24T02:00:00-0500",
                        "authors": [
                            {
                                "name": "entry_author2"
                            }
                        ]
                    }
                ]
            }""", response.getText());
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
        assertEquals("""
            {
                "version": "https://jsonfeed.org/version/1.1",
                "title": "feed_title",
                "home_page_url": "feed_link",
                "description": "feed_description",
                "authors": [
                    {
                        "name": "feed_author"
                    }
                ],
                "language": "feed_language",
                "items": [
                    {
                        "id": "entry_link1",
                        "url": "entry_link1",
                        "tags": [],
                        "title": "entry_title1",
                        "content_html": "\\"entry\\"\\n<content>1",
                        "date_published": "2023-11-24T01:00:00-0500",
                        "authors": [
                            {
                                "name": "entry_author1"
                            }
                        ],
                        "image": "https://rife2.com/images/logo.svg?0"
                    },
                    {
                        "id": "entry_link2",
                        "url": "entry_link2",
                        "tags": [],
                        "title": "entry_title2",
                        "content_html": "\\"entry\\"\\n<content>2",
                        "date_published": "2023-11-24T02:00:00-0500",
                        "authors": [
                            {
                                "name": "entry_author2"
                            }
                        ]
                    },
                    {
                        "id": "entry_link3",
                        "url": "entry_link3",
                        "tags": [],
                        "title": "entry_title3",
                        "content_html": "\\"entry\\"\\n<content>3",
                        "date_published": "2023-11-24T03:00:00-0500",
                        "authors": [
                            {
                                "name": "entry_author3"
                            }
                        ],
                        "image": "https://rife2.com/images/logo.svg?2"
                    }
                ]
            }""", response.getText());
    }
}
