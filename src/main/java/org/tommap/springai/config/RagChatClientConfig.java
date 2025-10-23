package org.tommap.springai.config;

import org.springframework.context.annotation.Configuration;

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
     */
}
