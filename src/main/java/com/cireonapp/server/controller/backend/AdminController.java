package com.cireonapp.server.controller.backend;

import com.cireonapp.server.domain.session.Session;
import com.cireonapp.server.domain.session.SessionManager;
import com.cireonapp.server.domain.user.User;
import com.cireonapp.server.dto.AdminLoginToUserDto;
import com.cireonapp.server.dto.CommonResponseDto;
import com.cireonapp.server.dto.SuccessResponseDto;
import com.cireonapp.server.util.CookieHelper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static com.cireonapp.server.controller.backend.AuthenticationController.AUTH_COOKIE_NAME;
import static com.cireonapp.server.domain.session.SessionManager.SESSION_EXPIRATION_TIME_SECONDS;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @PostMapping("/login")
    public ResponseEntity<?> login(HttpServletRequest request, HttpServletResponse response, @Valid @RequestBody AdminLoginToUserDto userCred) {
        Optional<User> userOpt = CookieHelper.getUserFromSessionCookie(request);
        if (userOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CommonResponseDto.Error.NOT_LOGGED_IN);

        User user = userOpt.get();

        if (!user.isAdministrator())
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(CommonResponseDto.Error.INSUFFICIENT_PERMISSIONS);

        Optional<Session> newSession = SessionManager.create(userCred.username, "");
        if (newSession.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(CommonResponseDto.Error.USER_NOT_FOUND);



        Optional<Cookie> cookie = CookieHelper.getAuthCookie(request);

        if (cookie.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CommonResponseDto.Error.NOT_LOGGED_IN);

        SessionManager.delete(cookie.get().getValue());




        Cookie newCookie = new Cookie(AUTH_COOKIE_NAME, newSession.get().getToken());
        newCookie.setMaxAge((int) SESSION_EXPIRATION_TIME_SECONDS);
        newCookie.setSecure(false); // Does not matter since it's a local app to run on a home server...
        newCookie.setHttpOnly(true);
        newCookie.setPath("/");
        newCookie.setValue(newSession.get().getToken());
        response.addCookie(newCookie);

        return ResponseEntity.ok(new SuccessResponseDto("Successfully logged in to user " + user.getUsername() + " as Administrator."));

    }



}
