/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.feed;

import java.util.Date;
import java.util.Map;

/**
 * A bean representing a feed, or rather a feed's metadata.
 * <p>A <code>Feed</code> is a set of metadata that helps to describe a feed
 * for the user and/or the engine processing the feed.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see rife.feed.Entry
 * @since 1.8.0
 */
public class Feed {
    private String id_ = null;
    private String title_ = null;
    private String link_ = null;
    private String description_ = null;
    private String language_ = null;
    private String copyright_ = null;
    private Date publishedDate_ = null;
    private String author_ = null;
    private Map<String, String> namespaces_ = null;

    public Feed id(String id) {
        setId(id);
        return this;
    }

    public String getId() {
        if (null == id_) {
            return getLink();
        }

        return id_;
    }

    public void setId(String id) {
        id_ = id;
    }

    public Feed title(String title) {
        setTitle(title);
        return this;
    }

    public String getTitle() {
        return title_;
    }

    public void setTitle(String title) {
        title_ = title;
    }

    public Feed link(String link) {
        setLink(link);
        return this;
    }

    public String getLink() {
        return link_;
    }

    public void setLink(String link) {
        link_ = link;
    }

    public Feed description(String description) {
        setDescription(description);
        return this;
    }

    public String getDescription() {
        return description_;
    }

    public void setDescription(String description) {
        description_ = description;
    }

    public Feed language(String language) {
        setLanguage(language);
        return this;
    }

    public String getLanguage() {
        return language_;
    }

    public void setLanguage(String language) {
        language_ = language;
    }

    public Feed copyright(String copyright) {
        setCopyright(copyright);
        return this;
    }

    public String getCopyright() {
        return copyright_;
    }

    public void setCopyright(String copyright) {
        copyright_ = copyright;
    }

    public Feed publishedDate(Date publishedDate) {
        setPublishedDate(publishedDate);
        return this;
    }

    public Date getPublishedDate() {
        return publishedDate_;
    }

    public void setPublishedDate(Date publishedDate) {
        publishedDate_ = publishedDate;
    }

    public Feed author(String author) {
        setAuthor(author);
        return this;
    }

    public String getAuthor() {
        return author_;
    }

    public void setAuthor(String author) {
        author_ = author;
    }

    public Map<String, String> getNamespaces() {
        return namespaces_;
    }

    public void setNamespaces(Map<String, String> namespaces) {
        namespaces_ = namespaces;
    }

    public Feed namespaces(Map<String, String> namespaces) {
        setNamespaces(namespaces);
        return this;
    }
}
