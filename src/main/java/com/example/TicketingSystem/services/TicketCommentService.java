package com.example.TicketingSystem.services;

import com.example.TicketingSystem.models.TicketComments;
import com.example.TicketingSystem.models.Tickets;
import com.example.TicketingSystem.repositories.TicketCommentsRepository;
import com.example.TicketingSystem.repositories.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TicketCommentService {

    @Autowired
    private TicketCommentsRepository ticketCommentRepository;

    @Autowired
    private TicketRepository ticketRepository;



    @Autowired
    private MainEmailService mainEmailService;

    // Retrieve comments for a specific ticket
    public List<TicketComments> getCommentsByTicket(Tickets ticketId) {
        if (ticketId == null) {
            throw new IllegalArgumentException("Ticket cannot be null");
        }
        return ticketCommentRepository.findByTicketId(ticketId);
    }

    // Add a new comment to a ticket
    public TicketComments addComment(String ticketId,TicketComments commentDto) {
        Tickets ticket = ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));


        TicketComments newComment = new TicketComments();
        newComment.setTicketId(ticket);
        newComment.setComment(commentDto.getComment());
        newComment.setCommentedBy(commentDto.getCommentedBy());
        newComment.setCommentedDate(LocalDateTime.now());

        TicketComments savedComment = ticketCommentRepository.save(newComment);

        mainEmailService.sendCommentEmail(ticket.getCreatedBy(), ticketId, savedComment.getCommentId(),
                savedComment.getComment(), savedComment.getCommentedBy(), false);

        return savedComment;
    }

    // Edit an existing comment
    public TicketComments editComment(String commentId, String newComment, String user) {

        TicketComments comment = ticketCommentRepository.findByCommentId(Long.parseLong(commentId))
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        validateCommentOwnership(comment, user);
        validateCommentEditTime(comment);


        comment.setComment(newComment);
        comment.setEdited(true);
        comment.setEditedDate(LocalDateTime.now());
        TicketComments updatedComment = ticketCommentRepository.save(comment);

        mainEmailService.sendCommentEmail(comment.getTicketId().getCreatedBy(), comment.getTicketId().getTicketId(),
                updatedComment.getCommentId(), updatedComment.getComment(), updatedComment.getCommentedBy(), true);

        return updatedComment;
    }

    // Validate if the user owns the comment
    private void validateCommentOwnership(TicketComments comment, String user) {
        if (!comment.getCommentedBy().equals(user)) {
            throw new IllegalArgumentException("You can only edit your own comments");
        }
    }

    // Validate if the comment can still be edited
    private void validateCommentEditTime(TicketComments comment) {
        if (comment.getCommentedDate().plusMinutes(15).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Comment can only be edited within 15 minutes");
        }
    }
}
