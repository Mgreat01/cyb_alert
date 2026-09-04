package com.cybAlert.cybAlert.infrastructure.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Getter
    private UUID id;

    @Column(length = 100)
    private String fullname;

    @Getter
    @Column(length = 100, nullable = false, unique = true)
    private String email;

    protected UserEntity() {
    }

    private UserEntity(String fullname, String email) {
        this.fullname = fullname;
        this.email = email;
    }
}
