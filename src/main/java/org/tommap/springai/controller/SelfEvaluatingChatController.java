package org.tommap.springai.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.evaluation.Evaluator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tommap.springai.exception.InvalidModelResponseException;
import org.tommap.springai.model.request.SelfEvaluatingChatRequest;
import org.tommap.springai.model.request.SelfEvaluatingPromptRequest;
import org.tommap.springai.model.response.ApiResponse;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

/*
    - runtime evaluation in app logic
        + problem
            - tests can get lucky
            - LLMs are non-deterministic
                + might return good answers during tests ... but bad ones in production
                + even passing evaluator-based tests do not guarantee safe runtime
        + solution
            - use evaluators at runtime -> apply after every response & combine with retry|cover mechanism to try again
            - pros -> to ensure end users are always going to get the proper response
            - cons -> token consumption will get higher because we are trying to call LLM models multiple times
 */
@RestController
@RequestMapping("/api/v1/self-evaluating")
@Slf4j
public class SelfEvaluatingChatController {
    @Value("classpath:/promptTemplates/systemPromptTemplate.st")
    Resource systemPromptTemplate;

    private final ChatClient chatClient;
    private final Evaluator evaluator;

    public SelfEvaluatingChatController(
        @Qualifier("openAiChatClientBuilder") ChatClient.Builder openAiChatClientBuilder
    ) {
        this.chatClient = openAiChatClientBuilder
                .defaultAdvisors(List.of(new SimpleLoggerAdvisor()))
                .build();
        this.evaluator = new FactCheckingEvaluator(openAiChatClientBuilder);
    }

    @Retryable(retryFor = {InvalidModelResponseException.class}, maxAttempts = 3)
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<String>> chat(
        @RequestBody @Valid SelfEvaluatingChatRequest request
    ) {
        var response = chatClient.prompt()
                .user(request.getMessage())
                .call()
                .content();

        validateAiModelResponse(request.getMessage(), response, List.of());

        return ResponseEntity.ok(ApiResponse.ok("self evaluating chat response generated successfully", response));
    }

    @PostMapping("/prompt-stuffing")
    @Retryable(retryFor = {InvalidModelResponseException.class}, maxAttempts = 3)
    public ResponseEntity<ApiResponse<String>> promptStuffing(
        @RequestBody @Valid SelfEvaluatingPromptRequest request
    ) throws IOException {
        var response = chatClient.prompt()
                .system(systemPromptTemplate)
                .user(request.getMessage())
                .call()
                .content();

        //get relevant docs
        String systemPrompt = systemPromptTemplate.getContentAsString(StandardCharsets.UTF_8);
        validateAiModelResponse(request.getMessage(), response, List.of(new Document(systemPrompt)));

        return ResponseEntity.ok(ApiResponse.ok("self evaluating prompt response generated successfully", response));
    }

    private void validateAiModelResponse(String prompt, String response, List<Document> relevantDocs) {
        EvaluationRequest evaluationRequest = new EvaluationRequest(prompt, relevantDocs, response);
        EvaluationResponse evaluationResponse = evaluator.evaluate(evaluationRequest);

        if (!evaluationResponse.isPass()) {
            log.debug("evaluation check failed - try to generate response again");
            throw new InvalidModelResponseException(prompt, response);
        }
    }

    /*
        - return type of recover & retry methods should be the same
        - accept the same exception for which we are trying to perform retry operation
     */
    @Recover
    public ResponseEntity<ApiResponse<String>> recover(InvalidModelResponseException ex) {
        var response = String.format("I'm sorry! I could not answer your question! Please try to rephrasing it! Reason: %s", ex.getMessage());

        return ResponseEntity.status(UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(UNPROCESSABLE_ENTITY.value(), response));
    }
}
