package com.api.springcore.repository;

import com.api.springcore.entity.SessionQr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SessionQrRepository extends JpaRepository<SessionQr, Long> {

    Optional<SessionQr> findBySessionIdAndActiveTrue(Long sessionId);

    @Modifying
    @Query("UPDATE SessionQr q SET q.active = false WHERE q.session.id = :sessionId")
    void deactivateAllBySessionId(@Param("sessionId") Long sessionId);
}
