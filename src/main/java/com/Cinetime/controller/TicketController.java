package com.Cinetime.controller;

import com.Cinetime.entity.User;
import com.Cinetime.payload.dto.TicketDto;
import com.Cinetime.payload.dto.TicketPurchaseRequest;
import com.Cinetime.payload.dto.user.TicketRequestDto;
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
    private final TicketDto ticketDto;

    //T01 Return movies that an authenticated user bought and haven't used yet
    @GetMapping("/auth/current-tickets")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<Page<TicketDto>> getCurrentTickets(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String type) {
        User user = (User) authentication.getPrincipal();

        Page<TicketDto> tickets = ticketService.getCurrentTickets(user, page, size, sort, type);
        return ResponseEntity.ok(tickets);
    }

    //T02 Return movies that an authenticated user bought and used
    @GetMapping("/auth/passed-tickets")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<Page<TicketDto>> getPassedTickets(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String type) {

        User user = (User) authentication.getPrincipal();
        Page<TicketDto> tickets = ticketService.getPassedTickets(user, page, size, sort, type);
        return ResponseEntity.ok(tickets);
    }

    //T03 reserve movie ticket
    @PostMapping("/reserve")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<TicketDto> reserveTicket(
            @RequestBody TicketRequestDto request,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        TicketDto reserved = ticketService.reserveTicket(request, user);
        return ResponseEntity.ok(reserved);
    }

    //T04 Buy Ticket

    @PostMapping("/buy-ticket")
    public ResponseEntity<List<TicketDto>> buyTickets(@RequestBody TicketPurchaseRequest request,
                                                      Authentication authentication) {

        // Giriş yapmamış kullanıcı olabilir (anonim alışveriş)
        User user = (authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String))
                ? (User) authentication.getPrincipal()
                : null;

        List<TicketDto> tickets = ticketService.buyTickets(request, user);
        return ResponseEntity.ok(tickets);
    }
}
