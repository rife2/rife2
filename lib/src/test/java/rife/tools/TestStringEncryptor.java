/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestStringEncryptor {
    @Test
    void testAdaptiveEncrypt() {
        var encrypted_value_sha = "SHA:Hw/gszrpwzs0GnKTaKbwetEqBb0=";
        var encrypted_value_shahex = "SHAHEX:1F0FE0B33AE9C33B341A729368A6F07AD12A05BD";
        var encrypted_value_md5 = "MD5:JZ3Y+j2LYEvTmtYRY1Khsw==";
        var encrypted_value_md5hex = "MD5HEX:259DD8FA3D8B604BD39AD6116352A1B3";
        var encrypted_value_obf = "OBF:1w261wtu1ugo1vo41vmy1uh21wuk1w1c";
        var encrypted_value_wrp = "WRP:XRadzcqykoN26sWuGe0j85TRoOaErinQr0+QKWsy1hlTMJdtRMkLUo+A4S/Wr3xcj+Va6jc0XLGfcWusuNMrcg==";
        var encrypted_value_wrphex = "WRPHEX:5D169DCDCAB2928376EAC5AE19ED23F394D1A0E684AE29D0AF4F90296B32D6195330976D44C90B528F80E12FD6AF7C5C8FE55AEA37345CB19F716BACB8D32B72";
        var encrypted_value_none = "thevalue";
        var value = "thevalue";

        try {
            assertEquals(encrypted_value_sha, StringEncryptor.adaptiveEncrypt(value, "SHA:123"));
            assertEquals(encrypted_value_shahex, StringEncryptor.adaptiveEncrypt(value, "SHAHEX:123"));
            assertEquals(encrypted_value_md5, StringEncryptor.adaptiveEncrypt(value, "MD5:123"));
            assertEquals(encrypted_value_md5hex, StringEncryptor.adaptiveEncrypt(value, "MD5HEX:123"));
            assertEquals(encrypted_value_obf, StringEncryptor.adaptiveEncrypt(value, "OBF:123"));
            assertEquals(encrypted_value_wrp, StringEncryptor.adaptiveEncrypt(value, "WRP:123"));
            assertEquals(encrypted_value_wrphex, StringEncryptor.adaptiveEncrypt(value, "WRPHEX:123"));
            var encrypted_drupal = StringEncryptor.adaptiveEncrypt(value, "$S$");
            assertNotEquals(encrypted_drupal, value);
            assertTrue(StringEncryptor.matches(value, encrypted_drupal));
            assertEquals(encrypted_value_none, StringEncryptor.adaptiveEncrypt(value, "123"));
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
    }

    @Test
    void testEncryptorInstance() {
        var encrypted_value_sha = "SHA:Hw/gszrpwzs0GnKTaKbwetEqBb0=";
        var encrypted_value_shahex = "SHAHEX:1F0FE0B33AE9C33B341A729368A6F07AD12A05BD";
        var encrypted_value_md5 = "MD5:JZ3Y+j2LYEvTmtYRY1Khsw==";
        var encrypted_value_md5hex = "MD5HEX:259DD8FA3D8B604BD39AD6116352A1B3";
        var encrypted_value_obf = "OBF:1w261wtu1ugo1vo41vmy1uh21wuk1w1c";
        var encrypted_value_wrp = "WRP:XRadzcqykoN26sWuGe0j85TRoOaErinQr0+QKWsy1hlTMJdtRMkLUo+A4S/Wr3xcj+Va6jc0XLGfcWusuNMrcg==";
        var encrypted_value_wrphex = "WRPHEX:5D169DCDCAB2928376EAC5AE19ED23F394D1A0E684AE29D0AF4F90296B32D6195330976D44C90B528F80E12FD6AF7C5C8FE55AEA37345CB19F716BACB8D32B72";
        var value = "thevalue";

        try {
            assertEquals(encrypted_value_sha, StringEncryptor.SHA.encrypt(value));
            assertEquals(encrypted_value_shahex, StringEncryptor.SHAHEX.encrypt(value));
            assertEquals(encrypted_value_md5, StringEncryptor.MD5.encrypt(value));
            assertEquals(encrypted_value_md5hex, StringEncryptor.MD5HEX.encrypt(value));
            assertEquals(encrypted_value_obf, StringEncryptor.OBF.encrypt(value));
            assertEquals(encrypted_value_wrp, StringEncryptor.WHIRLPOOL.encrypt(value));
            assertEquals(encrypted_value_wrphex, StringEncryptor.WHIRLPOOLHEX.encrypt(value));
            var encrypted_drupal = StringEncryptor.DRUPAL.encrypt(value);
            assertNotEquals(encrypted_drupal, value);
            assertTrue(StringEncryptor.matches(value, encrypted_drupal));
            assertNull(StringEncryptor.getEncryptor("BLAH"));
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
    }

    @Test
    void testGetEncryptor() {
        var encrypted_value_sha = "SHA:Hw/gszrpwzs0GnKTaKbwetEqBb0=";
        var encrypted_value_shahex = "SHAHEX:1F0FE0B33AE9C33B341A729368A6F07AD12A05BD";
        var encrypted_value_md5 = "MD5:JZ3Y+j2LYEvTmtYRY1Khsw==";
        var encrypted_value_md5hex = "MD5HEX:259DD8FA3D8B604BD39AD6116352A1B3";
        var encrypted_value_obf = "OBF:1w261wtu1ugo1vo41vmy1uh21wuk1w1c";
        var encrypted_value_wrp = "WRP:XRadzcqykoN26sWuGe0j85TRoOaErinQr0+QKWsy1hlTMJdtRMkLUo+A4S/Wr3xcj+Va6jc0XLGfcWusuNMrcg==";
        var encrypted_value_wrphex = "WRPHEX:5D169DCDCAB2928376EAC5AE19ED23F394D1A0E684AE29D0AF4F90296B32D6195330976D44C90B528F80E12FD6AF7C5C8FE55AEA37345CB19F716BACB8D32B72";
        var value = "thevalue";

        try {
            assertEquals(encrypted_value_sha, StringEncryptor.getEncryptor(StringEncryptor.SHA.getIdentifier()).encrypt(value));
            assertEquals(encrypted_value_shahex, StringEncryptor.getEncryptor(StringEncryptor.SHAHEX.getIdentifier()).encrypt(value));
            assertEquals(encrypted_value_md5, StringEncryptor.getEncryptor(StringEncryptor.MD5.getIdentifier()).encrypt(value));
            assertEquals(encrypted_value_md5hex, StringEncryptor.getEncryptor(StringEncryptor.MD5HEX.getIdentifier()).encrypt(value));
            assertEquals(encrypted_value_obf, StringEncryptor.getEncryptor(StringEncryptor.OBF.getIdentifier()).encrypt(value));
            assertEquals(encrypted_value_wrp, StringEncryptor.getEncryptor(StringEncryptor.WHIRLPOOL.getIdentifier()).encrypt(value));
            assertEquals(encrypted_value_wrphex, StringEncryptor.getEncryptor(StringEncryptor.WHIRLPOOLHEX.getIdentifier()).encrypt(value));
            var encrypted_drupal = StringEncryptor.getEncryptor(StringEncryptor.DRUPAL.getIdentifier()).encrypt(value);
            assertNotEquals(encrypted_drupal, value);
            assertTrue(StringEncryptor.matches(value, encrypted_drupal));
            assertNull(StringEncryptor.getEncryptor("BLAH"));
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
    }

    @Test
    void testEncryptPlain() {
        var value = "somevalue";

        try {
            var encrypted = StringEncryptor.autoEncrypt(value);
            assertEquals(encrypted, value);
            assertTrue(StringEncryptor.matches(value, encrypted));
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
    }

    @Test
    void testEncryptOBF() {
        var value = "somevalue";
        var value2 = "somevalue";
        var value3 = "somevalue2";
        var value4 = "SHA:somevalue";

        try {
            var encrypted = StringEncryptor.autoEncrypt("OBF:" + value);
            assertTrue(StringEncryptor.matches(value2, encrypted));
            assertFalse(StringEncryptor.matches(value3, encrypted));
            assertFalse(StringEncryptor.matches(value4, encrypted));

            assertEquals(StringEncryptor.deobfuscate(encrypted), value);
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
    }

    @Test
    void testEncryptMD5() {
        var value = "MD5:somevalue";
        var value2 = "somevalue";
        var value3 = "somevalue2";
        var value4 = "SHA:somevalue";

        try {
            var encrypted = StringEncryptor.autoEncrypt(value);
            assertTrue(StringEncryptor.matches(value2, encrypted));
            assertFalse(StringEncryptor.matches(value3, encrypted));
            assertFalse(StringEncryptor.matches(value4, encrypted));
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
    }

    @Test
    void testEncryptMD5HEX() {
        var value = "MD5HEX:somevalue";
        var value2 = "somevalue";
        var value3 = "somevalue2";
        var value4 = "SHA:somevalue";

        try {
            var encrypted = StringEncryptor.autoEncrypt(value);
            assertTrue(StringEncryptor.matches(value2, encrypted));
            assertFalse(StringEncryptor.matches(value3, encrypted));
            assertFalse(StringEncryptor.matches(value4, encrypted));
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
    }

    @Test
    void testEncryptSHA() {
        var value = "SHA:somevalue";
        var value2 = "somevalue";
        var value3 = "somevalue2";
        var value4 = "MD5:somevalue";

        try {
            var encrypted = StringEncryptor.autoEncrypt(value);
            assertTrue(StringEncryptor.matches(value2, encrypted));
            assertFalse(StringEncryptor.matches(value3, encrypted));
            assertFalse(StringEncryptor.matches(value4, encrypted));
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
    }

    @Test
    void testEncryptSHAHEX() {
        var value = "SHAHEX:somevalue";
        var value2 = "somevalue";
        var value3 = "somevalue2";
        var value4 = "MD5HEX:somevalue";

        try {
            var encrypted = StringEncryptor.autoEncrypt(value);
            assertTrue(StringEncryptor.matches(value2, encrypted));
            assertFalse(StringEncryptor.matches(value3, encrypted));
            assertFalse(StringEncryptor.matches(value4, encrypted));
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
    }

    @Test
    void testEncryptWRP() {
        var value = "WRP:somevalue";
        var value2 = "somevalue";
        var value3 = "somevalue2";
        var value4 = "MD5:somevalue";

        try {
            var encrypted = StringEncryptor.autoEncrypt(value);
            assertTrue(StringEncryptor.matches(value2, encrypted));
            assertFalse(StringEncryptor.matches(value3, encrypted));
            assertFalse(StringEncryptor.matches(value4, encrypted));
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
    }

    @Test
    void testEncryptWRPHEX() {
        var value = "WRPHEX:somevalue";
        var value2 = "somevalue";
        var value3 = "somevalue2";
        var value4 = "MD5HEX:somevalue";

        try {
            var encrypted = StringEncryptor.autoEncrypt(value);
            assertTrue(StringEncryptor.matches(value2, encrypted));
            assertFalse(StringEncryptor.matches(value3, encrypted));
            assertFalse(StringEncryptor.matches(value4, encrypted));
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
    }

    @Test
    void testEncryptDRUPAL() {
        var value = "$S$somevalue";
        var value2 = "somevalue";
        var value3 = "somevalue2";
        var value4 = "$S$somevalue";

        try {
            var encrypted = StringEncryptor.autoEncrypt(value);
            assertTrue(StringEncryptor.matches(value2, encrypted));
            assertFalse(StringEncryptor.matches(value3, encrypted));
            assertFalse(StringEncryptor.matches(value4, encrypted));
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
    }
}
