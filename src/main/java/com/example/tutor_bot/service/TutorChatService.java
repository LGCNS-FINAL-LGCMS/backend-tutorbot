package com.example.tutor_bot.service;

import com.example.tutor_bot.common.dto.exception.BaseException;
import com.example.tutor_bot.common.dto.exception.TutorError;
import com.example.tutor_bot.dto.request.ChatRequest;
import com.example.tutor_bot.dto.response.TutorChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TutorChatService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final ChatMemoryService chatMemoryService;

    private static final String NEGATIVE_MSG = "죄송";

    @Value("${spring.ai.bedrock.converse.chat.options.search.similarity-threshold}")
    private Double similarityThreshold;

    public TutorChatResponse processQuestion(ChatRequest request) {
        try {
            String lectureId = request.getLectureId();
            String userId = request.getUserId();
            String question = request.getQuestion();
            String context = buildContext(searchRelevantDocuments(question));

            List<String> previousChats = chatMemoryService.getChatMessage(lectureId, userId);

            String history = String.join("\n", previousChats);
            String currentUserPrompt = buildPrompt(question, context);
            String userPrompt = history + "\n" + currentUserPrompt;

            String answer = chatClient.prompt()
                    .user(userPrompt)
                    .call()
                    .content();

            if(!containsNegativeMsg(answer)){
                chatMemoryService.saveChatMessage(lectureId, userId, question);
                chatMemoryService.saveChatMessage(lectureId, userId, answer);
            }

            return TutorChatResponse.builder()
                    .question(question)
                    .answer(answer)
                    .build();

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BaseException(TutorError.CHAT_PROCESSING_ERROR);
        }
    }

    private boolean containsNegativeMsg(String answer) {
        return answer !=  null && answer.contains(NEGATIVE_MSG);
    }

    private List<Document> searchRelevantDocuments(String query) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .similarityThreshold(similarityThreshold)
                .build();

        return vectorStore.similaritySearch(searchRequest);
    }

    private String buildContext(List<Document> documents) {
        if (documents.isEmpty()) {
            return "관련 정보를 찾을 수 없습니다.";
        }
        StringBuilder context = new StringBuilder();

        for (Document doc : documents) {
            context.append(doc.getText());
        }

        return context.toString();
    }

    private String buildPrompt(String question, String context) {
        return String.format("참고 자료: %s, 질문: %s", context, question);
    }
}