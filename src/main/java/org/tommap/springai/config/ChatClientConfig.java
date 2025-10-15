package org.tommap.springai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {
    /*
        - ChatModel
            + define the contract for communicating with different AI providers
            + handle the actual API calls to the AI services
            + foundation layer for AI interactions

        - ChatClient
            + abstract layer built on top of ChatModel
            + easier interaction with AI models -> build a prompt | manage chat history | invoke LLM | extract content

        - spring AI autoconfigures a single chat client bean -> suitable for simple use cases
        - when multiple models are involved -> manually configure multiple chat client beans for each model -> inject desired builder based on business requirements
     */

    @Bean
    public ChatClient openAiChatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel).build();
    }

    @Bean
    public ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel) {
        return ChatClient.builder(ollamaChatModel).build();
    }
}
