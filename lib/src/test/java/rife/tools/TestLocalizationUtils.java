/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestLocalizationUtils {
    @Test
    public void testExtractLocalizedUrl() {
        assertEquals("/root", Localization.extractLocalizedUrl("en:/root,nl:/wortel"));
        assertEquals("/logout", Localization.extractLocalizedUrl("nl:/afmelden,/logout,fr:/deconnection"));
        assertNull(Localization.extractLocalizedUrl(null));
        assertEquals("/root", Localization.extractLocalizedUrl("/root"));
        assertNull(Localization.extractLocalizedUrl("nl:/afmelden,fr:/deconnection"));
        assertEquals("http://www.uwyn.com", Localization.extractLocalizedUrl("http://www.uwyn.com"));
        assertEquals("http://www.uwyn.com", Localization.extractLocalizedUrl("fr:/racine,en:http://www.uwyn.com,nl:/wortel"));
    }
}
