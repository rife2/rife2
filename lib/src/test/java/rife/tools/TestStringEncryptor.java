/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestStringEncryptor {
    @Test
    public void testAdaptiveEncrypt() {
        String encrypted_value_sha = "SHA:Hw/gszrpwzs0GnKTaKbwetEqBb0=";
        String encrypted_value_shahex = "SHAHEX:1f0fe0b33ae9c33b341a729368a6f07ad12a05bd";
        String encrypted_value_md5 = "MD5:JZ3Y+j2LYEvTmtYRY1Khsw==";
        String encrypted_value_md5hex = "MD5HEX:259dd8fa3d8b604bd39ad6116352a1b3";
        String encrypted_value_obf = "OBF:1w261wtu1ugo1vo41vmy1uh21wuk1w1c";
        String encrypted_value_wrp = "WRP:XRadzcqykoN26sWuGe0j85TRoOaErinQr0+QKWsy1hlTMJdtRMkLUo+A4S/Wr3xcj+Va6jc0XLGfcWusuNMrcg==";
        String encrypted_value_wrphex = "WRPHEX:5d169dcdcab2928376eac5ae19ed23f394d1a0e684ae29d0af4f90296b32d6195330976d44c90b528f80e12fd6af7c5c8fe55aea37345cb19f716bacb8d32b72";
        String encrypted_value_none = "thevalue";
        String value = "thevalue";

        try {
            assertEquals(encrypted_value_sha, StringEncryptor.adaptiveEncrypt(value, "SHA:123"));
            assertEquals(encrypted_value_shahex, StringEncryptor.adaptiveEncrypt(value, "SHAHEX:123"));
            assertEquals(encrypted_value_md5, StringEncryptor.adaptiveEncrypt(value, "MD5:123"));
            assertEquals(encrypted_value_md5hex, StringEncryptor.adaptiveEncrypt(value, "MD5HEX:123"));
            assertEquals(encrypted_value_obf, StringEncryptor.adaptiveEncrypt(value, "OBF:123"));
            assertEquals(encrypted_value_wrp, StringEncryptor.adaptiveEncrypt(value, "WRP:123"));
            assertEquals(encrypted_value_wrphex, StringEncryptor.adaptiveEncrypt(value, "WRPHEX:123"));
            assertEquals(encrypted_value_none, StringEncryptor.adaptiveEncrypt(value, "123"));
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
    }

    @Test
    public void testEncryptorInstance() {
        String encrypted_value_sha = "SHA:Hw/gszrpwzs0GnKTaKbwetEqBb0=";
        String encrypted_value_shahex = "SHAHEX:1f0fe0b33ae9c33b341a729368a6f07ad12a05bd";
        String encrypted_value_md5 = "MD5:JZ3Y+j2LYEvTmtYRY1Khsw==";
        String encrypted_value_md5hex = "MD5HEX:259dd8fa3d8b604bd39ad6116352a1b3";
        String encrypted_value_obf = "OBF:1w261wtu1ugo1vo41vmy1uh21wuk1w1c";
        String encrypted_value_wrp = "WRP:XRadzcqykoN26sWuGe0j85TRoOaErinQr0+QKWsy1hlTMJdtRMkLUo+A4S/Wr3xcj+Va6jc0XLGfcWusuNMrcg==";
        String encrypted_value_wrphex = "WRPHEX:5d169dcdcab2928376eac5ae19ed23f394d1a0e684ae29d0af4f90296b32d6195330976d44c90b528f80e12fd6af7c5c8fe55aea37345cb19f716bacb8d32b72";
        String value = "thevalue";

        try {
            assertEquals(encrypted_value_sha, StringEncryptor.SHA.encrypt(value));
            assertEquals(encrypted_value_shahex, StringEncryptor.SHAHEX.encrypt(value));
            assertEquals(encrypted_value_md5, StringEncryptor.MD5.encrypt(value));
            assertEquals(encrypted_value_md5hex, StringEncryptor.MD5HEX.encrypt(value));
            assertEquals(encrypted_value_obf, StringEncryptor.OBF.encrypt(value));
            assertEquals(encrypted_value_wrp, StringEncryptor.WRP.encrypt(value));
            assertEquals(encrypted_value_wrphex, StringEncryptor.WRPHEX.encrypt(value));
            assertNull(StringEncryptor.getEncryptor("BLAH"));
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
    }

    @Test
    public void testGetEncryptor() {
        String encrypted_value_sha = "SHA:Hw/gszrpwzs0GnKTaKbwetEqBb0=";
        String encrypted_value_shahex = "SHAHEX:1f0fe0b33ae9c33b341a729368a6f07ad12a05bd";
        String encrypted_value_md5 = "MD5:JZ3Y+j2LYEvTmtYRY1Khsw==";
        String encrypted_value_md5hex = "MD5HEX:259dd8fa3d8b604bd39ad6116352a1b3";
        String encrypted_value_obf = "OBF:1w261wtu1ugo1vo41vmy1uh21wuk1w1c";
        String encrypted_value_wrp = "WRP:XRadzcqykoN26sWuGe0j85TRoOaErinQr0+QKWsy1hlTMJdtRMkLUo+A4S/Wr3xcj+Va6jc0XLGfcWusuNMrcg==";
        String encrypted_value_wrphex = "WRPHEX:5d169dcdcab2928376eac5ae19ed23f394d1a0e684ae29d0af4f90296b32d6195330976d44c90b528f80e12fd6af7c5c8fe55aea37345cb19f716bacb8d32b72";
        String value = "thevalue";

        try {
            assertEquals(encrypted_value_sha, StringEncryptor.getEncryptor(StringEncryptor.SHA.name()).encrypt(value));
            assertEquals(encrypted_value_shahex, StringEncryptor.getEncryptor(StringEncryptor.SHAHEX.name()).encrypt(value));
            assertEquals(encrypted_value_md5, StringEncryptor.getEncryptor(StringEncryptor.MD5.name()).encrypt(value));
            assertEquals(encrypted_value_md5hex, StringEncryptor.getEncryptor(StringEncryptor.MD5HEX.name()).encrypt(value));
            assertEquals(encrypted_value_obf, StringEncryptor.getEncryptor(StringEncryptor.OBF.name()).encrypt(value));
            assertEquals(encrypted_value_wrp, StringEncryptor.getEncryptor(StringEncryptor.WRP.name()).encrypt(value));
            assertEquals(encrypted_value_wrphex, StringEncryptor.getEncryptor(StringEncryptor.WRPHEX.name()).encrypt(value));
            assertNull(StringEncryptor.getEncryptor("BLAH"));
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
    }

    @Test
    public void testEncryptPlain() {
        String value = "somevalue";

        try {
            String encrypted = StringEncryptor.autoEncrypt(value);
            assertEquals(encrypted, value);
            assertTrue(StringEncryptor.matches(value, encrypted));
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
    }

    @Test
    public void testEncryptOBF() {
        String value = "somevalue";
        String value2 = "somevalue";
        String value3 = "somevalue2";
        String value4 = "SHA:somevalue";

        try {
            String encrypted = StringEncryptor.autoEncrypt("OBF:" + value);
            assertTrue(StringEncryptor.matches(value2, encrypted));
            assertFalse(StringEncryptor.matches(value3, encrypted));
            assertFalse(StringEncryptor.matches(value4, encrypted));

            assertEquals(StringEncryptor.deobfuscate(encrypted), value);
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
    }

    @Test
    public void testEncryptMD5() {
        String value = "MD5:somevalue";
        String value2 = "somevalue";
        String value3 = "somevalue2";
        String value4 = "SHA:somevalue";

        try {
            String encrypted = StringEncryptor.autoEncrypt(value);
            assertTrue(StringEncryptor.matches(value2, encrypted));
            assertFalse(StringEncryptor.matches(value3, encrypted));
            assertFalse(StringEncryptor.matches(value4, encrypted));
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
    }

    @Test
    public void testEncryptMD5HEX() {
        String value = "MD5HEX:somevalue";
        String value2 = "somevalue";
        String value3 = "somevalue2";
        String value4 = "SHA:somevalue";

        try {
            String encrypted = StringEncryptor.autoEncrypt(value);
            assertTrue(StringEncryptor.matches(value2, encrypted));
            assertFalse(StringEncryptor.matches(value3, encrypted));
            assertFalse(StringEncryptor.matches(value4, encrypted));
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
    }

    @Test
    public void testEncryptSHA() {
        String value = "SHA:somevalue";
        String value2 = "somevalue";
        String value3 = "somevalue2";
        String value4 = "MD5:somevalue";

        try {
            String encrypted = StringEncryptor.autoEncrypt(value);
            assertTrue(StringEncryptor.matches(value2, encrypted));
            assertFalse(StringEncryptor.matches(value3, encrypted));
            assertFalse(StringEncryptor.matches(value4, encrypted));
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
    }

    @Test
    public void testEncryptSHAHEX() {
        String value = "SHAHEX:somevalue";
        String value2 = "somevalue";
        String value3 = "somevalue2";
        String value4 = "MD5HEX:somevalue";

        try {
            String encrypted = StringEncryptor.autoEncrypt(value);
            assertTrue(StringEncryptor.matches(value2, encrypted));
            assertFalse(StringEncryptor.matches(value3, encrypted));
            assertFalse(StringEncryptor.matches(value4, encrypted));
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
    }

    @Test
    public void testEncryptWRP() {
        String value = "WRP:somevalue";
        String value2 = "somevalue";
        String value3 = "somevalue2";
        String value4 = "MD5:somevalue";

        try {
            String encrypted = StringEncryptor.autoEncrypt(value);
            assertTrue(StringEncryptor.matches(value2, encrypted));
            assertFalse(StringEncryptor.matches(value3, encrypted));
            assertFalse(StringEncryptor.matches(value4, encrypted));
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
    }

    @Test
    public void testEncryptWRPHEX() {
        String value = "WRPHEX:somevalue";
        String value2 = "somevalue";
        String value3 = "somevalue2";
        String value4 = "MD5HEX:somevalue";

        try {
            String encrypted = StringEncryptor.autoEncrypt(value);
            assertTrue(StringEncryptor.matches(value2, encrypted));
            assertFalse(StringEncryptor.matches(value3, encrypted));
            assertFalse(StringEncryptor.matches(value4, encrypted));
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
    }
}
