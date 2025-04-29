package com.example.TicketingSystem.controllers;

import com.example.TicketingSystem.models.TicketDepartment;
import com.example.TicketingSystem.repositories.TicketDepartmentRepository;
import com.example.TicketingSystem.services.TicketDepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/department")
@Tag(name = "Department Management", description = "APIs for managing departments")  // Grouping APIs under "Department Management"
public class TicketDepartmentController {

    @Autowired
    private TicketDepartmentService ticketDepartmentService;

    @Autowired
    private TicketDepartmentRepository ticketDepartmentRepository;

    // ✅ Get all departments (Fetch departments)
    @Operation(summary = "Get all departments", description = "Retrieve a list of all available departments.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Departments retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No departments found")
    })
    @GetMapping
    public ResponseEntity<List<TicketDepartment>> getAllDepartments() {
        List<TicketDepartment> departments = ticketDepartmentService.getAllDepartments();
        if (departments.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/unique")
    public ResponseEntity<List<String>> getUniqueDepartments() {
        List<String> departments = ticketDepartmentService.getUniqueDepartments();
        return departments.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(departments);
    }

    // ✅ Get department by email (Fetch department using email)
    @Operation(summary = "Get department by email", description = "Retrieve the department details for a given email.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Department not found for the given email")
    })
    @PostMapping("/adduser")
    public ResponseEntity<Map<String, Object>> addUserToDepartment(@RequestBody TicketDepartment request) {
        Map<String, Object> response = new HashMap<>();

        if (request.getEmailId() == null || request.getEmailId().trim().isEmpty() ||
                request.getDepartment() == null || request.getDepartment().trim().isEmpty()) {
            response.put("success", false);
            response.put("msg", "Email and department are required.");
            return ResponseEntity.badRequest().body(response);
        }

        // Check if user exists in any department
        Optional<TicketDepartment> existingUser = ticketDepartmentRepository.findByEmailId(request.getEmailId());

        if (existingUser.isPresent()) {
            TicketDepartment user = existingUser.get();

            if (user.getIsActive()) {
                response.put("success", false);
                response.put("msg", "User is already active in another department.");
                return ResponseEntity.badRequest().body(response);
            }

            // If user is inactive, allow adding to a new department
            user.setDepartment(request.getDepartment());
            user.setIsActive(true);
            ticketDepartmentRepository.save(user);

            response.put("success", true);
            response.put("msg", "User was inactive and has now been added to the new department.");
            return ResponseEntity.ok(response);
        }

        // If user does not exist, create a new record
        TicketDepartment newUser = new TicketDepartment();
        newUser.setId(UUID.randomUUID().toString()); // Generate unique ID
        newUser.setEmailId(request.getEmailId());
        newUser.setDepartment(request.getDepartment());
        newUser.setIsActive(true);

        ticketDepartmentRepository.save(newUser);

        response.put("success", true);
        response.put("msg", "User added successfully!");
        return ResponseEntity.ok(response);
    }

   


    @GetMapping("/by-email/{email}")
    public ResponseEntity<TicketDepartment> getDepartmentByEmail(@PathVariable String email) {
        email = email.trim(); // Remove leading/trailing spaces

        return ticketDepartmentRepository.findByEmailId(email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    TicketDepartment defaultDept = new TicketDepartment();
                    defaultDept.setDepartment(null);  // Return null instead of "Department Not Found"
                    return ResponseEntity.ok(defaultDept); // Return 200 OK instead of 404
                });
    }



    @GetMapping("/by-department/{department}")
    public ResponseEntity<List<TicketDepartment>> getUsersByDepartment(@PathVariable String department) {
        List<TicketDepartment> users = ticketDepartmentRepository.findByDepartment(department);
        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
        return ResponseEntity.ok(users);
    }

    // ✅ Save a user by department (Save user to department)
    @Operation(summary = "Save a user to a department", description = "Assign a user to a specific department.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User successfully saved to department"),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping
    public ResponseEntity<TicketDepartment> saveUserByDepartment( @RequestBody TicketDepartment ticketDepartment) {
        TicketDepartment savedUser = ticketDepartmentService.saveUserToDepartment(ticketDepartment);
        return ResponseEntity.ok(savedUser);
    }

    // ✅ Update user status
    @Operation(summary = "Update user status", description = "Update the status (active/inactive) of a user in a department.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Missing 'isActive' value in request body"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })

    @PutMapping("/status/{email}")
    public ResponseEntity<Map<String, String>> updateUserStatus(
            @PathVariable String email,
            @RequestBody Map<String, Boolean> requestBody
    ) {
        Boolean isActive = requestBody.get("isActive");
        Map<String, String> response = new HashMap<>();

        if (isActive == null) {
            response.put("error", "Missing 'isActive' value in request body.");
            return ResponseEntity.badRequest().body(response);
        }

        return ticketDepartmentService.updateUserStatusByEmail(email, isActive)
                .map(user -> {
                    response.put("success", "User status updated successfully.");
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.status(404).body(Collections.singletonMap("error", "User not found.")));
    }
}
