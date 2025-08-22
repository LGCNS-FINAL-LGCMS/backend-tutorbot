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
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfEtlService {
    private final VectorStore vectorStore;

    // 클래스 패스에서 pdf 리소스 처리
    public void processClasspathPdf(Resource pdfResource) {
        try{
            if(!pdfResource.exists()) {
                throw new BaseException(TutorError.PDF_NOT_FOUND);
            }

            // pdf 리더 설정
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

            // 텍스트 분할
            TokenTextSplitter textSplitter = new TokenTextSplitter(1000,200,50,10000,true);

            // 텍스트 청크 분할
            List<Document> chunks = textSplitter.apply(documents);

            // 메타데이터 추가
            chunks.forEach(chunk -> {
                chunk.getMetadata().put("source", pdfResource.getFilename());
                chunk.getMetadata().put("type", "pdf");
                chunk.getMetadata().put("processed_at", System.currentTimeMillis());
                chunk.getMetadata().put("chunk_size", chunk.getFormattedContent().length());
            });

            // 벡터 스토어에 저장
            vectorStore.add(chunks);

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BaseException(TutorError.PDF_PROCESSING_ERROR);
        }
    }
}