package org.tommap.springai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryClientConfig {
    /*
        - spring AI autoconfigures a ChatMemory bean that we can use directly in the application
            + default it uses in-memory repository to store messages & MessageWindowChatMemory implementation to manage conversation history
            + in-memory serious drawback -> whenever we restart the app - all ChatMemory conversations will be lost -> only for demos or lower critical apps
        - built-in memory advisors
            + MessageChatMemoryAdvisor -> real-time chat memory -> store chat as a list of structured messages (system | user | assistant message roles)
            + PromptChatMemoryAdvisor -> token-optimized conversations -> convert to plain text format & append to system prompt (like a summary)
            + VectorStoreAdvisor -> long-term or knowledge-based chats -> select only relevant past messages instead of entire chat history
        - context window
            + like the memory span of LLM models -> it tells the model how much text (tokens) it can see at one time while generating the response
            + imagine a whiteboard that fits only so much text -> once full then you have to erase the older parts to make space
            + similarly if the total token count exceeds the context window size -> older messages get dropped -> LLM 'forget' what gets dropped
     */
    @Bean
    public ChatClient openAiChatMemoryClient(OpenAiChatModel openAiChatModel, ChatMemory chatMemory) {
        Advisor loggerAdvisor = new SimpleLoggerAdvisor();
        Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();

        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(loggerAdvisor, memoryAdvisor)
                .build();
    }

    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(10)
                .build();
    }
}
