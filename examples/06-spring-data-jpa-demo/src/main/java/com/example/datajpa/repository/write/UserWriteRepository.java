package com.example.datajpa.repository.write;

import com.example.datajpa.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserWriteRepository extends JpaRepository<User, Long> {
}
