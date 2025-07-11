package org.apache.camel.dataloader.data.ingestion.common;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "camel.dataloader")
public interface IngestionSinkConfiguration {

    Qdrant qdrant();

    LangChain4jEmbeddings langChain4jEmbeddings();

    interface Qdrant {
        Collection collection();

        @WithDefault("384")
        int size();

        interface Collection {
            @WithDefault("camel")
            String name();
        }
    }

    interface LangChain4jEmbeddings {
        @WithDefault("assistantBean")
        String embeddingId();
    }
}
