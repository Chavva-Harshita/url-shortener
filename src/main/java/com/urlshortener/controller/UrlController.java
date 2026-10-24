
package com.urlshortener.controller;

import com.urlshortener.entity.Url;
import com.urlshortener.service.UrlService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/urls")
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    // 1. Create a short URL with optional expiration
    @PostMapping
    public ResponseEntity<Map<String, String>> shortenUrl(
            @RequestBody Map<String, String> request) {

        String originalUrl = request.get("originalUrl");

        Url savedUrl = urlService.shortenUrl(originalUrl);

        String expiresAtValue = request.get("expiresAt");

        if (expiresAtValue != null && !expiresAtValue.isBlank()) {

            LocalDateTime expiresAt;

            try {
                expiresAt = LocalDateTime.parse(expiresAtValue);
            } catch (Exception ex) {
                throw new IllegalArgumentException(
                        "Invalid expiration format. Use yyyy-MM-ddTHH:mm:ss"
                );
            }

            if (!expiresAt.isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException(
                        "Expiration time must be in the future"
                );
            }

            savedUrl.setExpiresAt(expiresAt);
            savedUrl = urlService.saveUrl(savedUrl);
        }

        Map<String, String> response = Map.of(
                "originalUrl", savedUrl.getOriginalUrl(),
                "shortCode", savedUrl.getShortCode(),
                "shortUrl", "http://localhost:8080/" + savedUrl.getShortCode()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. Redirect to the original URL
      // 3. Get URL statistics without incrementing click count
    @GetMapping("/{shortCode}/stats")
    public ResponseEntity<Map<String, Object>> getUrlStats(
            @PathVariable String shortCode) {

        Url url = urlService.getUrlByShortCode(shortCode);

        Map<String, Object> stats = Map.of(
                "originalUrl", url.getOriginalUrl(),
                "shortCode", url.getShortCode(),
                "clickCount", url.getClickCount()
        );

        return ResponseEntity.ok(stats);
    }
}
