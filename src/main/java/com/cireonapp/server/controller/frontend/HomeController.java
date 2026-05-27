package com.cireonapp.server.controller.frontend;

import com.cireonapp.server.domain.media.common.*;
import com.cireonapp.server.domain.media.movie.Movie;
import com.cireonapp.server.domain.media.movie.MovieManager;
import com.cireonapp.server.domain.media.source.SourceType;
import com.cireonapp.server.domain.session.Session;
import com.cireonapp.server.domain.session.SessionManager;
import com.cireonapp.server.domain.user.User;
import com.cireonapp.server.domain.user.UserManager;
import com.cireonapp.server.util.CookieHelper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.repository.Cursor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;

@Controller
public class HomeController {
    @GetMapping("/")
    String home(Model model, HttpServletRequest request) {
        Optional<User> user = CookieHelper.getUserFromSessionCookie(request);

        if (user.isEmpty()) {
            return "redirect:/login";
        }

        Billboard billboard = billboardHandler(user.get());
        model.addAttribute("mediaBillboard", billboard);

        ArrayList<Carousel> carousels = new ArrayList<>();
        if (!MovieManager.getAll().isEmpty()) {
            carousels.add(getRecentlyAddedMoviesCarousel());
            carousels.add(getNewestReleasesMovieCarousel());
        }
        model.addAttribute("carousels", carousels);

        model.addAttribute("user", user.get());

        model.addAttribute("theme", UserManager.getThemeLabel(user.get()).label);
        return "home/index";
    }

    private Carousel getRecentlyAddedMoviesCarousel() {
        Carousel carousel = new Carousel();
        carousel.setTitle("Recently Added Movies");
        carousel.setDescription("Latest movies in the catalogue");
        carousel.setSourceType(SourceType.MOVIE);
        List<Movie> movies = MovieManager.getByCreationDate(SortOrder.Descending, 15);
        for (Movie movie : movies) {
            carousel.addPoster(CarouselPoster.createCarouselPosterFromMovie(movie));
        }
        return carousel;
    }

    private Carousel getNewestReleasesMovieCarousel() {
        Carousel carousel = new Carousel();
        carousel.setTitle("Newest Releases");
        carousel.setDescription("Latest movies by release date");
        carousel.setSourceType(SourceType.MOVIE);
        List<Movie> movies = MovieManager.getByReleaseDate(SortOrder.Descending, 15);
        for (Movie movie : movies) {
            if (carousel.getPosters().size() >= 15) break;
            carousel.addPoster(CarouselPoster.createCarouselPosterFromMovie(movie));
        }
        return carousel;
    }


    private Billboard billboardHandler(User user) {
        Optional<Movie> movieBillboard = pickRandomMovieForUser(user);
        if (movieBillboard.isEmpty()) {
            return null; // or better: return Optional<Billboard> from billboardHandler
        }
        Movie movie = movieBillboard.get();

        Billboard billboard = new Billboard();
        billboard.id = movie.getHash();
        billboard.sourceType = "movie";
        billboard.name = movie.getMetadata().getTitle();
        billboard.isAdult = movie.getMetadata().isAdult();
        billboard.releaseDate = movie.getMetadata().getReleaseDate();
        billboard.genres = Genres.GenresToStringArray(movie.getMetadata().getGenres());
        billboard.runtime = movie.getMetadata().getRuntime();
        billboard.artwork = movie.getMetadata().getArtworks();
        return billboard;

    }

    private Optional<Movie> pickRandomMovieForUser(User user) {
        long seed = Objects.hash(
                user.getUsername(),
                LocalDateTime.now().truncatedTo(ChronoUnit.HOURS)
        );

        SplittableRandom rng = new SplittableRandom(seed);
        Cursor<Movie> movies = MovieManager.getAll();

        Movie chosen = null;
        long seen = 0;

        for (Movie movie : movies) {
            seen++;
            // Replace current choice with probability 1/seen
            if (rng.nextLong(seen) == 0) {
                chosen = movie;
            }
        }

        return Optional.ofNullable(chosen);
    }

}
