package org.tommap.springai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.tommap.springai.rag.WebSearchDocumentRetriever;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Configuration
public class RagChatClientConfig {
    /*
        - simple prompting
            + with just LLM -> when you give a prompt -> the LLM generates a response based on its pre-trained knowledge
            + LLM does not know everything! -> it cannot fetch real-time or private company data ...

        - prompt stuffing
            + try to add all relevant information directly in the prompt to help the model answer accurately
            + only works well when the documents are small
            + if the size increases
                - prompt size becomes too large -> leads to more token consumption -> more bills
                - only a tiny portion of the context may be relevant to the question

        - RAG
            + break large documents into smaller chunks
            + during each user query
                - only the most relevant chunks are added to the prompt
                - keeps the prompt compact & focused
                - improve accuracy & reduce token waste
            + e.g. user query: Tell me about a product XXXX
                - retriever -> search company docs | PDFs | vector database
                - augmentation -> pick the most relevant chunks of text -> send prompt & relevant context to LLM
                - generator -> write an answer using that retrieved knowledge -> more accurate | up-to-date | personalized response!

        - vector databases
            + https://docs.spring.io/spring-ai/reference/api/vectordbs.html
            + vector search understands context & meaning -> enable semantic search (meaning-based)
                - e.g. search how to fix a laptop screen -> retrieve content related to repairing a broken display even if exact words do not match

        - how vector stores enable RAG
            + index the knowledge
                - split original documents into smaller document chunks -> convert into vector embeddings -> store them in a vector store
            + semantic retrieval
                - when a user asks a question -> convert it into a query vector
                - use the vector store to retrieve most similar document chunks
            + augment the prompt
                - inject the retrieved chunks into the prompt
                - the LLM model uses this relevant context to generate accurate answers

        - enable rag flow with web search
            + why? -> whatever core LLM models that we are trying to integrate with - they're not capable of reading the latest information from the web -> they're always going to have a cut-off knowledge date
            + use search engine API -> tavily - https://www.tavily.com/

        - naive RAG -> simple approach where retrieved documents are directly passed to the LLM models without optimization -> lead to irrelevant or redundant context
        - advanced RAG -> enhanced pipeline that applies techniques like query rewriting | filtering | reranking | summarization ... before and after retrieval to deliver more accurate & concise results
            + pre-retrieval -> reduce redundant context without losing the meaning - https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html#_pre_retrieval
                - convert input query into a more effective form
                - handle poor structured queries | resolve ambiguous terms | simplify complex vocab
                - adapt queries across unsupported languages for better search results
            + post-retrieval -> https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html#_post_retrieval
                - refine the retrieved documents before augmentation -> ranking relevance | compress content | mask sensitive information ...
     */

    private static final String TOKEN_PREFIX = "Bearer ";

    @Bean
    public ChatClient webSearchRagChatClient(
        OpenAiChatModel openAiChatModel,
        ChatMemory chatMemory,
        @Qualifier("tavilyRestClient") RestClient tavilyRestClient,
        TavilyProperties tavilyProperties
    ) {
        Advisor loggerAdvisor = new SimpleLoggerAdvisor();
        Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        Advisor webSearchRagAdvisor = RetrievalAugmentationAdvisor.builder() //rag with web search
                .documentRetriever(WebSearchDocumentRetriever.builder()
                        .resultLimit(tavilyProperties.getMaxResults())
                        .restClient(tavilyRestClient)
                        .build()
                )
                .build();

        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(loggerAdvisor, memoryAdvisor, webSearchRagAdvisor)
                .build();
    }

    @Bean
    public RestClient tavilyRestClient(TavilyProperties tavilyProperties) {
        return RestClient.builder()
                .baseUrl(tavilyProperties.getSearchBaseUrl())
                .defaultHeader(AUTHORIZATION, String.format("%s%s", TOKEN_PREFIX, tavilyProperties.getApiKey()))
                .build();
    }
}
