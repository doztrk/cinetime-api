package com.Cinetime.controller;

import com.Cinetime.entity.Cinema;
import com.Cinetime.payload.dto.response.CinemaHallResponse;
import com.Cinetime.payload.dto.response.CinemaResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.service.CinemaService;
import com.Cinetime.service.HallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cinemas")
@RequiredArgsConstructor
@Tag(name = "Cinema Management", description = "APIs for managing cinemas and their halls")
public class CinemaController {

    private final CinemaService cinemaService;
    private final HallService hallService;

    //C01
    @Operation(
            summary = "Get All Cinemas {C01}",
            description = "Returns cinemas based on optional city and special hall filters with pagination and sorting options. "
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved cinemas list",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "401", description = "Not authorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseMessage<Page<CinemaResponse>> getCinemasByFilters(
            @Parameter(description = "Filter cinemas by city ID") @RequestParam(required = false) Long cityId,
            @Parameter(description = "Filter cinemas by special hall type (e.g., 'imax')") @RequestParam(required = false) String specialHall,
            @Parameter(description = "Page number (zero-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of records per page(Default:10)") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "name") String sort,
            @Parameter(description = "Sort direction (asc or desc)") @RequestParam(defaultValue = "asc") String type
    ) {
        return cinemaService.getCinemasByFilters(cityId, specialHall, page, size, sort, type);
    }


    //C03
    @Operation(
            summary = "Get Cinema Details {C03}",
            description = "Returns detailed information about a specific cinema by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved cinema details",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "Cinema not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseMessage<Cinema> getCinemaById(
            @Parameter(description = "ID of the cinema to retrieve", required = true) @PathVariable Long id
    ) {
        return cinemaService.getCinemaById(id);
    }

    //C04
    @Operation(
            summary = "Get Cinema Halls {C04}",
            description = "Returns all halls belonging to a specific cinema"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved cinema halls",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "Cinema not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{cinemaId}/halls")
    public ResponseMessage<CinemaHallResponse> getCinemaHalls(
            @Parameter(description = "ID of the cinema to retrieve halls for", required = true) @PathVariable Long cinemaId
    ) {
        return cinemaService.getHallsByCinemaId(cinemaId);
    }

    @Operation(
            summary = "Get Cinemas By Movie ID {C05}",
            description = "Returns a paginated list of cinemas showing a specific movie identified by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved cinemas list",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "No cinemas found for the specified movie"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/movie/{movieId}")
    public ResponseMessage<Page<CinemaResponse>> getCinemasByMovieId(@PathVariable Long movieId,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "10") int size,
                                                                     @RequestParam(defaultValue = "name") String sort,
                                                                     @RequestParam(defaultValue = "asc") String type) {
        return cinemaService.getCinemasByMovieId(movieId, page, size, sort, type);
    }


    @Operation(
            summary = "Get Cinemas By Hall Name Provided {C06}",
            description = "Returns a paginated list of cinemas having specific Hall Name given in parameters"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved cinemas list",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "401", description = "Not authorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/hall/{hallName}")
    public ResponseMessage<Page<CinemaResponse>> getCinemasByHallName(@PathVariable String hallName,
                                                                    @RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "10") int size,
                                                                    @RequestParam(defaultValue = "name") String sort,
                                                                    @RequestParam(defaultValue = "asc") String type) {


        return cinemaService.getCinemasByHallName(hallName, page, size, sort, type);
    }
}