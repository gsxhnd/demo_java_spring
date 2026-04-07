package com.example.ioc.config;

import com.example.ioc.model.User;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Bean 作用域配置演示
 *
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#SCOPE_SINGLETON
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#SCOPE_PROTOTYPE
 */
@Configuration
public class ScopeConfig {

    /**
     * 单例 Bean（默认）
     * 整个容器只有一个实例，容器启动时创建
     */
    @Bean
    public User singletonUser() {
        User user = new User();
        user.setId(System.currentTimeMillis());
        user.setName("单例用户");
        System.out.println("[ScopeConfig] 创建单例 User: " + user.getId());
        return user;
    }

    /**
     * 原型 Bean
     * 每次 getBean 都创建新实例，容器不管理销毁
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public User prototypeUser() {
        User user = new User();
        user.setId(System.currentTimeMillis());
        user.setName("原型用户");
        System.out.println("[ScopeConfig] 创建原型 User: " + user.getId());
        return user;
    }
}
