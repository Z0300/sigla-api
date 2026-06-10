package com.api.springcore.repository;

import com.api.springcore.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    boolean existsByTitle(String title);

    @Query("SELECT e.id FROM Event e WHERE " +
            "(:searchTerm IS NULL OR " +
            "  LOWER(e.title)       LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "  LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            ") " +
            "AND (:status IS NULL OR e.status = :status)")
    Page<Long> findIdsBySearch(@Param("searchTerm") String searchTerm,
                               @Param("status") String status,
                               Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.id IN :ids ORDER BY e.createdAt DESC")
    List<Event> findAllByIds(@Param("ids") List<Long> ids);

    @Query("""
        SELECT e.id FROM Event e
        WHERE e.status IN ('published', 'ongoing')
        AND (:searchTerm IS NULL OR
            LOWER(e.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR
            LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
        AND (:status IS NULL OR e.status = :status)
        """)
    Page<Long> findPublicEventIds(
            @Param("searchTerm") String searchTerm,
            @Param("status") String status,
            Pageable pageable
    );
}
