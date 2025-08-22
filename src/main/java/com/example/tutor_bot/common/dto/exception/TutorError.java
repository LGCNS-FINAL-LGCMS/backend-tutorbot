package com.example.tutor_bot.common.dto.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TutorError implements ErrorCodeInterface{

    CHAT_PROCESSING_ERROR("CHAT-01", "채팅 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    CHAT_EMPTY_QUESTION("CHAT-02", "질문이 비어있습니다.", HttpStatus.BAD_REQUEST),
    PDF_PROCESSING_ERROR("PDF-01", "PDF 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PDF_NOT_FOUND("PDF-02", "PDF 파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ;

    private final String status;
    private final String message;
    private final HttpStatus httpStatus;

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.builder()
                .status(status)
                .message(message)
                .httpStatus(httpStatus)
                .build();
    }
}
