package org.tommap.springai.rag;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HrPolicyDataLoader {
    @Value("classpath:/tom_hr_policies.pdf")
    Resource hrPolicies;

    private final VectorStore vectorStore;

    @PostConstruct
    public void loadPdf() {
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(hrPolicies);
        List<Document> documents = tikaDocumentReader.get();

        /*
            - split original documents to smaller chunks
            - but who is calculating the embedded values for the document chunks?
                + embeddings -> complex process to calculate vector numbers for a given text
                + EmbeddingModel(I)-> EmbeddingResponse call(EmbeddingRequest request) -> method to calculate embedded values for each of the document
         */
        TextSplitter textSplitter = TokenTextSplitter.builder()
                .withChunkSize(200) //max number of tokens per chunk
                .withMaxNumChunks(400) //max number of chunks to generate from a text
                .build();

        vectorStore.add(textSplitter.split(documents));
    }
}
