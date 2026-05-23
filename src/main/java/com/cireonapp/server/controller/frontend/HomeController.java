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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class HomeController {
    @GetMapping("/")
    String home(Model model, HttpServletRequest request) {
        Optional<Cookie> cookie = CookieHelper.getAuthCookie(request);
        if (cookie.isEmpty()) {
            return "redirect:/login";
        }

        Optional<Session> session = SessionManager.get(cookie.get().getValue());
        if (session.isEmpty()) {
            return "redirect:/login";
        }

        Optional<User> user = UserManager.get(session.get().getUsername());
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
            if(carousel.getPosters().size() >= 15) break;
            carousel.addPoster(CarouselPoster.createCarouselPosterFromMovie(movie));
        }
        return carousel;
    }


    private Billboard billboardHandler(User user) {


        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
        String formattedTime = now.format(formatter);

        long seed = user.getUsername().hashCode() + formattedTime.hashCode();

        Random random = new Random(seed);
        Set<Movie> allMovies = MovieManager.getAll();
        if (allMovies.isEmpty()) {
            return null;
        }
        int randomNumber = random.nextInt(allMovies.size());
        Optional<Movie> movieBillboard = allMovies.stream()
                .skip(randomNumber)
                .findFirst();


        Billboard billboard = new Billboard();


        if (movieBillboard.isPresent()) {
            Movie movie = movieBillboard.get();
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

        return null;
    }

}
