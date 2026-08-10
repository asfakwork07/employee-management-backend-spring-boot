package com.asfak.employee_management_backend.ai.service;

import com.asfak.employee_management_backend.ai.dto.AiChatRequest;
import com.asfak.employee_management_backend.ai.dto.AiChatResponse;

public interface AiChatService {

    AiChatResponse chat(
            AiChatRequest request,
            String loggedInEmail
    );
}