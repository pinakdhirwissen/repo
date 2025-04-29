package com.example.TicketingSystem.services;

import com.example.TicketingSystem.models.TicketDepartment;
import com.example.TicketingSystem.models.Tickets;
import com.example.TicketingSystem.repositories.TicketDepartmentRepository;
import com.example.TicketingSystem.repositories.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketDepartmentRepository ticketDepartmentRepository;

    @Autowired
    private NotificationService notificationService;



    @Autowired
    private MainEmailService mainEmailService;

    public Optional<Tickets> getTicketById(String ticketId) {
        return ticketRepository.findById(ticketId);
    }

    public Tickets createTicket(Tickets ticket)  {
        return ticketRepository.save(ticket);
    }
    public List<String> getUniqueStatus(){
        return ticketRepository.findAll()
                .stream()
                .map(Tickets::getStatus)
                .distinct()
                .collect(Collectors.toList());
    }

    @Scheduled(fixedRate = 900000) // Runs every 15 minutes
    public void assignUnassignedTickets() {
        LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);

        List<Tickets> openTickets = ticketRepository.findByStatusAndCreatedDateBeforeAndAssignToIsNull("Open", fifteenMinutesAgo);

        for (Tickets ticket : openTickets) {
            String department = ticket.getDepartment();
            List<TicketDepartment> activeUsers = ticketDepartmentRepository.findByDepartment(department)
                    .stream()
                    .filter(TicketDepartment::getIsActive)
                    .toList();
            if (activeUsers.isEmpty()) {
                System.out.println("No active users in department: " + department);
                continue;
            }

            Optional<TicketDepartment> leastBusyUser = activeUsers.stream()
                    .min(Comparator.comparingInt(user -> ticketRepository.countByAssignTo((user.getEmailId()))));

                String assigneeEmail = leastBusyUser.get().getEmailId();

                Optional<Tickets> latestTicket = ticketRepository.findById(ticket.getTicketId());
                if (latestTicket.isPresent() && latestTicket.get().getAssignTo() == null) {
                    ticket.setAssignTo(assigneeEmail);
                    ticket.setStatus("Assigned");
                    ticket.setUpdatedDate(LocalDateTime.now());
                    ticket.setUpdatedBy(assigneeEmail);
                    ticketRepository.save(ticket);

                    notificationService.sendNotification(assigneeEmail,
                            "You have been assigned a new ticket (ID: " + ticket.getTicketId() + ").");

                    mainEmailService.sendThymeleafTicketEmail(
                            assigneeEmail, ticket.getTicketId(), ticket.getTitle(), ticket.getDescription(),
                            ticket.getCreatedDate().toString(), ticket.getPriority(), ticket.getDueDate().toString(),
                            "Assigned", false, "You have been assigned a new ticket"
                    );


                    System.out.println("Ticket ID: " + ticket.getTicketId() + " assigned to " + assigneeEmail);
                }

        }
    }

    public List<Tickets> getTicketsByCreatedBy(String createdBy) {
        return ticketRepository.findByCreatedBy(createdBy);
    }

    public List<Tickets> getTicketsByAssignTo(String assignTo) {
        return ticketRepository.findByAssignTo(assignTo);
    }

    public Optional<Tickets> updateTicket(String ticketId, Tickets updatedTicket) {
        return ticketRepository.findById(ticketId).map(existingTicket -> {
            existingTicket.setTitle(updatedTicket.getTitle());
            existingTicket.setDescription(updatedTicket.getDescription());
            existingTicket.setDepartment(updatedTicket.getDepartment());
            existingTicket.setAssignTo(updatedTicket.getAssignTo());
            existingTicket.setPriority(updatedTicket.getPriority());
            existingTicket.setDueDate(updatedTicket.getDueDate());
            existingTicket.setUpdatedBy(updatedTicket.getUpdatedBy());
            return ticketRepository.save(existingTicket);
        });
    }

    public Tickets findByTicketId(String ticketId) {
        return ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
    }

    // Combined search (optional filters)
    public List<Tickets> searchTickets(String createdBy, String assignTo) {
        return ticketRepository.findByCreatedByOrAssignTo(createdBy, assignTo);
    }
    public Tickets assignTicket(String ticketId, String userEmail) {
        Tickets ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with ID: " + ticketId));

        ticket.setAssignTo(userEmail);
        ticket.setStatus("Assigned");
        ticket.setUpdatedBy(userEmail);

        return ticketRepository.save(ticket);
    }



    @Scheduled(cron = "0 0 0 * * *")
    public void sendRemindersToAssignees() {
        System.out.print("Scheduler here");
        List<Tickets> tickets = ticketRepository.findAll();

        for (Tickets ticket : tickets) {
            String assignee = ticket.getAssignTo();
            LocalDate dueDate = ticket.getDueDate();
            LocalDate createdDate = ticket.getCreatedDate().toLocalDate();
            LocalDateTime updatedDate = ticket.getUpdatedDate();
            String status = ticket.getStatus();


            if (assignee != null && dueDate != null && updatedDate != null && !"Closed".equalsIgnoreCase(status) ) {
                Optional<TicketDepartment> user = ticketDepartmentRepository.findByEmailId(assignee);
                if (user.isPresent() && user.get().getIsActive()) {
                long diffDays = ChronoUnit.DAYS.between(createdDate, dueDate);
                boolean isShortDue = diffDays <= 2;
                boolean isOverdue = LocalDate.now().isAfter(dueDate);

                if (isShortDue||isOverdue) {
                    String subject="";
                    if(diffDays<=2 &&diffDays>=0){
                     subject = "⏳ Reminder: Only "  +diffDays+  " day(s) left to complete Ticket ID: " + ticket.getTicketId();
                    }
                    else{
                        subject = "⏳ Reminder: Ticket ID: " + ticket.getTicketId() + " is overdue!";
                    }
                    System.out.print(ticket.getTicketId());


                    mainEmailService.sendThymeleafTicketEmail(
                            assignee,
                            ticket.getTicketId(),
                            ticket.getTitle(),
                            ticket.getDescription(),
                            ticket.getCreatedDate().toString(),
                            ticket.getPriority(),
                            ticket.getDueDate().toString(),
                            ticket.getStatus(),
                            false,
                            subject
                    );
                    notificationService.sendNotification(
                            assignee,
                            "Ticket " + ticket.getTicketId()+"is Overdue"
                    );

                    System.out.println("Reminder sent to: " + assignee + " for Ticket ID: " + ticket.getTicketId());
                }
            }
        }
        }
    }
}
