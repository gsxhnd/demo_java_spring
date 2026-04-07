package com.example.ioc.service;

import com.example.ioc.model.User;
import com.example.ioc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;

/**
 * 用户服务 - 演示构造器注入（推荐方式）
 *
 * Spring 官方推荐使用构造器注入：
 * 1. 不可变性：依赖通过构造函数注入，对象创建后不可变
 * 2. 易测试：无需 Spring 容器即可实例化进行单元测试
 * 3. 防止循环依赖：构造器注入在实例化时就完成依赖解析
 */
@Service
public class UserService {

    // 构造器注入（推荐方式）
    private final UserRepository userRepository;

    // 可选依赖注入示例
    private final org.springframework.beans.factory.ObjectProvider<EmailService> emailServiceProvider;

    @Autowired
    public UserService(UserRepository userRepository,
                       @Lazy org.springframework.beans.factory.ObjectProvider<EmailService> emailServiceProvider) {
        this.userRepository = userRepository;
        this.emailServiceProvider = emailServiceProvider;
        System.out.println("[UserService] 构造器注入 UserRepository: " + userRepository);
    }

    @PostConstruct
    public void init() {
        System.out.println("[UserService] @PostConstruct 初始化方法被调用");
        // 初始化示例数据
        if (userRepository.count() == 0) {
            userRepository.save(new User(1L, "张三", "zhangsan@example.com", 25));
            userRepository.save(new User(2L, "李四", "lisi@example.com", 30));
            userRepository.save(new User(3L, "王五", "wangwu@example.com", 28));
        }
    }

    @PreDestroy
    public void destroy() {
        System.out.println("[UserService] @PreDestroy 销毁方法被调用");
    }

    public User getUserRepository() {
        // 返回注入的依赖，演示依赖注入
        return new User(0L, "UserRepository", userRepository.getClass().getName(), 0);
    }

    public User createUser(String name, String email, Integer age) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setAge(age);
        return userRepository.save(user);
    }

    public User findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * 演示 @Lazy 延迟注入
     * EmailService 不会在启动时创建，而是在首次使用时才初始化
     */
    public void sendWelcomeEmail(User user) {
        EmailService emailService = emailServiceProvider.getIfAvailable();
        if (emailService != null) {
            emailService.sendEmail(user.getEmail(), "欢迎注册", "欢迎 " + user.getName() + "！");
        }
    }
}
