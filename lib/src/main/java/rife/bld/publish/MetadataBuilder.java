/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.publish;

import rife.template.TemplateFactory;
import rife.tools.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Provides the functionalities to build a Maven metadata xml file.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5.7
 */
public class MetadataBuilder {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private PublishInfo info_ = null;
    private ZonedDateTime timestamp_ = null;

    /**
     * Provides the publishing info to build the metadata for.
     *
     * @param info the publishing info to use
     * @return this {@code MetadataBuilder} instance
     * @since 1.5.7
     */
    public MetadataBuilder info(PublishInfo info) {
        info_ = info;
        return this;
    }

    /**
     * Retrieves the publishing info to build the metadata for.
     *
     * @return the publishing info
     * @since 1.5.7
     */
    public PublishInfo info() {
        return info_;
    }

    /**
     * Provides the updated timestamp for the metadata.
     *
     * @param timestamp the publishing updated timestamp
     * @return this {@code MetadataBuilder} instance
     * @since 1.5.7
     */
    public MetadataBuilder updated(ZonedDateTime timestamp) {
        timestamp_ = timestamp;
        return this;
    }

    /**
     * Retrieves the updated timestamp for the metadata.
     *
     * @return the publishing updated timestamp
     * @since 1.5.7
     */
    public ZonedDateTime updated() {
        return timestamp_;
    }

    /**
     * Builds the Maven metadata xml file.
     *
     * @return the generated Maven metadata xml file as a string
     * @since 1.5.7
     */
    public String build() {
        var t = TemplateFactory.XML.get("bld.maven_metadata_blueprint");

        var info = info();
        if (info != null) {
            t.setValueEncoded("groupId", Objects.requireNonNullElse(info.groupId(), ""));
            t.setValueEncoded("artifactId", Objects.requireNonNullElse(info.artifactId(), ""));
            t.setValueEncoded("version", Objects.requireNonNullElse(info.version(), ""));
        }
        if (timestamp_ != null) {
            t.setValueEncoded("timestamp", Objects.requireNonNullElse(TIMESTAMP_FORMATTER.format(updated().withZoneSameInstant(ZoneId.of("UTC"))), ""));
        }

        return StringUtils.stripBlankLines(t.getContent());
    }
}
