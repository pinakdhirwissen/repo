package com.example.TicketingSystem.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MainEmailService {

    private static final Logger logger = LoggerFactory.getLogger(MainEmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired(required = false)
    private TemplateEngine templateEngine;

    // 1. Ticket Creation/Update Email (Thymeleaf Template)
    @Async
    public void sendThymeleafTicketEmail(String recipient, String ticketId, String title, String description,
                                         String createdDate, String priority, String dueDate,
                                         String status, boolean isCreator, String emailHeader) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            String subject = isCreator ? "You Created a New Request" : "New Request in Department";
            helper.setTo(recipient);
            helper.setSubject(subject);

            Context context = new Context();
            context.setVariable("header", emailHeader);
            context.setVariable("ticketId", ticketId);
            context.setVariable("title", title);
            context.setVariable("description", description);
            context.setVariable("createdDate", createdDate);
            context.setVariable("priority", priority);
            context.setVariable("dueDate", dueDate);
            context.setVariable("status", status);

            String htmlContent = templateEngine.process("ticket-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            logger.error("Failed to send ticket email to {}", recipient, e);
        }
    }

    // 2. Ticket Creation/Update Email (Plain HTML)
    @Async
    public void sendPlainTicketEmail(String recipientEmail, String ticketId, String title, String description,
                                     String createdDate, String priority, String dueDate,
                                     String status, String subject, String assignee, String createdBy) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(recipientEmail);
            helper.setSubject("Ticket " + ticketId + " - " + subject);

            String content = "<html><body><h2>Ticket Update</h2>"
                    + "<p><strong>Ticket ID:</strong> " + ticketId + "</p>"
                    + "<p><strong>Title:</strong> " + title + "</p>"
                    + "<p><strong>Description:</strong> " + description + "</p>"
                    + "<p><strong>Created Date:</strong> " + createdDate + "</p>"
                    + "<p><strong>Priority:</strong> " + priority + "</p>"
                    + "<p><strong>Due Date:</strong> " + dueDate + "</p>"
                    + "<p><strong>Status:</strong> " + status + "</p>"
                    + "<p><strong>Assigned To:</strong> " + assignee + "</p>"
                    + "<p><strong>Created By:</strong> " + createdBy + "</p>"
                    + "<br><hr><p>This is an automated email. Please do not reply.</p>"
                    + "</body></html>";

            helper.setText(content, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            logger.error("❌ Error sending plain ticket email to {}: {}", recipientEmail, e.getMessage());
        }
    }

    // 3. Comment Email (Plain Text)
    @Async
    public void sendCommentEmail(String recipientEmail, String ticketId, Long commentId, String comment, String commentedBy, boolean isEdited) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            String subject = isEdited ? "Comment Edited on Ticket #" + ticketId : "New Comment on Ticket #" + ticketId;
            String content = (isEdited ?
                    "Dear User,\n\nWe would like to inform you that a comment has been edited on Ticket #" + ticketId + ".\n\n" :
                    "Dear User,\n\nA new comment has been added to Ticket #" + ticketId + ".\n\n") +
                    "<div style=\"font-family: Arial, sans-serif; color: #333; line-height: 1.6;\">" +
                    "<p style=\"font-size: 16px;\">Comment: <strong>" + comment + "</strong></p>" +
                    "<p style=\"font-size: 16px;\">Commented By: <strong>" + commentedBy + "</strong></p>" +
                    "<p style=\"font-size: 16px;\">For more details, please review the ticket.</p>" +
                    "<br>" +
                    "<p style=\"font-size: 16px;\">Best regards,</p>" +
                    "<p style=\"font-size: 16px;\">Your Support Team</p>" +
                    "</div>";

            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(content, true); // Set 'true' to indicate HTML content

            mailSender.send(message);
        } catch (MessagingException e) {
            logger.error("❌ Error sending comment email to {}: {}", recipientEmail, e.getMessage());
        }
    }


    // 4. Feedback Request to Creator
    @Async
    public void sendFeedbackRequestEmail(String recipient, String ticketId, String title,String message) {
        try {
            MimeMessage mailMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mailMessage, true);

            helper.setTo(recipient);
            helper.setSubject("Feedback Request for Ticket #" + ticketId);
            helper.setText("<p>Dear User,</p>"
                            + "<p>Your ticket <b>" + title + "</b> has been closed.</p>"
                            + "<p>Please provide feedback: </p>"
                            + "<a href='http://localhost:5173/feedback?ticketId=" + ticketId + "'>Submit Feedback</a>",
                    true);

            mailSender.send(mailMessage);
        } catch (Exception e) {
            logger.error("❌ Error sending feedback request to {}: {}", recipient, e.getMessage());
        }
    }

    // 5. Feedback Submission to Assignee
    @Async
    public void sendFeedbackToAssignee(String assigneeEmail, String ticketId, String rating, String comments) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(assigneeEmail);
            helper.setSubject("Feedback Received for Ticket #" + ticketId);

            String content = "<h3>Feedback for Ticket #" + ticketId + "</h3>"
                    + "<p><b>Rating:</b> " + rating + "/5</p>"
                    + "<p><b>Comments:</b> " + comments + "</p>"
                    + "<br><p>Thank you for your service!</p>";

            helper.setText(content, true);

            mailSender.send(message);
        } catch (Exception e) {
            logger.error("❌ Error sending feedback email to {}: {}", assigneeEmail, e.getMessage());
        }
    }
}
