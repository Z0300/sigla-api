package com.api.springcore.repository;

import com.api.springcore.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT p FROM Permission p WHERE p.id IN :ids")
    List<Permission> findAllByIdIn(@Param("ids") Set<Long> ids);

    @Query("""
            select p from Permission p
            where (:name is null or lower (p.name)
            like lower(concat('%', :name, '%')))
            """)
    Page<Permission> findAllWithSearch(@Param("name") String name, Pageable pageable);
}
