package edu.dosw.domain.model.ValueObject;

public record Email(String value) {
    public Email {
        if (value == null || !value.matches("^[A-Za-z0-9+_.-]+@mail\\.escuelaing\\.edu\\.co$")) {
            throw new IllegalArgumentException("Invalid institutional email format");
        }
    }
}