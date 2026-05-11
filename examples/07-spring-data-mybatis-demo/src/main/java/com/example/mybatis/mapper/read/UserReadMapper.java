package com.example.mybatis.mapper.read;

import com.example.mybatis.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserReadMapper {

    @Select("SELECT * FROM users")
    List<User> findAll();

    @Select("SELECT * FROM users WHERE id = #{id}")
    Optional<User> findById(@Param("id") Long id);

    @Select("SELECT * FROM users WHERE email = #{email}")
    Optional<User> findByEmail(@Param("email") String email);

    @Select("SELECT * FROM users WHERE username = #{username}")
    Optional<User> findByUsername(@Param("username") String username);

    @Select("SELECT * FROM users WHERE status = #{status}")
    List<User> findByStatus(@Param("status") String status);

    @Select("SELECT * FROM users WHERE age BETWEEN #{minAge} AND #{maxAge}")
    List<User> findByAgeBetween(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);

    @Select("SELECT * FROM users WHERE username LIKE CONCAT('%', #{keyword}, '%') OR email LIKE CONCAT('%', #{keyword}, '%')")
    List<User> searchByKeyword(@Param("keyword") String keyword);

    @Select("SELECT * FROM users WHERE city = #{city}")
    List<User> findByCity(@Param("city") String city);

    @Select("SELECT COUNT(*) FROM users WHERE email = #{email} AND status != 'DELETED'")
    int countByEmail(@Param("email") String email);

    @Select("SELECT COUNT(*) FROM users WHERE username = #{username} AND status != 'DELETED'")
    int countByUsername(@Param("username") String username);

    @Select("SELECT COUNT(*) FROM users WHERE status = #{status}")
    long countByStatus(@Param("status") String status);

    List<User> findWithDynamicConditions(
            @Param("username") String username,
            @Param("email") String email,
            @Param("status") String status,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("city") String city,
            @Param("orderBy") String orderBy);

    int countWithDynamicConditions(
            @Param("username") String username,
            @Param("email") String email,
            @Param("status") String status,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("city") String city);

    List<User> findByIds(@Param("ids") List<Long> ids);
}
