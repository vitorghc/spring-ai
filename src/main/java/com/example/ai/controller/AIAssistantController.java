package com.example.ai.controller;

import com.example.ai.dto.response.ActorResponse;
import com.example.ai.service.AIAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class AIAssistantController {

    private final AIAssistantService AIAssistantService;

    //Monta um response em formato de String.
    @GetMapping("/informations")
    public String informationChat(@RequestParam String message) {
        return AIAssistantService.getInformation(message);
    }

    //Monta um response em formato de String porém async.
    @GetMapping("/informations-async")
    public Flux<String> informationChat1(@RequestParam String message) {
        return AIAssistantService.getInformationAsync(message);
    }

    //Monta um response em formato de ChatResponseMetadata
    @GetMapping("/informations-metadata")
    public ChatResponse informationChat2(@RequestParam String message) {
        return AIAssistantService.getInformationWithMetaData(message);
    }

    //Monta um response em formato de String porém com template específico.
    @GetMapping("/reviews-movie")
    public String reviewMovie(@RequestParam String movie) {
        return AIAssistantService.getMovieReview(movie);
    }

    //Monta um response em formato de Lista de String porém com template específico.
    @GetMapping("/reviews-movie-list")
    public List<String> reviewMovieList(@RequestParam String movie) {
        return AIAssistantService.getReviewMovieList(movie);
    }

    //Monta um response estruturado.
    @GetMapping("/reviews-actor")
    public ActorResponse reviewActor(@RequestParam String actor) {
        return AIAssistantService.getReviewActorWithActorResponse(actor);
    }

    //Retorna o que ele conseguiu entender da imagem em String.
    @GetMapping("/image-describe")
    public String describeImage() throws IOException {
        return AIAssistantService.describeImage();
    }

    //Retorna o que ele conseguiu entender da imagem e explica o código em String.
    @GetMapping("/image-code-describe")
    public String describeCodeImage() throws IOException {
        return AIAssistantService.describeCodeImage();
    }

    //Retorna um Map com nome do autor e os links de referência.
    @GetMapping("/author")
    public Map<String, Object> getAuthorLinks(@RequestParam String author) {
        return AIAssistantService.getAuthorLinks(author);
    }

    //Retorna o que conseguiu entender do audio em String.
    @GetMapping("/transcribe")
    public String transcribe() {
        return AIAssistantService.transcribe();
    }

    //Retorna o que conseguiu achar similar dentro do vetor.
    @GetMapping("/olympic-vector")
    public List<String> getSportInVector(@RequestParam String query) {
        return AIAssistantService.getSportInVector(query);
    }


}
