package com.example.tutor_bot.event.consumer;

import com.example.tutor_bot.common.kafka.dto.KafkaEvent;
import com.example.tutor_bot.common.kafka.dto.LectureUploadDto;
import com.example.tutor_bot.common.kafka.utils.KafkaEventFactory;
import com.example.tutor_bot.service.PdfEtlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LectureEventConsumer {
    private final PdfEtlService pdfEtlService;
    private final KafkaEventFactory  kafkaEventFactory;

    // 토픽 구독 후 이벤트 받음
    @KafkaListener(topics = "LECTURE_UPLOAD", containerFactory = "defaultFactory")
    public void topicEvent(KafkaEvent<?> kafkaEvent) {
        LectureUploadDto uploadDto = kafkaEventFactory.convert(kafkaEvent, LectureUploadDto.class);
        pdfEtlService.processUrlPdf(uploadDto.getBookKey());
    }
}
