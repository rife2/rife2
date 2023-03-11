/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies;

import org.xml.sax.Attributes;
import rife.bld.DependencySet;
import rife.xml.Xml2Data;

import java.util.*;
import java.util.regex.Pattern;

public class Xml2MavenPom extends Xml2Data {
    record PomDependency(String groupId, String artifactId, String version, String type, String classifier, String scope, String optional) {
        PomDependency(String groupId, String artifactId, String version, String type, String classifier, String scope, String optional) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.type = (type == null || type.isEmpty() ? "jar" : type);
            this.classifier = (classifier == null ? "" : classifier);
            this.scope = (scope == null || scope.isEmpty() ? "compile" : scope);
            this.optional = (optional == null ? "" : optional);
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PomDependency that = (PomDependency) o;
            return groupId.equals(that.groupId) && artifactId.equals(that.artifactId) && classifier.equals(that.classifier) && type.equals(that.type);
        }

        public int hashCode() {
            return Objects.hash(groupId, artifactId, classifier, type);
        }
    }

    private final Repository repository_;
    private Map<Scope, DependencySet> resolvedDependencies_ = null;

    private final Map<PomDependency, PomDependency> dependencyManagement_ = new LinkedHashMap<>();
    private final Map<Scope, Set<PomDependency>> dependencies_ = new HashMap<>();
    private final Map<String, String> properties_ = new HashMap<>();
    private final Stack<String> elementStack_ = new Stack<>();

    private boolean collectProperties_ = false;
    private boolean collectDependencyManagement_ = false;
    private boolean collectDependencies_ = false;
    private StringBuilder characterData_ = null;
    private String lastGroupId_ = null;
    private String lastArtifactId_ = null;
    private String lastVersion_ = null;
    private String lastType_ = null;
    private String lastClassifier_ = null;
    private String lastScope_ = null;
    private String lastOptional_ = null;

    public Xml2MavenPom(Repository repository) {
        repository_ = repository;
    }

    public DependencySet getDependencies(Scope scope) {
        if (scope == null) {
            return new DependencySet();
        }

        if (resolvedDependencies_ == null) {
            var resolved_dependencies = new HashMap<Scope, DependencySet>();

            var resolved_dependency_set = resolved_dependencies.computeIfAbsent(scope, k -> new DependencySet());
            var dependencies = dependencies_.get(scope);
            if (dependencies != null && !dependencies.isEmpty()) {
                for (var dependency : dependencies) {

                    var managed_dependency = dependencyManagement_.get(dependency);
                    var version = dependency.version;
                    var optional = dependency.optional;
                    if (managed_dependency != null) {
                        if (version == null || version.isEmpty()) {
                            version = managed_dependency.version;
                        }
                        if (optional == null || optional.isEmpty()) {
                            optional = managed_dependency.optional;
                        }
                    }
                    optional = resolveProperties(optional);
                    if (optional.equals("true")) {
                        continue;
                    }

                    var resolved_dependency = new Dependency(
                        resolveProperties(dependency.groupId),
                        resolveProperties(dependency.artifactId),
                        VersionNumber.parse(resolveProperties(version)),
                        resolveProperties(dependency.classifier),
                        resolveProperties(dependency.type));
                    resolved_dependency_set.add(resolved_dependency);
                }
            }

            resolvedDependencies_ = resolved_dependencies;
        }

        var result = resolvedDependencies_.get(scope);
        if (result == null) {
            return new DependencySet();
        }

        return result;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        characterData_ = new StringBuilder();

        switch (qName) {
            case "parent" -> resetState();
            case "properties" -> collectProperties_ = true;
            case "dependencyManagement" -> collectDependencyManagement_ = true;
            case "dependencies" -> {
                if (isChildOfProject()) {
                    resetState();
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
            case "parent" -> {
                var parent_dependency = new Dependency(lastGroupId_, lastArtifactId_, VersionNumber.parse(lastVersion_));
                var parent = new DependencyResolver(repository_, parent_dependency).getMavenPom();

                parent.properties_.keySet().removeAll(properties_.keySet());
                properties_.putAll(parent.properties_);

                parent.dependencyManagement_.keySet().removeAll(dependencyManagement_.keySet());
                dependencyManagement_.putAll(parent.dependencyManagement_);

                for (var parent_entry : parent.dependencies_.entrySet()) {
                    var dependency_set = dependencies_.computeIfAbsent(parent_entry.getKey(), k -> new LinkedHashSet<>());
                    parent_entry.getValue().removeAll(dependency_set);
                    dependency_set.addAll(parent_entry.getValue());
                }
                resetState();
            }
            case "properties" -> collectProperties_ = false;
            case "dependencyManagement" -> collectDependencyManagement_ = false;
            case "dependencies" -> collectDependencies_ = false;
            case "dependency" -> {
                if (lastScope_ == null || lastScope_.isEmpty()) {
                    lastScope_ = "compile";
                }
                if (lastType_ == null || lastType_.equalsIgnoreCase("jar")) {
                    var dependency = new PomDependency(lastGroupId_, lastArtifactId_, lastVersion_, lastType_, lastClassifier_, lastScope_, lastOptional_);
                    if (collectDependencyManagement_) {
                        dependencyManagement_.put(dependency, dependency);
                    } else if (collectDependencies_) {
                        var scope = Scope.valueOf(lastScope_);
                        if (scope != null) {
                            var dependency_set = dependencies_.computeIfAbsent(scope, k -> new LinkedHashSet<>());
                            dependency_set.add(dependency);
                        }
                    }
                }
                resetState();
            }
            case "groupId" -> {
                if (isChildOfProject()) {
                    addProjectProperty(qName);
                } else if(isChildOfParent() || isChildOfDependency()) {
                    lastGroupId_ = getCharacterData();
                }
            }
            case "artifactId" -> {
                if (isChildOfProject()) {
                    addProjectProperty(qName);
                } else if(isChildOfParent() || isChildOfDependency()) {
                    lastArtifactId_ = getCharacterData();
                }
            }
            case "version" -> {
                if (isChildOfProject()) {
                    addProjectProperty(qName);
                } else if (isChildOfParent() || isChildOfDependency()) {
                    lastVersion_ = getCharacterData();
                }
            }
            case "type" -> {
                if (isChildOfDependency()) {
                    lastType_ = getCharacterData();
                }
            }
            case "classifier" -> {
                if (isChildOfDependency()) {
                    lastClassifier_ = getCharacterData();
                }
            }
            case "scope" -> {
                if (isChildOfDependency()) {
                    lastScope_ = getCharacterData();
                }
            }
            case "optional" -> {
                if (isChildOfDependency()) {
                    lastOptional_ = getCharacterData();
                }
            }
            case "packaging", "name", "description", "url", "inceptionYear" -> {
                if (isChildOfProject()) {
                    addProjectProperty(qName);
                }
            }
            default -> {
                if (collectProperties_) {
                    properties_.put(qName, getCharacterData());
                }
            }
        }

        characterData_ = null;
    }

    private boolean isChildOfProject() {
        return elementStack_.peek().equals("project");
    }

    private boolean isChildOfParent() {
        return elementStack_.peek().equals("parent");
    }

    private boolean isChildOfDependency() {
        return elementStack_.peek().equals("dependency");
    }

    private void addProjectProperty(String name) {
        properties_.put("project." + name, getCharacterData());
    }

    private String getCharacterData() {
        return characterData_.toString();
    }

    private static final Pattern MAVEN_PROPERTY = Pattern.compile("\\$\\{([^<>{}]+)}");

    private String resolveProperties(String data) {
        if (data == null) {
            return null;
        }

        var processed_data = new StringBuilder();
        var matcher = MAVEN_PROPERTY.matcher(data);
        var last_end = 0;
        while (matcher.find()) {
            if (matcher.groupCount() == 1) {
                var property = matcher.group(1);
                if (properties_.containsKey(property)) {
                    processed_data.append(data, last_end, matcher.start());
                    processed_data.append(properties_.get(property));
                    last_end = matcher.end();
                }
            }
        }
        if (last_end < data.length()) {
            processed_data.append(data.substring(last_end));
        }

        return processed_data.toString();
    }

    private void resetState() {
        lastGroupId_ = null;
        lastArtifactId_ = null;
        lastVersion_ = null;
        lastType_ = null;
        lastClassifier_ = null;
        lastScope_ = null;
        lastOptional_ = null;
    }

    public void characters(char[] ch, int start, int length) {
        if (characterData_ != null) {
            characterData_.append(String.copyValueOf(ch, start, length));
        }
    }
}
