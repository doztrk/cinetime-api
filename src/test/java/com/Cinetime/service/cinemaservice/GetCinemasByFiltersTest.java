package com.Cinetime.service.cinemaservice;

import com.Cinetime.entity.Cinema;

import com.Cinetime.helpers.PageableHelper;
import com.Cinetime.payload.dto.response.CinemaResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;

import com.Cinetime.payload.mappers.CinemaMapper;
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
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CinemaService - getCinemasByFilters Tests")
class GetCinemasByFiltersTest {

    @Mock
    private CinemaRepository cinemaRepository;

    @Mock
    private PageableHelper pageableHelper;

    @Mock
    private CinemaMapper cinemaMapper;

    @InjectMocks
    private CinemaService cinemaService;

    private Pageable mockPageable;
    private Cinema testCinema1;
    private Cinema testCinema2;
    private CinemaResponse testCinemaResponse1;
    private CinemaResponse testCinemaResponse2;

    @BeforeEach
    void setUp() {
        mockPageable = PageRequest.of(0, 10);

        testCinema1 = Cinema.builder()
                .id(1L)
                .name("Test Cinema 1")
                .build();

        testCinema2 = Cinema.builder()
                .id(2L)
                .name("Test Cinema 2")
                .build();

        testCinemaResponse1 = CinemaResponse.builder()
                .id(1L)
                .name("Test Cinema 1")
                .build();

        testCinemaResponse2 = CinemaResponse.builder()
                .id(2L)
                .name("Test Cinema 2")
                .build();
    }

    @Test
    @DisplayName("Should return paginated cinemas when cinemas exist with city filter")
    void getCinemasByFilters_WithCityFilter_ShouldReturnCinemas() {
        // Given
        Long cityId = 1L;
        String specialHallName = null;
        int page = 0, size = 10;
        String sort = "name", type = "asc";

        List<Cinema> cinemaList = Arrays.asList(testCinema1, testCinema2);
        Page<Cinema> cinemaPage = new PageImpl<>(cinemaList, mockPageable, 2);
        Page<CinemaResponse> cinemaResponsePage = new PageImpl<>(
                Arrays.asList(testCinemaResponse1, testCinemaResponse2), mockPageable, 2);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(mockPageable);
        when(cinemaRepository.findCinemasByFilters(eq(cityId), eq(null), eq(mockPageable)))
                .thenReturn(cinemaPage);
        when(cinemaMapper.mapCinemaToCinemaResponse(testCinema1)).thenReturn(testCinemaResponse1);
        when(cinemaMapper.mapCinemaToCinemaResponse(testCinema2)).thenReturn(testCinemaResponse2);

        // When
        ResponseMessage<Page<CinemaResponse>> result = cinemaService.getCinemasByFilters(
                cityId, specialHallName, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getContent()).hasSize(2);
        assertThat(result.getMessage()).isNull();

        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(cinemaRepository).findCinemasByFilters(cityId, null, mockPageable);
    }

    @Test
    @DisplayName("Should return paginated cinemas when cinemas exist with special hall filter")
    void getCinemasByFilters_WithSpecialHallFilter_ShouldReturnCinemas() {
        // Given
        Long cityId = null;
        String specialHallName = "IMAX";
        String formattedSpecialHall = "%IMAX%";
        int page = 0, size = 10;
        String sort = "name", type = "asc";

        List<Cinema> cinemaList = Collections.singletonList(testCinema1);
        Page<Cinema> cinemaPage = new PageImpl<>(cinemaList, mockPageable, 1);
        Page<CinemaResponse> cinemaResponsePage = new PageImpl<>(
                Collections.singletonList(testCinemaResponse1), mockPageable, 1);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(mockPageable);
        when(cinemaRepository.findCinemasByFilters(eq(null), eq(formattedSpecialHall), eq(mockPageable)))
                .thenReturn(cinemaPage);
        when(cinemaMapper.mapCinemaToCinemaResponse(testCinema1)).thenReturn(testCinemaResponse1);

        // When
        ResponseMessage<Page<CinemaResponse>> result = cinemaService.getCinemasByFilters(
                cityId, specialHallName, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getContent()).hasSize(1);

        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(cinemaRepository).findCinemasByFilters(null, formattedSpecialHall, mockPageable);
    }

    @Test
    @DisplayName("Should return paginated cinemas when cinemas exist with both filters")
    void getCinemasByFilters_WithBothFilters_ShouldReturnCinemas() {
        // Given
        Long cityId = 1L;
        String specialHallName = "IMAX";
        String formattedSpecialHall = "%IMAX%";
        int page = 0, size = 10;
        String sort = "name", type = "asc";

        List<Cinema> cinemaList = Collections.singletonList(testCinema1);
        Page<Cinema> cinemaPage = new PageImpl<>(cinemaList, mockPageable, 1);
        Page<CinemaResponse> cinemaResponsePage = new PageImpl<>(
                Collections.singletonList(testCinemaResponse1), mockPageable, 1);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(mockPageable);
        when(cinemaRepository.findCinemasByFilters(eq(cityId), eq(formattedSpecialHall), eq(mockPageable)))
                .thenReturn(cinemaPage);
        when(cinemaMapper.mapCinemaToCinemaResponse(testCinema1)).thenReturn(testCinemaResponse1);

        // When
        ResponseMessage<Page<CinemaResponse>> result = cinemaService.getCinemasByFilters(
                cityId, specialHallName, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getObject().getContent()).hasSize(1);

        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(cinemaRepository).findCinemasByFilters(cityId, formattedSpecialHall, mockPageable);
    }

    @Test
    @DisplayName("Should return NO_CONTENT when no cinemas found")
    void getCinemasByFilters_WhenNoCinemasFound_ShouldReturnNoContent() {
        // Given
        Long cityId = 999L;
        String specialHallName = null;
        int page = 0, size = 10;
        String sort = "name", type = "asc";

        Page<Cinema> emptyCinemaPage = new PageImpl<>(Collections.emptyList(), mockPageable, 0);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(mockPageable);
        when(cinemaRepository.findCinemasByFilters(eq(cityId), eq(null), eq(mockPageable)))
                .thenReturn(emptyCinemaPage);

        // When
        ResponseMessage<Page<CinemaResponse>> result = cinemaService.getCinemasByFilters(
                cityId, specialHallName, page, size, sort, type);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getMessage()).isEqualTo("No cinemas found");
        assertThat(result.getObject()).isNull();

        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(cinemaRepository).findCinemasByFilters(cityId, null, mockPageable);
        verify(cinemaMapper, never()).mapCinemaToCinemaResponse(any());
    }

    @Test
    @DisplayName("Should handle null special hall name correctly")
    void getCinemasByFilters_WithNullSpecialHall_ShouldPassNullToRepository() {
        // Given
        Long cityId = 1L;
        String specialHallName = null;
        int page = 0, size = 10;
        String sort = "name", type = "asc";

        Page<Cinema> emptyCinemaPage = new PageImpl<>(Collections.emptyList(), mockPageable, 0);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(mockPageable);
        when(cinemaRepository.findCinemasByFilters(eq(cityId), eq(null), eq(mockPageable)))
                .thenReturn(emptyCinemaPage);

        // When
        cinemaService.getCinemasByFilters(cityId, specialHallName, page, size, sort, type);

        // Then
        verify(cinemaRepository).findCinemasByFilters(cityId, null, mockPageable);
    }

    @Test
    @DisplayName("Should handle empty string special hall name correctly")
    void getCinemasByFilters_WithEmptySpecialHall_ShouldFormatCorrectly() {
        // Given
        Long cityId = 1L;
        String specialHallName = "";
        String formattedSpecialHall = "%%";
        int page = 0, size = 10;
        String sort = "name", type = "asc";

        Page<Cinema> emptyCinemaPage = new PageImpl<>(Collections.emptyList(), mockPageable, 0);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(mockPageable);
        when(cinemaRepository.findCinemasByFilters(eq(cityId), eq(formattedSpecialHall), eq(mockPageable)))
                .thenReturn(emptyCinemaPage);

        // When
        cinemaService.getCinemasByFilters(cityId, specialHallName, page, size, sort, type);

        // Then
        verify(cinemaRepository).findCinemasByFilters(cityId, formattedSpecialHall, mockPageable);
    }

    @Test
    @DisplayName("Should pass correct pagination parameters")
    void getCinemasByFilters_WithCustomPagination_ShouldPassCorrectParameters() {
        // Given
        Long cityId = null;
        String specialHallName = null;
        int page = 2, size = 20;
        String sort = "createdDate", type = "desc";

        Pageable customPageable = PageRequest.of(page, size);
        Page<Cinema> emptyCinemaPage = new PageImpl<>(Collections.emptyList(), customPageable, 0);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(customPageable);
        when(cinemaRepository.findCinemasByFilters(eq(null), eq(null), eq(customPageable)))
                .thenReturn(emptyCinemaPage);

        // When
        cinemaService.getCinemasByFilters(cityId, specialHallName, page, size, sort, type);

        // Then
        verify(pageableHelper).pageableSort(page, size, sort, type);
        verify(cinemaRepository).findCinemasByFilters(null, null, customPageable);
    }

    @Test
    @DisplayName("Should handle special characters in special hall name")
    void getCinemasByFilters_WithSpecialCharactersInHallName_ShouldFormatCorrectly() {
        // Given
        Long cityId = null;
        String specialHallName = "4D-X";
        String formattedSpecialHall = "%4D-X%";
        int page = 0, size = 10;
        String sort = "name", type = "asc";

        Page<Cinema> emptyCinemaPage = new PageImpl<>(Collections.emptyList(), mockPageable, 0);

        when(pageableHelper.pageableSort(page, size, sort, type)).thenReturn(mockPageable);
        when(cinemaRepository.findCinemasByFilters(eq(null), eq(formattedSpecialHall), eq(mockPageable)))
                .thenReturn(emptyCinemaPage);

        // When
        cinemaService.getCinemasByFilters(cityId, specialHallName, page, size, sort, type);

        // Then
        verify(cinemaRepository).findCinemasByFilters(null, formattedSpecialHall, mockPageable);
    }
}