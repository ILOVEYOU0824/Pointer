# SafeContract API (Spring Boot)

AI 계약서 위험도 분석 백엔드입니다. PDF를 업로드하면 OCR로 텍스트를 추출하고, AI가 독소조항을 분석해 점수를 반환합니다.

## 사전 요구사항

- **JDK 17** 이상
- **Google Gemini API 키**

## 실행 방법

```bash
cd backend

# 환경 변수 설정 (PowerShell)
$env:GEMINI_API_KEY="your-gemini-api-key"
$env:CORS_ALLOWED_ORIGINS="http://localhost:5173"

# 실행 (Maven 미설치 시 mvnw 사용)
.\mvnw.cmd spring-boot:run
```

서버가 `http://localhost:8080` 에서 시작됩니다.

## API

### 헬스체크

```
GET /api/health
```

### 계약서 분석

```
POST /api/analyze
Content-Type: multipart/form-data

file: (PDF 파일)
```

**응답 예시:**

```json
{
  "fileName": "terms.pdf",
  "score": 72,
  "scoreLabel": "보통",
  "summary": "전반적으로 표준적이나 일부 조항에서 불리한 조건이 확인됩니다.",
  "toxicClauses": [
    {
      "id": 1,
      "title": "일방적 해지권",
      "originalText": "회사는 사전 통지 없이 서비스를 중단할 수 있다.",
      "reason": "소비자에게 불리한 일방적 해지 조항",
      "severity": "high",
      "page": 3,
      "suggestion": "상호 협의 및 사전 통지 기간 명시 권장"
    }
  ],
  "analysisSteps": [
    "계약서를 읽는 중...",
    "위험 조항을 분석하는 중...",
    "법률 기준과 비교하는 중...",
    "위험도를 계산하는 중...",
    "AI 요약을 생성하는 중..."
  ]
}
```

## 처리 흐름

```
PDF 업로드
  → PDFBox로 텍스트 추출
  → 텍스트 부족 시 Gemini Vision OCR (스캔 PDF)
  → Gemini가 독소조항 분석 + 점수 산출
  → JSON 응답
```

## 환경 변수

| 변수 | 설명 | 기본값 |
|------|------|--------|
| `GEMINI_API_KEY` | Gemini API 키 | (필수) |
| `GEMINI_API_BASE_URL` | API 베이스 URL | `https://generativelanguage.googleapis.com/v1beta` |
| `GEMINI_MODEL` | 분석용 모델 | `gemini-2.0-flash` |
| `GEMINI_VISION_MODEL` | OCR용 모델 | `gemini-2.0-flash` |
| `SERVER_PORT` | 서버 포트 | `8080` |
| `CORS_ALLOWED_ORIGINS` | 허용할 프론트 origin | `http://localhost:5173` |

## Gemini API 키 발급

1. [Google AI Studio](https://aistudio.google.com/apikey) 접속
2. **Create API Key** 클릭
3. 생성된 키를 `GEMINI_API_KEY`에 설정

## 프론트엔드 연동

프론트에서 아래처럼 호출하면 됩니다:

```javascript
const formData = new FormData();
formData.append("file", selectedFile);

const response = await fetch("http://localhost:8080/api/analyze", {
  method: "POST",
  body: formData,
});

const result = await response.json();
```

## 프로젝트 구조

```
backend/
├── src/main/java/com/safecontract/
│   ├── controller/     # REST API
│   ├── service/        # PDF 추출, AI 분석
│   ├── dto/            # 요청/응답 객체
│   ├── config/         # CORS, 설정
│   └── exception/      # 예외 처리
└── src/main/resources/
    └── application.yml
```
