
package com.examly.springapp.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.examly.springapp.model.User;
import com.examly.springapp.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<User> currentUserOpt = userService.findByUsername(currentUsername);
        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        User currentUser = currentUserOpt.get();
        if (!currentUser.getRole().name().equals("ADMIN")) {
            return ResponseEntity.status(403).body("Forbidden: Only admin can delete users");
        }
        try {
            userService.deleteUserById(id);
            return ResponseEntity.ok().body("User deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public Optional<User> getUser(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        System.out.println("[DEBUG] Authenticated username: " + currentUsername);
        Optional<User> currentUserOpt = userService.findByUsername(currentUsername);
        if (currentUserOpt.isEmpty()) {
            System.out.println("[DEBUG] No user found for username: " + currentUsername);
            throw new RuntimeException("Unauthorized");
        }
        User currentUser = currentUserOpt.get();
        System.out.println("[DEBUG] Current user ID: " + currentUser.getId() + ", Role: " + currentUser.getRole().name());
        System.out.println("[DEBUG] Requested update for user ID: " + id);
        System.out.println("[DEBUG] Incoming update payload: username=" + user.getUsername() + ", specialization=" + user.getSpecialization() + ", gender=" + user.getGender() + ", experienceYears=" + user.getExperienceYears() + ", imageUrl=" + user.getImageUrl() + ", about=" + user.getAbout());
        // Allow if admin or updating own profile
        if (!currentUser.getRole().name().equals("ADMIN") && !currentUser.getId().equals(id)) {
            System.out.println("[DEBUG] Forbidden: User " + currentUser.getId() + " tried to update user " + id);
            throw new RuntimeException("Forbidden: You can only update your own profile");
        }
        return userService.updateUser(id, user);
    }

    @GetMapping("/doctors")
    public List<User> getAllDoctors() {
        return userService.findAllDoctors();
    }

    @GetMapping("/patients")
    public List<User> getAllPatients() {
        return userService.findAllPatients();
    }
}