package com.example.TicketingSystem.repositories;

import com.example.TicketingSystem.models.Tickets;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Tickets, String> {
//    Find all tickers by status
    List<Tickets> findByStatus(String status);

//    Find all tickets by createdBy using email
    List<Tickets> findByCreatedBy(String createdBy);

//    Find all tickets by assignTo using email
    List<Tickets> findByAssignTo(String assignTo);

//    Find all tickets by ticketId
    Optional<Tickets> findByTicketId(String ticketId);


//    Find all tickets by createdBy and assignTo using email
    @Query("SELECT t FROM Tickets t " +
            "WHERE (:createdBy IS NULL OR t.createdBy = :createdBy) " +
            "OR (:assignTo IS NULL OR t.assignTo = :assignTo)")
    List<Tickets> findByCreatedByOrAssignTo(@Param("createdBy") String createdBy,
                                            @Param("assignTo") String assignTo);


//    Find all tickets by department
    List<Tickets> findByDepartment(@Size(max = 50) String department);


//    Counts the number of tickets assigned to a specific user (email).
    int countByAssignTo(String assignTo);

//    Find all tickets which are in open status
    List<Tickets> findByStatusAndCreatedDateBeforeAndAssignToIsNull(String status, LocalDateTime createdDate);
}
