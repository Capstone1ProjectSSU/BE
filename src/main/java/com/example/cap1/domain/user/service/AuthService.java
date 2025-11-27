package com.example.cap1.domain.user.service;

import com.example.cap1.global.auth.JwtAuthenticationFilter;
import com.example.cap1.domain.user.domain.User;
import com.example.cap1.domain.user.dto.request.SignUpRequest;
import com.example.cap1.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registerUser(SignUpRequest signUpRequest) {
        // 중복 체크
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new RuntimeException("이미 사용중인 사용자명입니다");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("이미 사용중인 이메일입니다");
        }

        // 사용자 생성
        User user = User.builder()
                .username(signUpRequest.getUsername())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .email(signUpRequest.getEmail())
//                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    @Transactional
    public void withdrawUser(User user) {
        // User 엔티티에서 cascade 설정이 되어 있다면
        // 사용자 삭제 시 자동으로 Post, Comment도 삭제됨
        userRepository.delete(user);
    }
}
