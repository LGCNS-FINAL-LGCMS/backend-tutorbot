package com.example.tutor_bot.controller;

import com.example.tutor_bot.common.dto.BaseResponse;
import com.example.tutor_bot.common.dto.exception.TutorError;
import com.example.tutor_bot.service.PdfEtlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pdf-etl")
public class PdfEtlController {
    private final PdfEtlService pdfEtlService;

    @GetMapping("/process")
    public ResponseEntity<BaseResponse<String>> processDefaultPdf(){
        try{
            pdfEtlService.processClasspathPdf("data/Chapter10-RAG.pdf");
            return ResponseEntity.ok(BaseResponse.ok("PDF 처리가 완료되었습니다."));
        }catch(Exception e){
            TutorError error = TutorError.PDF_PROCESSING_ERROR;
            return ResponseEntity
                    .status(error.getHttpStatus())
                    .body(BaseResponse.onFailure(error.getStatus(), "PDF 처리 실패: " + e.getMessage(),  null));
        }
    }
}
