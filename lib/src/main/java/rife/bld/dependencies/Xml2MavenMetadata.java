/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies;

import org.xml.sax.Attributes;
import rife.xml.Xml2Data;

import java.util.ArrayList;
import java.util.List;

public class Xml2MavenMetadata extends Xml2Data {
    private VersionNumber latest_ = VersionNumber.UNKNOWN;
    private VersionNumber release_ = VersionNumber.UNKNOWN;
    private final List<VersionNumber> versions_;

    private StringBuilder characterData_ = null;

    public Xml2MavenMetadata() {
        versions_ = new ArrayList<>();
    }

    public VersionNumber getLatest() {
        return latest_;
    }

    public VersionNumber getRelease() {
        return release_;
    }

    public List<VersionNumber> getVersions() {
        return versions_;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        characterData_ = new StringBuilder();
    }

    public void endElement(String uri, String localName, String qName) {
        switch (qName) {
            case "latest" -> latest_ = VersionNumber.parse(characterData_.toString());
            case "release" -> release_ = VersionNumber.parse(characterData_.toString());
            case "version" -> versions_.add(VersionNumber.parse(characterData_.toString()));
        }

        characterData_ = null;
    }

    public void characters(char[] ch, int start, int length) {
        if (characterData_ != null) {
            characterData_.append(String.copyValueOf(ch, start, length));
        }
    }
}
