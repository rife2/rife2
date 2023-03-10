/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies;

import java.util.Objects;
import java.util.regex.Pattern;

public record VersionNumber(Integer major, Integer minor, Integer revision, String qualifier, String separator) {
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

        var major = matcher.group("major");
        var minor = matcher.group("minor");
        var revision = matcher.group("revision");

        var major_integer = (major != null ? Integer.parseInt(major) : null);
        var minor_integer = (minor != null ? Integer.parseInt(minor) : null);
        var revision_integer = (revision != null ? Integer.parseInt(revision) : null);

        var qualifier = matcher.group("qualifier");
        var separator = matcher.group("separator");

        return new VersionNumber(major_integer, minor_integer, revision_integer, qualifier, separator);
    }

    public VersionNumber(Integer major) {
        this(major, null, null, "");
    }

    public VersionNumber(Integer major, Integer minor) {
        this(major, minor, null, "");
    }

    public VersionNumber(Integer major, Integer minor, Integer revision) {
        this(major, minor, revision, "");
    }

    public VersionNumber(Integer major, Integer minor, Integer revision, String qualifier) {
        this(major, minor, revision, qualifier, "-");
    }

    public VersionNumber(Integer major, Integer minor, Integer revision, String qualifier, String separator) {
        this.major = major;
        this.minor = minor;
        this.revision = revision;
        this.qualifier = (qualifier == null ? "" : qualifier);
        this.separator = separator;
    }

    private int majorInt() {
        return major == null ? 0 : major;
    }

    private int minorInt() {
        return minor == null ? 0 : minor;
    }

    private int revisionInt() {
        return revision == null ? 0 : revision;
    }

    public VersionNumber getBaseVersion() {
        return new VersionNumber(major, minor, revision, null);
    }

    public int compareTo(VersionNumber other) {
        if (majorInt() != other.majorInt()) {
            return majorInt() - other.majorInt();
        }
        if (minorInt() != other.minorInt()) {
            return minorInt() - other.minorInt();
        }
        if (revisionInt() != other.revisionInt()) {
            return revisionInt() - other.revisionInt();
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
        version.append(majorInt());
        if (minor != null || revision != null) {
            version.append(".");
            version.append(minorInt());
        }
        if (revision != null) {
            version.append(".");
            version.append(revisionInt());
        }
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
        int result = majorInt();
        result = 31 * result + minorInt();
        result = 31 * result + minorInt();
        result = 31 * result + Objects.hashCode(qualifier);
        return result;
    }
}
