package rife.tools;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestTOTPUtils {

    @Test
    public void testGenerateSecret() {
        var secretOne = TOTPUtils.generateSecret();
        var secretTwo = TOTPUtils.generateSecret();
        assertNotEquals(secretOne, secretTwo);
    }

    @Test
    public void testGetCode() {
        var secret = TOTPUtils.generateSecret();
        assertNotNull(TOTPUtils.getCode(secret));
        assertEquals(6, TOTPUtils.getCode(secret).length());
        assertDoesNotThrow(() -> TOTPUtils.getCode(null));
        assertDoesNotThrow(() -> TOTPUtils.getCode(""));
        assertDoesNotThrow(() -> TOTPUtils.getCode("ABCDEFGHIQPOWER!@#$%^&*()12343567890///\\\\"));
        assertNull(TOTPUtils.getCode(null));
        assertNull(TOTPUtils.getCode(""));
    }

    @Test
    public void testGetAndValidateCode() {
        var secret = TOTPUtils.generateSecret();
        var code = TOTPUtils.getCode(secret);
        assertFalse(TOTPUtils.validateCode(secret, code, -1));
        assertTrue(TOTPUtils.validateCode(secret, code, 0));
        assertTrue(TOTPUtils.validateCode(secret, code, 1));
        assertTrue(TOTPUtils.validateCode(secret, code, 10));

        assertFalse(TOTPUtils.validateCode("", code));
        assertFalse(TOTPUtils.validateCode(secret, ""));
        assertFalse(TOTPUtils.validateCode(null, code));
        assertFalse(TOTPUtils.validateCode(secret, null));
    }


    @Test
    public void testGetURL() {
        var secret = TOTPUtils.generateSecret();
        assertNotNull(TOTPUtils.getUrl(secret, "TestIssuer", "TestUser"));
        assertNull(TOTPUtils.getUrl(secret, null, null));
        assertNull(TOTPUtils.getUrl(secret, "", ""));
        assertNull(TOTPUtils.getUrl(secret, "Issuer:WithColon", "TestUser"));
        assertNull(TOTPUtils.getUrl(secret, "TestIssuer", "User:WithColon"));
        assertNotNull(TOTPUtils.getUrl(secret, "ABCDEFGHIQPOWER!@#$%^&*()12343567890///\\\\", "ABCDEFGHIQPOWER!@#$%^&*()12343567890///\\\\"));
    }
}
