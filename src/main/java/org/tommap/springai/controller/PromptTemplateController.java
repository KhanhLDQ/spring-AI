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
import org.tommap.springai.model.request.PromptTemplateRequest;
import org.tommap.springai.model.response.ApiResponse;

import javax.validation.Valid;

import static org.tommap.springai.constant.SystemRoleConstants.EMAIL_PROMPT_TEMPLATE_SYSTEM_ROLE;

@RestController
@RequestMapping("/api/v1/prompt-template")
public class PromptTemplateController {
    @Value("classpath:/promptTemplates/userPromptTemplate.st")
    Resource userPromptTemplate;

    private final ChatClient openAiChatClient;

    public PromptTemplateController(
        @Qualifier("openAiChatClient") ChatClient openAiChatClient
    ) {
        this.openAiChatClient = openAiChatClient;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> buildPromptTemplate(
        @RequestBody @Valid PromptTemplateRequest request
    ) {
        var response = openAiChatClient.prompt()
                .system(EMAIL_PROMPT_TEMPLATE_SYSTEM_ROLE)
                .user(promptUserSpec -> promptUserSpec.text(userPromptTemplate)
                        .param("customerName", request.getCustomerName())
                        .param("customerMsg", request.getCustomerMsg())
                )
                .call()
                .content();

        return ResponseEntity.ok(ApiResponse.ok("prompt template generated successfully", response));
    }
}
