
package com.examly.springapp.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.examly.springapp.model.Appointment;
import com.examly.springapp.model.User;
import com.examly.springapp.repository.AppointmentRepository;
import com.examly.springapp.repository.UserRepository;

@Service
public class UserService {
    @Autowired
    private AppointmentRepository appointmentRepository;

    public void deleteUserById(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) throw new RuntimeException("User not found");
        User user = userOpt.get();
        if (user.getRole() == null || !user.getRole().name().equals("DOCTOR")) {
            throw new RuntimeException("Only doctors can be deleted with this endpoint");
        }
        // Prevent deletion if doctor has future appointments
        List<Appointment> appointments = appointmentRepository.findByDoctor(user);
        boolean hasFuture = appointments.stream().anyMatch(a -> a.getAppointmentDate() != null && a.getAppointmentDate().isAfter(java.time.LocalDateTime.now()));
        if (hasFuture) {
            throw new RuntimeException("Cannot delete doctor with future appointments");
        }
        // Optionally: delete all past appointments for this doctor
        appointments.forEach(appointmentRepository::delete);
        userRepository.deleteById(id);
    }
    public List<User> findAllPatients() {
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream()
            .filter(u -> u.getRole() != null && u.getRole().name().equals("PATIENT"))
            .collect(Collectors.toList());
    }
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NotificationService notificationService;

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        // Notify all admins if new doctor or patient registers
        if (savedUser.getRole() != null && (savedUser.getRole().name().equals("DOCTOR") || savedUser.getRole().name().equals("PATIENT"))) {
            List<User> admins = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && u.getRole().name().equals("ADMIN"))
                .collect(Collectors.toList());
            String message = "New user registered: " + savedUser.getUsername() + " (Role: " + savedUser.getRole().name() + ")";
            for (User admin : admins) {
                notificationService.createNotification(admin.getId(), message, "REGISTRATION");
            }
        }
        return savedUser;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User updateUser(Long id, User user) {
    Optional<User> existingOpt = userRepository.findById(id);
    if (existingOpt.isEmpty()) throw new RuntimeException("User not found");
    User existing = existingOpt.get();
    // Update only allowed fields
    if (user.getUsername() != null) existing.setUsername(user.getUsername());
    if (user.getEmail() != null) existing.setEmail(user.getEmail());
    if (user.getSpecialization() != null) existing.setSpecialization(user.getSpecialization());
    if (user.getAge() != null) existing.setAge(user.getAge());
    if (user.getExperienceYears() != null) existing.setExperienceYears(user.getExperienceYears());
    if (user.getGender() != null) existing.setGender(user.getGender());
    if (user.getImageUrl() != null) existing.setImageUrl(user.getImageUrl());
    if (user.getAbout() != null) existing.setAbout(user.getAbout());
    // Don't update password or role here for safety
    return userRepository.save(existing);
    }

    public List<User> findAllDoctors() {
        List<User> allUsers = userRepository.findAll();
        System.out.println("[DEBUG] All users:");
        for (User u : allUsers) {
            System.out.println("[DEBUG] User: id=" + u.getId() + ", username=" + u.getUsername() + ", role=" + (u.getRole() != null ? u.getRole().name() : "null"));
        }
        List<User> doctors = allUsers.stream()
            .filter(u -> u.getRole() != null && u.getRole().name().equals("DOCTOR"))
            .collect(Collectors.toList());
        System.out.println("[DEBUG] Doctors found: " + doctors.size());
        return doctors;
    }
}