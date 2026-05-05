package com.example.core.repository;

/**
 * 用户仓储接口 — 定义数据访问层的契约，是"面向接口编程"思想的体现。
 *
 * <h3>为什么使用接口？</h3>
 * <p>在 Spring 依赖注入中，"面向接口编程"有两大好处：</p>
 * <ol>
 *   <li><b>解耦</b>：调用方（如 UserBusiness）依赖的是接口而非具体实现类，
 *       未来可以轻松切换到不同的实现（例如从内存实现切换到 MySQL/Redis 实现），
 *       而调用方代码无需任何修改，符合"开闭原则（OCP）"。</li>
 *   <li><b>方便测试</b>：单元测试时可以用 Mockito 等框架创建接口的 Mock 对象，
 *       无需真实数据库连接即可测试业务逻辑。</li>
 * </ol>
 *
 * <h3>与 @Repository 的关系</h3>
 * <p>接口本身不需要也不能直接标注 @Repository。@Repository 应该标注在实现类上（即 UserRepositoryImpl），
 * Spring 会自动将实现类注入到所有依赖 UserRepository 接口的地方。</p>
 *
 * <h3>Spring 如何确定注入哪个实现？</h3>
 * <p>当只有一个实现类时，Spring 会自动匹配。当有多个实现类时，需要配合
 * {@code @Qualifier} 或 {@code @Primary} 注解来指定具体使用哪一个。</p>
 */
public interface UserRepository {

    /**
     * 根据用户 ID 从数据源中查询用户信息。
     *
     * @param id 用户唯一标识，业务层传递过来的用户 ID
     * @return 用户信息对象（本示例中返回的是模拟的字符串数据）
     */
    Object findById(String id);

    /**
     * 将用户数据持久化到数据源。
     *
     * @param id   用户唯一标识
     * @param user 要持久化的用户对象或数据
     */
    void save(String id, Object user);
}
