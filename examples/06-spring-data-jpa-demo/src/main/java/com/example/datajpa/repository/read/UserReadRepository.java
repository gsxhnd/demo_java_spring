package com.example.datajpa.repository.read;

import com.example.datajpa.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserReadRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    List<User> findByStatus(User.UserStatus status);

    Page<User> findByStatus(User.UserStatus status, Pageable pageable);

    List<User> findByAgeBetween(Integer minAge, Integer maxAge);

    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.email LIKE %:keyword%")
    List<User> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT u FROM User u WHERE u.address.city = :city")
    List<User> findByCity(@Param("city") String city);

    @Query("SELECT u FROM User u JOIN FETCH u.address WHERE u.id = :id")
    Optional<User> findByIdWithAddress(@Param("id") Long id);

    @Query(value = "SELECT COUNT(*) FROM users WHERE status = ?1", nativeQuery = true)
    long countByStatusNative(String status);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
