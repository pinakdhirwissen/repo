package com.example.TicketingSystem.repositories;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.TicketingSystem.models.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // Custom query to find feedback by ticket ID
    @Query("SELECT f FROM Feedback f WHERE f.ticketId = :ticketId")
    List<Feedback> findByTicketId(@Param("ticketId") String ticketId);

}