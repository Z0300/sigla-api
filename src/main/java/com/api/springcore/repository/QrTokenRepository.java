package com.api.springcore.repository;

import com.api.springcore.entity.QrToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QrTokenRepository extends JpaRepository<QrToken, Long> {

    Optional<QrToken> findByToken(String token);

    Optional<QrToken> findByAttendeeIdAndUsedFalse(Long attendeeId);
}
