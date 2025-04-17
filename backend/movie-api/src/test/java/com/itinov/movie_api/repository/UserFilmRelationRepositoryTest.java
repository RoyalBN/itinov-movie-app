package com.itinov.movie_api.repository;

import com.itinov.movie_api.model.Film;
import com.itinov.movie_api.model.User;
import com.itinov.movie_api.model.UserFilmRelation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserFilmRelationRepositoryTest {

    private User user;
    private Film film1;
    private Film film2;

    private UserFilmRelation relation;

    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserFilmRelationRepository repository;

    private UserFilmRelation persistRelation(boolean isFavorite, boolean hasBeenWatched, Film filmToPersist) {
        UserFilmRelation relation = UserFilmRelation.builder()
                .user(user)
                .film(filmToPersist)
                .isFavorite(isFavorite)
                .hasBeenWatched(hasBeenWatched)
                .build();
        entityManager.persist(relation);
        entityManager.flush();

        return relation;
    }

    @BeforeEach
    void setUp() {
        user = User.builder().username("Peter").build();

        film1 = Film.builder()
                .title("Film A")
                .rating(8.2)
                .releaseDate(LocalDate.of(  2019, 2, 10))
                .posterUrl("url1")
                .build();

        film2 = Film.builder()
                .title("Film B")
                .rating(9.0)
                .releaseDate(LocalDate.of(2022, 5, 23))
                .posterUrl("url2")
                .build();

        entityManager.persist(user);
        entityManager.persist(film1);
        entityManager.persist(film2);
        entityManager.flush();
    }

    @Test
    @DisplayName("[N] Find relation by user and film")
    void should_find_relation_by_user_and_film() {
        // Arrange
        persistRelation(true, false, film1);

        // Act
        Optional<UserFilmRelation> foundRelation = repository.findByUserAndFilm(user, film1);

        // Assert
        assertThat(foundRelation).isPresent();
        assertThat(foundRelation.get().isFavorite()).isTrue();
        assertThat(foundRelation.get().isHasBeenWatched()).isFalse();
    }

    @Test
    @DisplayName("[N] Return empty when relation does not exist")
    void should_return_empty_when_relation_does_not_exist() {
        // Act
        Optional<UserFilmRelation> foundRelation = repository.findByUserAndFilm(user, film1);

        // Assert
        assertThat(foundRelation).isEmpty();
    }

    @Test
    @DisplayName("[N] Remove a film from favorite")
    void should_remove_film_from_favorite() {
        // Arrange
        persistRelation(true, false, film1);

        // Act
        Optional<UserFilmRelation> foundRelation = repository.findByUserAndFilm(user, film1);
        foundRelation.get().setFavorite(false);
        repository.save(foundRelation.get());
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<UserFilmRelation> updatedRelation = repository.findByUserAndFilm(user, film1);
        assertThat(updatedRelation.get().isFavorite()).isFalse();
        assertThat(updatedRelation.get().isHasBeenWatched()).isFalse();
    }

    @Test
    @DisplayName("[N] Get favorite films sorted by release date (ASC)")
    void should_return_list_of_favorite_films_sorted_by_release_date_asc() {
        // Arrange
        persistRelation(true, false, film1);
        persistRelation(true, false, film2);

        // Act
        List<UserFilmRelation> result = repository.findByUserAndIsFavoriteTrueOrderByFilm_ReleaseDateAsc(user);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFilm().getTitle()).isEqualTo("Film A");
        assertThat(result.get(1).getFilm().getTitle()).isEqualTo("Film B");
    }

    @Test
    @DisplayName("[N] Get favorite films sorted by release date (DESC)")
    void should_return_list_of_favorite_films_sorted_by_release_date_desc() {
        // Arrange
        persistRelation(true, false, film1);
        persistRelation(true, false, film2);

        // Act
        List<UserFilmRelation> result = repository.findByUserAndIsFavoriteTrueOrderByFilm_ReleaseDateDesc(user);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFilm().getTitle()).isEqualTo("Film B");
        assertThat(result.get(1).getFilm().getTitle()).isEqualTo("Film A");
    }

    @Test
    @DisplayName("[N] Get favorite films sorted by rating (ASC)")
    void should_return_list_of_favorite_films_sorted_by_rating_asc() {
        // Arrange
        persistRelation(true, false, film1);
        persistRelation(true, false, film2);

        // Act
        List<UserFilmRelation> result = repository.findByUserAndIsFavoriteTrueOrderByFilm_RatingAsc(user);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFilm().getTitle()).isEqualTo("Film A");
        assertThat(result.get(1).getFilm().getTitle()).isEqualTo("Film B");
    }

    @Test
    @DisplayName("[N] Get favorite films sorted by rating (DESC)")
    void should_return_list_of_favorite_films_sorted_by_rating_desc() {
        // Arrange
        persistRelation(true, false, film1);
        persistRelation(true, false, film2);

        // Act
        List<UserFilmRelation> result = repository.findByUserAndIsFavoriteTrueOrderByFilm_RatingDesc(user);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFilm().getTitle()).isEqualTo("Film B");
        assertThat(result.get(1).getFilm().getTitle()).isEqualTo("Film A");
    }

    @Test
    @DisplayName("[N] Mark a film as watched")
    void should_mark_a_film_as_watched() {
        // Arrange
        persistRelation(true, false, film1);

        // Act
        Optional<UserFilmRelation> foundRelation = repository.findByUserAndFilm(user, film1);
        foundRelation.get().setHasBeenWatched(true);
        foundRelation.get().setWatchedAt(LocalDateTime.now());
        repository.save(foundRelation.get());
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<UserFilmRelation> updatedRelation = repository.findByUserAndFilm(user, film1);
        assertThat(updatedRelation.get().isFavorite()).isTrue();
        assertThat(updatedRelation.get().isHasBeenWatched()).isTrue();
        assertThat(updatedRelation.get().getWatchedAt()).isNotNull();
    }

    @Test
    @DisplayName("[N] Mark a film as unwatched")
    void should_mark_a_film_as_unwatched() {
        // Arrange
        persistRelation(true, true, film1);

        // Act
        Optional<UserFilmRelation> foundRelation = repository.findByUserAndFilm(user, film1);
        foundRelation.get().setHasBeenWatched(false);
        foundRelation.get().setWatchedAt(null);
        repository.save(foundRelation.get());
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<UserFilmRelation> updatedRelation = repository.findByUserAndFilm(user, film1);
        assertThat(updatedRelation.get().isFavorite()).isTrue();
        assertThat(updatedRelation.get().isHasBeenWatched()).isFalse();
        assertThat(updatedRelation.get().getWatchedAt()).isNull();
    }

    @Test
    @DisplayName("[N] Return list of watched film")
    void should_return_list_of_watched_film() {
        // Arrange
        persistRelation(true, true, film1);
        persistRelation(true, false, film2);

        // Act
        List<UserFilmRelation> foundRelation = repository.findByUserAndHasBeenWatchedTrue(user);

        // Assert
        assertThat(foundRelation).hasSize(1);
        assertThat(foundRelation.get(0).getFilm().getTitle()).isEqualTo("Film A");
        assertThat(foundRelation.get(0).isHasBeenWatched()).isTrue();
    }

    @Test
    @DisplayName("[N] Return list of unwatched film")
    void should_return_list_of_unwatched_film() {
        // Arrange
        persistRelation(true, true, film1);
        persistRelation(true, false, film2);

        // Act
        List<UserFilmRelation> foundRelation = repository.findByUserAndHasBeenWatchedFalse(user);

        // Assert
        assertThat(foundRelation).hasSize(1);
        assertThat(foundRelation.get(0).getFilm().getTitle()).isEqualTo("Film B");
        assertThat(foundRelation.get(0).isHasBeenWatched()).isFalse();
    }

}