package com.example.tutor_bot.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorChatResponse {
    private String question;
    private String answer;
}