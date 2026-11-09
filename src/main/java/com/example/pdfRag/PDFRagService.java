package com.example.pdfRag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PDFRagService {
    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    @Value("${app.rag.top-k:5}")
    private int topK;

    @Value("${app.rag.similarity-threshold:0.75}")
    private double similarityThreshold;

    public PDFRagService(VectorStore vectorStore, ChatClient.Builder chatClient){
        this.vectorStore = vectorStore;
        this.chatClient = chatClient.build();
    }

    public RagResponse ask(String question){
        List<Document> chunks =
                vectorStore.similaritySearch(
                        SearchRequest.builder()
                                .query(question)
                                .topK(topK)
                                .build()
                );

        if(chunks.isEmpty()){
            return RagResponse.builder().withAnswer("Couldn't find relevant information in the document").build() ;
        }

        String context = chunks.stream().map(Document::getText).reduce("",(a,b)->a+"/n/n"+b);

        String answer = chatClient.prompt().system("""
                        You are a helpful assistant. Answer the user's question using ONLY the supplied context.
                        
                            Guidelines:
                            1. Account for minor typos, spelling errors, or related terminology in the user's question.
                            2. If the context contains information answering the intent of the user's question, provide a clear answer based solely on that context.
                            3. If the supplied context does not contain the answer or related relevant facts, respond strictly with: "I don't know based on the available documents."
                            4. Do not invent details or use external knowledge outside the provided context.
                        """)
                        .user("""
                            Context:
                            %s
                        
                            Question:
                            %s
                            """.formatted(context, question))
                .call()
                .content();
        return RagResponse.builder().
                withAnswer(answer).
                withSource(chunks.stream().map(Document::getText).toList()).build();
    }
}
