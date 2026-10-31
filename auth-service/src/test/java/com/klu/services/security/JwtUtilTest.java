package com.klu.services.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String secret = "TalentSyncSecretKeyForJWTSecurityAuthenticationSOA20262027VerySecureKey";

    @BeforeEach
    public void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L);
    }

    @Test
    @DisplayName("Positive: Generate token and extract valid claims for candidate")
    public void testGenerateTokenCandidate() {
        String token = jwtUtil.generateToken("candidateUser", "ROLE_CANDIDATE");
        assertNotNull(token);

        Claims claims = jwtUtil.validateAndExtractClaims(token);
        assertEquals("candidateUser", claims.getSubject());
        assertEquals("ROLE_CANDIDATE", claims.get("role"));
        assertNotNull(claims.getExpiration());
    }

    @Test
    @DisplayName("Positive: Generate token and extract valid claims for recruiter")
    public void testGenerateTokenRecruiter() {
        String token = jwtUtil.generateToken("recruiterUser", "ROLE_RECRUITER");
        assertNotNull(token);

        Claims claims = jwtUtil.validateAndExtractClaims(token);
        assertEquals("recruiterUser", claims.getSubject());
        assertEquals("ROLE_RECRUITER", claims.get("role"));
    }

    @Test
    @DisplayName("Negative: Tampered token throws signature exception")
    public void testTamperedTokenFails() {
        String token = jwtUtil.generateToken("testUser", "ROLE_CANDIDATE");
        String tamperedToken = token + "xyz";

        assertThrows(SignatureException.class, () -> {
            jwtUtil.validateAndExtractClaims(tamperedToken);
        });
    }

    @Test
    @DisplayName("Negative: Expired token throws ExpiredJwtException")
    public void testExpiredTokenFails() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);
        String token = jwtUtil.generateToken("expiredUser", "ROLE_CANDIDATE");

        assertThrows(ExpiredJwtException.class, () -> {
            jwtUtil.validateAndExtractClaims(token);
        });
    }
}
