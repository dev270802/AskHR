package com.example.pdfRag;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class PDFIngestionService {

    private final VectorStore vectorStore;

    public PDFIngestionService(VectorStore vectorStore){
        this.vectorStore = vectorStore;
    }

    public void execute(){
        PagePdfDocumentReader reader =
                new PagePdfDocumentReader(
                        new ClassPathResource(
                                "documents/Apex Global HR Policy Document.pdf"
                        )
                );

        List<Document> documents = reader.read();

        TokenTextSplitter splitter =
                TokenTextSplitter.builder()
                        .withChunkSize(500)
                        .withMinChunkSizeChars(200)
                        .withMinChunkLengthToEmbed(10)
                        .withMaxNumChunks(10000)
                        .withKeepSeparator(true)
                        .build();

        List<Document> chunks = splitter.apply(documents);

        for(Document chunk : chunks){
            chunk.getMetadata().put("Category","Human Resource");
        }

        // 3. Generate embeddings and store the chunks
        vectorStore.add(chunks);


    }

}
