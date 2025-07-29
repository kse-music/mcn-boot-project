package cn.hiboot.mcn.cloud.security;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.function.Consumer;

/**
 * JwtUtil
 *
 * @author DingHao
 * @since 2025/7/29 11:31
 */
public final class JwtUtil {

    private final JWSHeader header;
    private final JWSSigner jwsSigner;
    private final JWSVerifier jwsVerifier;

    public JwtUtil(String secret) {
        this(JWSAlgorithm.HS256, secret, null);
    }

    public JwtUtil(KeyPair keyPair) {
        this(JWSAlgorithm.RS256, null, keyPair);
    }

    private JwtUtil(JWSAlgorithm algorithm, String secret, KeyPair keyPair) {
        this.header = new JWSHeader.Builder(algorithm).type(JOSEObjectType.JWT).build();
        try {
            if (secret == null) {
                this.jwsSigner = new RSASSASigner(keyPair.getPrivate());
                this.jwsVerifier = new RSASSAVerifier((RSAPublicKey) keyPair.getPublic());
            } else {
                this.jwsSigner = new MACSigner(secret);
                this.jwsVerifier = new MACVerifier(secret);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String create(Map<String, Object> claims, long expireSeconds) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expireSeconds);
        return create(builder -> {
            claims.forEach(builder::claim);
            builder.expirationTime(Date.from(exp)).issueTime(Date.from(now));
        });
    }

    public String create(Consumer<JWTClaimsSet.Builder> consumer) {
        try {
            JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
            consumer.accept(builder);
            JWTClaimsSet claims = builder.build();
            SignedJWT jwt = new SignedJWT(this.header, claims);
            jwt.sign(jwsSigner);
            return jwt.serialize();
        } catch (Exception e) {
            throw new SecurityException("Create token failed", e);
        }
    }

    public JWTClaimsSet verify(String token) {
        try {
            JWTClaimsSet claims = claimsSet(token);
            Date exp = claims.getExpirationTime();
            if (exp != null && exp.before(new Date())) {
                throw new SecurityException("Token has expired");
            }
            return claims;
        } catch (Exception e) {
            throw new SecurityException("Token verification failed", e);
        }
    }

    public JWTClaimsSet claimsSet(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (!signedJWT.verify(jwsVerifier)) {
                throw new SecurityException("Invalid token signature.");
            }
            return signedJWT.getJWTClaimsSet();
        } catch (Exception e) {
            throw new SecurityException("Invalid token.");
        }
    }

    public boolean isExpire(String token) {
        try {
            JWTClaimsSet claims = claimsSet(token);
            Date exp = claims.getExpirationTime();
            return exp != null && exp.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

}
