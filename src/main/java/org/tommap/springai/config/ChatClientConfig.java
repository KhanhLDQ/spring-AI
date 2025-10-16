package org.tommap.springai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.tommap.springai.constant.SystemRoleConstants.IT_HELPDESK_ASSISTANT_SYSTEM_ROLE;
import static org.tommap.springai.constant.UserRoleConstants.DEFAULT_USER_ROLE;

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

        - defaults -> preconfigured values or behaviors that are applied automatically to each request made through the ChatClient - unless overridden
     */

    @Bean
    public ChatClient openAiChatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel)
                .defaultSystem(IT_HELPDESK_ASSISTANT_SYSTEM_ROLE)
                .defaultUser(DEFAULT_USER_ROLE)
                .build();
    }

    @Bean
    public ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel) {
        return ChatClient.builder(ollamaChatModel).build();
    }
}
