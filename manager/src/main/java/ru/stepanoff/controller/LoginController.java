package ru.stepanoff.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.stepanoff.model.login.UserRequest;
import ru.stepanoff.service.LoginService;

@RestController
@RequiredArgsConstructor
@RequestMapping("security")
public class LoginController {
    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserRequest userRequest) {
        try {
            return new ResponseEntity<>(loginService.login(userRequest), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
