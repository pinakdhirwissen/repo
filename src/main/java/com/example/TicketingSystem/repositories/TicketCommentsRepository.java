package com.example.TicketingSystem.repositories;

import com.example.TicketingSystem.models.TicketComments;
import com.example.TicketingSystem.models.Tickets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketCommentsRepository extends JpaRepository<TicketComments, String> {
//    Retrieve comments for a specific ticket
    List<TicketComments> findByTicketId(Tickets ticketId);

//    Finds a specific comment by its ID.
      Optional<TicketComments> findByCommentId(Long commentId);

}
