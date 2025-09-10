package com.example.tutor_bot.service;

import com.example.tutor_bot.common.dto.exception.BaseException;
import com.example.tutor_bot.common.dto.exception.TutorError;
import com.example.tutor_bot.dto.request.ChatRequest;
import com.example.tutor_bot.dto.response.TutorChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    public TutorChatResponse processQuestion(ChatRequest request, Long memberId) {
        try {
            String lectureId = request.getLectureId();

            if(lectureId == null ||lectureId.isBlank()) {
                throw new IllegalAccessException("lectureId는 null 값 허용하지 않습니다.");
            }
            String question = request.getQuestion();
            List<String> previousChats = chatMemoryService.getChatMessage(lectureId, memberId)
                    .stream()
                    .filter(msg->!msg.contains(NEGATIVE_MSG))
                    .toList();
          
            List<Message> messages = new ArrayList<>();
            for(int i = 0; i<previousChats.size(); i++){
                String msg = previousChats.get(i);
                if(i % 2 == 0) messages.add(new UserMessage(msg));
                else messages.add(new AssistantMessage(msg));
            }

            String context = buildContext(searchRelevantDocuments(question, lectureId));
            if(!context.isBlank()){
                messages.add(new UserMessage("참고 자료: " + context));
            }
            messages.add(new UserMessage(question));

            String answer = chatClient.prompt()
                    .messages(messages)
                    .call()
                    .content();

            if(!containsNegativeMsg(answer)){
                chatMemoryService.saveChatMessage(lectureId, memberId, question);
                chatMemoryService.saveChatMessage(lectureId, memberId, answer);
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

    private List<Document> searchRelevantDocuments(String query, String lectureId) {
        FilterExpressionBuilder filterExpressionBuilder = new FilterExpressionBuilder();
        Filter.Expression metadataLectureId = filterExpressionBuilder.eq("lectureId", lectureId).build();

        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .similarityThreshold(similarityThreshold)
                .filterExpression(metadataLectureId)
                .build();

        return vectorStore.similaritySearch(searchRequest);
    }

    private String buildContext(List<Document> documents) {
        StringBuilder context = new StringBuilder();

        for (Document doc : documents) {
            context.append(doc.getText());
        }

        return context.toString();
    }
}