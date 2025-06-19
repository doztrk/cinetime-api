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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketService - GetPassedTickets Tests")
class GetPassedTicketsTest {

    @Mock
    private SecurityService securityService;

    @Mock
    private PageableHelper pageableHelper;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketMapper ticketMapper;

    @InjectMocks
    private TicketService ticketService;

    private User testUser;
    private Ticket testTicket1;
    private Ticket testTicket2;
    private TicketResponse ticketResponse1;
    private TicketResponse ticketResponse2;
    private Pageable testPageable;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .phoneNumber("555-1234")
                .build();

        testTicket1 = Ticket.builder()
                .id(1L)
                .seatLetter("A")
                .seatNumber(1)
                .price(25.50)
                .status(TicketStatus.USED)
                .user(testUser)
                .build();

        testTicket2 = Ticket.builder()
                .id(2L)
                .seatLetter("B")
                .seatNumber(2)
                .price(25.50)
                .status(TicketStatus.USED)
                .user(testUser)
                .build();

        ticketResponse1 = TicketResponse.builder()
                .id(1L)
                .seatLetter("A")
                .seatNumber(1)
                .price(25.50)
                .movieName("Inception")
                .showTimeDate(LocalDate.now().minusDays(1))
                .startTime(LocalTime.of(19, 30))
                .endTime(LocalTime.of(22, 0))
                .ticketOwnerNameSurname("John Doe")
                .hallName("Hall 1")
                .cinemaName("Cinema City")
                .cinemaAdress("123 Main St")
                .build();

        ticketResponse2 = TicketResponse.builder()
                .id(2L)
                .seatLetter("B")
                .seatNumber(2)
                .price(25.50)
                .movieName("Inception")
                .showTimeDate(LocalDate.now().minusDays(1))
                .startTime(LocalTime.of(19, 30))
                .endTime(LocalTime.of(22, 0))
                .ticketOwnerNameSurname("John Doe")
                .hallName("Hall 1")
                .cinemaName("Cinema City")
                .cinemaAdress("123 Main St")
                .build();

        testPageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("Should return passed tickets successfully when user has used tickets")
    void getPassedTickets_ShouldReturnTicketsSuccessfully_WhenUserHasUsedTickets() {
        // Given
        int page = 0, size = 10;
        String sort = "id", type = "asc";

        List<Ticket> ticketList = Arrays.asList(testTicket1, testTicket2);
        Page<Ticket> ticketPage = new PageImpl<>(ticketList, testPageable, 2);

        when(securityService.getCurrentUser()).thenReturn(testUser);
        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(ticketRepository.findByUserAndStatus(testUser, TicketStatus.USED, testPageable))
                .thenReturn(ticketPage);
        when(ticketMapper.mapTicketToTicketResponse(testTicket1)).thenReturn(ticketResponse1);
        when(ticketMapper.mapTicketToTicketResponse(testTicket2)).thenReturn(ticketResponse2);

        // When
        ResponseMessage<Page<TicketResponse>> result = ticketService.getPassedTickets(page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.TICKETS_FOUND);
        assertThat(result.getObject()).isNotNull();

        Page<TicketResponse> responsePage = result.getObject();
        assertThat(responsePage.getContent()).hasSize(2);
        assertThat(responsePage.getTotalElements()).isEqualTo(2);
        assertThat(responsePage.getNumber()).isEqualTo(0);
        assertThat(responsePage.getSize()).isEqualTo(10);

        // Verify interactions
        verify(securityService).getCurrentUser();
        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(ticketRepository).findByUserAndStatus(testUser, TicketStatus.USED, testPageable);
        verify(ticketMapper).mapTicketToTicketResponse(testTicket1);
        verify(ticketMapper).mapTicketToTicketResponse(testTicket2);
    }

    @Test
    @DisplayName("Should return empty page when user has no used tickets")
    void getPassedTickets_ShouldReturnEmptyPage_WhenUserHasNoUsedTickets() {
        // Given
        int page = 0, size = 10;
        String sort = "id", type = "asc";

        List<Ticket> emptyTicketList = Collections.emptyList();
        Page<Ticket> emptyTicketPage = new PageImpl<>(emptyTicketList, testPageable, 0);

        when(securityService.getCurrentUser()).thenReturn(testUser);
        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(ticketRepository.findByUserAndStatus(testUser, TicketStatus.USED, testPageable))
                .thenReturn(emptyTicketPage);

        // When
        ResponseMessage<Page<TicketResponse>> result = ticketService.getPassedTickets(page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo(SuccessMessages.TICKETS_FOUND);
        assertThat(result.getObject()).isNotNull();

        Page<TicketResponse> responsePage = result.getObject();
        assertThat(responsePage.getContent()).isEmpty();
        assertThat(responsePage.getTotalElements()).isEqualTo(0);

        // Verify interactions
        verify(securityService).getCurrentUser();
        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(ticketRepository).findByUserAndStatus(testUser, TicketStatus.USED, testPageable);
        verify(ticketMapper, never()).mapTicketToTicketResponse(any());
    }

    @Test
    @DisplayName("Should handle different pagination parameters correctly")
    void getPassedTickets_ShouldHandleDifferentPaginationParameters_Correctly() {
        // Given
        int page = 2, size = 5;
        String sort = "createdAt", type = "desc";
        Pageable customPageable = PageRequest.of(page, size);

        List<Ticket> ticketList = Arrays.asList(testTicket1);
        Page<Ticket> ticketPage = new PageImpl<>(ticketList, customPageable, 11); // Total 11 tickets

        when(securityService.getCurrentUser()).thenReturn(testUser);
        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(customPageable);
        when(ticketRepository.findByUserAndStatus(testUser, TicketStatus.USED, customPageable))
                .thenReturn(ticketPage);
        when(ticketMapper.mapTicketToTicketResponse(testTicket1)).thenReturn(ticketResponse1);

        // When
        ResponseMessage<Page<TicketResponse>> result = ticketService.getPassedTickets(page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);

        Page<TicketResponse> responsePage = result.getObject();
        assertThat(responsePage.getNumber()).isEqualTo(2);
        assertThat(responsePage.getSize()).isEqualTo(5);
        assertThat(responsePage.getTotalElements()).isEqualTo(11);
        assertThat(responsePage.getTotalPages()).isEqualTo(3);

        // Verify correct parameters passed
        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(ticketRepository).findByUserAndStatus(testUser, TicketStatus.USED, customPageable);
    }

    @Test
    @DisplayName("Should verify execution order of dependencies")
    void getPassedTickets_ShouldVerifyExecutionOrder() {
        // Given
        int page = 0, size = 10;
        String sort = "id", type = "asc";

        List<Ticket> ticketList = Arrays.asList(testTicket1);
        Page<Ticket> ticketPage = new PageImpl<>(ticketList, testPageable, 1);

        when(securityService.getCurrentUser()).thenReturn(testUser);
        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(ticketRepository.findByUserAndStatus(testUser, TicketStatus.USED, testPageable))
                .thenReturn(ticketPage);
        when(ticketMapper.mapTicketToTicketResponse(testTicket1)).thenReturn(ticketResponse1);

        // When
        ticketService.getPassedTickets(page, size, sort, type);

        // Then - Verify execution order
        InOrder inOrder = inOrder(securityService, pageableHelper, ticketRepository, ticketMapper);
        inOrder.verify(securityService).getCurrentUser();
        inOrder.verify(pageableHelper).pageableSort(page, size, sort, type);
        inOrder.verify(ticketRepository).findByUserAndStatus(testUser, TicketStatus.USED, testPageable);
        inOrder.verify(ticketMapper).mapTicketToTicketResponse(testTicket1);
    }

    @Test
    @DisplayName("Should maintain response message structure consistency")
    void getPassedTickets_ShouldMaintainResponseMessageStructureConsistency() {
        // Given
        int page = 0, size = 10;
        String sort = "id", type = "asc";

        List<Ticket> ticketList = Arrays.asList(testTicket1);
        Page<Ticket> ticketPage = new PageImpl<>(ticketList, testPageable, 1);

        when(securityService.getCurrentUser()).thenReturn(testUser);
        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(ticketRepository.findByUserAndStatus(testUser, TicketStatus.USED, testPageable))
                .thenReturn(ticketPage);
        when(ticketMapper.mapTicketToTicketResponse(testTicket1)).thenReturn(ticketResponse1);

        // When
        ResponseMessage<Page<TicketResponse>> result = ticketService.getPassedTickets(page, size, sort, type);

        // Then - Verify ResponseMessage structure
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isNotNull().isNotEmpty();
        assertThat(result.getHttpStatus()).isNotNull();
        assertThat(result.getObject()).isNotNull();

        // Verify Page structure
        Page<TicketResponse> responsePage = result.getObject();
        assertThat(responsePage.getContent()).isNotNull();
        assertThat(responsePage.getTotalElements()).isNotNegative();
        assertThat(responsePage.getNumber()).isNotNegative();
        assertThat(responsePage.getSize()).isPositive();
    }

    @Test
    @DisplayName("Should only query for USED status tickets")
    void getPassedTickets_ShouldOnlyQueryForUsedStatus() {
        // Given
        int page = 0, size = 10;
        String sort = "id", type = "asc";

        when(securityService.getCurrentUser()).thenReturn(testUser);
        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(ticketRepository.findByUserAndStatus(testUser, TicketStatus.USED, testPageable))
                .thenReturn(Page.empty());

        // When
        ticketService.getPassedTickets(page, size, sort, type);

        // Then
        verify(ticketRepository).findByUserAndStatus(testUser, TicketStatus.USED, testPageable);
        verify(ticketRepository, never()).findByUserAndStatus(eq(testUser), eq(TicketStatus.PAID), any());
        verify(ticketRepository, never()).findByUserAndStatus(eq(testUser), eq(TicketStatus.RESERVED), any());
    }
}