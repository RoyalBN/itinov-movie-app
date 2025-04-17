package com.itinov.movie_api.controller;

import com.itinov.movie_api.dto.FilmDTO;
import com.itinov.movie_api.model.FilmSortBy;
import com.itinov.movie_api.service.UserFilmService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*; // pour .status(), .jsonPath(), etc.


import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@WebMvcTest(UserFilmController.class)
class UserFilmControllerTest {

    private static final Long EXISTING_USER_ID = 42L;
    private static final Long EXISTING_FILM_ID = 123L;
    private static final Long NON_EXISTING_ID = 999L;

    public static final String BASE_URL = "/api/users/{userId}/favorites/{filmId}";

    private FilmDTO film1;
    private FilmDTO film2;

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private UserFilmController userFilmController;

    @MockitoBean
    private UserFilmService userFilmService;

    @BeforeEach
    void setUp() {
        film1 = FilmDTO.builder()
                .id(1L)
                .title("Inception")
                .rating(8.8)
                .releaseDate(LocalDate.of(2010, 7, 16))
                .posterUrl("url1")
                .isFavorite(true)
                .hasBeenWatched(false)
                .build();

        film2 = FilmDTO.builder()
                .id(2L)
                .title("Interstellar")
                .rating(9.0)
                .releaseDate(LocalDate.of(2014, 11, 7))
                .posterUrl("url2")
                .isFavorite(true)
                .hasBeenWatched(true)
                .build();
    }


    @Test
    @DisplayName("[N] Add a film to favorite")
    void should_add_film_to_favorite() throws Exception {
        // Act & Assert
        mockMvc.perform(post(BASE_URL, EXISTING_USER_ID, EXISTING_FILM_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userFilmService, times(1)).addFilmToFavorite(EXISTING_USER_ID, EXISTING_FILM_ID);
    }

    @Test
    @DisplayName("[E] Return 404 when user not found")
    void should_return_404_when_user_not_found() throws Exception {
        // Arrange
        doThrow(new EntityNotFoundException("User not found with id: " + NON_EXISTING_ID))
                .when(userFilmService).addFilmToFavorite(anyLong(), anyLong());

        // Act & Assert
        mockMvc.perform(post(BASE_URL, NON_EXISTING_ID, EXISTING_FILM_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userFilmService, times(1)).addFilmToFavorite(anyLong(), anyLong());
    }

    @Test
    @DisplayName("[E] Return 404 when film not found")
    void should_return_404_when_film_not_found() throws Exception {
        // Arrange
        doThrow(new EntityNotFoundException("Film not found with id: " + NON_EXISTING_ID))
                .when(userFilmService)
                .addFilmToFavorite(EXISTING_USER_ID, NON_EXISTING_ID);

        // Act & Assert
        mockMvc.perform(post(BASE_URL, EXISTING_USER_ID, NON_EXISTING_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userFilmService, times(1)).addFilmToFavorite(EXISTING_USER_ID, NON_EXISTING_ID);
    }

    @Test
    @DisplayName("[E] Return 400 when invalid request")
    void should_return_400_when_invalid_request() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Invalid request"))
                .when(userFilmService).addFilmToFavorite(NON_EXISTING_ID, NON_EXISTING_ID);

        // Act & Assert
        mockMvc.perform(post(BASE_URL, NON_EXISTING_ID, NON_EXISTING_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userFilmService, times(1)).addFilmToFavorite(anyLong(), anyLong());
    }

    @Test
    @DisplayName("[N] Remove a film from favorite")
    void should_remove_film_from_favorite() throws Exception {
        // Act & Assert
        mockMvc.perform(put(BASE_URL, EXISTING_USER_ID, EXISTING_FILM_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userFilmService).removeFilmFromFavorite(EXISTING_USER_ID, EXISTING_FILM_ID);
    }

    @Test
    @DisplayName("[E] Throw exception when userId is invalid")
    void should_throw_exception_when_userId_is_invalid() throws Exception {
        // Act & Assert
        mockMvc.perform(put(BASE_URL, "abc", EXISTING_FILM_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userFilmService, never()).removeFilmFromFavorite(anyLong(), anyLong());
    }

    @Test
    @DisplayName("[E] Throw exception when filmId is invalid")
    void should_throw_exception_when_filmId_is_invalid() throws Exception {
        // Act & Assert
        mockMvc.perform(put(BASE_URL, EXISTING_USER_ID, "abc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userFilmService, never()).removeFilmFromFavorite(anyLong(), anyLong());
    }

    @Test
    @DisplayName("[E] Throw exception when no entity found")
    void should_throw_exception_when_no_entity_found() throws Exception {
        // Arrange
        doThrow(new EntityNotFoundException("User not found with id: " + NON_EXISTING_ID))
                .when(userFilmService).removeFilmFromFavorite(NON_EXISTING_ID, EXISTING_FILM_ID);

        // Act
        mockMvc.perform(put(BASE_URL, NON_EXISTING_ID, EXISTING_FILM_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Assert
        verify(userFilmService, times(1)).removeFilmFromFavorite(NON_EXISTING_ID, EXISTING_FILM_ID);
    }

    @Test
    @DisplayName("[E] Throw exception when invalid request")
    void should_throw_exception_when_invalid_request() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Invalid request"))
                .when(userFilmService).removeFilmFromFavorite(any(), any());

        // Act
        mockMvc.perform(put(BASE_URL, NON_EXISTING_ID, NON_EXISTING_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Assert
        verify(userFilmService, times(1)).removeFilmFromFavorite(any(), any());
    }

    @Test
    @DisplayName("[E] Throw exception when internal error")
    void should_throw_exception_when_internal_error() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Unexpected error"))
                .when(userFilmService).removeFilmFromFavorite(anyLong(), anyLong());

        // Act
        mockMvc.perform(put("/api/users/1/favorites/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        // Assert
        verify(userFilmService, times(1)).removeFilmFromFavorite(anyLong(), anyLong());
    }

    @Test
    @DisplayName("[E] Throw exception when userId is negative")
    void should_throw_exception_when_userId_is_negative() throws Exception {
        // Act & Assert
        mockMvc.perform(put(BASE_URL, -1, 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.code").value("VALIDATION_ERROR"),
                        jsonPath("$.message").value(containsString("ID must be positive"))
                );

        verify(userFilmService, never()).removeFilmFromFavorite(any(), any());
    }

    @Test
    @DisplayName("[E] Return 400 when filmId is negative")
    void should_return_400_when_filmId_is_negative() throws Exception {
        // Act & Assert
        mockMvc.perform(put(BASE_URL, EXISTING_USER_ID, -1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.code").value("VALIDATION_ERROR"),
                        jsonPath("$.message").value(containsString("ID must be positive"))
                );

        verify(userFilmService, never()).removeFilmFromFavorite(any(), any());
    }

    @Test
    @DisplayName("[N] Return list of favorite films sorted by release date (ASC)")
    void should_return_list_of_favorite_films_sorted_by_release_date_asc() throws Exception {
        // Arrange
        Long userId = EXISTING_USER_ID;
        List<FilmDTO> expectedResponse = List.of(film1, film2);

        when(userFilmService.getFavoriteFilmsSorted(userId, FilmSortBy.RELEASE_DATE, Sort.Direction.ASC))
                .thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/users/{userId}/favorites", userId)
                        .param("sortBy", "RELEASE_DATE")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].title").value("Inception"))
                .andExpect(jsonPath("$[1].title").value("Interstellar"));
    }

    @Test
    @DisplayName("[N] Return list of favorite films sorted by release date (DESC)")
    void should_return_list_of_favorite_films_sorted_by_release_date_desc() throws Exception {
        // Arrange
        Long userId = EXISTING_USER_ID;
        List<FilmDTO> expectedResponse = List.of(film2, film1);

        when(userFilmService.getFavoriteFilmsSorted(userId, FilmSortBy.RELEASE_DATE, Sort.Direction.DESC))
                .thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/users/{userId}/favorites", userId)
                        .param("sortBy", "RELEASE_DATE")
                        .param("direction", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].title").value("Interstellar"))
                .andExpect(jsonPath("$[1].title").value("Inception"));
    }

    @Test
    @DisplayName("[N] Return list of favorite films sorted by rating date (ASC)")
    void should_return_list_of_favorite_films_sorted_by_rating_asc() throws Exception {
        // Arrange
        Long userId = EXISTING_USER_ID;
        List<FilmDTO> expectedResponse = List.of(film1, film2);

        when(userFilmService.getFavoriteFilmsSorted(userId, FilmSortBy.RATING, Sort.Direction.ASC))
                .thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/users/{userId}/favorites", userId)
                        .param("sortBy", "RATING")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].title").value("Inception"))
                .andExpect(jsonPath("$[1].title").value("Interstellar"));
    }

    @Test
    @DisplayName("[N] Return list of favorite films sorted by rating date (DESC)")
    void should_return_list_of_favorite_films_sorted_by_rating_desc() throws Exception {
        // Arrange
        Long userId = EXISTING_USER_ID;
        List<FilmDTO> expectedResponse = List.of(film2, film1);

        when(userFilmService.getFavoriteFilmsSorted(userId, FilmSortBy.RATING, Sort.Direction.DESC))
                .thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/users/{userId}/favorites", userId)
                        .param("sortBy", "RATING")
                        .param("direction", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].title").value("Interstellar"))
                .andExpect(jsonPath("$[1].title").value("Inception"));
    }

    @Test
    @DisplayName("[E] Should return 400 Bad Request for invalid enum value")
    void should_return_bad_request_for_invalid_enum_value() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users/1/favorites")
                        .param("sortBy", "UNKNOWN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[N] Mark a film as watched")
    void should_mark_a_film_as_watched() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/users/{userId}/films/{filmId}/watched", EXISTING_USER_ID, EXISTING_FILM_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userFilmService).toggleFilmAsWatched(EXISTING_USER_ID, EXISTING_FILM_ID);
    }

    @Test
    @DisplayName("[N] Mark a film as unwatched")
    void should_mark_a_film_as_unwatched() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/users/{userId}/films/{filmId}/watched", EXISTING_USER_ID, EXISTING_FILM_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userFilmService).toggleFilmAsWatched(EXISTING_USER_ID, EXISTING_FILM_ID);
    }

    @Test
    @DisplayName("[N] Return list of watched films")
    void should_return_list_of_watched_films() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users/{userId}/films/watched", EXISTING_USER_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userFilmService).getWatchedFilms(EXISTING_USER_ID);
    }

    @Test
    @DisplayName("[N] Return list of unwatched films")
    void should_return_list_of_unwatched_films() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users/{userId}/films/unwatched", EXISTING_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userFilmService).getUnwatchedFilms(EXISTING_USER_ID);
    }
}