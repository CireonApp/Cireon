package com.cireonapp.server.controller.frontend;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class ManifestController {

    @GetMapping(value = "/assets/favicon/site.webmanifest", produces = "application/manifest+json")
    public ResponseEntity<Resource> getWebManifest() throws IOException {
        Resource resource = new ClassPathResource("static/assets/favicon/site.webmanifest");
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("application/manifest+json"))
                .contentLength(resource.contentLength())
                .body(resource);
    }
}

