package com.example.tutor_bot.service;

import com.example.tutor_bot.common.dto.exception.BaseException;
import com.example.tutor_bot.common.dto.exception.TutorError;
import org.springframework.ai.vectorstore.VectorStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfEtlService {
    private final VectorStore vectorStore;

    // 이벤트 발행 시에만 사용 new
    public void processUrlPdf(String pdfUrl, String lectureId) {
        try{
            Resource pdfResource = new UrlResource(new URI(pdfUrl).toURL());
            if(!pdfResource.exists()) {
                throw new BaseException(TutorError.PDF_NOT_FOUND);
            }

            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(
                    pdfResource,
                    PdfDocumentReaderConfig.builder()
                            .withPageBottomMargin(0)
                            .withPageExtractedTextFormatter(
                                    new ExtractedTextFormatter.Builder()
                                            .withNumberOfTopTextLinesToDelete(0)
                                            .withNumberOfBottomTextLinesToDelete(0)
                                            .withNumberOfTopPagesToSkipBeforeDelete(0)
                                            .withLeftAlignment(true)
                                            .build()
                            )
                            .withPagesPerDocument(1)
                            .build());

            List<Document> documents = pdfReader.get();

            // pdf 공백 처리
            List<Document> trimContent = documents.stream()
                    .map(doc -> {
                        String content = doc.getFormattedContent();
                        String normalizedContent = content.replaceAll("\\s+", " ");
                        return new Document(normalizedContent, doc.getMetadata());

                    })
                    .collect(Collectors.toList());

            TokenTextSplitter textSplitter = new TokenTextSplitter(500,0,10,5000,true);

            List<Document> chunks = textSplitter.apply(trimContent);

            Resource finalPdfResource = pdfResource;
            chunks.forEach(chunk -> {
                chunk.getMetadata().put("source", finalPdfResource.getFilename());
                chunk.getMetadata().put("type", "pdf");
                chunk.getMetadata().put("lectureId", lectureId);
                chunk.getMetadata().put("processed_at", System.currentTimeMillis());
                chunk.getMetadata().put("chunk_size", chunk.getFormattedContent().length());
            });

            vectorStore.add(chunks);

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BaseException(TutorError.PDF_PROCESSING_ERROR);
        }
    }
}