package org.tommap.springai.rag;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Builder
public class WebSearchDocumentRetriever implements DocumentRetriever {
    private final int resultLimit;
    private final RestClient restClient;

    @Override
    public List<Document> retrieve(Query query) {
        Assert.notNull(query, "query cannot be null!");

        log.info("processing user query: {}", query.text());

        TavilyResponsePayload response = restClient.post() //https://docs.tavily.com/documentation/api-reference/introduction
                .body(new TavilyRequestPayload(query.text(), "advanced", resultLimit))
                .retrieve()
                .body(TavilyResponsePayload.class);

        if (null == response || CollectionUtils.isEmpty(response.results())) {
            return List.of();
        }

        return response.results().stream()
                .map(hit -> Document.builder()
                        .text(hit.content())
                        .metadata("title", hit.title())
                        .metadata("url", hit.url())
                        .score(hit.score())
                        .build()
                )
                .toList();
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    record TavilyRequestPayload(String query, String searchDepth, int maxResults) {}

    record TavilyResponsePayload(List<Hit> results) {
        record Hit(String title, String url, String content, Double score) {}
    }
}
