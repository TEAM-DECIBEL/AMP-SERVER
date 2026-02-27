# 프론트엔드 로그인 API 명세 (프로덕션)

## 도메인 정보

| 역할 | 프론트엔드 | 백엔드 API |
|------|-----------|------------|
| AUDIENCE (관객) | `https://ampnotice.kr` | `https://api.ampnotice.kr` |
| ORGANIZER (주최자) | `https://host.ampnotice.kr` | `https://api.ampnotice.kr` |

---

## 1. 로그인 시작

### Request
```javascript
// 그냥 이 URL로 브라우저 이동시키면 됨
window.location.href = 'https://api.ampnotice.kr/oauth2/authorization/google';
```

**파라미터 없음** - 백엔드에서 Origin/Referer 헤더를 보고 자동으로 UserType 결정
- `ampnotice.kr`에서 접속 → AUDIENCE
- `host.ampnotice.kr`에서 접속 → ORGANIZER

---

## 2. 로그인 성공 후 (리다이렉트)

### Case A: 신규 사용자 (최초 로그인)

**백엔드 처리**:
1. Google 인증 완료
2. DB에 사용자 생성 (`registrationStatus: PENDING`)
3. 접속한 도메인에 따라 `userType` 자동 설정
4. JWT 토큰을 **쿠키**에 설정
5. `/onboarding`으로 리다이렉트

**프론트엔드가 받는 것**:
```
리다이렉트 → https://ampnotice.kr/onboarding
쿠키 설정됨: accessToken (HttpOnly, Secure, Domain: .ampnotice.kr)
```

---

### Case B: 기존 사용자 (온보딩 완료)

**백엔드 처리**:
1. Google 인증 완료
2. DB에서 사용자 조회 (`registrationStatus: COMPLETED`)
3. 도메인-역할 일치 확인
4. JWT 토큰을 **쿠키**에 설정
5. 메인 페이지로 리다이렉트

**프론트엔드가 받는 것**:
```
리다이렉트 → https://ampnotice.kr (도메인 일치 시)
쿠키 설정됨: accessToken
```

---

### Case C: 기존 사용자 + 잘못된 도메인 접근

예: ORGANIZER 사용자가 `ampnotice.kr`(AUDIENCE 도메인)에서 로그인 시도

**프론트엔드가 받는 것**:
```
리다이렉트 → https://host.ampnotice.kr (올바른 도메인으로 이동)
쿠키 설정됨: accessToken (Domain: .ampnotice.kr 이므로 공유됨)
```

---

## 3. 온보딩 페이지 처리

### 3-1. 온보딩 상태 확인

```javascript
const response = await fetch('https://api.ampnotice.kr/api/v1/auth/onboarding/status', {
  method: 'GET',
  credentials: 'include'  // 쿠키 자동 전송 (필수!)
});
```

**Response (200 OK)**:
```json
{
  "email": "user@gmail.com",
  "registrationStatus": "PENDING",
  "userType": "AUDIENCE",
  "needsOnboarding": true
}
```

| 필드 | 타입 | 값 | 설명 |
|------|------|-----|------|
| `registrationStatus` | string | `PENDING` / `COMPLETED` | 온보딩 상태 |
| `userType` | string | `AUDIENCE` / `ORGANIZER` | 사용자 타입 |
| `needsOnboarding` | boolean | true/false | **이게 true면 온보딩 필요** |

---

### 3-2. 온보딩 완료 요청

#### AUDIENCE (관객)인 경우

```javascript
const response = await fetch('https://api.ampnotice.kr/api/v1/auth/onboarding/complete', {
  method: 'POST',
  credentials: 'include',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    userType: 'AUDIENCE',
    nickname: '닉네임입력값'  // 2-12자
  })
});
```

#### ORGANIZER (주최자)인 경우

```javascript
const response = await fetch('https://api.ampnotice.kr/api/v1/auth/onboarding/complete', {
  method: 'POST',
  credentials: 'include',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    userType: 'ORGANIZER',
    organizerName: '주최사명입력값'  // 2-12자
  })
});
```

**Response (200 OK)**:
```json
{
  "userId": 1,
  "email": "user@gmail.com",
  "nickname": "닉네임입력값",
  "userType": "AUDIENCE",
  "registrationStatus": "COMPLETED",
  "message": "온보딩이 완료되었습니다."
}
```

**온보딩 성공 후** → 메인 페이지로 이동
```javascript
window.location.href = '/';
```

---

## 4. 일반 API 요청

```javascript
// 모든 API 요청에 credentials: 'include' 필수
const response = await fetch('https://api.ampnotice.kr/api/v1/audience/festivals', {
  method: 'GET',
  credentials: 'include'
});
```

---

## 5. 에러 코드 상세

### 공통 응답 형식

```json
{
  "status": 403,
  "code": "AUTH_403_001",
  "msg": "접근 도메인과 사용자 역할이 일치하지 않습니다.",
  "timestamp": "2024-01-01T00:00:00.000Z",
  "correctDomain": "https://host.ampnotice.kr",
  "userType": "ORGANIZER"
}
```

---

### 5-1. 인증 관련 에러 (AUTH)

#### 401 Unauthorized - 인증 실패

| 코드 | 메시지 | 설명 |
|------|--------|------|
| `AUTH_401_001` | 유효하지 않은 토큰입니다. | JWT 토큰이 유효하지 않음 |
| `AUTH_401_002` | 만료된 토큰입니다. | JWT 토큰 만료 |
| `AUTH_401_003` | 인증 토큰이 없습니다. | 쿠키/헤더에 토큰 없음 |
| `AUTH_401_004` | 토큰 형식이 올바르지 않습니다. | JWT 형식 오류 |
| `AUTH_401_005` | 지원하지 않는 토큰 형식입니다. | 지원하지 않는 토큰 |
| `AUTH_401_006` | 토큰 서명이 유효하지 않습니다. | 서명 검증 실패 |

#### 401 Unauthorized - OAuth2 실패

| 코드 | 메시지 | 설명 |
|------|--------|------|
| `AUTH_401_010` | 소셜 로그인에 실패했습니다. | OAuth2 인증 실패 |
| `AUTH_401_011` | 소셜 계정에서 이메일 정보를 가져올 수 없습니다. | 이메일 없음 |
| `AUTH_401_012` | 지원하지 않는 소셜 로그인 제공자입니다. | 지원하지 않는 provider |

#### 403 Forbidden - 접근 제한

| 코드 | 메시지 | 추가 필드 | 설명 |
|------|--------|----------|------|
| `AUTH_403_001` | 접근 도메인과 사용자 역할이 일치하지 않습니다. | `correctDomain`, `userType` | 도메인 불일치 |
| `AUTH_403_002` | 온보딩을 완료해주세요. | `onboardingUrl` | 온보딩 미완료 |
| `AUTH_403_003` | 접근 권한이 없습니다. | - | 권한 없음 |
| `AUTH_403_004` | 관객 전용 기능입니다. | - | AUDIENCE 전용 |
| `AUTH_403_005` | 주최자 전용 기능입니다. | - | ORGANIZER 전용 |

---

### 5-2. 온보딩 관련 에러 (OBD)

#### 400 Bad Request - 필수 값 누락

| 코드 | 메시지 | 설명 |
|------|--------|------|
| `OBD_400_001` | 이미 온보딩이 완료되었습니다. | 중복 온보딩 시도 |
| `OBD_400_002` | 유효하지 않은 사용자 타입입니다. | userType 값 오류 |
| `OBD_400_003` | 주최사명은 필수입니다. | ORGANIZER인데 organizerName 누락 |
| `OBD_400_004` | 닉네임은 필수입니다. | AUDIENCE인데 nickname 누락 |
| `OBD_400_005` | 사용자 타입은 필수입니다. | userType 누락 |
| `OBD_400_006` | 요청한 사용자 타입이 현재 설정된 타입과 일치하지 않습니다. | userType 불일치 |

#### 400 Bad Request - 유효성 검증 실패

| 코드 | 메시지 | 설명 |
|------|--------|------|
| `OBD_400_010` | 닉네임은 2-12자 사이여야 합니다. | 닉네임 길이 오류 |
| `OBD_400_011` | 주최사명은 2-12자 사이여야 합니다. | 주최사명 길이 오류 |
| `OBD_400_012` | 닉네임 형식이 올바르지 않습니다. | 닉네임 형식 오류 |
| `OBD_400_013` | 주최사명 형식이 올바르지 않습니다. | 주최사명 형식 오류 |

#### 409 Conflict - 중복

| 코드 | 메시지 | 설명 |
|------|--------|------|
| `OBD_409_001` | 이미 사용 중인 닉네임입니다. | 닉네임 중복 |
| `OBD_409_002` | 이미 사용 중인 주최사명입니다. | 주최사명 중복 |

---

### 5-3. 공통 에러 (COM)

#### 400 Bad Request

| 코드 | 메시지 | 설명 |
|------|--------|------|
| `COM_400_001` | 잘못된 입력값입니다. | 입력값 오류 |
| `COM_400_002` | 입력값의 타입이 일치하지 않습니다. | 타입 불일치 |
| `COM_400_003` | 필수 파라미터가 누락되었습니다. | 파라미터 누락 |
| `COM_400_004` | JSON 파싱 중 오류가 발생했습니다. | JSON 형식 오류 |

#### 401 Unauthorized

| 코드 | 메시지 | 설명 |
|------|--------|------|
| `COM_401_001` | 인증이 필요합니다. | 인증 필요 |
| `COM_401_004` | 사용자를 찾을 수 없습니다. | 사용자 없음 |

---

## 6. 에러 처리 예시

### 공통 에러 핸들링 함수

```javascript
const handleApiError = async (response) => {
  if (!response.ok) {
    const error = await response.json();

    switch (error.code) {
      // 도메인 불일치 - 올바른 도메인으로 리다이렉트
      case 'AUTH_403_001':
        window.location.href = error.correctDomain;
        break;

      // 온보딩 필요 - 온보딩 페이지로 이동
      case 'AUTH_403_002':
        window.location.href = '/onboarding';
        break;

      // 토큰 만료/무효 - 재로그인
      case 'AUTH_401_001':
      case 'AUTH_401_002':
      case 'AUTH_401_003':
        window.location.href = '/login';
        break;

      // 닉네임 필수
      case 'OBD_400_004':
        alert('닉네임을 입력해주세요.');
        break;

      // 닉네임 길이 오류
      case 'OBD_400_010':
        alert('닉네임은 2-12자 사이여야 합니다.');
        break;

      // 닉네임 중복
      case 'OBD_409_001':
        alert('이미 사용 중인 닉네임입니다.');
        break;

      // 주최사명 필수
      case 'OBD_400_003':
        alert('주최사명을 입력해주세요.');
        break;

      // 주최사명 길이 오류
      case 'OBD_400_011':
        alert('주최사명은 2-12자 사이여야 합니다.');
        break;

      // 주최사명 중복
      case 'OBD_409_002':
        alert('이미 사용 중인 주최사명입니다.');
        break;

      // userType 불일치
      case 'OBD_400_006':
        alert('잘못된 접근입니다. 다시 로그인해주세요.');
        window.location.href = '/login';
        break;

      default:
        alert(error.msg || '오류가 발생했습니다.');
    }

    throw error;
  }

  return response.json();
};
```

### API 요청 예시

```javascript
// 온보딩 완료 요청
const completeOnboarding = async (userType, nickname, organizerName) => {
  try {
    const response = await fetch('https://api.ampnotice.kr/api/v1/auth/onboarding/complete', {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userType,
        nickname,       // AUDIENCE일 때
        organizerName   // ORGANIZER일 때
      })
    });

    return await handleApiError(response);
  } catch (error) {
    console.error('온보딩 실패:', error);
    throw error;
  }
};
```

---

## 7. 로그아웃

```javascript
await fetch('https://api.ampnotice.kr/api/v1/auth/logout', {
  method: 'POST',
  credentials: 'include'
});

// 쿠키 삭제 (프로덕션)
document.cookie = 'accessToken=; Max-Age=0; path=/; domain=.ampnotice.kr;';

window.location.href = '/login';
```

---

## 8. 전체 플로우 다이어그램

```
[사용자]
    │
    ▼ 로그인 버튼 클릭
[프론트엔드] ──────────────────────────────────────────┐
    │                                                  │
    │ window.location.href =                           │
    │ 'https://api.ampnotice.kr/oauth2/authorization/google'
    │                                                  │
    ▼                                                  │
[백엔드] → Google OAuth → 인증 성공                     │
    │                                                  │
    ├─ 신규 사용자 (PENDING)                            │
    │   └─ 리다이렉트: /onboarding + 쿠키 설정           │
    │                                                  │
    └─ 기존 사용자 (COMPLETED)                          │
        ├─ 도메인 일치 → 리다이렉트: / + 쿠키 설정        │
        └─ 도메인 불일치 → 리다이렉트: 올바른 도메인       │
                                                       │
    ◀──────────────────────────────────────────────────┘
    │
    ▼ /onboarding 페이지
[프론트엔드]
    │
    │ GET /api/v1/auth/onboarding/status
    │ (needsOnboarding 확인)
    │
    │ POST /api/v1/auth/onboarding/complete
    │ { userType, nickname 또는 organizerName }
    │
    ▼ 완료 후 메인 페이지로 이동
```

---

## 핵심 요약

| 항목 | 내용 |
|------|------|
| **로그인** | `window.location.href`로 리다이렉트 |
| **토큰 전달** | 쿠키 (자동) - `credentials: 'include'` 필수 |
| **신규 사용자** | `/onboarding`으로 리다이렉트됨 |
| **온보딩 Request** | `userType` + (`nickname` 또는 `organizerName`) |
| **에러 구분** | `code` 필드로 종류 구분 후 처리 |
