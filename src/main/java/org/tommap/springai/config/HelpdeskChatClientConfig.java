package org.tommap.springai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.execution.DefaultToolExecutionExceptionProcessor;
import org.springframework.ai.tool.execution.ToolExecutionExceptionProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.tommap.springai.tool.TimeTools;

@Configuration
public class HelpdeskChatClientConfig {
    @Value("classpath:/promptTemplates/systemPromptHelpdeskTemplate.st")
    Resource systemPromptHelpdeskTemplate;

    @Bean
    public ChatClient helpdeskChatClient(
        OpenAiChatModel openAiChatModel,
        ChatMemory chatMemory,
        TimeTools timeTools
    ) {
        Advisor loggerAdvisor = new SimpleLoggerAdvisor();
        Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();

        return ChatClient.builder(openAiChatModel)
                .defaultSystem(systemPromptHelpdeskTemplate)
                .defaultTools(timeTools)
                .defaultAdvisors(loggerAdvisor, memoryAdvisor)
                .build();
    }

    /*
        - either converting the error message to a String that can be sent back to the AI model or throwing an exception to be handled by the caller
     */
//    @Bean
//    public ToolExecutionExceptionProcessor toolExecutionExceptionProcessor() { //demo handling tool errors
//        return new DefaultToolExecutionExceptionProcessor(true);
//    }
}
