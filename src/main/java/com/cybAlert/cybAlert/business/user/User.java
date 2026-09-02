package com.cybAlert.cybAlert.business.user;

import org.hibernate.validator.constraints.UUID;

public class User {
    private final UUID id;
    private final String fullname;
    private final String email;
    private final String password;

    public User(final UUID id, final String fullname, final String email, final String password) {
        this.id = id;
        this.fullname = fullname;
        this.email = email;
        this.password = password;

    }
}
