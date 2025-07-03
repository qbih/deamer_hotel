package com.example.dulich.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.dulich.entity.User;
import com.example.dulich.repository.UserRepository;

@Controller
public class TestController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping("/test/users")
    @ResponseBody
    public String testUsers() {
        List<User> users = userRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("Total users: ").append(users.size()).append("\n");
        
        for (User user : users) {
            sb.append("Username: ").append(user.getUsername())
              .append(", Enabled: ").append(user.isEnabled())
              .append(", Roles: ").append(user.getRoles() != null ? user.getRoles().size() : "null")
              .append("\n");
        }
        
        return sb.toString();
    }
    
    @GetMapping("/test/user")
    @ResponseBody
    public String testSpecificUser(@RequestParam String username) {
        try {
            // Test findByUsername
            var userOpt1 = userRepository.findByUsername(username);
            StringBuilder sb = new StringBuilder();
            sb.append("findByUsername: ").append(userOpt1.isPresent() ? "FOUND" : "NOT FOUND").append("\n");
            
            // Test findByUsernameWithRoles
            var userOpt2 = userRepository.findByUsernameWithRoles(username);
            sb.append("findByUsernameWithRoles: ").append(userOpt2.isPresent() ? "FOUND" : "NOT FOUND").append("\n");
            
            // Test findByUsernameWithRolesLeftJoin
            var userOpt3 = userRepository.findByUsernameWithRolesLeftJoin(username);
            sb.append("findByUsernameWithRolesLeftJoin: ").append(userOpt3.isPresent() ? "FOUND" : "NOT FOUND").append("\n");
            
            if (userOpt1.isPresent()) {
                User user = userOpt1.get();
                sb.append("User details:\n");
                sb.append("- Username: ").append(user.getUsername()).append("\n");
                sb.append("- Email: ").append(user.getEmail()).append("\n");
                sb.append("- Enabled: ").append(user.isEnabled()).append("\n");
                sb.append("- Password length: ").append(user.getPassword() != null ? user.getPassword().length() : "null").append("\n");
                sb.append("- Roles count: ").append(user.getRoles() != null ? user.getRoles().size() : "null").append("\n");
                
                if (user.getRoles() != null) {
                    user.getRoles().forEach(role -> 
                        sb.append("  - Role: ").append(role.getName()).append("\n")
                    );
                }
            }
            
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @GetMapping("/test/password")
    @ResponseBody
    public String testPassword(@RequestParam String username, @RequestParam String password) {
        try {
            var userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                boolean matches = passwordEncoder.matches(password, user.getPassword());
                return "Password matches: " + matches + "\n" +
                       "Raw password: " + password + "\n" +
                       "Encoded password: " + user.getPassword() + "\n" +
                       "Password encoder: " + passwordEncoder.getClass().getSimpleName();
            } else {
                return "User not found";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
