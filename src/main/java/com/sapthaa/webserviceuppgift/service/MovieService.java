package com.sapthaa.webserviceuppgift.service;
import com.sapthaa.webserviceuppgift.model.Movie;
import com.sapthaa.webserviceuppgift.movieRepository.MovieRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.Optional;

@Service
public class MovieService {

    private final WebClient movieWebClient;
    private final MovieRepository movieRepository;

    @Value("${api.key}")
    private String apiKey;

    public MovieService(WebClient.Builder movieWebClient, MovieRepository movieRepository) {
        this.movieWebClient = movieWebClient.baseUrl("https://api.themoviedb.org/3/movie/").build();
        this.movieRepository = movieRepository;
    }

    // Get
    public ResponseEntity<List<Movie>> getAllMovies() {
        List<Movie> movies = movieRepository.findAll();
        if (movies.isEmpty()) {
            return new ResponseEntity<>(movies, HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(movies, HttpStatus.OK);
    }

    // Get
    public ResponseEntity<Movie> getMovieById(Long id) {
        Movie movie = movieWebClient.get()
                .uri("/{id}?api_key={apiKey}", id, apiKey)
                .retrieve()
                .bodyToMono(Movie.class)
                .block();

        if(movie == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(movie, HttpStatus.OK);
    }

    // Overview (min databas)
    public ResponseEntity<String> getMovieOverviewById(Long id) {
        Optional<Movie> movie = movieRepository.findById(id);
        if (movie.isPresent()) {
            return new ResponseEntity<>(movie.get().getOverview(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Release date (min databas)
    public ResponseEntity<String> getMovieReleaseDateById(Long id) {
        Optional<Movie> movie = movieRepository.findById(id);
        if (movie.isPresent()) {
            return new ResponseEntity<>(movie.get().getRelease_date(), HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Search movie (min databas)
    public ResponseEntity<List<Movie>> searchMovieByTitle(String title) {
        List<Movie> movie = movieRepository.findByTitleContainingIgnoreCase(title);

        if (movie.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(movie, HttpStatus.OK);
    }

    // Post
    public ResponseEntity<Movie> getAndSaveMovieById(Long id){
        Movie movie = movieWebClient.get()
                .uri("/{id}?api_key={apiKey}", id, apiKey)
                .retrieve()
                .bodyToMono(Movie.class)
                .block();

        if(movie == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Movie savedMovie = movieRepository.save(movie);
        return new ResponseEntity<>(savedMovie, HttpStatus.OK);
    }

    // Put
    public ResponseEntity<Movie> updateMovieById(Long id, Movie movie){
        Optional<Movie> findMovie = movieRepository.findById(id);

        if(findMovie.isPresent()) {
            Movie updateMovie = findMovie.get();
            updateMovie.setTitle(movie.getTitle());
            updateMovie.setOverview(movie.getOverview());

            movieRepository.save(updateMovie);
            return new ResponseEntity<>(updateMovie, HttpStatus.OK);

        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);

    }

    // Delete
    public ResponseEntity<String> deleteMovieById(Long id) {
        if(movieRepository.existsById(id)) {
            movieRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);

    }


}
