package com.examly.springapp.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.examly.springapp.dto.AppointmentDTO;
import com.examly.springapp.model.Appointment;
import com.examly.springapp.model.User;
import com.examly.springapp.service.AppointmentService;
import com.examly.springapp.service.NotificationService;
import com.examly.springapp.service.UserService;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/")
    public AppointmentDTO createAppointment(@RequestBody AppointmentDTO dto) {
        if (dto.getAppointmentDate() == null || dto.getAppointmentDate().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment date and time must be in the future.");
        }
        Appointment appointment = new Appointment();
        appointment.setAppointmentDate(dto.getAppointmentDate());
        appointment.setStatus("PENDING");
        appointment.setDoctor(userService.findById(dto.getDoctorId()).orElse(null));
        appointment.setPatient(userService.findById(dto.getPatientId()).orElse(null));
        appointment.setReason(dto.getReason());
        Appointment saved = appointmentService.createAppointment(appointment);
        dto.setId(saved.getId());
        dto.setStatus(saved.getStatus());
        dto.setReason(saved.getReason());

        // Notification for doctor when appointment is booked
        if (saved.getDoctor() != null) {
            String message = "New appointment booked by patient ID: " + (saved.getPatient() != null ? saved.getPatient().getId() : "");
            notificationService.createNotification(saved.getDoctor().getId(), message, "APPOINTMENT");
        }
        return dto;
    }

    @GetMapping("/doctor/{doctorId}")
    public List<AppointmentDTO> getAppointmentsByDoctor(@PathVariable Long doctorId) {
        User doctor = userService.findById(doctorId).orElse(null);
        return appointmentService.getAppointmentsByDoctor(doctor).stream().map(a -> {
            AppointmentDTO dto = new AppointmentDTO();
            dto.setId(a.getId());
            dto.setAppointmentDate(a.getAppointmentDate());
            dto.setDoctorId(a.getDoctor().getId());
            dto.setPatientId(a.getPatient().getId());
            dto.setStatus(a.getStatus());
            dto.setReason(a.getReason());
            return dto;
        }).collect(Collectors.toList());
    }

    @GetMapping("/patient/{patientId}")
    public List<AppointmentDTO> getAppointmentsByPatient(@PathVariable Long patientId) {
        User patient = userService.findById(patientId).orElse(null);
        return appointmentService.getAppointmentsByPatient(patient).stream().map(a -> {
            AppointmentDTO dto = new AppointmentDTO();
            dto.setId(a.getId());
            dto.setAppointmentDate(a.getAppointmentDate());
            dto.setDoctorId(a.getDoctor().getId());
            dto.setPatientId(a.getPatient().getId());
            dto.setStatus(a.getStatus());
            dto.setReason(a.getReason());
            return dto;
        }).collect(Collectors.toList());
    }

    @PutMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam String status) {
        System.out.println("[DEBUG] updateStatus called with id=" + id + ", status=" + status);
        Optional<Appointment> opt = appointmentService.getAppointmentById(id);
        if (opt.isPresent()) {
            Appointment appointment = opt.get();
            System.out.println("[DEBUG] Appointment found: " + appointment);
            appointment.setStatus(status);
            try {
                appointmentService.createAppointment(appointment);
                System.out.println("[DEBUG] Appointment status updated and saved.");
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to save appointment: " + e.getMessage());
                e.printStackTrace();
                return "Failed to update status: " + e.getMessage();
            }

            // Notification for patient when doctor updates status
            if (appointment.getPatient() != null) {
                String message = "Your appointment status updated to: " + status;
                try {
                    notificationService.createNotification(appointment.getPatient().getId(), message, "STATUS_UPDATE");
                    System.out.println("[DEBUG] Notification sent to patient id=" + appointment.getPatient().getId());
                } catch (Exception e) {
                    System.err.println("[ERROR] Failed to send notification: " + e.getMessage());
                }
            }
            return "Status updated";
        }
        System.err.println("[ERROR] Appointment not found for id=" + id);
        return "Appointment not found";
    }

    @DeleteMapping("/{id}")
    public String deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return "Appointment deleted";
    }
}