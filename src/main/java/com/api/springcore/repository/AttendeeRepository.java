package com.api.springcore.repository;

import com.api.springcore.entity.Attendee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AttendeeRepository extends JpaRepository<Attendee, Long> {
    long countByEventId(Long eventId);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    Optional<Attendee> findByUserIdAndEventId(Long userId, Long eventId);

    List<Attendee> findAllByEventId(Long eventId);

    @Query("""
            SELECT a FROM Attendee a
            JOIN FETCH a.user
            WHERE a.event.id = :eventId
            AND (:status IS NULL OR a.status = :status)
            """)
    Page<Attendee> findByEventIdAndStatus(
            @Param("eventId") Long eventId,
            @Param("status") String status,
            Pageable pageable
    );

    List<Attendee> findAllByEventIdAndStatus(Long eventId, String status);

    @Query("""
            SELECT a FROM Attendee a
            WHERE a.event.id = :eventId
              AND a.status = 'registered'
              AND NOT EXISTS (
                  SELECT c FROM CheckIn c
                  WHERE c.attendee.id = a.id
              )
            """)
    List<Attendee> findNoShowsByEventId(@Param("eventId") Long eventId);
}
