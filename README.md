# Backend-TutorBot

# 🖥️ 서버 설명
- 온라인 강의 플랫폼을 위한 백엔드 튜터 챗봇 서버입니다.
- 학습 질의응답: 수강생이 학습 내용과 관련된 질문을 입력하면 튜터 챗봇이 강의 자료를 기반으로 답변을 제공합니다.
- 학습 상호작용: 강의별 학습 내용을 중심으로 수강생과 튜터 챗봇 간의 질의응답을 지원하여 학습 효과와 소통을 지원합니다.
- 비동기 처리: Kafka를 활용하여 강의 교안 파일(PDF) 임베딩 이벤트를 비동기적으로 처리함으로써 시스템 성능과 확장성을 확보합니다.


  - 패키지 구조

      ```
      tutor_bot
       ├─ main
       │   ├─ java/com/example/tutor_bot
       │   │   ├─ advice
       │   │   ├─ common
       │   │   │   ├─ aspect
       │   │   │   ├─ dto
       │   │   │   │  └─ exception
       │   │   │   └─ kafka
       │   │   │      ├─ dto
       │   │   │      └─ utils
       │   │   │          └─ serializer
       │   │   ├─ config
       │   │   ├─ controller
       │   │   ├─ dto
       │   │   │   ├─ request
       │   │   │   └─ response
       │   │   ├─ event
       │   │   │   ├─ consumer
       │   │   │   └─ producer
       │   │   └─ service
       │   └─ resources
       │       └─ prompts
       └─ test/java/com/example/tutor_bot
    
      ```


# 👨🏻‍💻 담당자

| 이름  | 역할                    |
|-----|-----------------------|
| 정수연 | 튜터 챗봇 개발              |
| 심규환 | Kafka 이벤트 처리 |
| 이재원 | CI/CD, 모니터링           |

---

# 🛠️ 기술 스택

### Languages

![java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=openjdk&logoColor=white)

### Framework

![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat-square&logo=spring-boot&logoColor=white)

### Middleware

![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?style=flat-square&logo=apache-kafka&logoColor=white)
![OpenTelemetry](https://img.shields.io/badge/OpenTelemetry-FFB01F?style=flat-square&logo=opentelemetry&logoColor=black)
### Database

![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white)


---

# 📌 기능

### 강의 생성 및 kafka 강의 자료 임베딩 이벤트

- 강사 → API 서버: 강의 메타데이터(제목, 설명 등) 및 강의 자료를 등록 요청합니다.
- API 서버: 강의 정보를 DB에 초기 저장하고 고유 ID를 생성합니다.
- 외부 인코딩 서버 → Kafka: 강의 자료 업로드 및 인코딩 완료 후, Kafka Embedding 이벤트를 발행합니다.
- API 서버 (Kafka Consumer): 이벤트를 수신하여 해당 강의 ID와 강의 자료를 pgvector에 저장합니다.


- 설명: 강사가 강의를 등록하고 강의 자료를 업로드하면 외부 서버에서 인코딩이 진행됩니다. 인코딩이 완료되면 Kafka 이벤트를 통해 강의 자료를 pgvector에 임베딩하여 검색 및 질의응답에 활용합니다.

### 강의별 맞춤 학습 Q&A

- 수강생 → API 서버: 특정 강의에 대한 질문을 등록합니다.
- API 서버: 질문을 redis에 저장합니다.
- 튜터 → API 서버: 학습 자료를 기반으로 등록된 질문에 대한 답변을 생성합니다.
- API 서버: 답변을 redis에 저장합니다.


- 설명: 수강생이 강의와 관련된 질문을 하면 튜터 챗봇이 강의 자료를 기반으로 답변을 제공합니다. 이를 통해 학습자는 실시간 피드백을 받아 이해도를 높이고 학습 효율성을 극대화할 수 있습니다.

---

# 📜 주요 기능

### System-Prompt 기반 챗봇 동작
  - 프롬프트에 페르소나와 조건을 정의하여 답변을 생성
  - 강의 자료 기반 응답을 우선 제공하며, 부족한 경우 RAG를 통해 보완

### PDF 임베딩 및 검색 최적화
  - PDF 파일을 `pgvector`에 임베딩하여 정확한 검색 지원
  - 청크 분할 및 공백 처리 최적화를 통해 검색 품질 향상

### Redis 기반 멀티턴 대화 지원
  - 최근 대화 기록을 Redis에 저장하여 연속적인 질의응답 가능
  - 사용자별·강의별 대화 세션 관리

### RAG(Retrieval-Augmented Generation) 통합
  - 강의 자료에 없는 질문은 외부 공식 데이터 소스를 검색하여 응답 보완
  - 검색 결과와 LLM 응답을 결합해 정확도 및 신뢰도 향상

---

# ⚡ 트러블슈팅
### Tutor Chatbot System-Prompt 작성의 중요성
- 프롬프트의 구성 방식에 따라 튜터 챗봇의 응답 품질이 크게 달라진다는 점을 확인했습니다.
- 개발 초기에는 단순히 페르소나만 정의했으나 튜터 챗봇이 기획 의도대로 동작하지 않는 문제를 경험했습니다. 이후 프롬프트를 재구성하는 과정에서 응답 품질이 크게 달라지는 것을 확인하며 프롬프트 작성의 중요성을 명확히 인식하게 되었습니다.
- 강의 자료를 기반으로 학습 지원을 제공하되 해당 자료에 질문에 대한 충분한 설명이 없는 경우 RAG(Retrieval-Augmented Generation)를 활용해 보완하도록 기획했습니다.

### PDF 파일 임베딩 시 청크 분할 전략
- PDF 파일을 `pgvector`에 임베딩할 때 청크 분할 방식에 따라 검색 정확도와 데이터 품질이 크게 달라진다는 것을 알게 되었습니다.
- 초기에는 공백 처리 문제로 인해 검색 성능이 저하되는 문제가 있었으나 청크를 더 세분화하고 공백 처리를 개선하여 `content`에 보다 안정적으로 저장되도록 조정했습니다.

### Redis 기반 ChatMemory 구현
- Redis를 사용해 ChatMemory를 구현하여 수강생과 튜터 간의 질의응답 기록을 저장했습니다.
- 그러나 시스템 프롬프트 조건과의 불일치로 인해 이전 대화 내용을 불러오지 못하는 문제가 발생하였고 이를 해결하기 위해 시스템 프롬프트 조건 및 데이터 구조를 재검토했습니다.

---

# 💡 느낀점
### Tutor Chatbot System-Prompt의 중요성
- 프롬프트의 페르소나와 조건을 변경할 때마다 응답 결과가 크게 달라지는 경험을 통해 원하는 성능의 챗봇을 구현하기 위해서는 **정확하고 체계적인 프롬프트 작성**이 필수적임을 깨달았습니다.