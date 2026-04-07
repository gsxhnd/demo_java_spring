package com.example.mvc.advice;

import com.example.mvc.dto.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 统一响应包装
 *
 * ResponseBodyAdvice 允许在响应体写入之前进行预处理
 * 实现此接口可以统一包装所有 @ResponseBody 的返回值
 */
@RestControllerAdvice
public class GlobalResponseBodyAdvice implements ResponseBodyAdvice<Object, Object> {

    /**
     * 判断哪些响应需要处理
     * 这里排除已经包装过的 ApiResponse
     */
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // 如果返回类型已经是 ApiResponse，不重复包装
        return !ApiResponse.class.isAssignableFrom(returnType.getParameterType());
    }

    /**
     * 在响应体写入之前进行包装
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends MappingJackson2HttpMessageConverter> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        // 将响应统一包装为 ApiResponse
        return ApiResponse.success(body);
    }
}
