package com.example.ioc.config;

import com.example.ioc.model.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Java Config 配置类 - 演示 @Bean 注册方式
 *
 * 适用场景：注册第三方库的类或需要精细控制实例化逻辑的 Bean
 */
@Configuration
public class AppConfig {

    /**
     * 演示：使用 @Bean 注册 Bean
     * 默认 Bean 名称为方法名
     */
    @Bean
    public String applicationName() {
        return "Spring IoC Demo Application";
    }

    /**
     * 演示：@Primary 指定主 Bean
     * 当同一类型有多个 Bean 时，优先使用带有 @Primary 的
     */
    @Bean
    @Primary
    public MessageService primaryMessageService() {
        return new MessageService("主消息服务");
    }

    @Bean
    public MessageService secondaryMessageService() {
        return new MessageService("备用消息服务");
    }

    /**
     * 演示：@Bean 注册外部类
     */
    @Bean
    public User defaultAdminUser() {
        User admin = new User();
        admin.setId(0L);
        admin.setName("系统管理员");
        admin.setEmail("admin@example.com");
        admin.setAge(0);
        return admin;
    }

    /**
     * 简单的消息服务类
     */
    public static class MessageService {
        private final String name;

        public MessageService(String name) {
            this.name = name;
        }

        public String getMessage() {
            return "来自 " + name + " 的消息";
        }

        @Override
        public String toString() {
            return "MessageService{name='" + name + "'}";
        }
    }
}
