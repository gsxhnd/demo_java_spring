package com.example.ioc.controller;

import com.example.ioc.config.AppConfig;
import com.example.ioc.model.User;
import com.example.ioc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IoC 演示控制器
 */
@RestController
@RequestMapping("/api/ioc")
public class IocController {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private UserService userService;

    @Autowired
    private AppConfig.MessageService messageService;

    /**
     * 获取所有用户
     */
    @GetMapping("/users")
    public Map<String, Object> getAllUsers() {
        List<User> users = userService.findAll();
        Map<String, Object> result = new HashMap<>();
        result.put("data", users);
        result.put("count", users.size());
        return result;
    }

    /**
     * 获取指定用户
     */
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);
    }

    /**
     * 创建用户
     */
    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        return userService.createUser(user.getName(), user.getEmail(), user.getAge());
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{id}")
    public Map<String, String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        Map<String, String> result = new HashMap<>();
        result.put("message", "用户已删除");
        return result;
    }

    /**
     * 演示 @Primary Bean 注入
     */
    @GetMapping("/message")
    public String getMessage() {
        return messageService.getMessage();
    }

    /**
     * 演示作用域：对比单例和原型
     */
    @GetMapping("/scope-demo")
    public Map<String, Object> scopeDemo() {
        User singleton1 = applicationContext.getBean("singletonUser", User.class);
        User singleton2 = applicationContext.getBean("singletonUser", User.class);
        User prototype1 = applicationContext.getBean("prototypeUser", User.class);
        User prototype2 = applicationContext.getBean("prototypeUser", User.class);

        Map<String, Object> result = new HashMap<>();
        result.put("singletonSame", singleton1 == singleton2);  // true
        result.put("prototypeSame", prototype1 == prototype2); // false
        result.put("singletonId1", singleton1.getId());
        result.put("singletonId2", singleton2.getId());
        result.put("prototypeId1", prototype1.getId());
        result.put("prototypeId2", prototype2.getId());

        return result;
    }

    /**
     * 演示 @Lazy 延迟注入
     */
    @GetMapping("/lazy-demo")
    public Map<String, String> lazyDemo() {
        userService.sendWelcomeEmail(new User(1L, "测试用户", "test@example.com", 20));
        Map<String, String> result = new HashMap<>();
        result.put("message", "延迟注入测试完成");
        return result;
    }
}
