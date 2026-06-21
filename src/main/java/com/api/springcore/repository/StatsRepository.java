package com.api.springcore.repository;

import com.api.springcore.entity.Attendee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StatsRepository extends JpaRepository<Attendee, Long> {

    @Query("SELECT COUNT(e) FROM Event e WHERE e.organizer.id = :organizerId")
    long countTotalEvents(@Param("organizerId") Long organizerId);

    @Query("""
        SELECT COUNT(e) FROM Event e
        WHERE e.organizer.id = :organizerId
          AND e.status IN ('published', 'ongoing')
        """)
    long countActiveEvents(@Param("organizerId") Long organizerId);

    @Query("""
        SELECT COUNT(a) FROM Attendee a
        WHERE a.event.organizer.id = :organizerId
        """)
    long countTotalRegistrations(@Param("organizerId") Long organizerId);

    @Query("""
        SELECT COUNT(c) FROM CheckIn c
        WHERE c.session.event.organizer.id = :organizerId
        """)
    long countTotalCheckIns(@Param("organizerId") Long organizerId);

    @Query("""
        SELECT a.status, COUNT(a) FROM Attendee a
        WHERE a.event.organizer.id = :organizerId
        GROUP BY a.status
        """)
    List<Object[]> countAttendeesByStatus(@Param("organizerId") Long organizerId);

    @Query("""
        SELECT s.title, s.event.title, COUNT(c), s.capacity
        FROM CheckIn c
        JOIN c.session s
        WHERE s.event.organizer.id = :organizerId
        GROUP BY s.id, s.title, s.event.title, s.capacity
        ORDER BY COUNT(c) DESC
        LIMIT 5
        """)
    List<Object[]> findTopSessionsByCheckIns(@Param("organizerId") Long organizerId);

    @Query("""
        SELECT CAST(a.registeredAt AS date), COUNT(a)
        FROM Attendee a
        WHERE a.event.organizer.id = :organizerId
        GROUP BY CAST(a.registeredAt AS date)
        ORDER BY CAST(a.registeredAt AS date) ASC
        """)
    List<Object[]> countRegistrationsByDay(@Param("organizerId") Long organizerId);
}
