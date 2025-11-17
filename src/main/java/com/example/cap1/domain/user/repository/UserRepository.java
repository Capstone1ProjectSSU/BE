package com.example.cap1.domain.user.repository;

import com.example.cap1.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
