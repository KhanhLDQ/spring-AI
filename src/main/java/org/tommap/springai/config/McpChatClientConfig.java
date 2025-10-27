package org.tommap.springai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
    - MCP architecture
        + use a host|client|server architecture to streamline how AI models interact with tools & contextual data
            - MCP host → central coordinator - manage permissions | tool access | session context → decide when to invoke a tool either based on user input or automatically
            - MCP client → initialized by the host - handle all communication between the host and MCP server → sending tool requests & receiving responses
            - MCP server → connect to local or remote system (e.g. database | file storage | 3rd party API | …) and expose capabilities → developers can either use public MCP servers or build their own
        + during the startup of MCP host (spring AI application)
            - MCP client is going to connect with MCP server & ask the list of tools that it’s exposing & provide response to MCP host
            - this communication happens by using MCP protocol (specialized layer that makes HTTP more suitable for AI use cases)
            - when users ask a question → MCP host provides prompt (system | user | … messages) & also tool definition details to the LLM
            - based upon prompt provided by users → LLM models give instructions to MCP host to invoke tool exposed by MCP server
            - spring AI app will give instructions to MCP server (via MCP client) to execute tool logic
        + the beauty of MCP
            - separate all the tools logic into a separate component called MCP server
            - this MCP server can be leveraged by anyone

    - https://github.com/modelcontextprotocol/inspector → quickly understand the tools | resources | prompts exposed by MCP server
 */

@Configuration
public class McpChatClientConfig {
    @Bean
    public ChatClient mcpChatClient(
        OpenAiChatModel openAiChatModel,
        ChatMemory chatMemory,
        ToolCallbackProvider toolCallbackProvider
    ) {
        Advisor loggerAdvisor = new SimpleLoggerAdvisor();
        Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();

        return ChatClient.builder(openAiChatModel)
                .defaultToolCallbacks(toolCallbackProvider) //inject tools from MCP servers into ChatClient bean via ToolCallbackProvider
                .defaultAdvisors(loggerAdvisor, memoryAdvisor)
                .build();
    }
}
