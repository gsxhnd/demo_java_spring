package com.example.mybatis.mapper.write;

import com.example.mybatis.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserWriteMapper {

    @Insert("INSERT INTO users (username, email, age, status, province, city, street, zip_code, created_at, updated_at) " +
            "VALUES (#{username}, #{email}, #{age}, #{status}, #{province}, #{city}, #{street}, #{zipCode}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE users SET username = #{username}, email = #{email}, age = #{age}, " +
            "province = #{province}, city = #{city}, street = #{street}, zip_code = #{zipCode}, " +
            "updated_at = NOW() WHERE id = #{id}")
    int update(User user);

    @Delete("DELETE FROM users WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Update("UPDATE users SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
