package org.tommap.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tommap.springai.model.request.HelpdeskInputRequest;
import org.tommap.springai.model.response.ApiResponse;
import org.tommap.springai.tool.HelpdeskTools;

import javax.validation.Valid;

import java.util.Map;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
@RequestMapping("/api/v1/helpdesk")
public class HelpdeskController {
    private final ChatClient helpdeskChatClient;
    private final HelpdeskTools helpdeskTools;

    public HelpdeskController(
        @Qualifier("helpdeskChatClient") ChatClient helpdeskChatClient,
        HelpdeskTools helpdeskTools
    ) {
        this.helpdeskChatClient = helpdeskChatClient;
        this.helpdeskTools = helpdeskTools;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> helpdesk(
        @RequestHeader("username") String username,
        @RequestBody @Valid HelpdeskInputRequest request
    ) {
        var response = helpdeskChatClient.prompt()
                .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, username))
                .user(request.getMessage())
                .tools(helpdeskTools)
                .toolContext(Map.of("username", username)) //pass contextual data
                .call()
                .content();

        return ResponseEntity.ok(ApiResponse.ok("helpdesk response generated successfully", response));
    }
}
