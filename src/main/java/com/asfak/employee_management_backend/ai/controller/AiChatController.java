package com.asfak.employee_management_backend.ai.controller;

import com.asfak.employee_management_backend.ai.dto.AiChatRequest;
import com.asfak.employee_management_backend.ai.dto.AiChatResponse;
import com.asfak.employee_management_backend.ai.service.AiChatService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ai/chat")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping
    public ResponseEntity<AiChatResponse> chat(

            @Valid
            @RequestBody
            AiChatRequest request,

            Authentication authentication
    ) {

        if (
                authentication == null
                        ||
                        authentication.getName() == null
        ) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated"
            );
        }

        String loggedInEmail =
                authentication.getName();

        return ResponseEntity.ok(
                aiChatService.chat(
                        request,
                        loggedInEmail
                )
        );
    }
}