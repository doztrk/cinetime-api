package com.Cinetime.controller;

import com.Cinetime.entity.Movie;
import com.Cinetime.entity.Showtime;
import com.Cinetime.payload.dto.request.MovieRequest;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.service.MovieService;
import com.Cinetime.service.ShowtimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/movie")
@Tag(name = "Movie Management", description = "APIs for managing and retrieving movies")
public class MovieController {

    private final MovieService movieService;
    private final ShowtimeService showtimeService;

    //M01
    @Operation(
            summary = "Search Movies",
            description = "Returns a paginated list of movies matching the search query"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved movies list"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<Page<Movie>> getMoviesByQuery(
            @Parameter(description = "Search query term (searches in title and summary)")
            @RequestParam(required = false) String q,
            @Parameter(description = "Page number (zero-based)")
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Number of records per page")
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Field to sort by")
            @RequestParam(value = "sort", defaultValue = "title") String sort,
            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(value = "type", defaultValue = "asc") String type
    ) {
        return movieService.getMoviesByQuery(q, page, size, sort, type);
    }

    // M02
    @Operation(
            summary = "Get Movies by Cinema Slug",
            description = "Returns a list of movies showing at a specific cinema identified by its slug"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved movies list"),
            @ApiResponse(responseCode = "404", description = "Cinema not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{slug}")
    public ResponseEntity<List<Movie>> getMoviesByCinemaSlug(
            @Parameter(description = "Cinema slug", required = true) @PathVariable String slug) {
        return movieService.getMoviesByCinemaSlug(slug);
    }

    //M03
    @Operation(
            summary = "Get Movies by Hall",
            description = "Returns a list of movies that are showing in a specific hall type"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved movies list"),
            @ApiResponse(responseCode = "404", description = "Hall not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{hallId}")
    public ResponseEntity<List<Movie>> getMovieByHall(
            @Parameter(description = "Page number (zero-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of records per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "releaseDate") String sort,
            @Parameter(description = "Sort direction (asc or desc)") @RequestParam(defaultValue = "asc") String type,
            @Parameter(description = "Hall type (e.g., 'imax', 'vip')") @PathVariable String hall) {

        return movieService.getMovieByHall(page, size, sort, type, hall);
    }

    //M04
    @Operation(
            summary = "Get Movies In Theaters",
            description = "Returns a list of movies currently showing in theaters"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved movies list"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/in-theaters")
    public ResponseEntity<List<Movie>> getInTheatersMovies(
            @Parameter(description = "Page number (zero-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of records per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "releaseDate") String sort,
            @Parameter(description = "Sort direction (asc or desc)") @RequestParam(defaultValue = "asc") String type) {

        return movieService.getInTheatersMovies(page, size, sort, type);
    }

    //M05
    @Operation(
            summary = "Get Coming Soon Movies",
            description = "Returns a list of movies that will be released soon"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved movies list"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/coming-soon")
    public ResponseEntity<List<Movie>> getComingSoonMovies(
            @Parameter(description = "Page number (zero-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of records per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "releaseDate") String sort,
            @Parameter(description = "Sort direction (asc or desc)") @RequestParam(defaultValue = "asc") String type) {

        return movieService.getComingSoonMovies(page, size, sort, type);
    }

    //M08

    @GetMapping("/auth/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE'")
    @Operation(
            summary = "Search Movies",
            description = "Returns a paginated list of movies matching the search query"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved movies list"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<Movie>> getMoviesByQueryByAdmin(
            @Parameter(description = "Search query term (searches in title and summary)")
            @RequestParam(required = false) String q,
            @Parameter(description = "Page number (zero-based)")
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Number of records per page")
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Field to sort by")
            @RequestParam(value = "sort", defaultValue = "title") String sort,
            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(value = "type", defaultValue = "asc") String type
    ) {
        return movieService.getMoviesByQuery(q, page, size, sort, type);
    }

    //M09
    @GetMapping("/{id}")
    public ResponseEntity<Movie>getMovieById(@PathVariable Long id){
        return movieService.getMovieById(id);
    }

    //M10
    @GetMapping("/{id}/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE'")
    public ResponseEntity<Movie>getMovieByIdAdminEmployee(@PathVariable Long id){
        return movieService.getMovieById(id);
    }

    //M11
    @Operation(
            summary = "Create Movie",
            description = "Creates a new movie with the provided details. Admin access only.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Movie successfully created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseMessage<Movie> createMovie(
            @Parameter(description = "Movie details to create", required = true)
            @ModelAttribute MovieRequest movieRequest) {
        return movieService.createMovie(movieRequest);
    }

    //M12
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseMessage<Movie>updateMovie(@PathVariable Long id, @RequestBody @Valid MovieRequest movieRequest){
        return movieService.updateMovie(id,movieRequest);
    }

    //M13
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseMessage deleteMovie(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        return movieService.deleteMovie(id, httpServletRequest);
    }

    //M14
    @Operation(
            summary = "Get Upcoming Showtimes",
            description = "Returns a list of upcoming showtimes for a specific movie"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved showtimes list"),
            @ApiResponse(responseCode = "404", description = "Movie not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{movieId}/show-times")
    public ResponseEntity<List<Showtime>> getUpcomingShowtimes(
            @Parameter(description = "ID of the movie to get showtimes for", required = true)
            @PathVariable Long movieId) {
        return showtimeService.getUpcomingShowtimes(movieId);
    }
}