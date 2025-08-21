package com.example.tutor_bot.controller;

import com.example.tutor_bot.common.dto.BaseResponse;
import com.example.tutor_bot.service.PdfEtlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.ClassPathResource;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PdfEtlController {
    private final PdfEtlService pdfEtlService;

    @GetMapping("/process")
    public ResponseEntity<BaseResponse<String>> processDefaultPdf() {
        ClassPathResource resource = new ClassPathResource("data/Chapter10-RAG.pdf");
        pdfEtlService.processClasspathPdf(resource);
        return ResponseEntity.ok(BaseResponse.ok("PDF 처리가 완료되었습니다."));
    }
}
