package org.tommap.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tommap.springai.model.request.McpServerRequest;
import org.tommap.springai.model.response.ApiResponse;

import javax.validation.Valid;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
@RequestMapping("/api/v1/mcp-server")
public class McpServerController {
    private final ChatClient mcpChatClient;

    public McpServerController(
        @Qualifier("mcpChatClient") ChatClient mcpChatClient
    ) {
        this.mcpChatClient = mcpChatClient;
    }

    @PostMapping("/stdio")
    public ResponseEntity<ApiResponse<String>> mcpServer(
        @RequestHeader("username") String username,
        @RequestBody @Valid McpServerRequest request
    ) {
        var response = mcpChatClient.prompt()
                .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, username))
                .user(String.format("Username: %s - User Question: %s", username, request.getMessage()))
                .call()
                .content();

        return ResponseEntity.ok(ApiResponse.ok("mcp server stdio response generated successfully", response));
    }
}
