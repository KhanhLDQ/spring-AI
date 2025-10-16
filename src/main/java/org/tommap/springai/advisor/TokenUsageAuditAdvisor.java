package org.tommap.springai.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.model.ChatResponse;

@Slf4j
public class TokenUsageAuditAdvisor implements CallAdvisor {
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);
        logTokenUsage(chatClientResponse.chatResponse());

        return chatClientResponse;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    //advisors with lower order values are executed first - https://docs.spring.io/spring-ai/reference/api/advisors.html#_advisor_order
    @Override
    public int getOrder() {
        return 1;
    }

    private void logTokenUsage(ChatResponse chatResponse) {
        if (null != chatResponse) {
            var tokenUsage = chatResponse.getMetadata().getUsage();

            if (null != tokenUsage) {
                log.debug("Token usage: {}", tokenUsage);
            }
        }
    }
}
