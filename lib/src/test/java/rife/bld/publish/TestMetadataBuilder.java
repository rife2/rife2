/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.publish;

import org.junit.jupiter.api.Test;
import rife.bld.dependencies.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TestMetadataBuilder {
    @Test
    void testInstantiation() {
        var builder = new MetadataBuilder();
        assertNull(builder.info());
        assertNull(builder.updated());
    }

    @Test
    void testEmptyBuild() {
        var builder = new MetadataBuilder();
        assertEquals("""
            <?xml version="1.0" encoding="UTF-8"?>
            <metadata>
              <groupId></groupId>
              <artifactId></artifactId>
              <versioning>
                <latest></latest>
                <release></release>
                <versions>
                  <version></version>
                </versions>
                <lastUpdated></lastUpdated>
              </versioning>
            </metadata>
            """, builder.build());
    }

    @Test
    void testMainInfoBuild() {
        var builder = new MetadataBuilder()
            .info(new PublishInfo()
                .groupId("com.example")
                .artifactId("myapp")
                .version(VersionNumber.parse("1.2.3-SNAPSHOT")));
        assertEquals("""
            <?xml version="1.0" encoding="UTF-8"?>
            <metadata>
              <groupId>com.example</groupId>
              <artifactId>myapp</artifactId>
              <versioning>
                <latest>1.2.3-SNAPSHOT</latest>
                <release>1.2.3-SNAPSHOT</release>
                <versions>
                  <version>1.2.3-SNAPSHOT</version>
                </versions>
                <lastUpdated></lastUpdated>
              </versioning>
            </metadata>
            """, builder.build());
    }

    @Test
    void testUpdatedBuild() {
        var builder = new MetadataBuilder()
            .updated(ZonedDateTime.of(2023, 3, 27, 8, 56, 17, 123, ZoneId.of("America/New_York")));
        assertEquals("""
            <?xml version="1.0" encoding="UTF-8"?>
            <metadata>
              <groupId></groupId>
              <artifactId></artifactId>
              <versioning>
                <latest></latest>
                <release></release>
                <versions>
                  <version></version>
                </versions>
                <lastUpdated>20230327125617</lastUpdated>
              </versioning>
            </metadata>
            """, builder.build());
    }

    @Test
    void testCompleteBuild() {
        var builder = new MetadataBuilder()
            .info(new PublishInfo()
                .groupId("com.example")
                .artifactId("myapp")
                .version(VersionNumber.parse("1.2.3-SNAPSHOT")))
            .updated(ZonedDateTime.of(2023, 3, 27, 8, 56, 17, 123, ZoneId.of("America/New_York")));
        assertEquals("""
            <?xml version="1.0" encoding="UTF-8"?>
            <metadata>
              <groupId>com.example</groupId>
              <artifactId>myapp</artifactId>
              <versioning>
                <latest>1.2.3-SNAPSHOT</latest>
                <release>1.2.3-SNAPSHOT</release>
                <versions>
                  <version>1.2.3-SNAPSHOT</version>
                </versions>
                <lastUpdated>20230327125617</lastUpdated>
              </versioning>
            </metadata>
            """, builder.build());
    }
}
