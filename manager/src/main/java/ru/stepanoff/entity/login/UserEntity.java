package ru.stepanoff.entity.login;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "user_login")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String userName;

    private String password;
}
