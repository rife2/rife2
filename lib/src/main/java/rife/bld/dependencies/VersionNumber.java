/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies;

import java.util.Objects;
import java.util.regex.Pattern;

public record VersionNumber(int major, int minor, int revision, String qualifier, String separator) {
    public static final VersionNumber UNKNOWN = new VersionNumber(0, 0, 0, "");

    private static final Pattern VERSION_PATTERN = Pattern.compile("^(?<major>\\d+)(?:\\.(?<minor>\\d+)(?:\\.(?<revision>\\d+))?)?+(?:(?<separator>[.\\-])(?<qualifier>.*[^.\\-]))??$");

    public static VersionNumber parse(String version) {
        if (version == null || version.isEmpty()) {
            return UNKNOWN;
        }

        var matcher = VERSION_PATTERN.matcher(version);
        if (!matcher.matches()) {
            return UNKNOWN;
        }

        var major = Integer.parseInt(matcher.group("major"));
        var minor = Integer.parseInt(Objects.requireNonNullElse(matcher.group("minor"), "0"));
        var revision = Integer.parseInt(Objects.requireNonNullElse(matcher.group("revision"), "0"));
        var qualifier = matcher.group("qualifier");
        var separator = matcher.group("separator");

        return new VersionNumber(major, minor, revision, qualifier, separator);
    }

    public VersionNumber(int major) {
        this(major, 0, 0, "");
    }

    public VersionNumber(int major, int minor) {
        this(major, minor, 0, "");
    }

    public VersionNumber(int major, int minor, int revision) {
        this(major, minor, revision, "");
    }

    public VersionNumber(int major, int minor, int revision, String qualifier) {
        this(major, minor, revision, qualifier, "-");
    }

    public VersionNumber(int major, int minor, int revision, String qualifier, String separator) {
        this.major = major;
        this.minor = minor;
        this.revision = revision;
        this.qualifier = (qualifier == null ? "" : qualifier);
        this.separator = separator;
    }

    public VersionNumber getBaseVersion() {
        return new VersionNumber(major, minor, revision, null);
    }

    public int compareTo(VersionNumber other) {
        if (major != other.major) {
            return major - other.major;
        }
        if (minor != other.minor) {
            return minor - other.minor;
        }
        if (revision != other.revision) {
            return revision - other.revision;
        }

        if (qualifier.equals(other.qualifier)) {
            return 0;
        } else if (qualifier.isEmpty()) {
            return 1;
        } else if (other.qualifier.isEmpty()) {
            return -1;
        }

        return qualifier.toLowerCase().compareTo(other.qualifier.toLowerCase());
    }

    public String toString() {
        var version = new StringBuilder();
        version.append(major);
        version.append(".");
        version.append(minor);
        version.append(".");
        version.append(revision);
        if (qualifier != null && !qualifier.isEmpty()) {
            version.append(separator);
            version.append(qualifier);
        }
        return version.toString();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof VersionNumber && compareTo((VersionNumber) other) == 0;
    }

    @Override
    public int hashCode() {
        int result = major;
        result = 31 * result + minor;
        result = 31 * result + minor;
        result = 31 * result + Objects.hashCode(qualifier);
        return result;
    }
}
