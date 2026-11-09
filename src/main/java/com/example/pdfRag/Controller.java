package com.example.pdfRag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    private final PDFIngestionService ingestionService;
    private final PDFRagService ragService;

    public Controller(PDFIngestionService pdfIngestionService, PDFRagService ragService){
        this.ingestionService = pdfIngestionService;
        this.ragService = ragService;
    }

    @PostMapping("/ingest")
    public String ingest() {
        ingestionService.execute();

        return "PDF ingested successfully";
    }

    @GetMapping("/ask")
    public RagResponse ask(@RequestParam String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question must not be empty"
            );
        }

        if (question.length() > 2000) {
            throw new IllegalArgumentException(
                    "Question must not exceed 2000 characters"
            );
        }
        return ragService.ask(question);
    }

}
