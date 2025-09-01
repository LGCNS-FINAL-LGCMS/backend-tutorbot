package com.example.tutor_bot.controller;

import com.example.tutor_bot.common.dto.BaseResponse;
import com.example.tutor_bot.common.dto.exception.BaseException;
import com.example.tutor_bot.common.dto.exception.TutorError;
import com.example.tutor_bot.dto.request.ChatRequest;
import com.example.tutor_bot.dto.response.TutorChatResponse;
import com.example.tutor_bot.service.TutorChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final TutorChatService tutorChatService;

    @PostMapping("/student/tutor")
    public ResponseEntity<BaseResponse<TutorChatResponse>> askQuestion(@RequestBody ChatRequest request) {
        if(request.getQuestion() == null || request.getQuestion().trim().isEmpty()){
            throw new BaseException(TutorError.CHAT_EMPTY_QUESTION);
        }
        TutorChatResponse response = tutorChatService.processQuestion(request);
        return ResponseEntity.ok(BaseResponse.ok(response));
    }
}