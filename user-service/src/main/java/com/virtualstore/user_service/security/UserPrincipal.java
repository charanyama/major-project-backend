package com.virtualstore.user_service.security;

import com.virtualstore.user_service.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * UserPrincipal
 *
 * Adapts our User entity into Spring Security's UserDetails interface.
 * This is what Spring Security uses internally during authentication.
 *
 * We expose the underlying User via getUser() so the AuthService
 * can access domain fields (id, email, roles) after authentication.
 */
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    @Getter
    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.getRole().toAuthority()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    /** Spring Security uses this as the "username" — we use email */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return user.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return user.isCredentialsNonExpired();
    }

    /** False until the user verifies their email */
    @Override
    public boolean isEnabled() {
        return user.isEnabled() && !user.isDeleted();
    }
}