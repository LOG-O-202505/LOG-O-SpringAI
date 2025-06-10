# LOG-O Spring 백엔드 프로젝트

## 🔧 초기 설정 가이드

### 환경 변수 설정 (필수)

프로젝트를 실행하기 전에 `application.yml` 파일내 환경 변수들을 설정해줘야 합니다.

1. **Spring 설정**
    ```bash
    spring:
      ...
      data:
        redis:
          host: ${REDIS_HOST} #Redis DB URL 주소 (ex. localhot)
          port: 6379 #Redis port number
      ...
      ai:
        openai:
          api-key: ${OPENAI_API_KEY} #사용자 OpenAI Key(ChatGPT Key)
          chat:
            options:
              model: ${GPT_MODEL} #GPT Model(ex. gpt-4o)
        anthropic:
          api-key: ${ANTHROPIC_API_KEY} #사용자 AnthropicAI Key(Claude Key)
          chat:
            options:
              model: ${CLAUDE_MODEL} #Claude Model(ex. claude-3-7-sonnet-latest)
              max-tokens: 40000
              temperature: 0.7
      datasource:
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: ${DB_URL} #MySQL DB URL (ex. jdbc:mysql://{DB 주소}:{DB PORT}/{DB 이름}?useSSL\=false&serverTimezone\=Asia/Seoul&characterEncoding\=UTF-8&allowPublicKeyRetrieval\=true&useUnicode\=true)
        username: ${DB_USER} #MySQL uername (ex. root)
        password: ${DB_PASSWORD} #MySQL password (ex. root)
      ...
      security:
        oauth2:
          client:
            ...
            registration:
              kakao:
                client-id: ${KAKAO_CLIENT_ID} #사용자 Kakao Client ID
                client-secret: ${KAKAO_CLIENT_SECRET} #사용자 Kakao Client Secret key
                ...
              naver:
                client-id: ${NAVER_CLIENT_ID} #사용자 Naver Client ID
                client-secret: ${NAVER_CLIENT_SECRET} #사용자 Naver Client Secret key
                ...
              google:
                client-id: ${GOOGLE_CLIENT_ID} #사용자 Google Client ID
                client-secret: ${GOOGLE_CLIENT_SECRET} #사용자 Google Client Secret key
                ...
      ...
    ```
2. **jwt 설정**
    ```bash
    # JWT 설정
    jwt:
      secret: ${JWT_SECRET} # HS512를 사용할 것이기 때문에, 512비트(64바이트) 이상이 되어야 함
      access-token-validity: 36000 # 10시간
      refresh-token-validity: 604800 # 7일
    ```
3. **MinIO 설정**
    ```bash
    # MinIO 설정 추가
    minio:
      endpoint: ${MINIO_ENDPOINT_URL} #MinIO URL 설정 (ex. http://localhost:9000)
      access-key: ${MINIO_ACCESS_KEY} #사용자 지정 MinIO Access Key 설정 (MinIO ID)
      secret-key: ${MINIO_SECRET_KEY} #사용자 지정 MinIO Secret Key 설정 (MinIO PW)
      bucket-name: ${MINIO_BUCKET_NAME} #사용자 지정 MinIO Bucket Name 설정 (직접 생성한 Bucket명)
    ```
4. **Notion 설정**
   ```bash
   notion:
     client:
       id: ${NOTION_ID} #데이터를 기입할 노션 Page ID (page url 마지막의 숫자/문자열)(테스트용, 실제 데이터는 DB의 User 테이블에 저장하여 동적으로 사용)
       secret: ${NOTION_SECRET} #Notion Integeration의 통합 key (ex. secret_...)
     uri:
       authorize-base-url: "https://api.notion.com/v1/oauth/authorize?owner=user"
       redirect-url: "http://localhost:8080/api/notion/ai-to-notion" #Notion Integration 설정 시 해당 redirectURL 추가해줘야함
   ```

### 사전 준비사항

1. **Docker DB Container Setting**
    - Docker에 mysql latest ver container 띄우기
    - Docker에 redis latest ver container 띄우기
    - Docker에 minio/minio latest ver container 띄우기

2. **MinIO Setting**
    - Docker에 minio container 실행 후 http://localhost:{port} 로 접속 후 버킷 생성

3. **API 키 발급**
    - [Google cloud](https://console.cloud.google.com/welcome)에서 프로젝트 생성 후 API 키 발급 (Redirect URL도 함께 설정!)
    - [카카오 Developers](https://developers.kakao.com)에서 애플리케이션 등록 후 API 키 발급 (Redirect URL도 함께 설정!)
    - [네이버 Developers](https://developers.naver.com/main)에서 애플리케이션 등록 후 API 키 발급 (Redirect URL도 함께 설정!)
    - [Notion Integration](https://www.notion.so/profile/integrations)에서 API 통합 등록 후 API 키 발급(public으로 설정)
      - 통합 생성 후 사용할 Notion Page 우측 상단 `...`클릭 -> 하단의 통합(connection) 선택 -> 방금 만든 통합 등록

### 프로젝트 실행

```bash
Step 1: 프로젝트 디렉토리로 이동
  bashcd /path/to/your/spring-project
  
Step 2: 실행
  ./gradlew bootRun

Step 3: 실행 확인
  콘솔에서 다음과 같은 메시지가 나오면 성공:
    Started SpringSecurityApplication in 3.456 seconds (JVM running for 4.123)
```

---

## 📖 프로젝트 개요

LOG-O는 **여행 일정 관리 및 AI 통합 서비스**를 제공하는 Spring Boot 기반의 백엔드 API 서버입니다. 사용자들이 여행 계획을 세우고, 위치를 인증하며, AI 챗봇과 상호작용할 수 있는 종합적인 여행 플랫폼을 목표로 합니다.

### 🎯 주요 기능
- **사용자 인증**: OAuth2.0 (Google, Naver, Kakao) 및 자체 로그인
- **여행 관리**: 여행 계획 생성, 루트 관리, 일정 관리
- **위치 인증**: GPS 기반 장소 방문 인증 시스템
- **AI 챗봇**: OpenAI GPT 및 Anthropic Claude 통합
- **이미지 관리**: MinIO를 통한 안전한 이미지 저장
- **소셜 기능**: 장소 좋아요, 리뷰 작성
- **Notion 연동**: 여행 계획 자동 문서화
- **멀티 포맷 출력**: .md 파일 생성 및 API 전송

## 🏗️ 시스템 아키텍처
![LOG-O PROJECT SERVER ARCHITECTURE.png](src/main/resources/static/assets/LOG-O%20PROJECT%20SERVER%20ARCHITECTURE.png)
```mermaid
flowchart TB
    subgraph "Client Layer"
        C[Client Applications]
        SW[Swagger UI]
    end
    
    subgraph "API Gateway Layer"
        SC[Spring Security]
        CC[CORS Configuration]
    end
    
    subgraph "Application Layer"
        subgraph "Controllers"
            UC[User Controller]
            TC[Travel Controller]
            AC[Auth Controller]
            AIC[AI Controller]
            PC[Place Controller]
        end
        
        subgraph "Services"
            US[User Service]
            TS[Travel Service]
            AS[Auth Service]
            AIS[AI Service]
            PS[Place Service]
        end
        
        subgraph "Security"
            JWT[JWT Provider]
            RT[Token Rotation]
            OAuth[OAuth2 Service]
        end
    end
    
    subgraph "Data Layer"
        subgraph "Repositories"
            UR[User Repository]
            TR[Travel Repository]
            PR[Place Repository]
        end
        
        subgraph "Databases"
            MySQL[(MySQL Database)]
            Redis[(Redis Cache)]
            MinIO[(MinIO Storage)]
        end
    end
    
    subgraph "External Services"
        OpenAI[OpenAI API]
        Claude[Anthropic Claude]
        Google[Google OAuth]
        Naver[Naver OAuth]
        Kakao[Kakao OAuth]
    end
    
    C --> SC
    SW --> SC
    SC --> UC
    SC --> TC
    SC --> AC
    SC --> AIC
    SC --> PC
    
    UC --> US
    TC --> TS
    AC --> AS
    AIC --> AIS
    PC --> PS
    
    AS --> JWT
    AS --> RT
    AS --> OAuth
    
    US --> UR
    TS --> TR
    PS --> PR
    
    UR --> MySQL
    TR --> MySQL
    PR --> MySQL
    
    RT --> Redis
    PS --> MinIO
    
    AIS --> OpenAI
    AIS --> Claude
    OAuth --> Google
    OAuth --> Naver
    OAuth --> Kakao
```

## 🔧 기술 스택 및 선택 이유

### 🖥️ Backend Framework
| 기술 | 버전 | 선택 이유 | 이점 |
|------|------|-----------|------|
| **Spring Boot** | 3.4.0 | • 빠른 개발 환경 구축<br>• 자동 설정 및 내장 서버<br>• 풍부한 생태계 | • 개발 생산성 향상<br>• 유지보수성 개선<br>• 확장성 확보 |
| **Spring Security** | 6.x | • 검증된 보안 프레임워크<br>• OAuth2 지원<br>• JWT 통합 | • 보안 취약점 최소화<br>• 표준 보안 구현<br>• 다양한 인증 방식 지원 |
| **Spring AI** | 1.0.0-M8 | • AI 서비스 통합 간소화<br>• 다중 AI 모델 지원<br>• Spring 생태계 통합 | • AI 통합 개발 시간 단축<br>• 일관된 AI 인터페이스<br>• 확장 가능한 AI 아키텍처 |

### 🗄️ Database & Storage
| 기술 | 선택 이유 | 이점 |
|------|-----------|------|
| **MySQL 8.0** | • 안정적인 관계형 DB<br>• 복잡한 관계 데이터 처리<br>• 트랜잭션 보장 | • 데이터 무결성 보장<br>• 복잡한 쿼리 최적화<br>• 대용량 데이터 처리 |
| **Redis 7.0** | • 인메모리 캐시<br>• 빠른 토큰 검증<br>• TTL 자동 관리 | • 토큰 보안성 향상<br>• 응답 속도 개선<br>• 자동 만료 처리 |
| **MinIO** | • S3 호환 로컬 스토리지<br>• 비용 효율적<br>• 개발 환경 적합 | • 이미지 저장 비용 절약<br>• 로컬 개발 환경 구축<br>• Presigned URL 보안 |

### 🔒 Security & Authentication
| 기술 | 구현 방식 | 보안 이점 |
|------|----------|----------|
| **JWT Token** | • Access Token (Header)<br>• Refresh Token (Cookie)<br>• RTR 방식 적용 | • 토큰 탈취 위험 최소화<br>• 자동 토큰 순환<br>• 보안성과 편의성 균형 |
| **OAuth2.0** | • Google, Naver, Kakao<br>• 사용자 정보 최소 수집<br>• 표준 프로토콜 준수 | • 사용자 편의성 증대<br>• 개인정보 보호<br>• 신뢰성 있는 인증 |

## 🔄 핵심 비즈니스 로직 Flow Chart

### 1. 사용자 인증 플로우

```mermaid
flowchart TD
    A[사용자 로그인 요청] --> B{인증 방식 선택}
    
    B -->|OAuth2| C[OAuth2 Provider 리다이렉트]
    B -->|일반 로그인| D[ID/PW 검증]
    
    C --> E[OAuth2 인증 완료]
    E --> F{신규 사용자?}
    F -->|Yes| G[사용자 정보 저장]
    F -->|No| H[기존 사용자 정보 업데이트]
    
    D --> I{인증 성공?}
    I -->|No| J[인증 실패 응답]
    I -->|Yes| H
    
    G --> K[추가 정보 입력 필요]
    K --> L[온보딩 페이지 리다이렉트]
    L --> M[필수 정보 입력]
    M --> N[사용자 정보 완성]
    
    H --> O[JWT 토큰 생성]
    N --> O
    O --> P[Access Token → Header]
    O --> Q[Refresh Token → Redis + Cookie]
    P --> R[로그인 완료]
    Q --> R
```

### 2. 여행 계획 생성 플로우

```mermaid
flowchart TD
    A[여행 생성 요청] --> B[사용자 인증 확인]
    B --> C{인증된 사용자?}
    C -->|No| D[401 Unauthorized]
    C -->|Yes| E[여행 정보 검증]
    
    E --> F{날짜 유효성}
    F -->|Invalid| G[400 Bad Request]
    F -->|Valid| H[Travel 엔티티 생성]
    
    H --> I[MySQL에 여행 저장]
    I --> J[여행 기간 계산]
    J --> K[일자별 TravelRoot 생성]
    
    K --> L{생성 성공?}
    L -->|No| M[트랜잭션 롤백]
    L -->|Yes| N[여행 생성 완료 응답]
    
    subgraph "TravelRoot 자동 생성 로직"
        O[Start Date: 2025-05-01]
        P[End Date: 2025-05-05]
        Q[Days = 5일]
        R[1일차: 2025-05-01 TravelRoot]
        S[2일차: 2025-05-02 TravelRoot]
        T[3일차: 2025-05-03 TravelRoot]
        U[4일차: 2025-05-04 TravelRoot]
        V[5일차: 2025-05-05 TravelRoot]
    end
    
    K -.-> O
```

### 3. 위치 인증 플로우

```mermaid
flowchart TD
    A[위치 인증 요청] --> B[인증 데이터 + 이미지 파일]
    B --> C[사용자 인증 확인]
    C --> D[장소 정보 검증]
    
    D --> E{장소 존재?}
    E -->|No| F[404 Place Not Found]
    E -->|Yes| G[이미지 파일 검증]
    
    G --> H{이미지 유효?}
    H -->|No| I[400 Invalid Image]
    H -->|Yes| J[Verification 엔티티 생성]
    
    J --> K[MySQL에 인증 정보 저장]
    K --> L[MinIO에 이미지 업로드]
    
    L --> M{업로드 성공?}
    M -->|No| N[이미지 업로드 실패 로그]
    M -->|Yes| O[TravelImage 엔티티 생성]
    
    O --> P[이미지 정보 MySQL 저장]
    N --> Q[인증 완료 응답]
    P --> Q
    
    subgraph "MinIO 이미지 처리"
        R[이미지 파일 수신]
        S[객체 키 생성: verifications/userId/verificationId_timestamp_uuid.ext]
        T[MinIO 업로드]
        U[객체 키 반환]
    end
    
    L -.-> R
```

### 4. JWT 토큰 순환 (RTR) 플로우

```mermaid
flowchart TD
    A[API 요청 with Access Token] --> B{Access Token 유효?}
    B -->|Valid| C[API 요청 처리]
    B -->|Expired| D[쿠키에서 Refresh Token 추출]
    
    D --> E{Refresh Token 존재?}
    E -->|No| F[401 Unauthorized]
    E -->|Yes| G[Redis에서 토큰 검증]
    
    G --> H{Redis 토큰 일치?}
    H -->|No| I[토큰 탈취 의심]
    H -->|Yes| J[새 토큰 쌍 생성]
    
    I --> K[모든 사용자 세션 무효화]
    K --> F
    
    J --> L[새 Access Token 생성]
    J --> M[새 Refresh Token 생성]
    
    L --> N[Header에 새 Access Token 설정]
    M --> O[Redis에 새 Refresh Token 저장]
    M --> P[쿠키에 새 Refresh Token 설정]
    
    O --> Q[기존 Refresh Token 삭제]
    N --> R[API 요청 처리]
    P --> R
    Q --> R
```

## 🎯 Use Case Diagram

```mermaid
graph TB
    subgraph "사용자 (User)"
        U1[일반 사용자]
        U2[OAuth2 사용자]
        U3[관리자]
    end
    
    subgraph "인증 관련 (Authentication)"
        UC1[회원가입]
        UC2[로그인]
        UC3[OAuth2 로그인]
        UC4[토큰 갱신]
        UC5[로그아웃]
        UC6[프로필 수정]
    end
    
    subgraph "여행 관리 (Travel Management)"
        UC7[여행 계획 생성]
        UC8[여행 정보 수정]
        UC9[여행 루트 관리]
        UC10[여행 지역 추가]
        UC11[여행 결제 내역 관리]
        UC12[여행 삭제]
    end
    
    subgraph "위치 및 장소 (Location & Place)"
        UC13[장소 검색]
        UC14[장소 상세 조회]
        UC15[위치 인증]
        UC16[장소 좋아요]
        UC17[리뷰 작성]
    end
    
    subgraph "AI 서비스 (AI Service)"
        UC18[ChatGPT 대화]
        UC19[Claude 대화]
    end
    
    subgraph "이미지 관리 (Image Management)"
        UC20[이미지 업로드]
        UC21[이미지 조회]
        UC22[이미지 URL 생성]
    end
    
    %% 사용자별 권한 관계
    U1 --> UC1
    U1 --> UC2
    U1 --> UC6
    U1 --> UC7
    U1 --> UC8
    U1 --> UC9
    U1 --> UC10
    U1 --> UC11
    U1 --> UC12
    U1 --> UC13
    U1 --> UC14
    U1 --> UC15
    U1 --> UC16
    U1 --> UC17
    U1 --> UC18
    U1 --> UC19
    U1 --> UC20
    U1 --> UC21
    U1 --> UC22
    
    U2 --> UC3
    U2 --> UC6
    U2 --> UC7
    U2 --> UC8
    U2 --> UC9
    U2 --> UC10
    U2 --> UC11
    U2 --> UC12
    U2 --> UC13
    U2 --> UC14
    U2 --> UC15
    U2 --> UC16
    U2 --> UC17
    U2 --> UC18
    U2 --> UC19
    U2 --> UC20
    U2 --> UC21
    U2 --> UC22
    
    U3 --> UC1
    U3 --> UC2
    U3 --> UC6
    U3 --> UC7
    U3 --> UC8
    U3 --> UC9
    U3 --> UC10
    U3 --> UC11
    U3 --> UC12
    U3 --> UC13
    U3 --> UC14
    U3 --> UC15
    U3 --> UC16
    U3 --> UC17
    U3 --> UC18
    U3 --> UC19
    U3 --> UC20
    U3 --> UC21
    U3 --> UC22
    
    %% 공통 인증 관련
    UC2 --> UC4
    UC3 --> UC4
    UC4 --> UC5
```

## 📋 핵심 비즈니스 로직 상세 설명

### 1. 🔐 JWT 토큰 관리 시스템

#### RTR (Refresh Token Rotation) 구현
```java
public boolean rotateTokens(HttpServletRequest request, HttpServletResponse response) {
    // 1. 쿠키에서 Refresh Token 추출
    String refreshToken = cookieProvider.getRefreshTokenFromCookies(request);
    
    // 2. 토큰 유효성 검증
    if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
        return false;
    }
    
    // 3. Redis에 저장된 토큰과 비교
    String storedToken = tokenStore.getRefreshToken(userId);
    if (!storedToken.equals(refreshToken)) {
        // 4. 토큰 불일치 시 모든 세션 무효화 (보안 조치)
        tokenStore.invalidateAllUserSessions(userId);
        return false;
    }
    
    // 5. 새 토큰 쌍 생성 및 교체
    String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
    String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);
    
    return true;
}
```

**보안 특징:**
- **토큰 탈취 감지**: Refresh Token 불일치 시 모든 세션 무효화
- **자동 순환**: 토큰 사용 시마다 새로운 토큰 쌍 생성
- **분리 저장**: Access Token(Header) + Refresh Token(Cookie)

### 2. 🗺️ 여행 계획 자동 생성 시스템

#### 일자별 TravelRoot 자동 생성
```java
private void createTravelRootsForTravelPeriod(Travel travel) {
    LocalDate startDate = travel.getStartDate();
    LocalDate endDate = travel.getEndDate();
    
    // 여행 기간 계산 (시작일과 종료일 포함)
    long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
    
    List<TravelRoot> travelRoots = new ArrayList<>();
    
    // 각 날짜별로 TravelRoot 생성
    for (int dayIndex = 0; dayIndex < daysBetween; dayIndex++) {
        LocalDate currentDate = startDate.plusDays(dayIndex);
        int dayNumber = dayIndex + 1;
        
        TravelRoot travelRoot = TravelRoot.builder()
                .travel(travel)
                .day(dayNumber)
                .travelDate(currentDate)
                .build();
        
        travelRoots.add(travelRoot);
    }
    
    travelRootRepository.saveAll(travelRoots);
}
```

**비즈니스 로직:**
- **자동 일정 생성**: 여행 기간에 따라 자동으로 일자별 루트 생성
- **날짜 계산**: 시작일과 종료일을 포함한 정확한 기간 계산
- **일괄 처리**: 모든 루트를 한 번에 생성하여 성능 최적화

### 3. 📍 위치 인증 시스템

#### 이미지와 함께하는 위치 인증
```java
@Transactional
public VerificationDto addVerificationWithImage(VerificationRequestDto requestDto, MultipartFile imageFile) {
    // 1. 사용자 및 장소 검증
    User user = getCurrentUser();
    Place place = validatePlace(requestDto.getPid());
    
    // 2. 인증 정보 생성
    Verification verification = Verification.builder()
            .user(user)
            .place(place)
            .star(requestDto.getStar())
            .review(requestDto.getReview())
            .build();
    
    Verification savedVerification = verificationRepository.save(verification);
    
    // 3. 이미지 MinIO 업로드
    if (imageFile != null && !imageFile.isEmpty()) {
        String objectKey = minIOService.uploadVerificationImage(
                imageFile, user.getUuid(), savedVerification.getVuid()
        );
        
        // 4. TravelImage 엔티티 생성
        TravelImage travelImage = TravelImage.builder()
                .user(user)
                .verification(savedVerification)
                .travel(findRelatedTravel(place, user))
                .name("방문 인증 이미지 - " + place.getName())
                .url(objectKey)
                .build();
        
        travelImageRepository.save(travelImage);
    }
    
    return VerificationDto.fromEntity(savedVerification);
}
```

**핵심 특징:**
- **이미지 증명**: 실제 방문을 증명하는 이미지 업로드 필수
- **MinIO 통합**: 안전한 이미지 저장 및 Presigned URL 생성
- **자동 연결**: 사용자의 여행 일정과 자동 연결

### 4. 🤖 AI 문서 자동화 시스템

#### 다중 AI 모델 지원
```java
@Service
public class AIService {
    private final ChatClient openAiChatClient;
    private final ChatClient anthropicChatClient;
    
    public ChatResponse chatWithOpenAi(ChatRequest chatRequest) {
        String answer = openAiChatClient
                .prompt()
                .user(chatRequest.getQuestion())
                .advisors(advisorSpec -> 
                    advisorSpec.param("chat_memory_conversation_id", chatId))
                .call()
                .content();
        
        return new ChatResponse(chatId, answer);
    }
    
    public ChatResponse chatWithAnthropic(ChatRequest chatRequest) {
        // Claude 모델과의 대화 처리
        return anthropicChatClient.prompt()...
    }
}
```

**AI 통합 특징:**
- **다중 모델**: OpenAI GPT와 Anthropic Claude 동시 지원
- **문서 자동화**: NotionAPI 연결을 통한 문서 자동 기록 및 .md 파일 형식으로 추출
- **프롬프트 설정**: resources/prompt에 강력 설계된 AI 프롬프트 설정

### 5. 🖼️ 이미지 관리 시스템

#### MinIO와 Presigned URL
```java
public String generatePresignedUrl(String objectKey, int expiryMinutes) {
    try {
        String presignedUrl = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectKey)
                        .expiry(expiryMinutes, TimeUnit.MINUTES)
                        .build()
        );
        
        return presignedUrl;
    } catch (Exception e) {
        throw new RuntimeException("이미지 URL 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
    }
}
```

**보안 및 성능 특징:**
- **임시 URL**: 지정된 시간 후 자동 만료
- **직접 접근 차단**: MinIO 스토리지 직접 접근 불가
- **비용 효율성**: 로컬 스토리지로 S3 대비 비용 절약

## 🚀 개발 환경 설정

### Prerequisites
- Java 21+
- Docker & Docker Compose
- MySQL 8.0+
- Redis 7.0+
- MinIO

### 환경 변수 설정
```bash
# Database
DB_URL=jdbc:mysql://{your-db-url}:{yout-db-port}/{your-db-name}?{addtional-setting}
DB_USER=root
DB_PASSWORD=root

# JWT
JWT_SECRET=your-jwt-secret-key

# AI APIs
OPENAI_API_KEY=your-openai-api-key
ANTHROPIC_API_KEY=your-anthropic-api-key
GPT_MODEL=gpt-4
CLAUDE_MODEL=claude-3-sonnet-20240229

# OAuth2
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
NAVER_CLIENT_ID=your-naver-client-id
NAVER_CLIENT_SECRET=your-naver-client-secret
KAKAO_CLIENT_ID=your-kakao-client-id
KAKAO_CLIENT_SECRET=your-kakao-client-secret

# MinIO
MINIO_ACCESS_KEY=your-minio-id
MINIO_SECRET_KEY=your-minio-pw
MINIO_BUCKET_NAME=your-minio-bucket-name

# Application
APP_DOMAIN=localhost
```

### Docker 컨테이너 실행
```bash
# Docker Compose로 데이터베이스 서비스 시작
cd src/main/resources
docker-compose up -d

# 서비스 상태 확인
docker-compose ps
```

### 애플리케이션 실행
```bash
# Gradle을 통한 실행
./gradlew bootRun

# 또는 JAR 파일 실행
./gradlew build
java -jar build/libs/logo-server-0.0.1-SNAPSHOT.jar
```

## 📊 API 문서

### Swagger UI 접근
- **URL**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs

### 주요 API 엔드포인트

#### 🔐 인증 관련
```http
POST /api/auth/signup          # 회원가입
POST /api/auth/login           # 로그인
POST /api/auth/refresh         # 토큰 갱신
POST /api/auth/logout          # 로그아웃
GET  /api/auth/me              # 내 정보 조회
```

#### 🗺️ 여행 관리
```http
GET    /api/travels            # 여행 목록 조회
POST   /api/travels            # 여행 생성
GET    /api/travels/{id}       # 여행 상세 조회
PUT    /api/travels/{id}       # 여행 정보 수정
DELETE /api/travels/{id}       # 여행 삭제
```

#### 📍 위치 인증
```http
POST /api/verifications/verify # 위치 인증 (이미지 포함)
GET  /api/verifications        # 인증 목록 조회
```

#### 🤖 AI 챗봇
```http
POST /api/chat/openai          # OpenAI 챗봇
POST /api/chat/anthropic       # Anthropic 챗봇
```

## 🧪 테스트

### 테스트 전략
1. **Postman Collection**: API 통합 테스트
2. **Swagger UI**: 실시간 API 테스트
3. **Unit Tests**: 비즈니스 로직 단위 테스트

### 테스트 실행
```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "UserServiceTest"
```

## 📈 성능 최적화

### 1. Database 최적화
- **FetchJoin 사용**: N+1 쿼리 문제 해결
- **인덱싱**: 자주 조회되는 컬럼에 인덱스 적용
- **Connection Pool**: HikariCP 사용

### 2. 캐싱 전략
- **Redis**: JWT 토큰 캐싱
- **MinIO**: 이미지 CDN 역할

### 3. 보안 최적화
- **CORS 설정**: 허용된 도메인만 접근
- **CSRF 보호**: Spring Security 기본 보호
- **SQL Injection 방지**: JPA Prepared Statement

## 🔧 모니터링 및 로깅

### 로깅 설정
```yaml
logging:
  level:
    com.ssafy.logoserver: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### 헬스 체크
```http
GET /health  # 서버 상태 확인
```

## 🤝 기여 가이드

### 코드 스타일
- **Java 21** 문법 준수
- **Spring Boot Convention** 따름
- **RESTful API** 설계 원칙 준수

### 브랜치 전략
```
main     : 배포 준비 완료된 코드
develop  : 개발 중인 코드
feature/ : 새로운 기능 개발
hotfix/  : 긴급 버그 수정
```

## 👨‍💻 개발자 정보

- **개발 인원**: 박병찬(팀장, FrontEnd, EslasticStack 및 AI 담당), 한승수(BackEnd, DB 및 인프라 구축 담당)
- **Connect**: qudcks8084@gmail.com, h2sorginal@gmail.com


|                                                          **박병찬**                                                          |                                                          **한승수**                                                          |
| :--------------------------------------------------------------------------------------------------------------------------: | :--------------------------------------------------------------------------------------------------------------------------: |
| [<img src="https://avatars.githubusercontent.com/u/47100335?v=4" height=150 width=150> <br/> @SIDEDISH](https://github.com/qudcks8084) | [<img src="https://github.com/user-attachments/assets/9948b5ea-272d-4ce4-9dd9-5a6d4cbd619f" height=150 width=150> <br/> @SEUNGSU-HAN](https://github.com/SEUNGSU-HAN) |
|                                                       FE │ ES │ AI                                                        |                                                       BE │ DB │ INFRA                                                        |


---

**LOG-O 프로젝트** - 여행의 모든 순간을 기록하고 공유하는 스마트한 여행 플랫폼 🌍
