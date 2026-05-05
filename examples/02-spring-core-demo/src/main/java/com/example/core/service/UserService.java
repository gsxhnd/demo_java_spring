package com.example.core.service;

/**
 * 用户服务接口 — 定义业务层的对外能力契约。
 *
 * <h3>为什么 Service 层也需要接口？</h3>
 * <p>与 Repository 接口的设计理念相同，"面向接口编程"使业务逻辑的调用者不依赖具体实现：</p>
 * <ol>
 *   <li>当底层实现从 UserServiceImpl 切换到另一个实现时，调用方无需修改。</li>
 *   <li>支持 AOP 切面拦截：Spring AOP 对接口代理默认使用 JDK 动态代理，
 *       如果只有具体类没有接口，Spring 将回退到 CGLIB 代理方式。</li>
 *   <li>方便服务之间的集成测试（Mock 接口即可）。</li>
 * </ol>
 *
 * <h3>本接口在本项目中的角色：</h3>
 * <p>UserService 与 UserRepository 共同构成了经典的<em>分层架构</em>：
 * Controller → Service（业务逻辑） → Repository（数据访问）。</p>
 *
 * @see com.example.core.service.UserServiceImpl
 */
public interface UserService {

    /**
     * 根据用户 ID 获取用户信息。
     *
     * @param userId 用户唯一标识
     * @return 格式化的用户信息字符串
     */
    String getUser(String userId);

    /**
     * 保存用户数据到持久层。
     *
     * @param userId   用户唯一标识
     * @param userData 用户数据的 JSON 字符串或其他格式
     */
    void saveUser(String userId, String userData);
}
