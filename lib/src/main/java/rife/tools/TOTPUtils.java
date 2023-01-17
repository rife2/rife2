package rife.tools;

/*
 * Licensed under the Apache License, Version 2.0 (the "License")
 * https://github.com/taimos/totp
 * Changes from original:
 * Renamed to TOTPUtils. Removed apache.commons.codec dependencies.  Moved URL generation into class. All public secret handling in Base32
 */

import java.math.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.lang.reflect.*;
import java.security.*;


public final class TOTPUtils
{
    private TOTPUtils() {
    }

    public static String generateSecret() {
        var random = new SecureRandom();
        var bytes = new byte[20];
        random.nextBytes(bytes);
        return StringUtils.encodeBase32(bytes);
    }

    public static String getCode(final String secret) {
        var decoded = StringUtils.decodeBase32String(secret);
        var hexKey = StringUtils.encodeHex(decoded);
        return getOTP(hexKey);
    }

    public static boolean validateCode(final String secret, final String inputCode) {
        return validateCode(secret, inputCode, 1);
    }

    public static boolean validateCode(final String secret, final String inputCode, int stepsBack) {
        long step = getStep();
        var decoded = StringUtils.decodeBase32String(secret);
        var hexKey = StringUtils.encodeHex(decoded);
        for (long i = 0; i <= stepsBack; i++) {
            if (getOTP(step - i, hexKey).equals(inputCode)) {
                return true;
            }
        }
        return false;
    }

    public static String getUrl(final String secret, final String issuer, final String user) {
        var rawURL = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", issuer, user, secret, issuer);
        return StringUtils.encodeUrl(rawURL);
    }

    private static String getOTP(final String key) {
        return getOTP(getStep(), key);
    }

    private static boolean validate(final String key, final String otp) {
        return validate(getStep(), key, otp);
    }

    private static boolean validate(final long step, final String key, final String otp) {
        return getOTP(step, key).equals(otp) || getOTP(step - 1L, key).equals(otp);
    }

    private static long getStep() {
        return System.currentTimeMillis() / 30000L;
    }

    private static String getOTP(final long step, final String key) {
        String steps;
        for (steps = Long.toHexString(step).toUpperCase(); steps.length() < 16; steps = "0" + steps) {}
        final byte[] msg = hexStr2Bytes(steps);
        final byte[] k = hexStr2Bytes(key);
        final byte[] hash = hmac_sha1(k, msg);
        final int offset = hash[hash.length - 1] & 0xF;
        final int binary = (hash[offset] & 0x7F) << 24 | (hash[offset + 1] & 0xFF) << 16 | (hash[offset + 2] & 0xFF) << 8 | (hash[offset + 3] & 0xFF);
        final int otp = binary % 1000000;
        String result;
        for (result = Integer.toString(otp); result.length() < 6; result = "0" + result) {}
        return result;
    }

    private static byte[] hexStr2Bytes(final String hex) {
        final byte[] bArray = new BigInteger("10" + hex, 16).toByteArray();
        final byte[] ret = new byte[bArray.length - 1];
        System.arraycopy(bArray, 1, ret, 0, ret.length);
        return ret;
    }

    private static byte[] hmac_sha1(final byte[] keyBytes, final byte[] text) {
        try {
            final Mac hmac = Mac.getInstance("HmacSHA1");
            final SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");
            hmac.init(macKey);
            return hmac.doFinal(text);
        }
        catch (final GeneralSecurityException gse) {
            throw new UndeclaredThrowableException(gse);
        }
    }
}

