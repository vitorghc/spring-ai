package com.example.ai.service;

import com.example.ai.dto.response.ActorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Media;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.openai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.ai.parser.ListOutputParser;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIAssistantService {

    private final VectorStore vectorStore;

    private final ChatClient.Builder chatClient;

    private final OpenAiAudioTranscriptionModel transcriptionModel;


    //Monta um response em formato de String.
    public String getInformation(String message) {
        log.info("Starting call to the chat. Question: {}", message);
        String response = chatClient.build()
                .prompt()
                .user(message)
                .call()
                .content();
        return response;
    }

    //Monta um response em formato de Json com meta dados.
    public ChatResponse getInformationWithMetaData(String message) {
        log.info("Starting call to the chat. Question: {}", message);
        ChatResponse response = chatClient.build()
                .prompt()
                .user(message)
                .call()
                .chatResponse();
        return response;
    }

    //Monta um response em formato de Json com meta dados assíncrono.
    public Flux<String> getInformationAsync(String message) {
        log.info("Starting call to the chat. Question: {}", message);
        Flux<String> response = chatClient.build()
                .prompt()
                .user(message)
                .stream()
                .content();
        return response;
    }

    public String getMovieReview(String movie) {
        PromptTemplate promptTemplate = new PromptTemplate("Forneça um resumo do filme {movie}.");
        promptTemplate.add("movie", movie);
        String response = chatClient.build()
                .prompt(promptTemplate.create())
                .call()
                .content();
        return response;
    }

    public List<String> getReviewMovieList(String movie) {
        PromptTemplate promptTemplate = new PromptTemplate("Forneça um resumo do filme {movie}. {format}");

        ListOutputParser outputParser = new ListOutputParser(new DefaultConversionService());

        promptTemplate.add("movie", movie);
        promptTemplate.add("format", outputParser.getFormat());

        String prompt = chatClient.build()
                .prompt(promptTemplate.create())
                .call()
                .content();
        List<String> response = outputParser.parse(prompt);
        return response;
    }

    public ActorResponse getReviewActorWithActorResponse(String actor) {
        PromptTemplate promptTemplate = new PromptTemplate("""
        Forneça a lista de filmes que o {actor}.
        {format}
        """);

        BeanOutputParser<ActorResponse> outputParser = new BeanOutputParser<>(ActorResponse.class);

        promptTemplate.add("actor", actor);
        promptTemplate.add("format", outputParser.getFormat());
        String prompt = chatClient.build()
                .prompt(promptTemplate.create())
                .call()
                .content();
        ActorResponse response = outputParser.parse(prompt);
        return response;
    }

    public String describeImage() throws IOException {
        byte[] imageData = new ClassPathResource("/images/logo.jpg").getContentAsByteArray();
        UserMessage userMessage = new UserMessage("Pode explicar o que tem na imagem?", List.of(new Media(MimeTypeUtils.IMAGE_JPEG, imageData)));

        String response = chatClient.build()
                .prompt(new Prompt(userMessage))
                .call()
                .content();
        return response;
    }

    public String describeCodeImage() throws IOException {
        byte[] imageData = new ClassPathResource("/images/code.png").getContentAsByteArray();
        UserMessage userMessage = new UserMessage("Dado a seguinte imagem, pode explicar o que o código está fazendo?", List.of(new Media(MimeTypeUtils.IMAGE_PNG, imageData)));

        String response = chatClient.build()
                .prompt(new Prompt(userMessage))
                .call()
                .content();
        return response;
    }

    public String transcribe() {
        ClassPathResource classPathResource = new ClassPathResource("/audio/audio.flac");

        OpenAiAudioTranscriptionOptions transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
                .withResponseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                .withTemperature(0f)
                //Baixa Temperatura (próxima de 0): Torna a saída mais determinística e focada. O modelo tende a repetir as respostas mais prováveis e previsíveis.
                //Alta Temperatura (próxima de 1): Torna a saída mais aleatória e criativa. O modelo pode produzir respostas menos previsíveis e mais diversas.
                .build();
        AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(classPathResource, transcriptionOptions);
        AudioTranscriptionResponse audioTranscriptionResponse = transcriptionModel.call(transcriptionRequest);
        String response = new String(audioTranscriptionResponse.getResult()
                .getOutput()
                .getBytes(), StandardCharsets.UTF_8);
        return response;
    }

    public Map<String, Object> getAuthorLinks(String author) {
        String promptMessage = """
                Gerar uma lista com os links de referência para o autor {author}.
                {format}
                """;

        MapOutputConverter outputParser = new MapOutputConverter();
        String format = outputParser.getFormat();

        PromptTemplate promptTemplate = new PromptTemplate(promptMessage, Map.of("author", author, "format", format));
        Prompt prompt = promptTemplate.create();
        ChatClient.CallPromptResponseSpec call = chatClient.build()
                .prompt(prompt)
                .call();
        String content = call.content();
        Map<String, Object> response = outputParser.parse(content);
        return response;
    }

    public List<String> getSportInVector(String query) {
        List<Document> documents = List.of(
                new Document("Esportes olímpicos: Arremesso de chinelo, Archery, athletics, badminton, basketball , basketball 3×3, boxing, canoe slalom"),
                new Document("Olimpíadas 2024 vai ser em París"),
                new Document("Jogos da Olimpíadas: 2024 vai ser na Russia"),
                new Document("Jogo de baseball"),
                new Document("Jogo")
        );

        vectorStore.add(documents);

        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.defaults()
                        .withQuery(query)
                        .withTopK(2)
                        .withSimilarityThreshold(0.0));

        List<String> list = results.stream()
                .map(Document::getContent)
                .toList();
        return list;
    }

}
