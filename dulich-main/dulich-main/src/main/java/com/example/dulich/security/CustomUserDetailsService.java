package com.example.dulich.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dulich.entity.User;
import com.example.dulich.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Đang tải người dùng với tên đăng nhập: " + username);
        
        // Thử tìm bằng LEFT JOIN trước để tránh lỗi khi user không có roles
        Optional<User> userOpt = userRepository.findByUsernameWithRolesLeftJoin(username);
        
        // Nếu không tìm thấy, thử tìm bằng method đơn giản
        if (!userOpt.isPresent()) {
            System.out.println("Không tìm thấy user với findByUsernameWithRolesLeftJoin, thử findByUsername");
            userOpt = userRepository.findByUsername(username);
        }
        
        User user = userOpt.orElseThrow(() -> {
            System.out.println("Không tìm thấy user nào với username: " + username);
            return new UsernameNotFoundException("Không tìm thấy người dùng: " + username);
        });

        System.out.println("Người dùng đã được tải: " + user.getUsername());
        System.out.println("User enabled: " + user.isEnabled());
        System.out.println("User password không rỗng: " + (user.getPassword() != null && !user.getPassword().isEmpty()));
        
        // Kiểm tra roles
        if (user.getRoles() != null) {
            System.out.println("User roles: " + user.getRoles().size());
            user.getRoles().forEach(role -> System.out.println("Role: " + role.getName()));
        } else {
            System.out.println("User roles is null");
        }
        
        // Đảm bảo user có ít nhất một role
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            System.out.println("WARNING: User không có roles!");
        }
        
        return new UserPrincipal(user);
    }
      @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + id));
        
        return new UserPrincipal(user);
    }
}
