package com.Cinetime.controller;

import com.Cinetime.entity.User;
import com.Cinetime.payload.dto.TicketDto;
import com.Cinetime.payload.dto.request.TicketPurchaseRequest;
import com.Cinetime.payload.dto.request.TicketRequestDto;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;


    //T01 Return movies that an authenticated user bought and haven't used yet
    @GetMapping("/auth/current-tickets")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseMessage<Page<TicketDto>> getCurrentTickets(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String type) {

        return ticketService.getCurrentTickets(authentication, page, size, sort, type);
    }

    //T02 Return movies that an authenticated user bought and used
    @GetMapping("/auth/passed-tickets")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseMessage<Page<TicketDto>> getPassedTickets(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String type) {

        return ticketService.getPassedTickets(authentication, page, size, sort, type);
    }

    //T03 reserve movie ticket
    @PostMapping("/reserve")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseMessage<TicketDto> reserveTicket(
            @RequestBody TicketRequestDto request,
            Authentication authentication) {

        return ticketService.reserveTicket(request, authentication);
    }

    //T04 Buy Ticket

    @PostMapping("/buy-ticket")
    public ResponseMessage<List<TicketDto>> buyTickets(@RequestBody TicketPurchaseRequest request,
                                                       Authentication authentication) {

        return ticketService.buyTickets(request, authentication);
    }
}
