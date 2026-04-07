package com.example.ioc.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * 自定义 BeanPostProcessor
 *
 * 演示 Bean 初始化前后的拦截处理
 * Spring 的 AOP、事务代理都是基于此机制
 */
@Component
public class CustomBeanPostProcessor implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(CustomBeanPostProcessor.class);

    public CustomBeanPostProcessor() {
        System.out.println("[CustomBeanPostProcessor] BeanPostProcessor 注册到容器");
    }

    /**
     * 在 Bean 初始化之前调用
     *
     * @param bean     原始 Bean 实例
     * @param beanName Bean 的名称
     * @return 处理后的 Bean（可以返回代理对象）
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (log.isDebugEnabled()) {
            log.debug("[CustomBeanPostProcessor] postProcessBeforeInitialization - bean: {}, name: {}",
                    bean.getClass().getSimpleName(), beanName);
        }
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }

    /**
     * 在 Bean 初始化之后调用
     *
     * 注意：AOP 代理通常在这里创建
     *
     * @param bean     初始化后的 Bean
     * @param beanName Bean 的名称
     * @return 处理后的 Bean
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (log.isDebugEnabled()) {
            log.debug("[CustomBeanPostProcessor] postProcessAfterInitialization - bean: {}, name: {}",
                    bean.getClass().getSimpleName(), beanName);
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
