/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cli.dependencies;

import org.xml.sax.Attributes;
import rife.xml.Xml2Data;

import java.util.*;

public class Xml2MavenPom extends Xml2Data {
    private final Map<String, List<Dependency>> dependencies_;

    private StringBuilder characterData_ = null;
    private String lastGroupId_ = null;
    private String lastArtifactId_ = null;
    private VersionNumber lastVersion_ = null;
    private String lastType_ = null;
    private String lastScope_ = null;

    public Xml2MavenPom() {
        dependencies_ = new HashMap<>();
    }

    public List<Dependency> getDependencies(String scope) {
        var result = dependencies_.get(scope);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        characterData_ = new StringBuilder();
        if (qName.equals("dependency")) {
            resetState();
        }
    }

    public void endElement(String uri, String localName, String qName) {
        switch (qName) {
            case "dependency" -> {
                if (lastScope_ == null || lastScope_.isEmpty()) {
                    lastScope_ = "compile";
                }
                if (lastType_ == null || lastType_.equalsIgnoreCase("jar")) {
                    var dependency_list = dependencies_.computeIfAbsent(lastScope_, k -> new ArrayList<>());
                    dependency_list.add(new Dependency(lastGroupId_, lastArtifactId_, lastVersion_));
                }
                resetState();
            }
            case "groupId" -> lastGroupId_ = characterData_.toString();
            case "artifactId" -> lastArtifactId_ = characterData_.toString();
            case "version" -> lastVersion_ = VersionNumber.parse(characterData_.toString());
            case "type" -> lastType_ = characterData_.toString();
            case "scope" -> lastScope_ = characterData_.toString();
        }

        characterData_ = null;
    }

    private void resetState() {
        lastGroupId_ = null;
        lastArtifactId_ = null;
        lastVersion_ = null;
        lastType_ = null;
        lastScope_ = null;
    }

    public void characters(char[] ch, int start, int length) {
        if (characterData_ != null) {
            characterData_.append(String.copyValueOf(ch, start, length));
        }
    }
}
