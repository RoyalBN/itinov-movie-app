package com.itinov.movie_api.service;

import com.itinov.movie_api.dto.FilmDTO;
import com.itinov.movie_api.model.Film;
import com.itinov.movie_api.model.FilmSortBy;
import com.itinov.movie_api.model.User;
import com.itinov.movie_api.model.UserFilmRelation;
import com.itinov.movie_api.repository.FilmRepository;
import com.itinov.movie_api.repository.UserFilmRelationRepository;
import com.itinov.movie_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserFilmServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilmRepository filmRepository;

    @Mock
    private UserFilmRelationRepository relationRepository;

    private UserFilmService userFilmService;

    private User user;
    private Film film;
    private UserFilmRelation userFilmRelation;

    @BeforeEach
    void setUp() {
        userFilmService = new UserFilmService(userRepository, filmRepository, relationRepository);
        user = User.builder().id(4L).username("alice_movie_lover").build();
        film = Film.builder().id(1L).title("Inception").rating(8.5).build();

        userFilmRelation = UserFilmRelation.builder()
                .id(1L)
                .user(user)
                .film(film)
                .hasBeenWatched(true)
                .isFavorite(false)
                .build();
    }

    @Test
    @DisplayName("[N] Add a film to favorite when relation does not exist")
    void should_add_film_to_favorite_when_relation_does_not_exist() {
        // Arrange
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(filmRepository.findById(film.getId())).thenReturn(Optional.of(film));
        when(relationRepository.findByUserAndFilm(user, film)).thenReturn(Optional.empty());
        when(relationRepository.save(any(UserFilmRelation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        userFilmService.addFilmToFavorite(user.getId(), film.getId());

        // Assert
        ArgumentCaptor<UserFilmRelation> relationCaptor = ArgumentCaptor.forClass(UserFilmRelation.class);
        verify(relationRepository).save(relationCaptor.capture());
        
        UserFilmRelation savedRelation = relationCaptor.getValue();
        assertThat(savedRelation.getUser()).isEqualTo(user);
        assertThat(savedRelation.getFilm()).isEqualTo(film);
        assertThat(savedRelation.isFavorite()).isTrue();
        assertThat(savedRelation.isHasBeenWatched()).isFalse();
    }

    @Test
    @DisplayName("[N] Toggle favorite status for existing relation")
    void should_toggle_favorite_status_for_existing_relation() {
        // Arrange
        UserFilmRelation existingRelation = UserFilmRelation.builder()
                .id(1L)
                .user(user)
                .film(film)
                .isFavorite(true)  // Déjà en favori
                .hasBeenWatched(true)
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(filmRepository.findById(film.getId())).thenReturn(Optional.of(film));
        when(relationRepository.findByUserAndFilm(user, film)).thenReturn(Optional.of(existingRelation));
        when(relationRepository.save(any(UserFilmRelation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        userFilmService.addFilmToFavorite(user.getId(), film.getId());

        // Assert
        ArgumentCaptor<UserFilmRelation> relationCaptor = ArgumentCaptor.forClass(UserFilmRelation.class);
        verify(relationRepository).save(relationCaptor.capture());
        
        UserFilmRelation savedRelation = relationCaptor.getValue();
        assertThat(savedRelation.isFavorite()).isFalse();  // Devrait être basculé à false
        assertThat(savedRelation.isHasBeenWatched()).isTrue();  // Ne devrait pas changer
    }

    @Test
    @DisplayName("[E] Cannot add to favorite when user does not exist")
    void should_throw_exception_when_user_does_not_exist() {
        // Arrange
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        // Act
        Throwable thrown = catchThrowable(() -> userFilmService.addFilmToFavorite(user.getId(), film.getId()));

        // Assert
        assertThat(thrown)
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("User not found with id: " + user.getId());
        verify(relationRepository, never()).save(any());
    }

    @Test
    @DisplayName("[E] Cannot add to favorite when film does not exist")
    void should_throw_exception_when_film_does_not_exist() {
        // Arrange
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(filmRepository.findById(film.getId())).thenReturn(Optional.empty());

        // Act
        Throwable thrown = catchThrowable(() -> userFilmService.addFilmToFavorite(user.getId(), film.getId()));

        // Assert
        assertThat(thrown)
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Film not found with id: " + film.getId());
        verify(relationRepository, never()).save(any());
    }

    @Test
    @DisplayName("[N] Throw IllegalArgumentException if userId or filmId is null")
    void should_throw_exception_if_user_id_or_film_id_is_null() throws Exception {
        // Arrange
        Long userId = null;
        Long filmId = null;

        // Act
        Throwable thrown = catchThrowable(() -> userFilmService.addFilmToFavorite(userId, filmId));

        // Assert
        assertThat(thrown)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("User ID and film ID must not be null");
    }

    @Test
    @DisplayName("[N] Remove a film from favorite")
    void should_remove_film_from_favorite() {
        // Arrange
        UserFilmRelation relation = UserFilmRelation.builder()
                .user(user)
                .film(film)
                .isFavorite(true)
                .hasBeenWatched(false)
                .build();

        when(userRepository.findById(4L)).thenReturn(Optional.of(user));
        when(filmRepository.findById(1L)).thenReturn(Optional.of(film));
        when(relationRepository.findByUserAndFilm(user, film)).thenReturn(Optional.of(relation));

        // Act
        userFilmService.removeFilmFromFavorite(user.getId(), film.getId());

        // Assert
        assertThat(relation.isFavorite()).isFalse();
        verify(relationRepository, times(1)).save(relation);
    }

    @Test
    @DisplayName("[N] Throw exception when relation not found")
    void should_throw_exception_when_relation_not_found() {
        // Arrange
        when(userRepository.findById(4L)).thenReturn(Optional.of(user));
        when(filmRepository.findById(1L)).thenReturn(Optional.of(film));
        when(relationRepository.findByUserAndFilm(user, film)).thenReturn(Optional.empty());

        // Act & Assert
        Throwable thrown = catchThrowable(() -> userFilmService.removeFilmFromFavorite(user.getId(), film.getId()));

        assertThat(thrown)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Relation not found for user ID: " + user.getId() + " and film ID: " + film.getId());

        verify(relationRepository, never()).save(any());
    }

    @Test
    @DisplayName("[E] Throw exception when user or film is null")
    void should_throw_an_exception_when_user_or_film_is_null() {
        // Arrange
        Long userId = null;
        Long filmId = null;

        // Act
        Throwable thrown = catchThrowable(() -> userFilmService.removeFilmFromFavorite(userId, filmId));

        // Assert
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID and film ID must not be null");

        verify(relationRepository, never()).save(any());
    }

    @Test
    @DisplayName("[N] Return list of favorite films sorted by release date (ASC)")
    void should_return_list_of_favorite_films_sorted_by_release_date_asc() {
        // Arrange
        User user = User.builder().id(1L).username("Alice").email("email@example.com").build();
        Film film1 = Film.builder()
                        .title("Film A")
                        .rating(8.2)
                        .releaseDate(LocalDate.of(  2019, 2, 10))
                        .posterUrl("url1")
                        .build();

        Film film2 = Film.builder()
                        .title("Film B")
                        .rating(9.0)
                        .releaseDate(LocalDate.of(2022, 5, 23))
                        .posterUrl("url2")
                        .build();

        UserFilmRelation rel1 = UserFilmRelation.builder().user(user).film(film1).isFavorite(true).hasBeenWatched(false).build();
        UserFilmRelation rel2 = UserFilmRelation.builder().user(user).film(film2).isFavorite(true).hasBeenWatched(true).build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(relationRepository.findByUserAndIsFavoriteTrueOrderByFilm_ReleaseDateAsc(user)).thenReturn(List.of(rel1, rel2));

        // Act
        List<FilmDTO> result = userFilmService.getFavoriteFilmsSorted(user.getId(), FilmSortBy.RELEASE_DATE, Sort.Direction.ASC);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Film A");
        assertThat(result.get(1).getTitle()).isEqualTo("Film B");
    }

    @DisplayName("[N] Return list of favorite films sorted by release date (DESC)")
    void should_return_list_of_favorite_films_sorted_by_release_date_desc() {
        // Arrange
        User user = User.builder().id(1L).username("Alice").email("email@example.com").build();
        Film film1 = Film.builder()
                .title("Film A")
                .rating(8.2)
                .releaseDate(LocalDate.of(  2019, 2, 10))
                .posterUrl("url1")
                .build();

        Film film2 = Film.builder()
                .title("Film B")
                .rating(9.0)
                .releaseDate(LocalDate.of(2022, 5, 23))
                .posterUrl("url2")
                .build();

        UserFilmRelation rel1 = UserFilmRelation.builder().user(user).film(film1).isFavorite(true).hasBeenWatched(false).build();
        UserFilmRelation rel2 = UserFilmRelation.builder().user(user).film(film2).isFavorite(true).hasBeenWatched(true).build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(relationRepository.findByUserAndIsFavoriteTrueOrderByFilm_ReleaseDateDesc(user)).thenReturn(List.of(rel2, rel1));

        // Act
        List<FilmDTO> result = userFilmService.getFavoriteFilmsSorted(user.getId(), FilmSortBy.RELEASE_DATE, Sort.Direction.DESC);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Film B");
        assertThat(result.get(1).getTitle()).isEqualTo("Film A");
    }

    @Test
    @DisplayName("[N] Return list of favorite films sorted by rating (ASC)")
    void should_return_list_of_favorite_films_sorted_by_rating_asc() {
        // Arrange
        User user = User.builder().id(1L).username("Alice").email("email@example.com").build();
        Film film1 = Film.builder()
                .title("Film A")
                .rating(8.2)
                .releaseDate(LocalDate.of(  2019, 2, 10))
                .posterUrl("url1")
                .build();

        Film film2 = Film.builder()
                .title("Film B")
                .rating(9.0)
                .releaseDate(LocalDate.of(2022, 5, 23))
                .posterUrl("url2")
                .build();

        UserFilmRelation rel1 = UserFilmRelation.builder().user(user).film(film1).isFavorite(true).hasBeenWatched(false).build();
        UserFilmRelation rel2 = UserFilmRelation.builder().user(user).film(film2).isFavorite(true).hasBeenWatched(true).build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(relationRepository.findByUserAndIsFavoriteTrueOrderByFilm_RatingAsc(user)).thenReturn(List.of(rel1, rel2));

        // Act
        List<FilmDTO> result = userFilmService.getFavoriteFilmsSorted(user.getId(), FilmSortBy.RATING, Sort.Direction.ASC);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Film A");
        assertThat(result.get(1).getTitle()).isEqualTo("Film B");
    }

    @Test
    @DisplayName("[N] Return list of favorite films sorted by rating (DESC)")
    void should_return_list_of_favorite_films_sorted_by_rating_desc() {
        // Arrange
        User user = User.builder().id(1L).username("Alice").email("email@example.com").build();
        Film film1 = Film.builder()
                .title("Film A")
                .rating(8.2)
                .releaseDate(LocalDate.of(  2019, 2, 10))
                .posterUrl("url1")
                .build();

        Film film2 = Film.builder()
                .title("Film B")
                .rating(9.0)
                .releaseDate(LocalDate.of(2022, 5, 23))
                .posterUrl("url2")
                .build();

        UserFilmRelation rel1 = UserFilmRelation.builder().user(user).film(film1).isFavorite(true).hasBeenWatched(false).build();
        UserFilmRelation rel2 = UserFilmRelation.builder().user(user).film(film2).isFavorite(true).hasBeenWatched(true).build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(relationRepository.findByUserAndIsFavoriteTrueOrderByFilm_RatingDesc(user)).thenReturn(List.of(rel2, rel1));

        // Act
        List<FilmDTO> result = userFilmService.getFavoriteFilmsSorted(user.getId(), FilmSortBy.RATING, Sort.Direction.DESC);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Film B");
        assertThat(result.get(1).getTitle()).isEqualTo("Film A");
    }

    @DisplayName("[N] Mark a film as watched")
    @Test
    void should_mark_a_film_as_watched() {
        // Arrange
        Long userId = 1L;
        Long filmId = 10L;
        User user = User.builder().id(userId).build();
        Film film = Film.builder().id(filmId).build();

        UserFilmRelation relation = UserFilmRelation.builder()
                .user(user)
                .film(film)
                .hasBeenWatched(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(filmRepository.findById(filmId)).thenReturn(Optional.of(film));
        when(relationRepository.findByUserAndFilm(user, film)).thenReturn(Optional.of(relation));

        // Act
        userFilmService.markFilmAsWatched(userId, filmId);

        // Assert
        assertThat(relation.isHasBeenWatched()).isTrue();
        assertThat(relation.getWatchedAt()).isNotNull();
        verify(relationRepository, times(1)).save(relation);
    }

    @DisplayName("[N] Mark a film as unwatched")
    @Test
    void should_mark_a_film_as_unwatched() {
        // Arrange
        Long userId = 1L;
        Long filmId = 10L;
        User user = User.builder().id(userId).build();
        Film film = Film.builder().id(filmId).build();

        UserFilmRelation relation = UserFilmRelation.builder()
                .user(user)
                .film(film)
                .hasBeenWatched(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(filmRepository.findById(filmId)).thenReturn(Optional.of(film));
        when(relationRepository.findByUserAndFilm(user, film)).thenReturn(Optional.of(relation));

        // Act
        userFilmService.markFilmAsWatched(userId, filmId);

        // Assert
        assertThat(relation.isHasBeenWatched()).isFalse();
        assertThat(relation.getWatchedAt()).isNull();
        verify(relationRepository, times(1)).save(relation);
    }

    // [N] Return list of watched films

    // [N] Return list of unwatched films

}
