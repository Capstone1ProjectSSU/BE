package com.example.cap1.domain.user.api;


import com.example.cap1.domain.user.domain.User;
import com.example.cap1.domain.user.dto.request.JwtResponse;
import com.example.cap1.domain.user.dto.request.LoginRequest;
import com.example.cap1.domain.user.dto.response.MessageResponse;
import com.example.cap1.domain.user.dto.request.SignUpRequest;
import com.example.cap1.domain.user.service.AuthService;
import com.example.cap1.global.auth.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor

public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AuthService authService;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        try {
            authService.registerUser(signUpRequest);
            return ResponseEntity.ok(new MessageResponse("회원가입이 완료되었습니다"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * 로그인
     */
    @Operation(summary = "로그인", description = "사용자 인증 후 JWT 토큰을 발급합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "로그인 실패")
    })
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.createToken(authentication);

            return ResponseEntity.ok(new JwtResponse(jwt, loginRequest.getUsername()));
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("로그인 실패: 비밀번호가 일치하지 않습니다"));
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("로그인 실패: 사용자를 찾을 수 없습니다"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("로그인 실패: " + e.getClass().getSimpleName() + " - " + e.getMessage()));
        }
    }

    /**
     * 로그아웃 (클라이언트에서 토큰 삭제)
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new MessageResponse("로그아웃되었습니다"));
    }

    /**
     * 현재 사용자 정보
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("인증되지 않은 사용자입니다"));
        }
        return ResponseEntity.ok(new MessageResponse("현재 사용자: " + authentication.getName()));
    }

    /**
     * 회원탈퇴
     */
    @Operation(summary = "회원탈퇴", description = "회원을 탈퇴합니다. 작성한 모든 게시글과 댓글이 삭제됩니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원탈퇴 성공"),
            @ApiResponse(responseCode = "400", description = "회원탈퇴 실패")
    })
    @DeleteMapping("/withdraw")
    public ResponseEntity<?> withdrawUser(@AuthenticationPrincipal User user) {
        try {
            authService.withdrawUser(user);
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok(new MessageResponse("회원탈퇴가 완료되었습니다"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
