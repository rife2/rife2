package rife.tools;

import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;
import static rife.tools.DrupalPassword.DRUPAL_MIN_HASH_COUNT;

public class TestDrupalPassword {
    @Test
    public void testGoodPassword()
    throws NoSuchAlgorithmException {
        assertTrue(DrupalPassword.checkPassword("terry-SELL-niggling", "$S$D0jOAf3SWo8LZbI3hnATd81YG0Q7XzQ9.hxtRfgcAO74X2qplNME"));
    }

    @Test
    public void testMismatchPassword()
    throws NoSuchAlgorithmException {
        assertFalse(DrupalPassword.checkPassword("wrong-PASSWORD", "$S$D0jOAf3SWo8LZbI3hnATd81YG0Q7XzQ9.hxtRfgcAO74X2qplNME"));
    }

    @Test
    public void testPasswordHashing()
    throws NoSuchAlgorithmException {
        // Set a log2 iteration count that is deliberately out of bounds to test
        // that it is corrected to be within bounds.
        var hash = new DrupalPassword(1);
        // Set up a md5 password 'baz'
        final var password = DrupalPassword.md5php("baz");
        // The md5 password should be flagged as needing an update.
        assertTrue(hash.passwordNeedsNewHash(password));
        // Re-hash the password.
        var old_pass = password;
        var pass = hash.hashPassword(password);
        assertEquals(DRUPAL_MIN_HASH_COUNT, DrupalPassword.passwordGetCountLog2(pass));
        assertNotEquals(old_pass, pass);
        assertTrue(DrupalPassword.checkPassword(password, pass));
        // Since the log2 setting hasn't changed and the user has a valid password,
        // passwordNeedsNewHash() should return false.
        assertFalse(hash.passwordNeedsNewHash(pass));
        // Increment the log2 iteration to MIN + 1.
        hash = new DrupalPassword(DRUPAL_MIN_HASH_COUNT + 1);
        assertTrue(hash.passwordNeedsNewHash(pass));
        // Re-hash the password.
        old_pass = pass;
        pass = hash.hashPassword(password);
        assertEquals(DRUPAL_MIN_HASH_COUNT + 1, DrupalPassword.passwordGetCountLog2(pass));
        assertNotEquals(old_pass, pass);
        // Now the hash should be OK.
        assertFalse(hash.passwordNeedsNewHash(pass));
        assertTrue(DrupalPassword.checkPassword(password, pass));
    }

    @Test
    public void testLongPassword()
    throws NoSuchAlgorithmException {
        var hash = new DrupalPassword();

        var password = StringUtils.repeat("x", 512);
        var result = hash.hashPassword(password);
        assertNotNull(result);

        password = StringUtils.repeat("x", 513);
        result = hash.hashPassword(password);
        assertNull(result);

        // Check a string of 3-byte UTF-8 characters.
        password = StringUtils.repeat("€", 170);
        result = hash.hashPassword(password);
        assertNotNull(result);

        password += "xx";
        result = hash.hashPassword(password);
        assertNotNull(result);

        password = StringUtils.repeat("€", 171);
        result = hash.hashPassword(password);
        assertNull(result);
    }
}
