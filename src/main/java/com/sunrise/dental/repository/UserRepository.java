package com.sunrise.dental.repository;

import com.sunrise.dental.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("select u from User u where lower(u.username) = lower(:identifier) or lower(u.email) = lower(:identifier)")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);

    long countByActiveTrue();
}
