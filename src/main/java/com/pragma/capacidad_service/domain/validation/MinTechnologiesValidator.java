package com.pragma.capacidad_service.domain.validation;

public final class MinTechnologiesValidator {

    private static final int MIN_LENGTH = 3;

    private MinTechnologiesValidator() {
    }

    public static boolean isValid(int size) {
        return size >= MIN_LENGTH;
    }
}
