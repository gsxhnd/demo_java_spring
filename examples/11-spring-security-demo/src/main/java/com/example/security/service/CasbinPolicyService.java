package com.example.security.service;

import com.example.security.dto.AddCasbinPolicyRequest;
import com.example.security.dto.CasbinCheckResponse;
import lombok.RequiredArgsConstructor;
import org.casbin.jcasbin.main.Enforcer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CasbinPolicyService {

    private final Enforcer enforcer;

    public CasbinCheckResponse check(String username, String resource, String action) {
        boolean allowed = enforcer.enforce(username, resource, action);
        return CasbinCheckResponse.builder()
                .username(username)
                .resource(resource)
                .action(action)
                .allowed(allowed)
                .build();
    }

    public boolean addPolicy(AddCasbinPolicyRequest request) {
        return enforcer.addPolicy(request.getRole(), request.getResource(), request.getAction());
    }

    public List<List<String>> getPoliciesForUser(String username) {
        return enforcer.getImplicitPermissionsForUser(username);
    }
}
