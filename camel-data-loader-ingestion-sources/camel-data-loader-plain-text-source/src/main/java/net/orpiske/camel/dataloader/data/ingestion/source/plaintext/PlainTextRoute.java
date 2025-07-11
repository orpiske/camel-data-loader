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

package net.orpiske.camel.dataloader.data.ingestion.source.plaintext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkus.arc.Unremovable;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import net.orpiske.camel.dataloader.data.ingestion.source.common.IngestionSourceConfiguration;
import net.orpiske.camel.dataloader.data.ingestion.source.common.SplitterUtil;
import net.orpiske.camel.dataloader.data.ingestion.source.common.UserParams;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.tokenizer.LangChain4jTokenizerDefinition;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jboss.logging.Logger;

@ApplicationScoped
@Unremovable
public class PlainTextRoute extends RouteBuilder {
    private static final Logger LOG = Logger.getLogger(PlainTextRoute.class);

    @Inject
    CamelContext context;

    @Inject
    IngestionSourceConfiguration configuration;

    private void convertBytesToPDFFile(Exchange e) throws IOException {
        final byte[] body = e.getMessage().getBody(byte[].class);

        // TODO: Camel should probably do this itself
        PDDocument document = Loader.loadPDF(body);

        String removePages = e.getMessage().getHeader(UserParams.REMOVE_PAGES, String.class);
        if (removePages != null) {
            LOG.infof("Removing pages %s", removePages);

            final List<Integer> pagesToRemove = getPagesToRemove(removePages);

            pagesToRemove.sort(Collections.reverseOrder());
            for (int page : pagesToRemove) {
                LOG.infof("Removing page %d", page);
                document.removePage(page);
            }
        }

        e.getMessage().setBody(document);
    }

    private static List<Integer> getPagesToRemove(String removePages) {
        String[] ranges = removePages.split(",");
        List<Integer> pagesToRemove = new ArrayList<>();
        for (String range : ranges) {
            if (range.contains("-")) {
                String[] pages = range.split("-");
                int initialPage = Integer.parseInt(pages[0]);
                int endPage = Integer.parseInt(pages[1]);

                for (int i = initialPage; i < endPage; i++) {
                    pagesToRemove.add(i - 1);
                }
            } else {
                int page = Integer.parseInt(range);

                pagesToRemove.add(page - 1);
            }
        }
        return pagesToRemove;
    }

    @Override
    public void configure() {
        rest("/api")
                .get("/hello").to("direct:hello")
                .post("/consume/text/static").to("direct:consumeTextStatic")
                .post("/consume/text/dynamic/{source}/{id}").consumes("application/octet-stream").to("direct:consumeTextDynamic")
                .post("/consume/pdf/static").to("direct:consumePdfStatic")
                .post("/consume/file/static").to("direct:consumeFileStatic");

        from("direct:hello")
                .routeId("source-web-hello")
                .transform().constant("Hello World");

        // A slightly more complex configuration, so that we can have flexibility choosing the splitter
        final LangChain4jTokenizerDefinition tokenizerDefinition = SplitterUtil.createTokenizer(tokenizer(), configuration);

        from("direct:consumePdfStatic")
                .routeId("source-consume-pdf-static-route")
                .process(this::convertBytesToPDFFile)
                .pipeline()
                .to("pdf:extractText")
                .to("seda:chunk?waitForTaskToComplete=Never")
                .transform().constant("Static data queued for chunking and loading");

        from("direct:consumeFileStatic")
                .routeId("source-consume-file-static-route")
                .to("seda:chunk?waitForTaskToComplete=Never")
                .transform().constant("Static data queued for chunking and loading");

        from("seda:chunk?concurrentConsumers=2&exchangePattern=InOnly")
                .routeId("source-chunk-route")
                .tokenize(tokenizerDefinition)
                .split().body()
                        .to("kafka:ingestion");

        from("direct:consumeTextDynamic")
                .routeId("source-consume-text-dynamic-route")
                .log("Received ${body}")
                .setHeader("dynamic", constant("true"))
                .to("kafka:ingestion")
                .transform().constant("Dynamic data loaded");

        from("direct:consumeTextStatic")
                .routeId("source-consume-text-static-route")
                .log("Received ${body}")
                .to("kafka:ingestion")
                .transform().constant("Static data loaded");
    }
}
