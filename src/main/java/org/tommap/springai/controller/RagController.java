package org.tommap.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tommap.springai.model.request.RagDocumentRequest;
import org.tommap.springai.model.request.RagRandomRequest;
import org.tommap.springai.model.request.RagWebSearchRequest;
import org.tommap.springai.model.response.ApiResponse;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
@RequestMapping("/api/v1/rag")
public class RagController {
    @Value("classpath:/promptTemplates/systemPromptRandomDataTemplate.st")
    Resource systemPromptRandomDataTemplate;

    @Value("classpath:/promptTemplates/systemPromptDocumentDataTemplate.st")
    Resource systemPromptDocumentDataTemplate;

    private final ChatClient openAiChatMemoryClient;
    private final ChatClient webSearchRagChatClient;
    private final VectorStore vectorStore;

    public RagController(
        @Qualifier("openAiChatMemoryClient") ChatClient openAiChatMemoryClient,
        @Qualifier("webSearchRagChatClient") ChatClient webSearchRagChatClient,
        VectorStore vectorStore
    ) {
        this.openAiChatMemoryClient = openAiChatMemoryClient;
        this.webSearchRagChatClient = webSearchRagChatClient;
        this.vectorStore = vectorStore;
    }

    @PostMapping("/random")
    public ResponseEntity<ApiResponse<String>> ragRandom(
        @RequestHeader("username") String username,
        @RequestBody @Valid RagRandomRequest request
    ) {
        //perform similarity search from qdrant
//        SearchRequest searchRequest = SearchRequest.builder()
//                .query(request.getMessage())
//                .topK(3) //what are the top number of documents that the search operation needs to consider
//                .similarityThreshold(0.5) //only include documents with similarity score >= 0.5
//                .build();
//
//        List<Document> relevantDocs = vectorStore.similaritySearch(searchRequest);

        //format the retrieved context
//        String relevantContext = relevantDocs.stream()
//                .map(Document::getText)
//                .collect(Collectors.joining(System.lineSeparator()));

        //generate response
        var response = openAiChatMemoryClient.prompt()
//                .system(promptSystemSpec -> promptSystemSpec
//                        .text(systemPromptRandomDataTemplate)
//                        .param("documents", relevantContext)
//                )
                .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, username)) //chat memory - set conversationId
                .user(request.getMessage())
                .call() //invoke LLMs
                .content();

        return ResponseEntity.ok(ApiResponse.ok("random rag response generated successfully", response));
    }

    @PostMapping("/document")
    public ResponseEntity<ApiResponse<String>> ragDocument(
        @RequestHeader("username") String username,
        @RequestBody @Valid RagDocumentRequest request
    ) {
        //perform similarity search from qdrant
//        SearchRequest searchRequest = SearchRequest.builder()
//                .query(request.getMessage())
//                .topK(3) //what are the top number of documents that the search operation needs to consider
//                .similarityThreshold(0.5) //only include documents with similarity score >= 0.5
//                .build();
//
//        List<Document> relevantDocs = vectorStore.similaritySearch(searchRequest);

        //format the retrieved context
//        String relevantContext = relevantDocs.stream()
//                .map(Document::getText)
//                .collect(Collectors.joining(System.lineSeparator()));

        //generate response
        var response = openAiChatMemoryClient.prompt()
//                .system(promptSystemSpec -> promptSystemSpec
//                        .text(systemPromptDocumentDataTemplate)
//                        .param("documents", relevantContext)
//                )
                .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, username)) //chat memory - set conversationId
                .user(request.getMessage())
                .call() //invoke LLMs
                .content();

        return ResponseEntity.ok(ApiResponse.ok("document rag response generated successfully", response));
    }

    @PostMapping("/web-search")
    public ResponseEntity<ApiResponse<String>> ragWebSearch(
        @RequestHeader("username") String username,
        @RequestBody @Valid RagWebSearchRequest request
    ) {
        var response = webSearchRagChatClient.prompt()
                .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, username))
                .user(request.getMessage())
                .call()
                .content();

        return ResponseEntity.ok(ApiResponse.ok("web search rag response generated successfully", response));
    }
}
