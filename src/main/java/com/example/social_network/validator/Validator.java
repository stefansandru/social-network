package com.example.social_network.validator;

public interface Validator<E> {
    void validate(E entity) throws ValidationException;
}