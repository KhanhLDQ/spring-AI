package org.tommap.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tommap.springai.model.request.CountryCitiesRequest;
import org.tommap.springai.model.response.ApiResponse;
import org.tommap.springai.model.response.CountryCitiesResponse;

import javax.validation.Valid;
import java.util.List;

import static org.tommap.springai.constant.SystemRoleConstants.GEOGRAPHY_SYSTEM_ROLE;

@RestController
@RequestMapping("/api/v1/structured-output")
public class StructuredOutputController {
    private final ChatClient openAiChatClient;

    public StructuredOutputController(
        @Qualifier("openAiChatClient") ChatClient openAiChatClient
    ) {
        this.openAiChatClient = openAiChatClient;
    }

    @PostMapping("/single-object")
    public ResponseEntity<ApiResponse<CountryCitiesResponse>> structuredOutputSingleObject(
        @RequestBody @Valid CountryCitiesRequest request
    ) {
        var response = openAiChatClient.prompt()
                .system(GEOGRAPHY_SYSTEM_ROLE)
                .user(request.getMessage())
                .call()
                .entity(CountryCitiesResponse.class);

        return ResponseEntity.ok(ApiResponse.ok("structured output generated successfully", response));
    }

    @PostMapping("/list-of-objects")
    public ResponseEntity<ApiResponse<List<CountryCitiesResponse>>> structuredOutputListOfObjects(
        @RequestBody @Valid CountryCitiesRequest request
    ) {
        var response = openAiChatClient.prompt()
                .system(GEOGRAPHY_SYSTEM_ROLE)
                .user(request.getMessage())
                .call()
                .entity(new ParameterizedTypeReference<List<CountryCitiesResponse>>() {});

        return ResponseEntity.ok(ApiResponse.ok("structured output generated successfully", response));
    }
}
