package com.example.TicketingSystem.repositories;

import com.example.TicketingSystem.models.TicketDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketDepartmentRepository extends JpaRepository<TicketDepartment, String> {

    // Find all departments
    @Query("SELECT t FROM TicketDepartment t WHERE t.department = :department")
    List<TicketDepartment> findByDepartment(String department);

//    Find user's department by email id
    @Query("SELECT td FROM TicketDepartment td WHERE LOWER(td.emailId) = LOWER(:email)")
    Optional<TicketDepartment> findByEmailId(@Param("email") String email);

//    Find users by active/inactive status (field isActive from entity)
    List<TicketDepartment> findByIsActive(Boolean isActive);
}
