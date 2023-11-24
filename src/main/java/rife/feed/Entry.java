/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.feed;

import java.util.Date;

/**
 * A bean representing an entry in a feed.
 * <p>An <code>Entry</code> is a single piece of content, (forum message, news article,
 * blog post), with its own title, link to permanent content,
 * published date, content and author. Has a many-to-one relationship
 * with <code>Feed</code>.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see rife.feed.Feed
 * @since 1.8.0
 */
public class Entry {
    private String id_ = null;
    private String title_ = null;
    private String link_ = null;
    private Date publishedDate_ = null;
    private String content_ = null;
    private String author_ = null;
    private String type_ = "text/html";
    private boolean escaped_ = true;
    private String image_ = null;

    public Entry id(String id) {
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

    public Entry title(String title) {
        setTitle(title);
        return this;
    }

    public String getTitle() {
        return title_;
    }

    public void setTitle(String title) {
        title_ = title;
    }

    public Entry link(String link) {
        setLink(link);
        return this;
    }

    public String getLink() {
        return link_;
    }

    public void setLink(String link) {
        link_ = link;
    }

    public Entry publishedDate(Date publishedDate) {
        setPublishedDate(publishedDate);
        return this;
    }

    public Date getPublishedDate() {
        return publishedDate_;
    }

    public void setPublishedDate(Date publishedDate) {
        publishedDate_ = publishedDate;
    }

    public Entry content(String content) {
        setContent(content);
        return this;
    }

    public String getContent() {
        return content_;
    }

    public void setContent(String content) {
        content_ = content;
    }

    public Entry author(String author) {
        setAuthor(author);
        return this;
    }

    public String getAuthor() {
        return author_;
    }

    public void setAuthor(String author) {
        author_ = author;
    }

    public String getType() {
        return type_;
    }

    public void setType(String type) {
        type_ = type;
    }

    public Entry type(String type) {
        setType(type);

        return this;
    }

    public boolean isEscaped() {
        return escaped_;
    }

    public void setEscaped(boolean escaped) {
        escaped_ = escaped;
    }

    public Entry escaped(boolean escaped) {
        setEscaped(escaped);
        return this;
    }

    public Entry image(String image) {
        setImage(image);
        return this;
    }

    public String getImage() {
        return image_;
    }

    public void setImage(String image) {
        image_ = image;
    }
}
