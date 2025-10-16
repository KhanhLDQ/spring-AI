package org.tommap.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tommap.springai.model.request.PromptStuffingRequest;
import org.tommap.springai.model.response.ApiResponse;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/prompt-stuffing")
public class PromptStuffingController {
    /*
        - technique -> give LLM an open book before answering a question
     */

    @Value("classpath:/promptTemplates/systemPromptTemplate.st")
    Resource systemPromptTemplate;

    private final ChatClient openAiChatClient;

    public PromptStuffingController(
        @Qualifier("openAiChatClient") ChatClient openAiChatClient
    ) {
        this.openAiChatClient = openAiChatClient;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> demoPromptStuffing(
        @RequestBody @Valid PromptStuffingRequest request
    ) {
        var response = openAiChatClient.prompt()
                .system(systemPromptTemplate)
                .user(request.getMessage())
                .call()
                .content();

        return ResponseEntity.ok(ApiResponse.ok("prompt stuffing generated successfully", response));
    }
}
