package com.example.social_network.validator;

import com.example.social_network.domain.User;

public class UserValidator implements Validator<User> {
    @Override
    public void validate(User user) throws ValidationException {
        if (user.getName() == null || user.getName().isEmpty()) {
            throw new ValidationException("User name must not be null or empty");
        }
    }
}