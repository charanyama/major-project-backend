package com.virtualstore.common.context;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class UserContext {

    private static final ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();

    private String userId;
    private String username;
    private Set<String> roles;
    private String requestId;
    private String correlationId;
    private Map<String, String> metadata;

    public UserContext() {
        this.roles = Collections.emptySet();
        this.metadata = Collections.emptyMap();
    }

    public UserContext(String userId, String username, Set<String> roles, String requestId, String correlationId, Map<String, String> metadata) {
        this.userId = userId == null ? "anonymous" : userId;
        this.username = username;
        this.roles = roles == null ? Collections.emptySet() : roles;
        this.requestId = requestId;
        this.correlationId = correlationId;
        this.metadata = metadata == null ? Collections.emptyMap() : metadata;
    }

    public static void set(UserContext userContext) {
        CONTEXT.set(Objects.requireNonNull(userContext, "UserContext must not be null"));
    }

    public static UserContext get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles == null ? Collections.emptySet() : roles;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata == null ? Collections.emptyMap() : metadata;
    }

    public boolean hasUser() {
        return userId != null && !userId.isBlank();
    }
}

