/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.orpiske.camel.dataloader.data.ingestion.sink;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class Resources {

    @ConfigProperty(name = "camel.dataloader.embedding.provider.url", defaultValue = "http://localhost:11434")
    String embeddedProviderUrl;

    @ConfigProperty(name = "camel.dataloader.embedding.provider.model.name", defaultValue = "nomic-embed-text:latest")
    String modelName;

    @Produces
    EmbeddingModel model() {
        return OllamaEmbeddingModel.builder()
                .baseUrl(embeddedProviderUrl)
                .modelName(modelName)
                .build();
    }
}
