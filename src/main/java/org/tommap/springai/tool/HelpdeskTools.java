package org.tommap.springai.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.tommap.springai.model.entity.HelpdeskTicket;
import org.tommap.springai.model.request.TicketRequest;
import org.tommap.springai.service.IHelpdeskService;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class HelpdeskTools {
    private final IHelpdeskService helpdeskService;

    /*
        - ToolCallingManager(I) -> control entire execution lifecycle -> invoke tools with the given input arguments and return the result
        - ToolCallResultConverter(I) -> convert tool call results to String and send back to AI model -> by default it's serialized to JSON with Jackson

        - by default tool result is sent back to LLM so it can reason and continue chatting
            + but sometimes we want to
                - return the tool result directly to the user
                - skip extra processing by the LLM
            + returnDirect flag of @Tool handles this scenario

        - handle tool errors
            + if the tool throws an error -> spring wraps it in a ToolExecutionException -> ToolExecutionExceptionProcessor
            + by default it converts the error message to String and sends back to the LLM
            + set the flag alwaysThrow = true in DefaultToolExecutionExceptionProcessor -> throw exceptions to be handled by the caller

     */

    @Tool(name = "createTicket", description = "create the support helpdesk ticket", returnDirect = true)
    String createTicket(
        @ToolParam(description = "details to create a support helpdesk ticket") TicketRequest ticketRequest,
        ToolContext toolContext //mechanism in spring AI that allows to inject extra data e.g. user|session metadata into the tool execution flow
    ) {
        String username = (String) toolContext.getContext().get("username"); //access contextual data in the tool

        log.info("create support helpdesk ticket for user: {} with details: {}", username, ticketRequest.issue());
        HelpdeskTicket savedTicket = helpdeskService.createTicket(ticketRequest, username);
        log.info("helpdesk ticket created successfully for user: {} - ticketId: {}", username, savedTicket.getId());

        return String.format("helpdesk ticket #%d created successfully for user: %s", savedTicket.getId(), username);
    }

    @Tool(name = "getHelpdeskTickets", description = "fetch the status of helpdesk tickets based on a given username")
    List<HelpdeskTicket> getHelpdeskTickets(ToolContext toolContext) {
        String username = (String) toolContext.getContext().get("username");

        log.info("fetch helpdesk tickets for user: {}", username);
        List<HelpdeskTicket> helpdeskTickets = helpdeskService.getByUsername(username);
        log.info("found {} helpdesk tickets for user: {}", helpdeskTickets.size(), username);

//        throw new RuntimeException("unable to fetch helpdesk tickets due to database connection errors"); //demo handling tool errors

        return helpdeskTickets;
    }
}
