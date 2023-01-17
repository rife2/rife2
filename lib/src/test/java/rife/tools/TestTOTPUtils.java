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
    public void testGetAndValidateCode() {
        var secret = TOTPUtils.generateSecret();
        var code = TOTPUtils.getCode(secret);
        assertTrue(TOTPUtils.validateCode(secret, code, 1));
    }

}
