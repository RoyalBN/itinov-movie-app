package com.itinov.movie_api.mapper;

import com.itinov.movie_api.dto.FilmDTO;
import com.itinov.movie_api.model.Film;
import com.itinov.movie_api.model.UserFilmRelation;
import org.springframework.stereotype.Component;

@Component
public class UserFilmMapper {

    public FilmDTO mapToFilmDTO(UserFilmRelation relation) {
        Film film = relation.getFilm();

        return FilmDTO.builder()
                .id(film.getId())
                .title(film.getTitle())
                .rating(film.getRating())
                .releaseDate(film.getReleaseDate())
                .posterUrl(film.getPosterUrl())
                .isFavorite(relation.isFavorite())
                .hasBeenWatched(relation.isHasBeenWatched())
                .build();
    }
}
