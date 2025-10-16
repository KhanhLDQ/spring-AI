package org.tommap.springai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tommap.springai.advisor.TokenUsageAuditAdvisor;

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

        - advisors
            - definition
                + like interceptors | middleware for the prompt flow
                + users -> ChatClient -> [advisors] -> LLMs -> response -> [advisors] -> users
                + implement cross-cutting concerns (logging | auditing ...) - not write core logic
            - order
                + advisors with lower order values are executed first
                + advisor chain operates as a stack -> the first in the chain is the first to process the request but also the last to process the response
                + higher values are considered as lower priority
                + if multiple advisors have the same order value -> execution order is not guaranteed
     */

    @Bean
    public ChatClient openAiChatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor(), new TokenUsageAuditAdvisor())
                .defaultSystem(IT_HELPDESK_ASSISTANT_SYSTEM_ROLE)
                .defaultUser(DEFAULT_USER_ROLE)
                .build();
    }

    @Bean
    public ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel) {
        return ChatClient.builder(ollamaChatModel).build();
    }
}
