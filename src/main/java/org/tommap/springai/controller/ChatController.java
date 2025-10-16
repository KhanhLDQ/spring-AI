package org.tommap.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tommap.springai.model.request.ChatRequest;
import org.tommap.springai.model.response.ApiResponse;

import javax.validation.Valid;

import static org.tommap.springai.constant.SystemRoleConstants.HR_ASSISTANT_SYSTEM_ROLE;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {
    private final ChatClient openAiChatClient;
    private final ChatClient ollamaChatClient;

    public ChatController(
        @Qualifier("openAiChatClient") ChatClient openAiChatClient,
        @Qualifier("ollamaChatClient") ChatClient ollamaChatClient
    ) {
        this.openAiChatClient = openAiChatClient;
        this.ollamaChatClient = ollamaChatClient;
    }

    /*
        - SystemRole -> provide instructions for how the LLMs should behave
        - UserRole -> what the user says or asks
        - AssistantRole -> the LLMs' response
        - FunctionRole -> special instructions to run a function or fetch data
     */
    @PostMapping
    public ResponseEntity<ApiResponse<String>> chat(
        @RequestBody @Valid ChatRequest chatRequest
    ) {
        var response = selectChatClient(chatRequest.getModel())
//                .prompt(chatRequest.getMessage())
                .prompt()
                .system(HR_ASSISTANT_SYSTEM_ROLE) //override default system message in ChatClient
                .user(chatRequest.getMessage()) //override default user message in ChatClient
                .call() //invoke LLMs
                .content();

        return ResponseEntity.ok(ApiResponse.ok("chat response generated successfully", response));
    }

    private ChatClient selectChatClient(String model) {
        return switch (model.toLowerCase()) {
            case "openai" -> openAiChatClient;
            case "ollama" -> ollamaChatClient;
            default -> throw new IllegalArgumentException("Unsupported model: " + model);
        };
    }
}
