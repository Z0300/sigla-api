package com.api.springcore.repository;

import com.api.springcore.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findAllByEventIdOrderByStartTimeAsc(Long eventId);

    @Query("""
        SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END
        FROM Session s
        WHERE s.event.id = :eventId
          AND s.room = :room
          AND s.id <> :excludeId
          AND s.startTime < :endTime
          AND s.endTime > :startTime
        """)
    boolean existsTimeConflict(
            @Param("eventId") Long eventId,
            @Param("room") String room,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeId") Long excludeId
    );

    @Query("SELECT COUNT(c) FROM CheckIn c WHERE c.session.id = :sessionId")
    long countCheckInsBySessionId(@Param("sessionId") Long sessionId);

    boolean existsByIdAndEventId(Long sessionId, Long eventId);

    @Query("SELECT s FROM Session s JOIN FETCH s.event WHERE s.id = :id")
    Optional<Session> findByIdWithEvent(@Param("id") Long id);
}
