package org.tommap.springai;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.qdrant.QdrantContainer;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public class AbstractIntegrationTest {
    protected static QdrantContainer qdrant = new QdrantContainer(DockerImageName.parse("qdrant/qdrant:latest"));

    static {
        Startables.deepStart(qdrant).join();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        //qdrant
        registry.add("spring.ai.vectorstore.qdrant.host", qdrant::getHost);
        registry.add("spring.ai.vectorstore.qdrant.port", qdrant::getGrpcPort);
    }
}
