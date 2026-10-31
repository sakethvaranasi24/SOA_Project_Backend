package com.klu.services.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;
    private GatewayFilterChain chain;
    private final String secret = "TalentSyncSecretKeyForJWTSecurityAuthenticationSOA20262027VerySecureKey";

    @BeforeEach
    public void setUp() {
        filter = new JwtAuthenticationFilter();
        ReflectionTestUtils.setField(filter, "secret", secret);
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    private String createToken(String username, String role, long expirationMillis) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    @DisplayName("Positive: Public endpoint /auth/login bypasses JWT filter")
    public void testPublicEndpointAllowedWithoutToken() {
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.POST, "/auth/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, times(1)).filter(exchange);
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Negative: Missing Authorization header returns 401 Unauthorized")
    public void testMissingAuthorizationHeaderReturns401() {
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.GET, "/jobs").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, never()).filter(exchange);
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Negative: Malformed header format returns 401 Unauthorized")
    public void testMalformedAuthorizationHeaderReturns401() {
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.GET, "/jobs")
                .header(HttpHeaders.AUTHORIZATION, "Basic invalidtoken")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, never()).filter(exchange);
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Negative: Expired JWT token returns 401 Unauthorized")
    public void testExpiredTokenReturns401() {
        String expiredToken = createToken("sai", "ROLE_CANDIDATE", -5000);
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.GET, "/jobs")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, never()).filter(exchange);
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Negative: Invalid JWT signature returns 401 Unauthorized")
    public void testInvalidSignatureReturns401() {
        String invalidToken = createToken("sai", "ROLE_CANDIDATE", 60000) + "tampered";
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.GET, "/jobs")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, never()).filter(exchange);
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Positive: Valid recruiter token accessing POST /jobs is permitted")
    public void testRecruiterAccessingJobPostIsPermitted() {
        String recruiterToken = createToken("sakeeth", "ROLE_RECRUITER", 60000);
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.POST, "/jobs")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, times(1)).filter(any());
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Negative: Candidate attempting to create job (POST /jobs) returns 403 Forbidden")
    public void testCandidateCreatingJobReturns403() {
        String candidateToken = createToken("sai", "ROLE_CANDIDATE", 60000);
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.POST, "/jobs")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + candidateToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, never()).filter(exchange);
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Negative: Candidate attempting to access /recruitments returns 403 Forbidden")
    public void testCandidateAccessingRecruitmentsReturns403() {
        String candidateToken = createToken("sai", "ROLE_CANDIDATE", 60000);
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.GET, "/recruitments/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + candidateToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, never()).filter(exchange);
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Negative: Candidate attempting to access /student/all returns 403 Forbidden")
    public void testCandidateAccessingStudentAllReturns403() {
        String candidateToken = createToken("sai", "ROLE_CANDIDATE", 60000);
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.GET, "/student/all")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + candidateToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, never()).filter(exchange);
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Negative: Recruiter attempting to apply for job (POST /applications) returns 403 Forbidden")
    public void testRecruiterApplyingForJobReturns403() {
        String recruiterToken = createToken("sakeeth", "ROLE_RECRUITER", 60000);
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.POST, "/applications")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, never()).filter(exchange);
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Positive: Candidate applying for job (POST /applications) is permitted")
    public void testCandidateApplyingForJobIsPermitted() {
        String candidateToken = createToken("sai", "ROLE_CANDIDATE", 60000);
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.POST, "/applications")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + candidateToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, times(1)).filter(any());
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Security: Gateway strips spoofed X-Auth headers and sets verified JWT claims")
    public void testHeaderSpoofingProtection() {
        String token = createToken("realUser", "ROLE_RECRUITER", 60000);
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.POST, "/jobs")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("X-Auth-User", "hackerUser")
                .header("X-Auth-Role", "FAKE_ROLE")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, times(1)).filter(argThat(ex -> {
            String userHeader = ex.getRequest().getHeaders().getFirst("X-Auth-User");
            String roleHeader = ex.getRequest().getHeaders().getFirst("X-Auth-Role");
            return "realUser".equals(userHeader) && "ROLE_RECRUITER".equals(roleHeader);
        }));
    }
}
