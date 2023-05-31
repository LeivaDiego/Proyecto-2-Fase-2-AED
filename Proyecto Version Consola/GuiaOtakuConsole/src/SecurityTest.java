import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SecurityTest {
    @Test
    void testEncrypt() {
        String plaintext = "admin123";
        String expected = "dgplq456";
        String actual = Security.encrypt(plaintext);

        assertEquals(expected, actual, "Error in encryption");
    }

    @Test
    void testDecrypt() {
        String cipherText = "dgplq456";
        String expected = "admin123";
        String actual = Security.decrypt(cipherText);

        assertEquals(expected, actual, "Error in decryption");
    }

}