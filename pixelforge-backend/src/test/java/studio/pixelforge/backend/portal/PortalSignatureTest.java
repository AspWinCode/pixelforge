package studio.pixelforge.backend.portal;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalSignatureTest {

    @Test
    void hexMatchesPythonHmac() {
        // Эталон: python hmac.new(b"secret", b"lp-student-1", sha256).hexdigest()
        assertEquals(
            "4484580de5e4760e91499d65f5a69932dc641b64091218e34b1825f23dc18f7e",
            PortalSignature.hmacSha256Hex("secret", "lp-student-1"));
        assertEquals(
            "652d44b57a53f761a7e6d1ec01303bdb200417f12874568cf1c68c7d2f59325f",
            PortalSignature.hmacSha256Hex("topsecret", "lp-student-99"));
    }

    @Test
    void byteAndStringOverloadsAgree() {
        String a = PortalSignature.hmacSha256Hex("secret", "lp-student-42");
        String b = PortalSignature.hmacSha256Hex("secret", "lp-student-42".getBytes(StandardCharsets.UTF_8));
        assertEquals(a, b);
    }

    @Test
    void matchesIsTrueOnlyForEqualHex() {
        String sig = PortalSignature.hmacSha256Hex("secret", "lp-student-7");
        assertTrue(PortalSignature.matches(sig, sig));
        assertFalse(PortalSignature.matches(sig, sig.substring(0, 63) + "0"));
        assertFalse(PortalSignature.matches(sig, null));
        assertFalse(PortalSignature.matches(null, sig));
    }

    @Test
    void differentSecretsProduceDifferentSignatures() {
        assertNotEquals(
            PortalSignature.hmacSha256Hex("a", "x"),
            PortalSignature.hmacSha256Hex("b", "x"));
    }
}
