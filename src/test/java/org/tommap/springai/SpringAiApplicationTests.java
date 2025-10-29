package org.tommap.springai;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.tommap.springai.controller.ChatController;
import org.tommap.springai.controller.PromptStuffingController;
import org.tommap.springai.controller.RagController;
import org.tommap.springai.model.request.ChatRequest;
import org.tommap.springai.model.request.PromptStuffingRequest;
import org.tommap.springai.model.request.RagDocumentRequest;
import org.tommap.springai.model.response.ApiResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.tommap.springai.constant.SystemRoleConstants.HR_ASSISTANT_SYSTEM_ROLE;

/*
    - by default (PER_METHOD) both JUnit 4-5 create a new instance of the test class before running each test method
 */
@TestInstance(PER_CLASS) //share the same test class instance
class SpringAiApplicationTests extends AbstractIntegrationTest{
    @Value("classpath:/promptTemplates/systemPromptTemplate.st")
    Resource systemPromptTemplate;

    @Autowired
    private ChatController chatController;

    @Autowired
    private PromptStuffingController promptStuffingController;

    @Autowired
    private RagController ragController;

    @Autowired
    @Qualifier("openAiChatClientBuilder")
    private ChatClient.Builder openAiChatClientBuilder;

    @Autowired
    private VectorStore vectorStore;

    /*
        - testing challenges with GenAI
            + non-deterministic response -> LLMs do not return the same output for the same prompt every time
            + traditional approach e.g. assertEquals() does not work -> make unit testing unreliable for LLM-generated outputs
        - solution: spring AI evaluators
            + a component that checks if the LLM response is acceptable for the given prompt instead of looking at exact matches
        - how it works
            + take 2 inputs -> prompt submitted to the LLM & response returned by the LLM
            + use another LLM to decide if the response passes
        - implementations of Evaluator(I)
            + RelevancyEvaluator -> check how relevant the LLM response is to the original prompt -> ensure the LLM does not provide unrelated information
            + FactCheckingEvaluator -> check factual accuracy of the LLM response based on a provided document or context
     */
    private RelevancyEvaluator relevancyEvaluator;
    private FactCheckingEvaluator factCheckingEvaluator;

    @BeforeAll
    void setUpSharedResources() {
        this.relevancyEvaluator = new RelevancyEvaluator(openAiChatClientBuilder);
        this.factCheckingEvaluator = new FactCheckingEvaluator(openAiChatClientBuilder);
    }

    @ParameterizedTest(name = "{2}")
    @MethodSource("chatTestData")
    void evaluateChat(String message, String model, String description) {
        //arrange
        ChatRequest chatRequest = new ChatRequest(message, model);

        //act
        ResponseEntity<ApiResponse<String>> responseEntity = chatController.chat(chatRequest);
        Assertions.assertNotNull(responseEntity.getBody());
        String response = responseEntity.getBody().getData();

        EvaluationRequest evaluationRequest = new EvaluationRequest(
            message,
            List.of(new Document(HR_ASSISTANT_SYSTEM_ROLE)),
            response
        );

        EvaluationResponse evaluationResponse = relevancyEvaluator.evaluate(evaluationRequest);

        //assert
        assertAll(
                () -> assertThat(response).isNotBlank(),
                () -> assertThat(evaluationResponse.isPass())
                        .withFailMessage("""
                                ========================================
                                The answer was not considered relevant.
                                Question: "%s"
                                Response: "%s"
                                ========================================
                                """, message, response
                        )
                        .isTrue()
        );
    }

    private static Stream<Arguments> chatTestData() {
        return Stream.of(
                Arguments.of("what's the leave policy of company?", "openai", "HR question - leave policy"),
                Arguments.of("What's the weather today in DaNang?", "openai", "non-HR question - weather")
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("promptStuffingTestData")
    @SneakyThrows //allow to throw checked exception without explicitly declaring in the method signature
    void evaluatePromptStuffing(String message, String description) {
        //arrange
        String context = systemPromptTemplate.getContentAsString(StandardCharsets.UTF_8);
        PromptStuffingRequest promptStuffingRequest = new PromptStuffingRequest(message);

        //act
        ResponseEntity<ApiResponse<String>> responseEntity = promptStuffingController.demoPromptStuffing(promptStuffingRequest);
        Assertions.assertNotNull(responseEntity.getBody());
        String response = responseEntity.getBody().getData();

        EvaluationRequest evaluationRequest = new EvaluationRequest(
            message,
            List.of(new Document(context)),
            response
        );

        EvaluationResponse evaluationResponse = factCheckingEvaluator.evaluate(evaluationRequest);

        //assert
        assertAll(
                () -> assertThat(response).isNotBlank(),
                () -> assertThat(evaluationResponse.isPass())
                        .withFailMessage("""
                        ========================================
                        The response was not considered factually accurate.
                        Question: %s
                        Response: %s
                        Context: %s
                        ========================================
                        """, message, response, context
                        )
                        .isTrue()
        );
    }

    private static Stream<Arguments> promptStuffingTestData() {
        return Stream.of(
                Arguments.of("this year I only used to 5 leaves - How many leaves will be carried to the next year", "leaves question"),
                Arguments.of("I want to know about working hours of my company", "working hours question")
        );
    }

    @Test
    void evaluateRagDocument() {
        //arrange
        String username = "Tom";
        RagDocumentRequest ragDocumentRequest = new RagDocumentRequest("How can I negotiate exit policy? via email or phone?");

        //act
        ResponseEntity<ApiResponse<String>> responseEntity = ragController.ragDocument(username, ragDocumentRequest);
        Assertions.assertNotNull(responseEntity.getBody());
        String response = responseEntity.getBody().getData();

        SearchRequest searchRequest = SearchRequest.builder()
                .query(ragDocumentRequest.getMessage())
                .topK(3)
                .similarityThreshold(0.5)
                .build();

        //retrieve relevant context from vector store
        List<Document> relevantDocs = vectorStore.similaritySearch(searchRequest);

        EvaluationRequest evaluationRequest = new EvaluationRequest(
            ragDocumentRequest.getMessage(),
            relevantDocs,
            response
        );

        EvaluationResponse evaluationResponse = relevancyEvaluator.evaluate(evaluationRequest);

        //assert
        assertAll(
                () -> assertThat(response).isNotBlank(),
                () -> assertThat(evaluationResponse.isPass())
                        .isTrue()
        );
    }
}
