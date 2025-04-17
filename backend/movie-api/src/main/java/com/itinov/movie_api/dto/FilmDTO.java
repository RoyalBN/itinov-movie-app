package com.itinov.movie_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilmDTO {
    private Long id;
    private String title;
    private double rating;
    private LocalDate releaseDate;
    private String posterUrl;
    private boolean isFavorite;
    private boolean hasBeenWatched;
}
