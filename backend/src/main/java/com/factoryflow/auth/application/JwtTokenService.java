package com.factoryflow.auth.application;

import com.factoryflow.auth.domain.UserAccount;
import java.time.Instant;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Service
public class JwtTokenService {

    private final JwtProperties properties;
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;

    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
        SecretKey key = new SecretKeySpec(properties.secret().getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        this.decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    public String createAccessToken(UserAccount user) {
        Instant issuedAt = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("factoryflow")
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(properties.accessTokenTtl()))
                .subject(user.getEmail())
                .claim("uid", user.getId())
                .build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    public String subject(String token) {
        return decoder.decode(token).getSubject();
    }

    public long expiresInSeconds() {
        return properties.accessTokenTtl().toSeconds();
    }
}
