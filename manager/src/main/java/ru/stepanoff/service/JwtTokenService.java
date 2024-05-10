package ru.stepanoff.service;


import java.util.List;

public interface JwtTokenService {
    String createToken(String userName, List<String> grantedAuthorities);

    String getUserName(String token);

    List<String> getAuthorities(String token);
}
