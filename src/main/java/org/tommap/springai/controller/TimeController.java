package org.tommap.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tommap.springai.model.request.TimeRequest;
import org.tommap.springai.model.response.ApiResponse;

import javax.validation.Valid;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
@RequestMapping("/api/v1/time")
public class TimeController {
    private final ChatClient timeChatClient;

    public TimeController(@Qualifier("timeChatClient") ChatClient timeChatClient) {
        this.timeChatClient = timeChatClient;
    }

    @PostMapping("/local")
    public ResponseEntity<ApiResponse<String>> localTime(
        @RequestHeader("username") String username,
        @RequestBody @Valid TimeRequest request
    ) {
        var response = timeChatClient.prompt()
                .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, username))
                .user(request.getMessage())
                .call()
                .content();

        return ResponseEntity.ok(ApiResponse.ok("local time response generated successfully", response));
    }
}
