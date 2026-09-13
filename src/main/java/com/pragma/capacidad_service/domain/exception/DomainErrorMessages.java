package com.pragma.capacidad_service.domain.exception;

public final class DomainErrorMessages {

    private DomainErrorMessages() {
    }

    public static final String NAME_REQUIRED = "El campo name es obligatorio";
    public static final String DESCRIPTION_REQUIRED = "El campo description es obligatorio";
    public static final String DUPLICATE_NAME = "El nombre de la capacidad ya está registrado";
    public static final String TECHNOLOGIES_MAX_LENGTH = "El número maximo de tecnologias es 20";
    public static final String TECHNOLOGIES_MIN_LENGTH = "El número minimo de tecnologias es 3";
    public static final String REPEATED_TECHNOLOGY = "No puede exisitir tecnologias repetidas";
    public static final String TECHNOLOGIES_IDS_REQUIRED = "La lista de tecnologias es obligatoria";
    public static final String TECHNOLOGY_NOT_FOUND = "Alguna de las tecnologias ingresadas no existe";
    public static final String INVALID_PAGE = "El parámetro page debe ser mayor o igual a 0";
    public static final String INVALID_SIZE = "El parámetro size debe ser mayor a 0";
    public static final String INVALID_SORT_BY = "El parámetro sortBy solo permite: name, numberTechnologies";
    public static final String INVALID_DIRECTION = "El parámetro direction solo permite: asc, desc";
}
