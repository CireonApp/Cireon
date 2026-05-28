package com.cireonapp.server.controller.frontend;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DocsController {

    @GetMapping("/api/docs")
    public String getDocumentation() {
        // Keep browser URL as /api/docs while rendering the springdoc UI page.
        return "forward:/api/docs/swagger-ui/index.html";
    }

    @GetMapping("/api/{asset:swagger-ui\\.css|swagger-ui-bundle\\.js|swagger-ui-standalone-preset\\.js|swagger-initializer\\.js|index\\.css|favicon-32x32\\.png|favicon-16x16\\.png}")
    public String redirectSwaggerAsset(@PathVariable String asset) {
        return "forward:/api/docs/swagger-ui/" + asset;
    }
}
