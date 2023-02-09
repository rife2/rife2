/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.nio.charset.StandardCharsets;
import java.security.*;

import static rife.tools.StringUtils.encodeHex;

/**
 * Java implementation of the Drupal 7 password hashing algorithm.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0.1
 */
public class DrupalPassword {
    /**
     * The string prefix that all Drupal password hashes have.
     */
    public static final String PREFIX = "$S$";

    /**
     * The standard log2 number of iterations for password stretching. This should
     * increase by 1 every Drupal version in order to counteract increases in the
     * speed and power of computers available to crack the hashes.
     */
    public static final int DRUPAL_HASH_COUNT = 15;

    /**
     * The minimum allowed log2 number of iterations for password stretching.
     */
    public static final int DRUPAL_MIN_HASH_COUNT = 7;

    /**
     * The maximum allowed log2 number of iterations for password stretching.
     */
    public static final int DRUPAL_MAX_HASH_COUNT = 30;

    /**
     * The expected (and maximum) number of characters in a hashed password.
     */
    public static final int DRUPAL_HASH_LENGTH = 55;

    /**
     * String for mapping an int to the corresponding base 64 character.
     */
    private static final String ITOA_64 = "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private final int passwordCountLog2_;

    public DrupalPassword() {
        passwordCountLog2_ = DRUPAL_HASH_COUNT;
    }

    public DrupalPassword(int passwordCountLog2) {
        passwordCountLog2_ = passwordCountLog2;
    }

    /**
     * Hash a password using a secure hash.
     *
     * @param password A plain-text password.
     * @return A string containing the hashed password (and a salt), or {@code null} on failure.
     * @since 1.0.1
     */
    public String hashPassword(String password)
    throws NoSuchAlgorithmException {
        return passwordCrypt(password, passwordGenerateSalt(passwordCountLog2_));
    }

    public static String md5php(String password)
    throws NoSuchAlgorithmException {
        var md5 = MessageDigest.getInstance("MD5");
        md5.update(password.getBytes(StandardCharsets.UTF_8));
        return encodeHex(md5.digest()).toLowerCase();
    }

    /**
     * Check whether a plain text password matches a stored hashed password.
     *
     * @param candidate       the clear text password
     * @param saltedEncrypted the salted encrypted Drupal 7 password string to check
     * @return {@code true} if the candidate matches; or {@code false} otherwise.
     * @throws java.security.NoSuchAlgorithmException when the hashing algorithm couldn't be found
     * @since 1.0.1
     */
    public static boolean checkPassword(String candidate, String saltedEncrypted)
    throws NoSuchAlgorithmException {
        if (candidate == null || saltedEncrypted == null) {
            return false;
        }
        if (saltedEncrypted.startsWith("U$")) {
            // This may be an updated password from user_update_7000(). Such hashes
            // have 'U' added as the first character and need an extra md5().
            saltedEncrypted = saltedEncrypted.substring(1);
            candidate = md5php(candidate);
        }

        var hash = passwordCrypt(candidate, saltedEncrypted);
        return saltedEncrypted.equalsIgnoreCase(hash);
    }

    /**
     * Parse the log2 iteration count from a stored hash or setting string.
     *
     * @param setting the Drupal 7 hash or setting string
     * @since 1.0.1
     */
    public static int passwordGetCountLog2(String setting) {
        return ITOA_64.indexOf(setting.charAt(3));
    }

    /**
     * Check whether a user's hashed password needs to be replaced with a new hash.
     * <p>
     * This is typically called during the login process when the plain text
     * password is available. A new hash is needed when the desired iteration count
     * has changed through a change in the variable password_count_log2 or
     * DRUPAL_HASH_COUNT or if the user's password hash was generated in an update
     * like user_update_7000().
     * <p>
     * Alternative implementations of this function might use other criteria based
     * on the fields in $account.
     *
     * @param password the password ot check
     * @return {@code true} when the password needs to be re-hashed; or
     * {code false} otherwise
     * @since 1.0.1
     */
    public boolean passwordNeedsNewHash(String password) {
        // Check whether this was an updated password.
        if (!password.startsWith(PREFIX) || password.length() != DRUPAL_HASH_LENGTH) {
            return true;
        }
        // Ensure that count_log2 is within set bounds.
        var count_log2 = passwordEnforceLog2Boundaries(passwordCountLog2_);
        // Check whether the iteration count used differs from the standard number.
        return passwordGetCountLog2(password) != count_log2;
    }

    private static byte[] joinBytes(byte[] a, byte[] b) {
        var combined = new byte[a.length + b.length];

        System.arraycopy(a, 0, combined, 0, a.length);
        System.arraycopy(b, 0, combined, a.length, b.length);
        return combined;
    }

    private static char iToA64(long value) {
        return iToA64((int) value);
    }

    private static char iToA64(int value) {
        return ITOA_64.charAt(value & 0x3F);
    }

    private static String passwordBase64Encode(byte[] input, int count) {
        var output = new StringBuilder();
        var i = 0;
        do {
            var value = signedByteToUnsignedLong(input[i++]);

            output.append(iToA64(value));
            if (i < count) {
                value |= signedByteToUnsignedLong(input[i]) << 8;
            }
            output.append(iToA64(value >> 6));
            if (i++ >= count) {
                break;
            }
            if (i < count) {
                value |= signedByteToUnsignedLong(input[i]) << 16;
            }

            output.append(iToA64(value >> 12));
            if (i++ >= count) {
                break;
            }
            output.append(iToA64(value >> 18));
        } while (i < count);

        return output.toString();
    }

    private static long signedByteToUnsignedLong(byte b) {
        return b & 0xFF;
    }

    /**
     * Generates a random base 64-encoded salt prefixed with settings for the hash.
     * <p>
     * Proper use of salts may defeat a number of attacks, including:
     * - The ability to try candidate passwords against multiple hashes at once.
     * - The ability to use pre-hashed lists of candidate passwords.
     * - The ability to determine whether two users have the same (or different)
     * password without actually having to guess one of the passwords.
     *
     * @param countLog2 Integer that determines the number of iterations used in the hashing
     *                  process. A larger value is more secure, but takes more time to complete.
     * @return A 12 character string containing the iteration count and a random salt.
     */
    private static String passwordGenerateSalt(int countLog2) {
        var output = PREFIX;
        // Ensure that countLog2 is within set bounds.
        countLog2 = passwordEnforceLog2Boundaries(countLog2);
        // We encode the final log2 iteration count in base 64.
        output += iToA64(countLog2);
        // 6 bytes is the standard salt for a portable phpass hash.
        output += passwordBase64Encode(randomBytes(6), 6);
        return output;
    }

    static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static byte[] randomBytes(int count) {
        var output = new byte[count];
        SECURE_RANDOM.nextBytes(output);
        return output;
    }

    /**
     * Ensures that countLog2 is within set bounds.
     *
     * @param countLog2 Integer that determines the number of iterations used in the hashing
     *                  process. A larger value is more secure, but takes more time to complete.
     * @return Integer within set bounds that is closest to $count_log2.
     */
    private static int passwordEnforceLog2Boundaries(int countLog2) {
        if (countLog2 < DRUPAL_MIN_HASH_COUNT) {
            return DRUPAL_MIN_HASH_COUNT;
        } else if (countLog2 > DRUPAL_MAX_HASH_COUNT) {
            return DRUPAL_MAX_HASH_COUNT;
        }

        return countLog2;
    }

    /**
     * Hash a password using a secure stretched hash with sha512 algo.
     * <p>
     * By using a salt and repeated hashing the password is "stretched". Its
     * security is increased because it becomes much more computationally costly
     * for an attacker to try to break the hash by brute-force computation of the
     * hashes of a large number of plain-text words or strings to find a match.
     *
     * @param password Plain-text password up to 512 bytes (128 to 512 UTF-8 characters) to hash.
     * @param setting  An existing hash or the output of _password_generate_salt().  Must be
     *                 at least 12 characters (the settings and salt).
     * @return A string containing the hashed password (and salt) or FALSE on failure.
     * The return string will be truncated at DRUPAL_HASH_LENGTH characters max.
     */
    private static String passwordCrypt(String password, String setting)
    throws NoSuchAlgorithmException {
        // Prevent DoS attacks by refusing to hash large passwords.
        if (password.getBytes(StandardCharsets.UTF_8).length > 512) {
            return null;
        }
        // The first 12 characters of an existing hash are its setting string.
        setting = setting.substring(0, 12);

        if (setting.charAt(0) != '$' || setting.charAt(2) != '$') {
            return null;
        }

        var count_log2 = passwordGetCountLog2(setting);

        // Hashes may be imported from elsewhere, so we allow != DRUPAL_HASH_COUNT
        if (count_log2 < DRUPAL_MIN_HASH_COUNT || count_log2 > DRUPAL_MAX_HASH_COUNT) {
            return null;
        }

        var salt = setting.substring(4, 12);
        // Hashes must have an 8 character salt.
        if (salt.length() != 8) {
            return null;
        }

        // Convert the base 2 logarithm into an integer.
        var count = 1 << count_log2;

        // Hash using sha512
        var sha_512 = MessageDigest.getInstance("SHA-512");
        var hash = sha_512.digest(salt.concat(password).getBytes(StandardCharsets.UTF_8));

        do {
            hash = sha_512.digest(joinBytes(hash, password.getBytes(StandardCharsets.UTF_8)));
        } while (--count > 0);

        var output = setting + passwordBase64Encode(hash, hash.length);
        return (output.length() > 0) ? output.substring(0, DRUPAL_HASH_LENGTH) : null;
    }
}