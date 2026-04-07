package com.example.mvc.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 请求日志拦截器
 *
 * 演示 HandlerInterceptor 的用法
 *
 * 执行顺序：preHandle -> Controller -> postHandle -> afterCompletion
 */
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);

    /**
     * 在请求处理之前调用
     *
     * @return true 继续执行后续处理器，返回 false 中断请求
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);

        log.info("[LoggingInterceptor] 请求: {} {}", request.getMethod(), request.getRequestURI());
        log.info("[LoggingInterceptor] 远程地址: {}", request.getRemoteAddr());
        log.info("[LoggingInterceptor] 处理类: {}",
                handler.getClass().getSimpleName());

        return true; // 继续执行
    }

    /**
     * 在 Controller 处理之后、视图渲染之前调用
     * 注意：如果 preHandle 返回 false，此方法不会被调用
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {
        Long startTime = (Long) request.getAttribute("startTime");
        long duration = System.currentTimeMillis() - startTime;

        log.info("[LoggingInterceptor] 请求处理完成: {} {}, 耗时: {}ms",
                request.getMethod(), request.getRequestURI(), duration);
    }

    /**
     * 在整个请求完成之后调用（视图渲染之后）
     * 无论请求成功与否都会调用
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        if (ex != null) {
            log.error("[LoggingInterceptor] 请求异常: {} {}",
                    request.getMethod(), request.getRequestURI(), ex);
        }
    }
}
