package com.api.springcore.repository;

import com.api.springcore.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    @Query("""
        SELECT c FROM CheckIn c
        JOIN FETCH c.attendee a
        JOIN FETCH a.user u
        WHERE c.session.id = :sessionId
        ORDER BY c.checkedInAt ASC
        """)
    List<CheckIn> findAllBySessionIdWithAttendee(@Param("sessionId") Long sessionId);
}