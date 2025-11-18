package com.example.cap1.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;


// 회원가입 요청
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
    @NotBlank(message = "사용자명은 필수입니다")
    @Size(min = 3, max = 20)
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 6, max = 40)
    private String password;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    private String email;
}

