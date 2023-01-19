package rife.tools;

/*
 * Licensed under the Apache License, Version 2.0 (the "License")
 * https://github.com/taimos/totp
 * Changes from original:
 * Renamed to TOTPUtils. Removed apache.commons.codec dependencies.  Moved URL generation into class. All public secret handling in Base32
 */

import java.math.*;
import java.nio.charset.StandardCharsets;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.lang.reflect.*;
import java.security.*;


/**
     * Utility class providing the necessary functions to build 2FA using a time-based OTP algorithm
     *
     * @since 1.0
     */
public final class TOTPUtils
{
    private TOTPUtils() {
    }

     /**
     * Generates a random secret
     *
     * @returns secret as a UTF-8 encoded {@code String}
     * @since 1.0
     **/
    public static String generateSecret() {
        var random = new SecureRandom();
        var bytes = new byte[20];
        random.nextBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

     /**
     * Generates the time-based code based on the given secret
     * @param secret should be the UTF-8 encoded secret
     * @returns time-based code as a {@code String} to use for authentication or {@code null} if secret is null or an empty string
     * @since 1.0
     **/
    public static String getCode(final String secret) {
        if (secret == null || secret.isEmpty()) {
            return null;
        }
        var hexKey = StringUtils.encodeHex(secret.getBytes(StandardCharsets.UTF_8));
        return getOTP(hexKey);
    }

    /**
     * Code validation with a default of 1-step back, granting a 30-60 second window
     * @param secret should be the UTF-8 encoded secret
     * @param inputCode should be the code input by the challenger
     *
     * @returns {@code true} if inputCode is valid, {@code false} if invalid or if secret or inputCode are null
     * @since 1.0
     **/
    public static boolean validateCode(final String secret, final String inputCode) {
        return validateCode(secret, inputCode, 1);
    }

    /**
     * Code validation where steps back can be customized to allow looser time-based authentication
     * @param secret should be the UTF-8 encoded secret
     * @param inputCode should be the code input by the challenger
     * @param stepsBack number of steps (30 second increments) to look back during authentication
     *
     * @returns {@code true} if inputCode is valid, @{code false} if invalid or if secret or inputCode are null
     * @since 1.0
     **/
    public static boolean validateCode(final String secret, final String inputCode, int stepsBack) {
        if (secret == null || secret.isEmpty() || inputCode == null || inputCode.isEmpty()) {
            return false;
        }
        long step = getStep();
        var hexKey = StringUtils.encodeHex(secret.getBytes(StandardCharsets.UTF_8));
        for (long i = 0; i <= stepsBack; i++) {
            if (getOTP(step - i, hexKey).equals(inputCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Generates a Google Authenticator-compatible URL
     * Formatting based on the document found here: https://github.com/google/google-authenticator/wiki/Key-Uri-Format
     * Can be used for QR-code image scanning
     *
     * @param secret should be the UTF-8 encoded secret
     * @param issuer should represent the account associated with the authentication
     * @param user represents the user associated with the authentication
     *
     * @returns the URL to be used as a {@code String}
     * @since 1.0
     **/
    public static String getUrl(final String secret, final String issuer, final String user) {
        if (secret == null || secret.isEmpty()) {
            return null;
        }
        if (issuer == null || issuer.isEmpty() || issuer.contains(":") || user == null || user.isEmpty() || user.contains(":") ) {
            return null;
        }
        var encoded = StringUtils.encodeBase32(secret.getBytes(StandardCharsets.UTF_8));
        var encoded_issuer = StringUtils.encodeUrl(issuer);
        var encoded_user = StringUtils.encodeUrl(user);
        var rawURL = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", encoded_issuer, encoded_user, encoded, encoded_issuer);
        return rawURL;
    }

    private static String getOTP(final String key) {
        return getOTP(getStep(), key);
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

