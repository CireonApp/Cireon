package com.cireonapp.server.controller.frontend;

import com.cireonapp.server.domain.media.common.UniversalMediaForPlayer;
import com.cireonapp.server.domain.media.movie.Movie;
import com.cireonapp.server.domain.media.movie.MovieManager;
import com.cireonapp.server.domain.media.source.SourceType;
import com.cireonapp.server.domain.user.User;
import com.cireonapp.server.util.CookieHelper;
import com.cireonapp.server.util.GuessMediaType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class MediaController {
    @GetMapping("/media/player")
    public String mediaPlayer(Model model, @RequestParam("id") String id, HttpServletRequest request) {
        // only logged-in users can reach this page.
        Optional<User> user = CookieHelper.getUserFromSessionCookie(request);
        if (user.isEmpty()) {
            return "redirect:/login";
        }
        SourceType mediaType = GuessMediaType.guess(id);

        switch (mediaType) {
            case MOVIE:
                Movie movie = MovieManager.get(id);
                if (movie == null) {
                    return "redirect:/";
                }
                // Check if movie has files (might be temporarily empty during rename)
                if (movie.getFiles() == null || movie.getFiles().isEmpty()) {

                    return "redirect:/";
                }
                UniversalMediaForPlayer universalMediaForPlayer = new UniversalMediaForPlayer();
                universalMediaForPlayer.type = mediaType;
                universalMediaForPlayer.title = movie.getMetadata().getTitle();
                universalMediaForPlayer.id = movie.getId();
                universalMediaForPlayer.defaultFile = movie.getFiles().getFirst().getHash();
                model.addAttribute("media", universalMediaForPlayer);
                break;
            case TV_SHOW:
                break;

            // on accept shows and movies. if the media type is unknown(probably by not finding content), redirect to home page.
            case null, default:
                return "redirect:/";
        }

        return "media/player/index";

    }
}
