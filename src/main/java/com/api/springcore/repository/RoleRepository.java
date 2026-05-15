package com.api.springcore.repository;

import com.api.springcore.entity.Role;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    @EntityGraph(attributePaths = {"permissions"})
    @Query("SELECT r FROM Role r WHERE r.id = :id")
    Optional<Role> findByIdWithPermissions(@Param("id") Long id);

    @NullMarked
    @EntityGraph(attributePaths = {"permissions"})
    List<Role> findAll();

    @Query("SELECT r FROM Role r WHERE r.id IN :ids")
    List<Role> findAllByIdIn(@Param("ids") Set<Long> ids);
}
