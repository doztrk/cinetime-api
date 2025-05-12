package com.Cinetime.controller;

import com.Cinetime.payload.dto.request.TicketPriceCalculationRequest;
import com.Cinetime.payload.dto.request.TicketPurchaseRequest;
import com.Cinetime.payload.dto.request.TicketReserveRequest;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.dto.response.TicketResponse;
import com.Cinetime.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;


    //T01 Return movies that an authenticated user bought and haven't used yet
    @GetMapping("/auth/current-tickets")
    @PreAuthorize("hasAnyRole('MEMBER')")
    public ResponseMessage<Page<TicketResponse>> getCurrentTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String type) {

        return ticketService.getCurrentTickets(page, size, sort, type);
    }

    //T02 Return movies that an authenticated user bought and used
    @GetMapping("/auth/passed-tickets")
    @PreAuthorize("hasAnyRole('MEMBER')")
    public ResponseMessage<Page<TicketResponse>> getPassedTickets(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String type) {

        return ticketService.getPassedTickets(page, size, sort, type);
    }

    //T03 reserve movie ticket
   /* @PostMapping("/reserve/{movieId}")
    @PreAuthorize("hasAnyRole('MEMBER')")
    public ResponseMessage<List<TicketResponse>> reserveTicket(@Valid
                                                               @RequestBody TicketReserveRequest request, Long movieId) {

        return ticketService.reserveTicket(request, movieId);
    }*/

    //T04 Buy Ticket
    // Allowing anonymous users to purchase tickets means there's no way to associate the ticket with a specific user account,
    // which would defeat aspects of our domain model (the Ticket entity has a non-nullable user field) This is a bait by Nihal hoca
    @Operation(
            summary = "Buy Movie Ticket {T04}",
            description = "Purchase tickets for a movie, creating a successful payment and paid tickets. " +
                    "Only authenticated users with MEMBER role can purchase tickets."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tickets successfully purchased",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "400", description = "Bad request - validation errors or no seats specified"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires MEMBER role"),
            @ApiResponse(responseCode = "404", description = "Movie or showtime not found"),
            @ApiResponse(responseCode = "409", description = "Conflict - Seats already occupied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/buy-ticket")
    @PreAuthorize("hasAnyRole('MEMBER')")
    public ResponseMessage<List<TicketResponse>> buyTickets(@Valid
                                                            @RequestBody TicketPurchaseRequest request) {
        return ticketService.buyTickets(request);
    }

    @Operation(
            summary = "Get Ticket Price",
            description = "Retrieves the ticket price for a specific showtime"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved ticket price",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "Showtime not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/ticketPrice/{showtimeId}")
    public ResponseMessage<Double> getTicketPrice(@PathVariable Long showtimeId) {
        return ticketService.getTicketPrice(showtimeId);
    }

    @GetMapping("/calculate-price")
    public ResponseMessage<Double> calculateTicketPrice(@Valid @RequestBody TicketPriceCalculationRequest request){
        return ticketService.calculateTicketPrice(request);
    }


}
