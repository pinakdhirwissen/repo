package com.example.TicketingSystem.services;
import com.example.TicketingSystem.models.TicketDepartment;
import com.example.TicketingSystem.repositories.TicketDepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketDepartmentServiceTest {

    @Mock
    private TicketDepartmentRepository ticketDepartmentRepository;

    @InjectMocks
    private TicketDepartmentService ticketDepartmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllDepartments() {
        TicketDepartment department1 = new TicketDepartment();
        TicketDepartment department2 = new TicketDepartment();
        when(ticketDepartmentRepository.findAll()).thenReturn(Arrays.asList(department1, department2));

        List<TicketDepartment> result = ticketDepartmentService.getAllDepartments();

        assertEquals(2, result.size());
        verify(ticketDepartmentRepository, times(1)).findAll();
    }

    @Test
    void testGetUniqueDepartments() {
        // Arrange
        TicketDepartment department1 = new TicketDepartment();
        department1.setDepartment("IT");
        TicketDepartment department2 = new TicketDepartment();
        department2.setDepartment("HR");
        TicketDepartment department3 = new TicketDepartment();
        department3.setDepartment("IT");
        when(ticketDepartmentRepository.findAll()).thenReturn(Arrays.asList(department1, department2, department3));

        List<String> result = ticketDepartmentService.getUniqueDepartments();

        assertEquals(2, result.size());
        assertTrue(result.contains("IT"));
        assertTrue(result.contains("HR"));
    }

    @Test
    void testSaveUserToDepartment() {
        TicketDepartment department = new TicketDepartment();
        when(ticketDepartmentRepository.save(any(TicketDepartment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0)); // return passed objec
        TicketDepartment result = ticketDepartmentService.saveUserToDepartment(department);

        assertNotNull(result.getId());
        assertTrue(result.getIsActive());
        ArgumentCaptor<TicketDepartment> captor = ArgumentCaptor.forClass(TicketDepartment.class);
        verify(ticketDepartmentRepository).save(captor.capture());
        TicketDepartment captured = captor.getValue();

        assertNotNull(captured.getId());
        assertTrue(captured.getIsActive());    }

    @Test
    void testUpdateUserStatus() {
        String id = UUID.randomUUID().toString();
        TicketDepartment department = new TicketDepartment();
        department.setId(id);
        department.setIsActive(false);
        when(ticketDepartmentRepository.findById(id)).thenReturn(Optional.of(department));

        Optional<TicketDepartment> result = ticketDepartmentService.updateUserStatus(id, true);

        assertTrue(result.isPresent());
        assertTrue(result.get().getIsActive());
        verify(ticketDepartmentRepository, times(1)).save(department);
    }

    @Test
    void testUpdateUserStatusByEmail() {
        String email = "test@example.com";
        TicketDepartment department = new TicketDepartment();
        department.setEmailId(email);
        department.setIsActive(false);
        when(ticketDepartmentRepository.findByEmailId(email)).thenReturn(Optional.of(department));

        Optional<TicketDepartment> result = ticketDepartmentService.updateUserStatusByEmail(email, true);

        assertTrue(result.isPresent());
        assertTrue(result.get().getIsActive());
        verify(ticketDepartmentRepository, times(1)).save(department);
    }

    @Test
    void testUpdateUserStatus_whenUserNotFound() {
        String id = UUID.randomUUID().toString();
        when(ticketDepartmentRepository.findById(id)).thenReturn(Optional.empty());

        Optional<TicketDepartment> result = ticketDepartmentService.updateUserStatus(id, true);

        assertFalse(result.isPresent());
        verify(ticketDepartmentRepository, never()).save(any());
    }

    @Test
    void testUpdateUserStatusByEmail_UserNotFound() {
        String email = "nonexistent@example.com";
        when(ticketDepartmentRepository.findByEmailId(email)).thenReturn(Optional.empty());

        Optional<TicketDepartment> result = ticketDepartmentService.updateUserStatusByEmail(email, true);

        assertFalse(result.isPresent());
        verify(ticketDepartmentRepository, never()).save(any());
    }

    @Test
    void testGetUsersByDepartment() {
        String department = "IT";
        List<TicketDepartment> mockUsers = Arrays.asList(new TicketDepartment(), new TicketDepartment());
        when(ticketDepartmentRepository.findByDepartment(department)).thenReturn(mockUsers);

        List<TicketDepartment> result = ticketDepartmentService.getUsersByDepartment(department);

        assertEquals(2, result.size());
        verify(ticketDepartmentRepository).findByDepartment(department);
    }

    @Test
    void testGetUsersByActiveStatus() {
        TicketDepartment activeUser = new TicketDepartment();
        activeUser.setIsActive(true);
        TicketDepartment inactiveUser = new TicketDepartment();
        inactiveUser.setIsActive(false);
        when(ticketDepartmentRepository.findByIsActive(true)).thenReturn(List.of(activeUser));

        List<TicketDepartment> result = ticketDepartmentService.getUsersByActiveStatus(true);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsActive());
        verify(ticketDepartmentRepository).findByIsActive(true);
    }
    @Test
    void testSaveUserToDepartment_NullInput() {
        assertThrows(NullPointerException.class, () -> ticketDepartmentService.saveUserToDepartment(null));
    }
    @Test
    void testGetUsersByDepartment_EmptyResult() {
        String department = "NonExistentDepartment";
        when(ticketDepartmentRepository.findByDepartment(department)).thenReturn(List.of());


        List<TicketDepartment> result = ticketDepartmentService.getUsersByDepartment(department);

        assertTrue(result.isEmpty());
        verify(ticketDepartmentRepository).findByDepartment(department);
    }



}