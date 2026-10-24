
package com.urlshortener.service;

import com.urlshortener.entity.Url;
import com.urlshortener.repository.UrlRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class UrlService {

    private final UrlRepository urlRepository;

    private static final String CHARACTERS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final int CODE_LENGTH = 6;

    private final SecureRandom random = new SecureRandom();

    public UrlService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    // 1. Create a short URL
    @Transactional
    public Url shortenUrl(String originalUrl) {

        if (originalUrl == null || originalUrl.isBlank()) {
            throw new IllegalArgumentException("URL cannot be empty");
        }

        originalUrl = originalUrl.trim();

        try {
            URI uri = URI.create(originalUrl);
            String scheme = uri.getScheme();

            if (scheme == null
                    || !(scheme.equalsIgnoreCase("http")
                    || scheme.equalsIgnoreCase("https"))
                    || uri.getHost() == null
                    || uri.getHost().isBlank()) {

                throw new IllegalArgumentException(
                        "Please provide a valid HTTP or HTTPS URL"
                );
            }

        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Please provide a valid HTTP or HTTPS URL"
            );
        }

        // Generate a unique short code
        for (int attempt = 0; attempt < 10; attempt++) {

            String shortCode = generateShortCode();

            if (!urlRepository.existsByShortCode(shortCode)) {

                Url url = new Url();
                url.setOriginalUrl(originalUrl);
                url.setShortCode(shortCode);

                return urlRepository.save(url);
            }
        }

        throw new IllegalStateException(
                "Could not generate a unique short code. Please try again."
        );
    }

    // 2. Generate a random six-character code
    private String generateShortCode() {

        StringBuilder code = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }

        return code.toString();
    }

    // 3. Retrieve a URL for redirection and increment clicks
    @Transactional
    public Url getOriginalUrl(String shortCode) {

        Url url = getUrlByShortCode(shortCode);

        if (url.getExpiresAt() != null
                && !url.getExpiresAt().isAfter(LocalDateTime.now())) {

            throw new UrlNotFoundException("Short URL has expired");
        }

        url.setClickCount(url.getClickCount() + 1);

        return urlRepository.save(url);
    }

    // 4. Find a URL without incrementing its click count
    @Transactional(readOnly = true)
    public Url getUrlByShortCode(String shortCode) {

        return urlRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new UrlNotFoundException("Short URL not found"));
    }

    // 5. Save changes to an existing URL
    @Transactional
    public Url saveUrl(Url url) {
        return urlRepository.save(url);
    }
}
