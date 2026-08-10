package com.asfak.employee_management_backend.ai.service.impl;

import com.asfak.employee_management_backend.ai.service.AiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Service;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiAiServiceImpl implements AiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.model}")
    private String model;

    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate =
            new RestTemplate();

    @Override
    public String generateText(
            String prompt
    ) {

        try {

            String url =
                    apiUrl
                            + "/"
                            + model
                            + ":generateContent";

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setContentType(
                    MediaType.APPLICATION_JSON
            );

            headers.set(
                    "x-goog-api-key",
                    apiKey
            );

            Map<String, Object> body =
                    Map.of(
                            "contents",
                            List.of(
                                    Map.of(
                                            "parts",
                                            List.of(
                                                    Map.of(
                                                            "text",
                                                            prompt
                                                    )
                                            )
                                    )
                            )
                    );

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(
                            body,
                            headers
                    );

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            request,
                            String.class
                    );

            String responseBody =
                    response.getBody();

            if (
                    responseBody == null ||
                            responseBody.isBlank()
            ) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "AI service returned an empty response."
                );
            }

            JsonNode root =
                    objectMapper.readTree(
                            responseBody
                    );

            JsonNode candidates =
                    root.path(
                            "candidates"
                    );

            if (
                    candidates.isArray()
                            &&
                            !candidates.isEmpty()
            ) {

                JsonNode parts =
                        candidates
                                .get(0)
                                .path("content")
                                .path("parts");

                if (
                        parts.isArray()
                                &&
                                !parts.isEmpty()
                ) {

                    for (
                            JsonNode part :
                            parts
                    ) {

                        String text =
                                part
                                        .path("text")
                                        .asText();

                        if (
                                text != null
                                        &&
                                        !text.isBlank()
                        ) {

                            return text.trim();
                        }
                    }
                }
            }

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "AI summary could not be generated."
            );

        } catch (
                HttpClientErrorException.TooManyRequests e
        ) {

            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "AI free quota reached. Please wait about 1 minute and try again."
            );

        } catch (
                HttpClientErrorException.Unauthorized e
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "AI API authentication failed. Please check Gemini API key."
            );

        } catch (
                HttpClientErrorException.Forbidden e
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "AI API access is not allowed for the configured key."
            );

        } catch (
                HttpClientErrorException.NotFound e
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Configured Gemini model is not available."
            );

        } catch (
                HttpClientErrorException e
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Gemini API request failed with status: "
                            + e.getStatusCode()
            );

        } catch (
                ResponseStatusException e
        ) {

            throw e;

        } catch (
                Exception e
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "AI service is temporarily unavailable."
            );
        }
    }
}