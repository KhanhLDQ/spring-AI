package org.tommap.springai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tommap.springai.tool.TimeTools;

@Configuration
public class TimeChatClientConfig {
    /*
        - tools are mainly used for
            + information retrieval -> from external resources e.g. database | web search engine | file system ... to augment the knowledge of LLM models
            + take action -> automate tasks that would require human intervention e.g. send an email | submit a form | create a new record in a database | book a flight ...

        - message roles in LLMs -> system | user | assistant | tool

        - how the tools get called by LLM models?
            + for beginners it may seem like spring AI exposes tools as the endpoints for OpenAI to call -> but that's not what happens
            + instead a conversation between the application & LLM model whenever a tool is involved
                - end user asks a question to the application
                - spring AI sends system|user messages & tool details to LLM
                - LLM do not have the answer immediately (e.g. real time data)
                    + try to understand the context of question
                    + if this question can be answered by using any of the tools -> LLM sends message (assistant) to invoke a given tool & finish reason (TOOL_CALLS) to the application (springAI)
                    + finish reason is not STOP which means that the conversation is not yet completed and LLM is waiting for its response
                    + spring AI takes instructions from LLM and execute required tool logic
                    + spring AI sends message (tool) to LLM including question and tool logic response
                    + LLM is going to prepare the response and send back to spring AI in form of assistant role & finish reason (STOP)
                    + spring AI forwards response to the end users
                - org.springframework.ai.model.tool.ToolCallingManager -> ToolExecutionResult executeToolCalls(Prompt prompt, ChatResponse chatResponse)
     */
    @Bean
    public ChatClient timeChatClient(OpenAiChatModel openAiChatModel, ChatMemory chatMemory, TimeTools timeTools) {
        Advisor loggerAdvisor = new SimpleLoggerAdvisor();
        Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();

        return ChatClient.builder(openAiChatModel)
                .defaultTools(timeTools)
                .defaultAdvisors(loggerAdvisor, memoryAdvisor)
                .build();
    }
}
