package com.example.core.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 用户仓储实现类 — 接口 UserRepository 的具体实现，模拟数据库操作。
 *
 * <h3>@Repository 注解详解：</h3>
 * <p>{@code @Repository} 是 {@code @Component} 的"语义化特化（stereotype）"注解，
 * 专门用于标注数据访问层（DAO / Repository）的类。它在功能上与 {@code @Component} 完全等价，
 * 但额外提供了以下好处：</p>
 * <ol>
 *   <li><b>语义清晰</b>：一眼就能看出这是数据访问层的 Bean，提高代码可读性。</li>
 *   <li><b>异常转换</b>：Spring 会自动将 JDBC / JPA 等数据库访问异常（checked exceptions）
 *       转换为 Spring 统一的 DataAccessException（unchecked），使业务层无需关心底层数据库实现细节。</li>
 *   <li><b>分层架构</b>：配合 @Service、@Controller 形成经典的三层架构标记体系。</li>
 * </ol>
 *
 * <h3>三个等级注释的关系：</h3>
 * <pre>
 *   @Component          // 通用组件（基类）
 *       ↓
 *   @Repository         // 数据访问层（DAO）
 *   @Service            // 业务逻辑层
 *   @Controller         // 控制层（Spring MVC）
 * </pre>
 *
 * <p>Spring 组件扫描（@ComponentScan）时会识别所有这四个注解，将它们注册为 Bean。</p>
 */
@Slf4j
@Repository // 【核心注解】标记为数据访问层组件，Spring 会自动将此类注册为容器中的一个 Bean
public class UserRepositoryImpl implements UserRepository {

    /**
     * Bean 实例化时的构造函数。
     * 本类没有需要注入的依赖，所以构造方法为空。
     * 当 Spring 容器启动时，会调用此构造方法创建 UserRepositoryImpl 的单例实例。
     */
    public UserRepositoryImpl() {
        log.info("UserRepository 初始化");
    }

    /**
     * 根据用户 ID 查找用户（模拟从数据库查询）。
     * <p>实际项目中这里会通过 JdbcTemplate / JPA / MyBatis 等方式操作真实数据库。</p>
     *
     * @param id 用户唯一标识
     * @return 拼接的用户信息字符串，模拟数据库返回的行数据
     */
    @Override
    public Object findById(String id) {
        log.info("从数据库查询用户: {}", id);
        return "User{id='" + id + "'}";
    }

    /**
     * 保存用户数据（模拟写入数据库）。
     * <p>实际项目中这里会通过持久层框架完成 INSERT / UPDATE 操作。</p>
     *
     * @param id   用户唯一标识
     * @param user 用户数据
     */
    @Override
    public void save(String id, Object user) {
        log.info("保存用户到数据库: {}", id);
    }
}
