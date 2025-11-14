package edu.dosw.application.dto;


public record TokenValidationDTO(
        boolean valid,
        String email,
        String userId,
        String role
) {}