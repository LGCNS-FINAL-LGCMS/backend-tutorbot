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

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfEtlService {
    private final VectorStore vectorStore;

    // 이벤트 발행 시에만 사용 new
    public void processUrlPdf(String pdfUrl) {
        try{
            Resource pdfResource = new UrlResource(new URI(pdfUrl).toURL());
            if(!pdfResource.exists()) {
                throw new BaseException(TutorError.PDF_NOT_FOUND);
            }

            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(
                    pdfResource,
                    PdfDocumentReaderConfig.builder()
                            .withPageExtractedTextFormatter(
                                    new ExtractedTextFormatter.Builder()
                                            .withNumberOfBottomTextLinesToDelete(3)
                                            .withNumberOfTopPagesToSkipBeforeDelete(1)
                                            .build()
                            )
                            .build());

            List<Document> documents = pdfReader.get();

            TokenTextSplitter textSplitter = new TokenTextSplitter(1000,200,50,10000,true);
            // 1000, 400, 10, 5000, true
            List<Document> chunks = textSplitter.apply(documents);

            Resource finalPdfResource = pdfResource;
            chunks.forEach(chunk -> {
                chunk.getMetadata().put("source", finalPdfResource.getFilename());
                chunk.getMetadata().put("type", "pdf");
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