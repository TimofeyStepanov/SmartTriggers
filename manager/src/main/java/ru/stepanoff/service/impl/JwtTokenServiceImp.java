package ru.stepanoff.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.stepanoff.service.JwtTokenService;

import java.util.Date;
import java.util.List;

@Service
public class JwtTokenServiceImp implements JwtTokenService {
    private static final int TTL_IN_MILLISECONDS = 30 * 60 * 1000;

    private final Algorithm algorithm;
    private final JWTVerifier jwtVerifier;

    public JwtTokenServiceImp(@Value("{$jwt.token.secret}") String secret) {
        algorithm = Algorithm.HMAC256(secret.getBytes());
        jwtVerifier = JWT.require(algorithm).build();
    }

    @Override
    public String createToken(String userName, List<String> grantedAuthorities) {
        return JWT.create()
                .withSubject(userName)
                .withExpiresAt(new Date(System.currentTimeMillis() + TTL_IN_MILLISECONDS))
                .withClaim("authorities", grantedAuthorities)
                .sign(algorithm);
    }

    @Override
    public String getUserName(String token) {
        return jwtVerifier.verify(token).getSubject();
    }

    @Override
    public List<String> getAuthorities(String token) {
        return jwtVerifier.verify(token).getClaim("authorities").asList(String.class);
    }

}