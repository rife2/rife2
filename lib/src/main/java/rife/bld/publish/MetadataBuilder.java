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

public class MetadataBuilder {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private PublishInfo info_ = null;
    private ZonedDateTime timestamp_ = null;

    public MetadataBuilder info(PublishInfo info) {
        info_ = info;
        return this;
    }

    public PublishInfo info() {
        return info_;
    }

    public MetadataBuilder updated(ZonedDateTime timestamp) {
        timestamp_ = timestamp;
        return this;
    }

    public ZonedDateTime updated() {
        return timestamp_;
    }

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
