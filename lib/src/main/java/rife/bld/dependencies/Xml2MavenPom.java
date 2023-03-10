/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies;

import org.xml.sax.Attributes;
import rife.bld.DependencySet;
import rife.xml.Xml2Data;

import java.util.*;

public class Xml2MavenPom extends Xml2Data {
    private final Map<Scope, DependencySet> dependencies_;

    private final Stack<String> elementStack_ = new Stack<>();
    private boolean collectDependencies_ = false;
    private StringBuilder characterData_ = null;
    private String lastGroupId_ = null;
    private String lastArtifactId_ = null;
    private VersionNumber lastVersion_ = null;
    private String lastType_ = null;
    private String lastScope_ = null;

    public Xml2MavenPom() {
        dependencies_ = new HashMap<>();
    }

    public DependencySet getDependencies(Scope scope) {
        var result = dependencies_.get(scope);
        if (result == null) {
            return new DependencySet();
        }

        return result;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        characterData_ = new StringBuilder();
        switch (qName) {
            case "dependencies" -> {
                if (elementStack_.peek().equals("project")) {
                    collectDependencies_ = true;
                }
            }
            case "dependency" -> {
                if (collectDependencies_) resetState();
            }
        }

        elementStack_.push(qName);
    }

    public void endElement(String uri, String localName, String qName) {
        elementStack_.pop();

        switch (qName) {
            case "dependencies" -> collectDependencies_ = false;
            case "dependency" -> {
                if (collectDependencies_) {
                    if (lastScope_ == null || lastScope_.isEmpty()) {
                        lastScope_ = "compile";
                    }
                    if (lastType_ == null || lastType_.equalsIgnoreCase("jar")) {
                        var scope = Scope.valueOf(lastScope_);
                        if (scope != null) {
                            var dependency_list = dependencies_.computeIfAbsent(scope, k -> new DependencySet());
                            dependency_list.add(new Dependency(lastGroupId_, lastArtifactId_, lastVersion_));
                        }
                    }
                    resetState();
                }
            }
            case "groupId" -> {
                if (collectDependencies_) lastGroupId_ = characterData_.toString();
            }
            case "artifactId" -> {
                if (collectDependencies_) lastArtifactId_ = characterData_.toString();
            }
            case "version" -> {
                if (collectDependencies_) lastVersion_ = VersionNumber.parse(characterData_.toString());
            }
            case "type" -> {
                if (collectDependencies_) lastType_ = characterData_.toString();
            }
            case "scope" -> {
                if (collectDependencies_) lastScope_ = characterData_.toString();
            }
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
