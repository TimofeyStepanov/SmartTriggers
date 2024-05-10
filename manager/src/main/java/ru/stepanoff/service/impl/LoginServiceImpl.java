package ru.stepanoff.service.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.stepanoff.model.login.UserRequest;
import ru.stepanoff.service.JwtTokenService;
import ru.stepanoff.service.LoginService;

import java.util.Collection;
import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {
    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;

    @Override
    public String login(@Valid UserRequest userRequest) {
        String name = userRequest.getName();
        String password = userRequest.getPassword();

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(name, password);
        Authentication authentication = authenticationManager.authenticate(token); // если пользователя нет, то выбросит ошибку!

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        List<String> authoritiesString = authorities
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return jwtTokenService.createToken(name, authoritiesString);
    }
}
