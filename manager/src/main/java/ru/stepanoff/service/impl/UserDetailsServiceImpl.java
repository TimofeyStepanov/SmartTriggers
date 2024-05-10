package ru.stepanoff.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import ru.stepanoff.entity.login.UserEntity;
import ru.stepanoff.repository.login.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        Optional<UserEntity> userOptional = userRepository.findUserEntityByUserName(userEmail);
        UserEntity user = userOptional.orElseThrow(() -> new UsernameNotFoundException("No user with such name!"));

        List<SimpleGrantedAuthority> authorityList = Stream.of("user")
                .map(SimpleGrantedAuthority::new)
                .toList();

        return new User(
                user.getUserName(),
                user.getPassword(),
                authorityList
        );
    }
}
