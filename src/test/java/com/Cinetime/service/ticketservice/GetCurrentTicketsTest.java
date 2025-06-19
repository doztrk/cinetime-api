package com.Cinetime.service.ticketservice;


import com.Cinetime.entity.Ticket;
import com.Cinetime.entity.User;
import com.Cinetime.enums.TicketStatus;
import com.Cinetime.helpers.PageableHelper;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.dto.response.TicketResponse;
import com.Cinetime.payload.mappers.TicketMapper;
import com.Cinetime.payload.messages.SuccessMessages;
import com.Cinetime.repo.TicketRepository;
import com.Cinetime.service.SecurityService;
import com.Cinetime.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCurrentTicketsTest {

    @Mock
    private SecurityService securityService;

    @Mock
    private PageableHelper pageableHelper;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketMapper ticketMapper;

    @InjectMocks
    private TicketService ticketService; // Assuming your service class name

    private User mockUser;
    private Ticket mockTicket;
    private TicketResponse mockTicketResponse;
    private Pageable mockPageable;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .phoneNumber("555-555-5555")
                .email("user@test.com")
                .build();

        mockTicket = Ticket.builder()
                .id(1L)
                .user(mockUser)
                .status(TicketStatus.PAID)
                .build();

        mockTicketResponse = TicketResponse.builder()
                .id(1L)
                .movieName("Test Movie")
                .seatLetter("A")
                .seatNumber(1)
                .price(15.50)
                .status(TicketStatus.PAID)
                .build();

        mockPageable = PageRequest.of(0, 10, Sort.by("id").ascending());
    }

    @Test
    void getCurrentTickets_ShouldReturnPaidTickets_WhenUserHasPaidTickets() {
        // Given
        int page = 0;
        int size = 10;
        String sort = "id";
        String type = "asc";

        Page<Ticket> ticketsPage = new PageImpl<>(List.of(mockTicket), mockPageable, 1);
        Page<TicketResponse> ticketResponsesPage = new PageImpl<>(List.of(mockTicketResponse), mockPageable, 1);

        when(securityService.getCurrentUser()).thenReturn(mockUser);
        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(mockPageable);
        when(ticketRepository.findByUserAndStatus(mockUser, TicketStatus.PAID, mockPageable)).thenReturn(ticketsPage);
        when(ticketMapper.mapTicketToTicketResponse(mockTicket)).thenReturn(mockTicketResponse);

        // When
        ResponseMessage<Page<TicketResponse>> result = ticketService.getCurrentTickets(page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.TICKETS_FOUND);
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getContent()).hasSize(1);
        assertThat(result.getObject().getContent().get(0)).isEqualTo(mockTicketResponse);
        assertThat(result.getObject().getTotalElements()).isEqualTo(1);

        // Verify interactions
        verify(securityService).getCurrentUser();
        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(ticketRepository).findByUserAndStatus(mockUser, TicketStatus.PAID, mockPageable);
        verify(ticketMapper).mapTicketToTicketResponse(mockTicket);
    }

    @Test
    void getCurrentTickets_ShouldReturnEmptyPage_WhenUserHasNoPaidTickets() {
        // Given
        int page = 0;
        int size = 10;
        String sort = "id";
        String type = "asc";

        Page<Ticket> emptyTicketsPage = new PageImpl<>(Collections.emptyList(), mockPageable, 0);
        Page<TicketResponse> emptyResponsePage = emptyTicketsPage.map(ticketMapper::mapTicketToTicketResponse);

        when(securityService.getCurrentUser()).thenReturn(mockUser);
        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(mockPageable);
        when(ticketRepository.findByUserAndStatus(mockUser, TicketStatus.PAID, mockPageable)).thenReturn(emptyTicketsPage);

        // When
        ResponseMessage<Page<TicketResponse>> result = ticketService.getCurrentTickets(page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.TICKETS_FOUND);
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getContent()).isEmpty();
        assertThat(result.getObject().getTotalElements()).isEqualTo(0);

        verify(securityService).getCurrentUser();
        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(ticketRepository).findByUserAndStatus(mockUser, TicketStatus.PAID, mockPageable);
        verifyNoInteractions(ticketMapper);
    }

    @Test
    void getCurrentTickets_ShouldThrowException_WhenSecurityServiceFails() {
        // Given
        int page = 0;
        int size = 10;
        String sort = "id";
        String type = "asc";

        when(securityService.getCurrentUser()).thenThrow(new SecurityException("User not authenticated"));

        // When & Then
        assertThatThrownBy(() -> ticketService.getCurrentTickets(page, size, sort, type))
                .isInstanceOf(SecurityException.class)
                .hasMessage("User not authenticated");

        verify(securityService).getCurrentUser();
        verifyNoInteractions(pageableHelper, ticketRepository, ticketMapper);
    }

    @Test
    void getCurrentTickets_ShouldHandleMultipleTickets_WithCorrectPagination() {
        // Given
        int page = 1;
        int size = 2;
        String sort = "createdDate";
        String type = "desc";

        Ticket ticket2 = Ticket.builder()
                .id(2L)
                .user(mockUser)
                .status(TicketStatus.PAID)
                .build();

        TicketResponse ticketResponse2 = TicketResponse.builder()
                .id(2L)
                .movieName("Test Movie 2")
                .seatLetter("B")
                .seatNumber(2)
                .price(18.00)
                .status(TicketStatus.PAID)
                .build();

        List<Ticket> tickets = List.of(mockTicket, ticket2);
        List<TicketResponse> ticketResponses = List.of(mockTicketResponse, ticketResponse2);

        Pageable customPageable = PageRequest.of(1, 2, Sort.by("createdDate").descending());
        Page<Ticket> ticketsPage = new PageImpl<>(tickets, customPageable, 5); // Total 5 tickets

        when(securityService.getCurrentUser()).thenReturn(mockUser);
        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(customPageable);
        when(ticketRepository.findByUserAndStatus(mockUser, TicketStatus.PAID, customPageable)).thenReturn(ticketsPage);
        when(ticketMapper.mapTicketToTicketResponse(mockTicket)).thenReturn(mockTicketResponse);
        when(ticketMapper.mapTicketToTicketResponse(ticket2)).thenReturn(ticketResponse2);

        // When
        ResponseMessage<Page<TicketResponse>> result = ticketService.getCurrentTickets(page, size, sort, type);

        // Then
        assertThat(result.getObject().getContent()).hasSize(2);
        assertThat(result.getObject().getTotalElements()).isEqualTo(5);
        assertThat(result.getObject().getNumber()).isEqualTo(1); // Current page
        assertThat(result.getObject().getSize()).isEqualTo(2);   // Page size
        assertThat(result.getObject().getTotalPages()).isEqualTo(3); // 5 total / 2 size = 3 pages

        verify(ticketMapper, times(2)).mapTicketToTicketResponse(any(Ticket.class));
    }
}