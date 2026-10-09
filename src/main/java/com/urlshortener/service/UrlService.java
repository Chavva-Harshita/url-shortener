

package com.urlshortener.service;

import com.urlshortener.entity.Url;
import com.urlshortener.repository.UrlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

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

    
@Transactional
public Url shortenUrl(String originalUrl) {

    if (originalUrl == null || originalUrl.isBlank()) {
        throw new IllegalArgumentException("URL cannot be empty");
    }

    originalUrl = originalUrl.trim();

    if (!originalUrl.matches("^https?://.+")) {
        throw new IllegalArgumentException(
                "URL must start with http:// or https://"
        );
    }

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
        

      
    private String generateShortCode() {

        StringBuilder code = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }

        return code.toString();
    }

   
public Url getOriginalUrl(String shortCode) {

    Url url = urlRepository.findByShortCode(shortCode)
            .orElseThrow(() ->
                    new IllegalArgumentException("Short URL not found"));

    url.setClickCount(url.getClickCount() + 1);

    return urlRepository.save(url);
}

public Url getUrlByShortCode(String shortCode) {
    return urlRepository.findByShortCode(shortCode)
            .orElseThrow(() ->
                    new IllegalArgumentException("Short URL not found"));
}
}