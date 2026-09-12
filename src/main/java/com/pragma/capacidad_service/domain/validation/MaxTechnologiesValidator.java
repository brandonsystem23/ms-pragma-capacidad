package com.pragma.capacidad_service.domain.validation;

public final class MaxTechnologiesValidator {

    private static final int MAX_LENGTH = 20;

    private MaxTechnologiesValidator() {
    }

    public static boolean isValid(int size) {
        return size <= MAX_LENGTH;
    }
}
