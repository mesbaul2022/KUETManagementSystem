package edu.kuet.ums.unit;

import edu.kuet.ums.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests: JwtTokenProvider Engine")
class JwtTokenProviderUnitTest {

    private JwtTokenProvider tokenProvider;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long expirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", secret);
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationInMs", expirationMs);
    }

    @Test
    @DisplayName("Unit Test: Generate token and validate successfully")
    void testGenerateAndValidateToken_Success() {
        String token = tokenProvider.generateTokenFromUsername("testuser", 1L, "ROLE_STUDENT", "test@kuet.ac.bd");

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("Unit Test: Extract correct username from signed JWT token")
    void testGetUsernameFromJWT_Success() {
        String token = tokenProvider.generateTokenFromUsername("mesbaul_student", 42L, "ROLE_STUDENT", "mesbaul@kuet.ac.bd");

        String username = tokenProvider.getUsernameFromJWT(token);

        assertEquals("mesbaul_student", username);
    }

    @Test
    @DisplayName("Unit Test: Validate token returns false for malformed token")
    void testValidateToken_MalformedToken_ReturnsFalse() {
        String invalidToken = "not.a.valid.jwt.token";

        assertFalse(tokenProvider.validateToken(invalidToken));
    }

    @Test
    @DisplayName("Unit Test: Validate token returns false for null or empty string")
    void testValidateToken_EmptyOrNullToken_ReturnsFalse() {
        assertFalse(tokenProvider.validateToken(""));
        assertFalse(tokenProvider.validateToken("   "));
    }

    @Test
    @DisplayName("Unit Test: Validate token returns false for expired token")
    void testValidateToken_ExpiredToken_ReturnsFalse() {
        JwtTokenProvider expiredTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(expiredTokenProvider, "jwtSecret", secret);
        // Set negative expiration so token is immediately expired
        ReflectionTestUtils.setField(expiredTokenProvider, "jwtExpirationInMs", -5000L);

        String expiredToken = expiredTokenProvider.generateTokenFromUsername("expired_user", 99L, "ROLE_STUDENT", "exp@kuet.ac.bd");

        assertFalse(tokenProvider.validateToken(expiredToken));
    }
}
