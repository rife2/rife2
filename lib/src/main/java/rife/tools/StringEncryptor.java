/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static rife.tools.StringUtils.encodeHex;

public enum StringEncryptor {
    OBF,
    MD5,
    MD5HEX,
    SHA,
    SHAHEX,
    WRP,
    WRPHEX;

    private static final String PREFIX_SEPARATOR_SUFFIX = ":";

    public String prefix() {
        return name() + PREFIX_SEPARATOR_SUFFIX;
    }

    public static StringEncryptor getEncryptor(String identifier) {
        try {
            return StringEncryptor.valueOf(identifier);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String encrypt(String value)
    throws NoSuchAlgorithmException {
        if (null == value) throw new IllegalArgumentException("value can't be null");

        return autoEncrypt(prefix() + value);
    }

    private static String encodeBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String autoEncrypt(String value)
    throws NoSuchAlgorithmException {
        if (null == value) throw new IllegalArgumentException("value can't be null");

        if (value.startsWith(OBF.prefix())) {
            return OBF.prefix() + obfuscate(value.substring(OBF.prefix().length()));
        } else {
            var encode_base64 = false;
            String prefix = null;
            byte[] bytes = null;
            if (value.startsWith(SHA.prefix()) || value.startsWith(SHAHEX.prefix())) {
                if (value.startsWith(SHA.prefix())) {
                    prefix = SHA.prefix();
                    encode_base64 = true;
                } else {
                    prefix = SHAHEX.prefix();
                    encode_base64 = false;
                }
                var digest = MessageDigest.getInstance("SHA");
                digest.update(value.substring(prefix.length()).getBytes());
                bytes = digest.digest();
            } else if (value.startsWith(WRP.prefix()) || value.startsWith(WRPHEX.prefix())) {
                if (value.startsWith(WRP.prefix())) {
                    prefix = WRP.prefix();
                    encode_base64 = true;
                } else {
                    prefix = WRPHEX.prefix();
                    encode_base64 = false;
                }
                var whirlpool = new Whirlpool();
                whirlpool.NESSIEinit();
                whirlpool.NESSIEadd(value.substring(prefix.length()));
                var digest = new byte[Whirlpool.DIGESTBYTES];
                whirlpool.NESSIEfinalize(digest);
                bytes = digest;
            } else if (value.startsWith(MD5.prefix()) || value.startsWith(MD5HEX.prefix())) {
                if (value.startsWith(MD5.prefix())) {
                    prefix = MD5.prefix();
                    encode_base64 = true;
                } else {
                    prefix = MD5HEX.prefix();
                    encode_base64 = false;
                }
                var digest = MessageDigest.getInstance("MD5");
                digest.update(value.substring(prefix.length()).getBytes());
                bytes = digest.digest();
            } else {
                return value;
            }

            if (encode_base64) {
                value = prefix + encodeBase64(bytes);
            } else {
                value = prefix + encodeHex(bytes);
            }
        }

        assert value != null;

        return value;
    }

    public static boolean matches(String checkedValue, String encryptedValue)
    throws NoSuchAlgorithmException {
        if (null == checkedValue) throw new IllegalArgumentException("checkedValue can't be null");
        if (null == encryptedValue) throw new IllegalArgumentException("encryptedValue can't be null");

        return encryptedValue.equals(adaptiveEncrypt(checkedValue, encryptedValue));
    }

    public static String adaptiveEncrypt(String clearValue, String encryptedValue)
    throws NoSuchAlgorithmException {
        if (null == clearValue) throw new IllegalArgumentException("clearValue can't be null");
        if (null == encryptedValue) throw new IllegalArgumentException("encryptedValue can't be null");

        if (encryptedValue.startsWith(OBF.prefix())) {
            clearValue = OBF.prefix() + clearValue;
        } else if (encryptedValue.startsWith(WRP.prefix())) {
            clearValue = WRP.prefix() + clearValue;
        } else if (encryptedValue.startsWith(WRPHEX.prefix())) {
            clearValue = WRPHEX.prefix() + clearValue;
        } else if (encryptedValue.startsWith(MD5.prefix())) {
            clearValue = MD5.prefix() + clearValue;
        } else if (encryptedValue.startsWith(MD5HEX.prefix())) {
            clearValue = MD5HEX.prefix() + clearValue;
        } else if (encryptedValue.startsWith(SHA.prefix())) {
            clearValue = SHA.prefix() + clearValue;
        } else if (encryptedValue.startsWith(SHAHEX.prefix())) {
            clearValue = SHAHEX.prefix() + clearValue;
        }

        return autoEncrypt(clearValue);
    }

    public static String obfuscate(String value) {
        if (null == value) throw new IllegalArgumentException("value can't be null");

        var buffer = new StringBuilder();
        var bytes = value.getBytes();
        for (var i = 0; i < bytes.length; i++) {
            var b1 = bytes[i];
            var b2 = bytes[value.length() - (i + 1)];
            var i1 = (int) b1 + (int) b2 + 127;
            var i2 = (int) b1 - (int) b2 + 127;
            var i0 = i1 * 256 + i2;
            var x = Integer.toString(i0, 36);

            switch (x.length()) {
                case 1:
                    buffer.append('0');
                case 2:
                    buffer.append('0');
                case 3:
                    buffer.append('0');
                default:
                    buffer.append(x);
            }
        }

        return buffer.toString();
    }

    public static String deobfuscate(String value) {
        if (null == value) throw new IllegalArgumentException("value can't be null");

        if (value.startsWith(OBF.prefix())) {
            value = value.substring(OBF.prefix().length());
        }

        var bytes = new byte[value.length() / 2];
        var l = 0;

        for (var i = 0; i < value.length(); i += 4) {
            var x = value.substring(i, i + 4);
            var i0 = Integer.parseInt(x, 36);
            var i1 = (i0 / 256);
            var i2 = (i0 % 256);
            bytes[l++] = (byte) ((i1 + i2 - 254) / 2);
        }

        return new String(bytes, 0, l);
    }

    public static void main(String[] arguments) {
        var valid_arguments = true;
        if (arguments.length < 1 ||
            arguments.length > 3) {
            valid_arguments = false;
        } else if (!arguments[0].startsWith("-")) {
            if (arguments.length > 1) {
                valid_arguments = false;
            }
        } else {
            if (!arguments[0].equals("-e") &&
                !arguments[0].equals("-d") &&
                !arguments[0].equals("-c")) {
                valid_arguments = false;
            } else if (!arguments[0].equals("-c") &&
                3 == arguments.length) {
                valid_arguments = false;
            }
        }

        if (!valid_arguments) {
            System.err.println("Usage : java " + StringEncryptor.class.getName() + " [-edc] string {encrypted}");
            System.err.println("Encrypts strings for usage with RIFE2.");
            System.err.println("  -e  encrypt a string (default)");
            System.err.println("  -d  decrypt a string if the algorithm support it");
            System.err.println("  -c  check the validity of the string against an encrypted version");
            System.exit(1);
        }
        try {
            if (1 == arguments.length) {
                System.err.println(autoEncrypt(arguments[0]));
                System.exit(0);
            } else if (arguments[0].equals("-e")) {
                System.err.println(autoEncrypt(arguments[1]));
                System.exit(0);
            }
            if (arguments[0].equals("-d")) {
                if (arguments[1].startsWith(OBF.prefix())) {
                    System.err.println(deobfuscate(arguments[1]));
                } else {
                    System.err.println("ERROR: the algorithm doesn't support decoding.");
                }
                System.exit(0);
            }
            if (arguments[0].equals("-c")) {
                if (matches(arguments[1], arguments[2])) {
                    System.err.println("VALID: the strings match.");
                } else {
                    System.err.println("invalid: the strings don't match.");
                }
                System.exit(0);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
