/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.feed;

import org.junit.jupiter.api.Test;
import rife.engine.Site;
import rife.feed.elements.FeedProvider;
import rife.test.MockConversation;
import rife.tools.StringUtils;

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
        assertEquals(StringUtils.convertLineSeparator("""
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
                <channel>
                    <title>feed_title</title>
                    <link>feed_link</link>
                    <description>feed_description</description>
                    <language>feed_language</language>
                    <copyright>feed_copyright</copyright>
                    <pubDate>Fri, 24 Nov 2023 02:00:00 -0500</pubDate>
                    <managingEditor>feed_author</managingEditor>
                    <item>
                        <title>entry_title1</title>
                        <link>entry_link1</link>
                        <description>&quot;entry&quot;
            &lt;content&gt;1</description>
                        <pubDate>Fri, 24 Nov 2023 01:00:00 -0500</pubDate>
                        <author>entry_author1</author>
                        <guid>entry_link1</guid>
                    </item>
                    <item>
                        <title>entry_title2</title>
                        <link>entry_link2</link>
                        <description>&quot;entry&quot;
            &lt;content&gt;2</description>
                        <pubDate>Fri, 24 Nov 2023 02:00:00 -0500</pubDate>
                        <author>entry_author2</author>
                        <guid>entry_link2</guid>
                    </item>
                </channel>
            </rss>"""), StringUtils.convertLineSeparator(response.getText()));
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
            <rss xmlns:media="http://search.yahoo.com/mrss" version="2.0">
                <channel>
                    <title>feed_title</title>
                    <link>feed_link</link>
                    <description>feed_description</description>
                    <language>feed_language</language>
                    <copyright>feed_copyright</copyright>
                    <pubDate>Fri, 24 Nov 2023 03:00:00 -0500</pubDate>
                    <managingEditor>feed_author</managingEditor>
                    <item>
                        <title>entry_title1</title>
                        <link>entry_link1</link>
                        <description>&quot;entry&quot;
            &lt;content&gt;1</description>
                        <pubDate>Fri, 24 Nov 2023 01:00:00 -0500</pubDate>
                        <author>entry_author1</author>
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
                        <guid>entry_link2</guid>
                    </item>
                    <item>
                        <title>entry_title3</title>
                        <link>entry_link3</link>
                        <description>&quot;entry&quot;
            &lt;content&gt;3</description>
                        <pubDate>Fri, 24 Nov 2023 03:00:00 -0500</pubDate>
                        <author>entry_author3</author>
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
            <rss xmlns:doap="http://usefulinc.com/ns/doap#" xmlns:foaf="http://xmlns.com/foaf/0.1/" version="2.0">
                <channel>
                    <title>feed_title_namespace</title>
                    <link>feed_link_namespace</link>
                    <description>feed_description_namespace</description>
                    <language>feed_language_namespace</language>
                    <copyright>feed_copyright_namespace</copyright>
                    <pubDate>Fri, 24 Nov 2023 02:00:00 -0500</pubDate>
                    <managingEditor>feed_author_namespace</managingEditor>
                    <item>
                        <title>entry_title_namespace1</title>
                        <link>entry_link_namespace1</link>
                        <description><doap:Project>entry_content_namespace1</doap:Project></description>
                        <pubDate>Fri, 24 Nov 2023 01:00:00 -0500</pubDate>
                        <author>entry_author_namespace1</author>
                        <guid>entry_link_namespace1</guid>
                    </item>
                    <item>
                        <title>entry_title_namespace2</title>
                        <link>entry_link_namespace2</link>
                        <description><doap:Project>entry_content_namespace2</doap:Project></description>
                        <pubDate>Fri, 24 Nov 2023 02:00:00 -0500</pubDate>
                        <author>entry_author_namespace2</author>
                        <guid>entry_link_namespace2</guid>
                    </item>
                </channel>
            </rss>""", response.getText());
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
        assertEquals("""
            <?xml version="1.0" encoding="UTF-8"?>
            <feed xmlns="http://purl.org/atom/ns#" xmlns:taxo="http://purl.org/rss/1.0/modules/taxonomy/" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:sy="http://purl.org/rss/1.0/modules/syndication/" xmlns:dc="http://purl.org/dc/elements/1.1/" version="0.3">
                <title>feed_title</title>
                <link rel="alternate" href="feed_link" type="text/html" />
                <author>
                    <name>feed_author</name>
                </author>
                <copyright>feed_copyright</copyright>
                <info>feed_description</info>
                <modified>2023-11-24T02:00:00-0500</modified>
                <dc:creator>feed_author</dc:creator>
                <dc:date>2023-11-24T02:00:00-0500</dc:date>
                <dc:language>feed_language</dc:language>
                <dc:rights>feed_copyright</dc:rights>
                <entry>
                    <title>entry_title1</title>
                    <link rel="alternate" href="entry_link1" type="text/html" />
                    <author>
                        <name>entry_author1</name>
                    </author>
                    <modified>2023-11-24T01:00:00-0500</modified>
                    <content type="text/html" mode="escaped">&quot;entry&quot;
            &lt;content&gt;1</content>
                    <id>entry_link1</id>
                    <issued>2023-11-24T01:00:00-0500</issued>
                    <dc:creator>entry_author1</dc:creator>
                    <dc:date>2023-11-24T01:00:00-0500</dc:date>
                </entry>
                <entry>
                    <title>entry_title2</title>
                    <link rel="alternate" href="entry_link2" type="text/html" />
                    <author>
                        <name>entry_author2</name>
                    </author>
                    <modified>2023-11-24T02:00:00-0500</modified>
                    <content type="text/html" mode="escaped">&quot;entry&quot;
            &lt;content&gt;2</content>
                    <id>entry_link2</id>
                    <issued>2023-11-24T02:00:00-0500</issued>
                    <dc:creator>entry_author2</dc:creator>
                    <dc:date>2023-11-24T02:00:00-0500</dc:date>
                </entry>
            </feed>""", response.getText());
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
        assertEquals("""
            <?xml version="1.0" encoding="UTF-8"?>
            <feed xmlns="http://purl.org/atom/ns#" xmlns:taxo="http://purl.org/rss/1.0/modules/taxonomy/" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:sy="http://purl.org/rss/1.0/modules/syndication/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:doap="http://usefulinc.com/ns/doap#" xmlns:foaf="http://xmlns.com/foaf/0.1/" version="0.3">
                <title>feed_title_namespace</title>
                <link rel="alternate" href="feed_link_namespace" type="text/html" />
                <author>
                    <name>feed_author_namespace</name>
                </author>
                <copyright>feed_copyright_namespace</copyright>
                <info>feed_description_namespace</info>
                <modified>2023-11-24T02:00:00-0500</modified>
                <dc:creator>feed_author_namespace</dc:creator>
                <dc:date>2023-11-24T02:00:00-0500</dc:date>
                <dc:language>feed_language_namespace</dc:language>
                <dc:rights>feed_copyright_namespace</dc:rights>
                <entry>
                    <title>entry_title_namespace1</title>
                    <link rel="alternate" href="entry_link_namespace1" type="text/html" />
                    <author>
                        <name>entry_author_namespace1</name>
                    </author>
                    <modified>2023-11-24T01:00:00-0500</modified>
                    <content type="application/rdf+xml"><doap:Project>entry_content_namespace1</doap:Project></content>
                    <id>entry_link_namespace1</id>
                    <issued>2023-11-24T01:00:00-0500</issued>
                    <dc:creator>entry_author_namespace1</dc:creator>
                    <dc:date>2023-11-24T01:00:00-0500</dc:date>
                </entry>
                <entry>
                    <title>entry_title_namespace2</title>
                    <link rel="alternate" href="entry_link_namespace2" type="text/html" />
                    <author>
                        <name>entry_author_namespace2</name>
                    </author>
                    <modified>2023-11-24T02:00:00-0500</modified>
                    <content type="application/rdf+xml"><doap:Project>entry_content_namespace2</doap:Project></content>
                    <id>entry_link_namespace2</id>
                    <issued>2023-11-24T02:00:00-0500</issued>
                    <dc:creator>entry_author_namespace2</dc:creator>
                    <dc:date>2023-11-24T02:00:00-0500</dc:date>
                </entry>
            </feed>""", response.getText());
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
            <feed xmlns="http://www.w3.org/2005/Atom">
                <title>feed_title</title>
                <subtitle>feed_description</subtitle>
                <id>feed_link</id>
                <updated>2023-11-24T02:00:00-0500</updated>
                <link href="feed_link" type="text/html" />
                <rights>feed_copyright</rights>
                <author>
                    <name>feed_author</name>
                </author>
                <entry>
                    <title>entry_title1</title>
                    <link rel="alternate" type="text/html" href="entry_link1" />
                    <id>entry_link1</id>
                    <updated>2023-11-24T01:00:00-0500</updated>
                    <published>2023-11-24T01:00:00-0500</published>
                    <author>
                        <name>entry_author1</name>
                    </author>
                    <content type="text/html">&quot;entry&quot;
            &lt;content&gt;1</content>
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
            <feed xmlns="http://www.w3.org/2005/Atom" xmlns:media="http://search.yahoo.com/mrss">
                <title>feed_title</title>
                <subtitle>feed_description</subtitle>
                <id>feed_link</id>
                <updated>2023-11-24T03:00:00-0500</updated>
                <link href="feed_link" type="text/html" />
                <rights>feed_copyright</rights>
                <author>
                    <name>feed_author</name>
                </author>
                <entry>
                    <title>entry_title1</title>
                    <link rel="alternate" type="text/html" href="entry_link1" />
                    <id>entry_link1</id>
                    <updated>2023-11-24T01:00:00-0500</updated>
                    <published>2023-11-24T01:00:00-0500</published>
                    <author>
                        <name>entry_author1</name>
                    </author>
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
                get("/atom", () -> new FeedProvider(FeedProvider.FeedType.ATOM_1_0, new NamespacesEntryProvider()));
            }
        });

        var response = conversation.doRequest("http://localhost/atom");
        assertEquals("application/xml; charset=UTF-8", response.getContentType());
        assertEquals("""
            <?xml version="1.0" encoding="utf-8"?>
            <feed xmlns="http://www.w3.org/2005/Atom" xmlns:doap="http://usefulinc.com/ns/doap#" xmlns:foaf="http://xmlns.com/foaf/0.1/">
                <title>feed_title_namespace</title>
                <subtitle>feed_description_namespace</subtitle>
                <id>feed_link_namespace</id>
                <updated>2023-11-24T02:00:00-0500</updated>
                <link href="feed_link_namespace" type="text/html" />
                <rights>feed_copyright_namespace</rights>
                <author>
                    <name>feed_author_namespace</name>
                </author>
                <entry>
                    <title>entry_title_namespace1</title>
                    <link rel="alternate" type="text/html" href="entry_link_namespace1" />
                    <id>entry_link_namespace1</id>
                    <updated>2023-11-24T01:00:00-0500</updated>
                    <published>2023-11-24T01:00:00-0500</published>
                    <author>
                        <name>entry_author_namespace1</name>
                    </author>
                    <content type="application/rdf+xml"><doap:Project>entry_content_namespace1</doap:Project></content>
                </entry>
                <entry>
                    <title>entry_title_namespace2</title>
                    <link rel="alternate" type="text/html" href="entry_link_namespace2" />
                    <id>entry_link_namespace2</id>
                    <updated>2023-11-24T02:00:00-0500</updated>
                    <published>2023-11-24T02:00:00-0500</published>
                    <author>
                        <name>entry_author_namespace2</name>
                    </author>
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
                        "title": "entry_title2",
                        "content_html": "\\"entry\\"\\n<content>2",
                        "date_published": "2023-11-24T02:00:00-0500",
                        "authors": [
                            {
                                "name": "entry_author2"
                            }
                        ]
                    },
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
                        "title": "entry_title3",
                        "content_html": "\\"entry\\"\\n<content>3",
                        "date_published": "2023-11-24T03:00:00-0500",
                        "authors": [
                            {
                                "name": "entry_author3"
                            }
                        ],
                        "image": "https://rife2.com/images/logo.svg?2"
                    },
                ]
            }""", response.getText());
    }
}
