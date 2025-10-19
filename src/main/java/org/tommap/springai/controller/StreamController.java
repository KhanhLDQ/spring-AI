package org.tommap.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/stream")
public class StreamController {
    private final ChatClient openAiChatClient;

    public StreamController(
        @Qualifier("openAiChatClient") ChatClient openAiChatClient
    ) {
        this.openAiChatClient = openAiChatClient;
    }

    @GetMapping
    public Flux<String> stream(
        @RequestParam("message") String message
    ) {
        return openAiChatClient.prompt()
                .user(message)
                .stream()
                .content();
    }
}
