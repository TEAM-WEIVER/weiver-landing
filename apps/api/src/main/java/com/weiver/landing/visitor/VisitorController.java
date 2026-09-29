package com.weiver.landing.visitor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/visitors")
public class VisitorController {

    private static final String COOKIE_NAME = "visitor_id";
    private static final int COOKIE_MAX_AGE = 15_552_000; // 180 days

    private final VisitorService visitorService;

    public VisitorController(VisitorService visitorService) {
        this.visitorService = visitorService;
    }

    @PostMapping("/bootstrap")
    public ResponseEntity<Map<String, String>> bootstrap(
            @Valid @RequestBody BootstrapRequest req,
            HttpServletRequest httpReq,
            HttpServletResponse httpRes) {

        UUID cookieId = extractCookie(httpReq);
        UUID visitorId = visitorService.bootstrap(cookieId, req);

        Cookie cookie = new Cookie(COOKIE_NAME, visitorId.toString());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(COOKIE_MAX_AGE);
        cookie.setAttribute("SameSite", "Lax");
        httpRes.addCookie(cookie);

        return ResponseEntity.ok(Map.of("visitorId", visitorId.toString()));
    }

    private UUID extractCookie(HttpServletRequest req) {
        if (req.getCookies() == null) return null;
        return Arrays.stream(req.getCookies())
            .filter(c -> COOKIE_NAME.equals(c.getName()))
            .findFirst()
            .map(c -> {
                try { return UUID.fromString(c.getValue()); }
                catch (IllegalArgumentException e) { return null; }
            })
            .orElse(null);
    }
}
