# Camel Data

## Run Ollama Locally

We will run Ollama to serve the embedding model.

1. Install and run Ollama (or any other OpenAI compatible API in a host accessible from the containers)

```shell
ollama serve
```

2. Pull the `mistral:latest` model on the host you are running Ollama

```shell
ollama pull nomic-embed-text:latest
```


## Trying it using Docker Compose

1. Start the containers using Docker Compose

```shell
docker-compose up
```

2. Proceed to the Loading Data section for details about how to load data

## Trying it manually

### Requirements 

- A Kafka instance up and running and able to receive remote requests. 
- Podman installed and running (locally or remote)

NOTE: URLs and hostnames can be configured in the `application.properties` file or exported via environment variables. For instance
if using Qdrant in another host, you can set its host using the `QDRANT_HOST` variable.

### Steps

1. Build the project

```shell
mvn clean package
```

2. Launch Qdrant:

```shell
podman run -d --rm --name qdrant -p 6334:6334 -p 6333:6333 -v $(pwd)/qdrant_storage:/qdrant/storage:z qdrant/qdrant:v1.13.6-unprivileged
```

3. Launch the ingestion sink: 

```shell
KAFKA_BROKERS=kafka-host:9092 java -jar ./assistant-ingestion-sink/target/quarkus-app/quarkus-run.jar
```

4. Launch the ingestion source:

```shell
KAFKA_BROKERS=kafka-host:9092 java -jar ./assistant-ingestion-sources/plain-text-source/target/quarkus-app/quarkus-run.jar
```

# Loading Data 

## Loading PDFs

To load PDF data (such as those from documentation, books, etc) into the QDrant DB, use the command:

```shell
cd camel-data-loader-cli && java -jar target/quarkus-app/quarkus-run.jar consume file /path/to/my-pdf-document.pdf
```

## Loading Datasets 

You can load data from the [Camel Dataset](https://huggingface.co/megacamelus). 

To download the dataset for data formats, components, EIPs and languages:

```shell
huggingface-cli download --repo-type dataset --local-dir camel-dataformats megacamelus/camel-dataformats
huggingface-cli download --repo-type dataset --local-dir camel-components megacamelus/camel-components
huggingface-cli download --repo-type dataset --local-dir camel-eips megacamelus/camel-eips
huggingface-cli download --repo-type dataset --local-dir camel-languages megacamelus/camel-languages
```

To download the dataset for components:

```shell
huggingface-cli download --repo-type dataset --local-dir camel-components megacamelus/camel-components
```

Use this command to load the dataset into the DB:

```shell
java -jar camel-data-loader-cli/target/quarkus-app/quarkus-run.jar consume dataset --path ~/code/datasets/camel-dataformats/ --source org.apache.camel
java -jar camel-data-loader-cli/target/quarkus-app/quarkus-run.jar consume dataset --path ~/code/datasets/camel-components/ --source org.apache.camel
java -jar camel-data-loader-cli/target/quarkus-app/quarkus-run.jar consume dataset --path ~/code/datasets/camel-eips/ --source org.apache.camel
java -jar camel-data-loader-cli/target/quarkus-app/quarkus-run.jar consume dataset --path ~/code/datasets/camel-languages/ --source org.apache.camel
```

## Checking if the data was loaded

Wait a few seconds after running the load command, and then check if the data is available in the Qdrant DB:

```shell
curl -X POST http://localhost:6333/collections/camel/points/scroll -H "Content-Type: application/json" -d "{\"limit\": 50 }" | jq .
```

## Building 

To build the containers locally: 

```shell
mvn clean package -Dquarkus.container-image.build=true
```

