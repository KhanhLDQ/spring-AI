package org.tommap.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tommap.springai.model.request.ChatMemoryRequest;
import org.tommap.springai.model.response.ApiResponse;

import javax.validation.Valid;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
@RequestMapping("/api/v1/chat-memory")
public class ChatMemoryController {
    private final ChatClient openAiChatMemoryClient;

    public ChatMemoryController(
        @Qualifier("openAiChatMemoryClient") ChatClient openAiChatMemoryClient
    ) {
        this.openAiChatMemoryClient = openAiChatMemoryClient;
    }

    /*
        - docs -> https://docs.spring.io/spring-ai/reference/api/chat-memory.html
        - LLMs do not remember past chats -> each interaction is like a fresh start - no memory!
     */
    @PostMapping
    public ResponseEntity<ApiResponse<String>> chatMemory(
        @RequestHeader("username") String username, //consider username as a conversation_id
        @RequestBody @Valid ChatMemoryRequest request
    ) {
        var response = openAiChatMemoryClient.prompt()
                .user(request.getMessage())
                .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, username))
                .call()
                .content();

        return ResponseEntity.ok(ApiResponse.ok("chat memory response generated successfully", response));
    }
}
