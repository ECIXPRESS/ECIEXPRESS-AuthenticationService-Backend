package edu.dosw.dto;

public record AuthResponseDto(
        String accessToken,
        String refreshToken,
        UserInfoDto userInfo,
        long expiresIn
) {}