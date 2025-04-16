package com.itinov.movie_api.repository;

import com.itinov.movie_api.model.Film;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilmRepository extends JpaRepository<Film, Long> {
}
