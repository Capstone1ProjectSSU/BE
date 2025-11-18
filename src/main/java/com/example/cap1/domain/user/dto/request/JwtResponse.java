package com.example.cap1.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 토큰 응답
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private String username;

    public JwtResponse(String accessToken, String username) {
        this.accessToken = accessToken;
        this.username = username;
    }
}
