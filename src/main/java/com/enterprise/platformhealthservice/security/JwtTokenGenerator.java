package com.enterprise.platformhealthservice.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

public class JwtTokenGenerator {

    private static final String SECRET =
            "my-super-secret-key-for-local-jwt-testing-2026";

    public static String generateToken() throws JOSEException {

        JWSSigner signer = new MACSigner(
                SECRET.getBytes(StandardCharsets.UTF_8)
        );

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("test-admin")
                .claim("permissions",
                        List.of("PLATFORM_HEALTH_READ"))
                .issueTime(new Date())
                .expirationTime(
                        new Date(System.currentTimeMillis() + 3600000)
                )
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256),
                claims
        );

        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    public static void main(String[] args) throws JOSEException {
        System.out.println(generateToken());
    }
}