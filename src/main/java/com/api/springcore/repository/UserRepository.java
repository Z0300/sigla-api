package com.api.springcore.repository;

import com.api.springcore.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailWithRolesAndPermissions(@Param("email") String email);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithRolesAndPermissions(@Param("id") Long id);

    @Query("SELECT u.id FROM User u WHERE " +
            "(:searchTerm IS NULL OR " +
            "LOWER(u.email)     LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName)  LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Long> findIdsBySearch(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.id IN :ids ORDER BY u.createdAt DESC")
    List<User> findAllByIds(@Param("ids") List<Long> ids);

    @Query("SELECT COUNT(u) FROM User u WHERE " +
            "(:searchTerm IS NULL OR " +
            "LOWER(u.email)     LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName)  LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    long countBySearch(@Param("searchTerm") String searchTerm);
}