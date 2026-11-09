package com.example.pdfRag;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(setterPrefix = "with")
public class RagResponse {
    String answer;
    List<String> source;
}
