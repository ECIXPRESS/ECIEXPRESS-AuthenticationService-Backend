package edu.dosw.application.dto;

public record UserInfoDTO(
        String userId,
        String email,
        String role
) {}