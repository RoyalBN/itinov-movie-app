package com.itinov.movie_api.repository;

import com.itinov.movie_api.dto.FilmDTO;
import com.itinov.movie_api.model.Film;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FilmRepository extends JpaRepository<Film, Long> {
}
