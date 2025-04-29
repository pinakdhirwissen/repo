package com.example.TicketingSystem.services;

import com.example.TicketingSystem.models.TicketDepartment;
import com.example.TicketingSystem.repositories.TicketDepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TicketDepartmentService {

    @Autowired
    private TicketDepartmentRepository ticketDepartmentRepository;

    // Retrieve all departments
    public List<TicketDepartment> getAllDepartments() {
        return ticketDepartmentRepository.findAll();
    }

    // Retrieve unique department names
    public List<String> getUniqueDepartments() {
        return ticketDepartmentRepository.findAll()
                .stream()
                .map(TicketDepartment::getDepartment)
                .distinct()
                .collect(Collectors.toList());
    }

    // Retrieve users by department
    public List<TicketDepartment> getUsersByDepartment(String department) {
        return ticketDepartmentRepository.findByDepartment(department);
    }

    // Retrieve users by active status
    public List<TicketDepartment> getUsersByActiveStatus(Boolean isActive) {
        return ticketDepartmentRepository.findByIsActive(isActive);
    }
    // Save a new user to a department
    public TicketDepartment saveUserToDepartment(TicketDepartment ticketDepartment) {
        ticketDepartment.setId(UUID.randomUUID().toString());
        ticketDepartment.setIsActive(true);
        return ticketDepartmentRepository.save(ticketDepartment);
    }

    // Update user active status by ID
    public Optional<TicketDepartment> updateUserStatus(String id, Boolean isActive) {
        Optional<TicketDepartment> departmentUser = ticketDepartmentRepository.findById(id);
        if (departmentUser.isPresent()) {
            TicketDepartment td = departmentUser.get();
            td.setIsActive(isActive);
            ticketDepartmentRepository.save(td);
        }
        return departmentUser;
    }

    // Update user active status by email
    public Optional<TicketDepartment> updateUserStatusByEmail(String email, Boolean isActive) {
        Optional<TicketDepartment> userOptional = ticketDepartmentRepository.findByEmailId(email);

        if (userOptional.isPresent()) {
            TicketDepartment user = userOptional.get();
            user.setIsActive(isActive);
            ticketDepartmentRepository.save(user);
        }

        return userOptional;
    }
}
