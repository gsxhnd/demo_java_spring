package com.example.multidb.repository.mongo;

import com.example.multidb.entity.mongo.UserLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface UserLogRepository extends MongoRepository<UserLog, String> {

    List<UserLog> findByUserIdAndAction(Long userId, String action);

    Page<UserLog> findByCreatedAtAfter(Instant since, Pageable pageable);
}
