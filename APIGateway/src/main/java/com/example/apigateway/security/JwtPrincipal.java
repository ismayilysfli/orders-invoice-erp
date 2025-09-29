package com.example.apigateway.security;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
public class JwtPrincipal {
    private final String userId;
    private final String subject;
    private final Set<String> roles;
    private final Map<String,Object> claims;


}

