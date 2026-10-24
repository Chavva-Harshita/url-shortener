
package com.urlshortener.service;

import com.urlshortener.entity.Url;
import com.urlshortener.repository.UrlRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @InjectMocks
    private UrlService urlService;

    @BeforeEach
    void setUp() {
        // Mockito creates the mock repository and injects it.
    }

    @Test
    void shouldShortenValidUrl() {
        when(urlRepository.existsByShortCode(anyString()))
                .thenReturn(false);

        when(urlRepository.save(any(Url.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Url result = urlService.shortenUrl(
                "https://www.google.com"
        );

        assertNotNull(result);
        assertEquals(
                "https://www.google.com",
                result.getOriginalUrl()
        );
        assertNotNull(result.getShortCode());
        assertEquals(6, result.getShortCode().length());

        verify(urlRepository).save(any(Url.class));
    }

    @Test
    void shouldRejectEmptyUrl() {
        assertThrows(
                IllegalArgumentException.class,
                () -> urlService.shortenUrl(" ")
        );

        verifyNoInteractions(urlRepository);
    }

    @Test
    void shouldThrowWhenShortCodeDoesNotExist() {
        when(urlRepository.findByShortCode("missing"))
                .thenReturn(Optional.empty());

        assertThrows(
                UrlNotFoundException.class,
                () -> urlService.getUrlByShortCode("missing")
        );
    }
}