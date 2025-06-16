package com.Cinetime.service.cinemaservice;

import com.Cinetime.entity.Cinema;
import com.Cinetime.entity.City;
import com.Cinetime.entity.District;
import com.Cinetime.helpers.PageableHelper;
import com.Cinetime.payload.dto.response.CinemaResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.payload.mappers.CinemaMapper;
import com.Cinetime.payload.messages.ErrorMessages;
import com.Cinetime.repo.CinemaRepository;
import com.Cinetime.service.CinemaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CinemaService - getCinemasByHallName Tests")
class GetCinemasByHallNameTest {

    @Mock
    private CinemaRepository cinemaRepository;

    @Mock
    private PageableHelper pageableHelper;

    @Mock
    private CinemaMapper cinemaMapper;

    @InjectMocks
    private CinemaService cinemaService;

    private Cinema testCinema;
    private CinemaResponse testCinemaResponse;
    private Pageable testPageable;
    private Page<Cinema> mockCinemaPage;
    private Page<CinemaResponse> mockCinemaResponsePage;
    private City testCity;
    private District testDistrict;

    @BeforeEach
    void setUp() {
        // Create test City entity
        testCity = new City();
        testCity.setId(1L);
        testCity.setName("Test City");

        // Create test District entity
        testDistrict = new District();
        testDistrict.setId(1L);
        testDistrict.setName("Test District");
        testDistrict.setCity(testCity);

        // Create test Cinema entity
        testCinema = Cinema.builder()
                .id(1L)
                .name("Test Cinema")
                .slug("test-cinema")
                .address("Test Address")
                .city(testCity)
                .district(testDistrict)
                .phone("123-456-7890")
                .email("test@cinema.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Create test CinemaResponse DTO
        testCinemaResponse = CinemaResponse.builder()
                .id(1L)
                .name("Test Cinema")
                .address("Test Address")
                .city("Test City")
                .district("Test District")
                .phone("123-456-7890")
                .email("test@cinema.com")
                .build();

        // Create test Pageable
        testPageable = PageRequest.of(0, 10, Sort.by("name").ascending());

        // Create test Page objects
        mockCinemaPage = new PageImpl<>(List.of(testCinema), testPageable, 1);
        mockCinemaResponsePage = new PageImpl<>(List.of(testCinemaResponse), testPageable, 1);
    }

    @Test
    @DisplayName("Should return cinemas when valid hall name is provided")
    void getCinemasByHallName_WithValidHallName_ShouldReturnCinemas() {
        // Given
        String hallName = "IMAX Hall";
        int page = 0;
        int size = 10;
        String sort = "name";
        String type = "ASC";

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(cinemaRepository.findCinemasByHallName(eq(hallName), eq(testPageable))).thenReturn(mockCinemaPage);
        when(cinemaMapper.mapCinemaToCinemaResponse(testCinema)).thenReturn(testCinemaResponse);

        // When
        ResponseMessage<Page<CinemaResponse>> result = cinemaService.getCinemasByHallName(hallName, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo("Cinemas found successfully");
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getContent()).hasSize(1);
        assertThat(result.getObject().getContent().get(0)).isEqualTo(testCinemaResponse);
        assertThat(result.getObject().getTotalElements()).isEqualTo(1);

        // Verify interactions
        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(cinemaRepository).findCinemasByHallName(hallName, testPageable);
        verify(cinemaMapper).mapCinemaToCinemaResponse(testCinema);
    }

    @Test
    @DisplayName("Should return NO_CONTENT when no cinemas found for hall name")
    void getCinemasByHallName_WithNonExistentHallName_ShouldReturnNoContent() {
        // Given
        String hallName = "Non-existent Hall";
        int page = 0;
        int size = 10;
        String sort = "name";
        String type = "ASC";
        Page<Cinema> emptyCinemaPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(cinemaRepository.findCinemasByHallName(eq(hallName), eq(testPageable))).thenReturn(emptyCinemaPage);

        // When
        ResponseMessage<Page<CinemaResponse>> result = cinemaService.getCinemasByHallName(hallName, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.CINEMA_NOT_FOUND);
        assertThat(result.getObject()).isNull();

        // Verify interactions
        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(cinemaRepository).findCinemasByHallName(hallName, testPageable);
        verify(cinemaMapper, never()).mapCinemaToCinemaResponse(any());
    }

    @Test
    @DisplayName("Should handle multiple cinemas for same hall name")
    void getCinemasByHallName_WithMultipleCinemas_ShouldReturnAllCinemas() {
        // Given
        String hallName = "Premium Hall";
        int page = 0;
        int size = 10;
        String sort = "name";
        String type = "ASC";

        Cinema cinema2 = Cinema.builder()
                .id(2L)
                .name("Second Cinema")
                .slug("second-cinema")
                .address("Second Address")
                .city(testCity)
                .district(testDistrict)
                .phone("987-654-3210")
                .email("second@cinema.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        CinemaResponse cinemaResponse2 = CinemaResponse.builder()
                .id(2L)
                .name("Second Cinema")
                .address("Second Address")
                .city("Second City")
                .district("Second District")
                .phone("987-654-3210")
                .email("second@cinema.com")
                .build();

        List<Cinema> cinemas = List.of(testCinema, cinema2);
        Page<Cinema> multipleCinemasPage = new PageImpl<>(cinemas, testPageable, 2);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(cinemaRepository.findCinemasByHallName(eq(hallName), eq(testPageable))).thenReturn(multipleCinemasPage);
        when(cinemaMapper.mapCinemaToCinemaResponse(testCinema)).thenReturn(testCinemaResponse);
        when(cinemaMapper.mapCinemaToCinemaResponse(cinema2)).thenReturn(cinemaResponse2);

        // When
        ResponseMessage<Page<CinemaResponse>> result = cinemaService.getCinemasByHallName(hallName, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo("Cinemas found successfully");
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getContent()).hasSize(2);
        assertThat(result.getObject().getTotalElements()).isEqualTo(2);

        // Verify interactions
        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(cinemaRepository).findCinemasByHallName(hallName, testPageable);
        verify(cinemaMapper, times(2)).mapCinemaToCinemaResponse(any(Cinema.class));
    }

    @Test
    @DisplayName("Should handle different pagination parameters correctly")
    void getCinemasByHallName_WithDifferentPaginationParams_ShouldWork() {
        // Given
        String hallName = "VIP Hall";
        int page = 1;
        int size = 5;
        String sort = "city";
        String type = "DESC";
        Pageable customPageable = PageRequest.of(1, 5, Sort.by("city").descending());
        Page<Cinema> customCinemaPage = new PageImpl<>(List.of(testCinema), customPageable, 1);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(customPageable);
        when(cinemaRepository.findCinemasByHallName(eq(hallName), eq(customPageable))).thenReturn(customCinemaPage);
        when(cinemaMapper.mapCinemaToCinemaResponse(testCinema)).thenReturn(testCinemaResponse);

        // When
        ResponseMessage<Page<CinemaResponse>> result = cinemaService.getCinemasByHallName(hallName, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessage()).isEqualTo("Cinemas found successfully");

        // Verify correct pagination parameters were used
        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(cinemaRepository).findCinemasByHallName(hallName, customPageable);
    }

    @Test
    @DisplayName("Should handle null or empty hall name gracefully")
    void getCinemasByHallName_WithNullHallName_ShouldHandleGracefully() {
        // Given
        String hallName = null;
        int page = 0;
        int size = 10;
        String sort = "name";
        String type = "ASC";
        Page<Cinema> emptyCinemaPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(cinemaRepository.findCinemasByHallName(eq(hallName), eq(testPageable))).thenReturn(emptyCinemaPage);

        // When
        ResponseMessage<Page<CinemaResponse>> result = cinemaService.getCinemasByHallName(hallName, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.CINEMA_NOT_FOUND);
        assertThat(result.getObject()).isNull();

        // Verify interactions
        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(cinemaRepository).findCinemasByHallName(hallName, testPageable);
    }

    @Test
    @DisplayName("Should handle empty string hall name")
    void getCinemasByHallName_WithEmptyHallName_ShouldReturnNoContent() {
        // Given
        String hallName = "";
        int page = 0;
        int size = 10;
        String sort = "name";
        String type = "ASC";
        Page<Cinema> emptyCinemaPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(testPageable);
        when(cinemaRepository.findCinemasByHallName(eq(hallName), eq(testPageable))).thenReturn(emptyCinemaPage);

        // When
        ResponseMessage<Page<CinemaResponse>> result = cinemaService.getCinemasByHallName(hallName, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getMessage()).isEqualTo(ErrorMessages.CINEMA_NOT_FOUND);
        assertThat(result.getObject()).isNull();
    }
}