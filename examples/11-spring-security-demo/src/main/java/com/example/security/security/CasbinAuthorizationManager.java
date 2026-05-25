package com.example.security.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.casbin.jcasbin.main.Enforcer;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class CasbinAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final Enforcer enforcer;

    @Override
    public AuthorizationResult authorize(
            Supplier<? extends Authentication> authenticationSupplier,
            RequestAuthorizationContext context) {
        Authentication authentication = authenticationSupplier.get();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }
        HttpServletRequest request = context.getRequest();
        String username = authentication.getName();
        String path = request.getRequestURI();
        String method = request.getMethod();
        boolean allowed = enforcer.enforce(username, path, method);
        return new AuthorizationDecision(allowed);
    }
}
