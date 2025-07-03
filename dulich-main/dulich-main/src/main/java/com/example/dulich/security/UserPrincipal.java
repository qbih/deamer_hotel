package com.example.dulich.security;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.dulich.entity.User;

public class UserPrincipal implements UserDetails {
    
    private final User user;
    
    public UserPrincipal(User user) {
        this.user = user;
    }
      @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            System.out.println("WARNING: User " + user.getUsername() + " không có roles!");
            return java.util.Collections.emptyList();
        }
        
        return user.getRoles().stream()
                .map(role -> {
                    String authority = "ROLE_" + role.getName().name();
                    System.out.println("Granted authority: " + authority);
                    return new SimpleGrantedAuthority(authority);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    
    @Override
    public String getUsername() {
        return user.getUsername();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
    
    // Getter để truy cập thông tin user
    public User getUser() {
        return user;
    }
    
    public String getFullName() {
        return user.getFullName();
    }
    
    public String getEmail() {
        return user.getEmail();
    }
}
