package com.api.springcore.repository;

import com.api.springcore.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
    boolean existsByTitle(String title);
}
