package com.Cinetime.controller;

import com.Cinetime.payload.dto.request.MovieRequest;
import com.Cinetime.payload.dto.request.MovieRequestUpdate;
import com.Cinetime.payload.dto.response.*;
import com.Cinetime.service.MovieService;
import com.Cinetime.service.ShowtimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/movies")
@Tag(name = "Movie Management", description = "APIs for managing and retrieving movies")
public class MovieController {

    private final MovieService movieService;
    private final ShowtimeService showtimeService;

    //M03
    @Operation(
            summary = "Get Movies by Hall {M03}",
            description = "Returns a list of movies that are showing in a specific hallName type"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved movies list"),
            @ApiResponse(responseCode = "404", description = "Hall not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })

    @GetMapping("/hall/{hallName}")
    public ResponseMessage<Page<MovieResponse>> getMovieByHall(
            @Parameter(description = "Page number (zero-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of records per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "releaseDate") String sort,
            @Parameter(description = "Sort direction (asc or desc)") @RequestParam(defaultValue = "asc") String type,
            @Parameter(description = "Hall type (e.g., 'imax', 'vip')") @PathVariable String hallName) {

        return movieService.getMovieByHall(page, size, sort, type, hallName);
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
    public ResponseMessage<Page<MovieResponse>> getInTheatersMovies(
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
    public ResponseMessage<Page<MovieResponse>> getComingSoonMovies(
            @Parameter(description = "Page number (zero-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of records per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "releaseDate") String sort,
            @Parameter(description = "Sort direction (asc or desc)") @RequestParam(defaultValue = "asc") String type) {

        return movieService.getComingSoonMovies(page, size, sort, type);
    }

    //M11
    @Operation(
            summary = "Create a new movie",
            description = "Create a new movie with all required attributes including poster image"
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseMessage<MovieResponse> createMovie(
            @Parameter(
                    description = "Movie data",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @ModelAttribute MovieRequest movieRequest) {
        return movieService.createMovie(movieRequest);
    }

    //M01
    @Operation(
            summary = "Search Movies {M01}",
            description = "Returns a paginated list of movies matching the search query"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved movies list"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseMessage<Page<MovieResponse>> getMoviesByQuery(
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
    @GetMapping("/slug/{cinemaSlug}")
    public ResponseMessage<List<MovieResponseCinema>> getMoviesByCinemaSlug(
            @Parameter(description = "Cinema slug", required = true) @PathVariable String cinemaSlug,
            @Parameter(description = "Page number (zero-based)")
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Number of records per page")
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Field to sort by")
            @RequestParam(value = "sort", defaultValue = "title") String sort,
            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(value = "type", defaultValue = "asc") String type
    ) {
        return movieService.getMoviesByCinemaSlug(cinemaSlug, page, size, sort, type);
    }

    //TODO: Create Showtime Controller and put this there

    //M08
    @Operation(
            summary = "Search Movies {M08}",
            description = "Returns a paginated list of movies matching the search query"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved movies list"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/auth/admin")
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseMessage<Page<MovieResponse>> getMoviesByQueryAdmin(
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
    @Operation(
            summary = "Search Movies By ID{M09}",
            description = "It will return the details of a movie based on id"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved movie"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{movieId}")
    @Transactional(readOnly = true)
    public ResponseMessage<MovieResponse> getMoviesById(
            @Parameter(description = "Search id movie")
            @PathVariable(required = false) Long movieId
    ) {
        return movieService.getMoviesById(movieId);
    }

    //M10
    @Operation(
            summary = "Search Movies {M10}",
            description = "It will return the details of a movie based on id(ADMIN)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved movie for admin"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{movieId}/admin")
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseMessage<MovieResponse> getMoviesByIdAdmin(
            @Parameter(description = "Search id movie")
            @PathVariable(required = false) Long movieId
    ) {
        return movieService.getMoviesById(movieId);
    }

    //M12
    @Operation(
            summary = "Update an existing movie",
            description = "Update an existing movie with the provided id"
    )
    @PutMapping("/{movieId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseMessage<MovieResponse> updateMovie(
            @Parameter(description = "ID of the movie to update", required = true)
            @PathVariable Long movieId,
            @Parameter(description = "Movie data")
            @ModelAttribute MovieRequestUpdate movieRequestUpdate)  throws BadRequestException{
        return movieService.updateMovie(movieId, movieRequestUpdate);
    }

    //M13
    @Operation(
            summary = "Delete an existing movie",
            description = "Delete an existing movie with the provided id"
    )
    @DeleteMapping("/{movieId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseMessage<?> deleteMovieById(
            @Parameter(description = "ID of the movie to Delete", required = true)
            @PathVariable Long movieId) {
        return movieService.deleteMovieById(movieId);
    }

    //GET ALL MOVIES
    @Operation(
            summary = "Get all movies by page",
            description = "Returns a list of all movies with pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved movies list"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/getAllMoviesByPage")
    @Transactional(readOnly = true)
    public ResponseMessage<Page<MovieResponse>> getAllMoviesByPage(
            @Parameter(description = "Page number (zero-based)")
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Number of records per page")
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Field to sort by")
            @RequestParam(value = "sort", defaultValue = "title") String sort,
            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(value = "type", defaultValue = "asc") String type) {

        return movieService.getAllMovies(page, size, sort, type);
    }

    @Operation(
            summary = "Get Movies by Hall ID",
            description = "Returns a paginated list of movies that are scheduled to be shown in a specific hall"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved movies list"),
            @ApiResponse(responseCode = "404", description = "Hall not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })

    @GetMapping("/getMoviesByHallId/{hallId}")
    public ResponseMessage<Page<MovieResponse>> getMoviesByHallId(
            @PathVariable Long hallId,
            @Parameter(description = "Page number (zero-based)")
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Number of records per page")
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Field to sort by")
            @RequestParam(value = "sort", defaultValue = "title") String sort,
            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(value = "type", defaultValue = "asc") String type) {
        return movieService.getMoviesByHallId(hallId, page, size, sort, type);
    }


}