package com.Cinetime.controller;

import com.Cinetime.payload.dto.request.ShowtimeRequest;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.dto.response.ShowtimeResponse;
import com.Cinetime.service.ShowtimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/showtime")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;


    @Operation(
            summary = "Get Showtime by ID",
            description = "Retrieves detailed information about a specific showtime using its ID",
            tags = {"Showtimes"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved showtime details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseMessage.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Showtime not found with the provided ID"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @GetMapping("/{showtimeId}")
    public ResponseMessage<ShowtimeResponse> getShowtimeById(@PathVariable Long showtimeId) {
        return showtimeService.getShowtimeById(showtimeId);
    }

    //M14
    @Operation(
            summary = "Get Future Showtimes {M14}",
            description = "Returns a list of  future showtimes for a specific movie",
            tags = {"Showtimes"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved showtimes list"),
            @ApiResponse(responseCode = "404", description = "Movie not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/upcoming/{movieId}")
    public ResponseMessage<Page<ShowtimeResponse>> getUpcomingShowtimes(
            @Parameter(description = "Page number (zero-based)")
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Number of records per page")
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Field to sort by")
            @RequestParam(value = "sort", defaultValue = "date") String sort,
            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(value = "type", defaultValue = "asc") String type,
            @Parameter(description = "ID of the movie to get showtimes for", required = true)
            @PathVariable Long movieId) {
        return showtimeService.getUpcomingShowtimes(page, size, sort, type, movieId);
    }

    @Operation(
            summary = "Create a new showtime for a movie",
            description = "Creates a new showtime scheduling for a specific movie and hall. Validates hall availability and calculates ticket price based on various factors. Requires ADMIN or EMPLOYEE role.",
            tags = {"Showtimes"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Showtime successfully created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseMessage.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or validation error (e.g., end time before start time)"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Requires ADMIN or EMPLOYEE role"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Movie or Hall not found with the provided IDs"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - Time slot already occupied or other scheduling conflict"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @PostMapping("/createShowtimeForMovie")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseMessage<ShowtimeResponse> createShowtimeForMovie(@RequestBody @Valid ShowtimeRequest showtimeRequest) {
        return showtimeService.createShowtimeForMovie(showtimeRequest);
    }



}
