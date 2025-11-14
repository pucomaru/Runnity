# 챌린지 진행 중 메시지 흐름

## 📋 개요

이 문서는 챌린지 진행 중 WebSocket 서버에서 처리하는 메시지 흐름을 정의합니다.

### 통신 채널

1. **WebSocket** (양방향): 클라이언트 ↔ WebSocket 서버
2. **Redis Pub/Sub** (양방향): WebSocket 서버 간 동기화
3. **Kafka** (단방향): WebSocket 서버 → Stream 서버 (브로드캐스트 스트리밍)

### Redis 저장소

#### `challenge:{challengeId}:meta`
챌린지 메타 정보를 저장하는 Redis Hash입니다.

**저장 시점**: 챌린지 시작 5분 전 (비즈니스 서버에서 저장)

**저장 정보**:
- `title`: 챌린지 제목
- `totalApplicantCount`: 전체 신청자 수 (TOTAL_APPLICANT_STATUSES 조회)
- `actualParticipantCount`: 실제 참여자 수 (초기값 0, 입장 시 +1)
- `distance`: 목표 거리 (km)
- `isBroadcast`: 브로드캐스트 여부

**사용 위치**:
- 목표 거리 조회 (완주 체크)
- 브로드캐스트 여부 확인 (Kafka 발행 조건)
- 실제 참여자 수 관리 (입장/퇴장 시 업데이트)

---

## 1️⃣ WebSocket 메시지

### 서버 → 클라이언트

#### 1.1. CONNECTED (연결 성공 + 참가자 목록)
**시점**: WebSocket 연결 직후  
**구조**:
```json
{
  "type": "CONNECTED",
  "challengeId": 100,
  "userId": 1,
  "participants": [
    {
      "userId": 2,
      "nickname": "러너2",
      "profileImage": "https://...",
      "distance": 0,
      "pace": 0
    },
    {
      "userId": 3,
      "nickname": "러너3",
      "profileImage": "https://...",
      "distance": 0,
      "pace": 0
    }
  ],
  "timestamp": 1699999999999
}
```

**설명**:
- 티켓 검증 성공 후 연결이 수립되었음을 알림
- **현재 참여 중인 다른 참가자 목록**을 함께 전송 (본인 제외)
- challengeId, userId는 디버깅 및 클라이언트 상태 확인용
- 프론트는 이 목록으로 초기 참가자 UI 구성
- 초기 distance, pace는 0

---

#### 1.2. USER_ENTERED (다른 참가자 입장)
**시점**: 내가 접속한 이후, 다른 사용자가 챌린지에 입장했을 때  
**구조**:
```json
{
  "type": "USER_ENTERED",
  "userId": 2,
  "nickname": "러너2",
  "profileImage": "https://...",
  "distance": 0.0,
  "pace": 0.0,
  "timestamp": 1699999999999
}
```

**설명**:
- Redis Pub/Sub을 통해 전달받은 입장 이벤트
- **프론트: 참가자 목록에 추가**
- 초기 distance, pace는 0.0

---

#### 1.3. USER_LEFT (다른 참가자 퇴장)
**시점**: 다른 사용자가 챌린지에서 퇴장했을 때  
**구조**:
```json
{
  "type": "USER_LEFT",
  "userId": 2,
  "reason": "QUIT|FINISH|TIMEOUT|DISCONNECTED|KICKED|EXPIRED|ERROR",
  "timestamp": 1699999999999
}
```

**설명**:
- Redis Pub/Sub을 통해 전달받은 퇴장 이벤트
- **프론트: 참가자 목록에서 제거**
- reason 값으로 UI에 적절한 메시지 표시 가능

---

#### 1.4. PARTICIPANT_UPDATE (참가자 정보 업데이트)
**시점**: 다른 참가자의 distance, pace가 업데이트될 때 (주기적)  
**구조**:
```json
{
  "type": "PARTICIPANT_UPDATE",
  "userId": 2,
  "distance": 3.5,
  "pace": 5.0,
  "timestamp": 1699999999999
}
```

**설명**:
- 다른 참가자의 실시간 러닝 정보 업데이트
- **프론트: 해당 참가자 정보만 업데이트**
- 순위는 프론트에서 distance 기준으로 계산

---

#### 1.5. ERROR (오류)
**시점**: 클라이언트 메시지 처리 중 오류 발생  
**구조**:
```json
{
  "type": "ERROR",
  "errorCode": "INVALID_MESSAGE|TIMEOUT|UNAUTHORIZED|...",
  "errorMessage": "메시지 형식이 올바르지 않습니다.",
  "timestamp": 1699999999999
}
```

**설명**: 오류 발생 시 클라이언트에게 알림 (연결 종료 여부는 errorCode에 따름)

---

### 클라이언트 → 서버

#### 1.6. RECORD (러닝 기록)
**시점**: 주기적으로 (예: 5초마다)  
**구조**:
```json
{
  "type": "RECORD",
  "distance": 2.5,
  "pace": 5.2,
  "timestamp": 1699999999999
}
```

**설명**:
- 누적 거리, 페이스 전송
- 서버는 이를 받아 Kafka로 발행 (eventType: running)
- challengeId, userId는 세션에서 자동 추출

**처리**:
- 참가자 정보 업데이트 (Redis)
- 순위 계산
- Redis Pub/Sub 발행 (`challenge:update`)
- Kafka 이벤트 발행 (`eventType: running`)
- 마지막 RECORD 시간 업데이트 (타임아웃 체크용)
- 완주 체크 (목표 거리 달성 시)

---

#### 1.7. QUIT (자발적 포기)
**시점**: 사용자가 포기 버튼을 누를 때  
**구조**:
```json
{
  "type": "QUIT",
  "timestamp": 1699999999999
}
```

**설명**:
- 챌린지 중도 포기
- 서버는 Kafka 발행 (eventType: leave, reason: QUIT)
- DB 상태 업데이트 (RUNNING → QUIT)

**처리**:
- 참가자 상태 DB 업데이트 (QUIT)
- 세션/Redis 정리
- Redis Pub/Sub 발행 (`challenge:leave`, reason: QUIT)
- Kafka 이벤트 발행 (`eventType: leave`, reason: QUIT)
- 연결 종료

---

#### 1.8. PING (연결 유지)
**시점**: 주기적으로 (예: 30초마다)  
**구조**:
```json
{
  "type": "PING",
  "timestamp": 1699999999999
}
```

**설명**:
- 연결 상태 확인용
- 서버는 PONG으로 응답

**처리**:
- PONG 응답 전송

---

#### 1.9. PONG (응답)
**시점**: 서버의 PING에 대한 응답  
**구조**:
```json
{
  "type": "PONG",
  "timestamp": 1699999999999
}
```

**설명**:
- 서버가 먼저 PING을 보내는 경우 클라이언트가 응답

**처리**:
- 단순 수신 (서버가 PING을 보낸 경우 클라이언트가 응답)

---

#### 1.10. KICKED (강제 퇴장)
**시점**: 이상 사용자가 자신의 웹소켓 연결에서 강제 퇴장 메시지를 받을 때  
**구조**:
```json
{
  "type": "KICKED",
  "timestamp": 1699999999999
}
```

**설명**:
- 이상 사용자가 자신의 웹소켓 연결에서 강제 퇴장 메시지를 받을 때 처리
- 서버는 Kafka 발행 (eventType: leave, reason: KICKED)
- DB 상태 업데이트 (RUNNING → KICKED)

**처리**:
- 참가자 상태 DB 업데이트 (KICKED)
- 세션/Redis 정리
- Redis Pub/Sub 발행 (`challenge:leave`, reason: KICKED)
- Kafka 이벤트 발행 (`eventType: leave`, reason: KICKED)
- 연결 종료

---

## 2️⃣ Redis Pub/Sub 메시지

### 채널: `challenge:enter`

#### 2.1. USER_ENTERED
**발행 시점**: 사용자가 챌린지에 입장했을 때  
**발행자**: 해당 사용자가 연결된 WebSocket 서버  
**구독자**: 모든 WebSocket 서버  
**구조**:
```json
{
  "challengeId": 100,
  "userId": 1,
  "nickname": "러너1",
  "profileImage": "https://...",
  "timestamp": 1699999999999
}
```

**처리**:
- 다른 WebSocket 서버들은 이 메시지를 받아서
- 해당 챌린지에 연결된 다른 클라이언트들에게 USER_ENTERED 메시지 전파

---

### 채널: `challenge:leave`

#### 2.2. USER_LEFT
**발행 시점**: 사용자가 챌린지에서 퇴장했을 때  
**발행자**: 해당 사용자가 연결된 WebSocket 서버  
**구독자**: 모든 WebSocket 서버  
**구조**:
```json
{
  "challengeId": 100,
  "userId": 1,
  "reason": "QUIT",
  "timestamp": 1699999999999
}
```

**reason 값**:
- `QUIT`: 자발적 포기
- `FINISH`: 완주
- `TIMEOUT`: 무응답 타임아웃
- `DISCONNECTED`: 연결 끊김
- `KICKED`: 강제 퇴장
- `ERROR`: 오류 발생
- `EXPIRED`: 시간 만료

**처리**:
- WebSocket 서버: 다른 참가자들에게 USER_LEFT 메시지 브로드캐스트
- WebSocket 서버: 참가자 상태를 해당 reason에 맞게 DB에 업데이트 (QUIT, COMPLETE, TIMEOUT, DISCONNECTED, KICKED, ERROR, EXPIRED)

---

### 채널: `challenge:update`

#### 2.3. PARTICIPANT_UPDATE
**발행 시점**: 참가자의 distance, pace가 업데이트될 때  
**발행자**: RECORD 메시지를 받은 WebSocket 서버  
**구독자**: 모든 WebSocket 서버  
**구조**:
```json
{
  "challengeId": 100,
  "userId": 1,
  "distance": 2.5,
  "pace": 5,
  "timestamp": 1699999999999
}
```

**처리**:
- 다른 WebSocket 서버들은 이 메시지를 받아서
- SessionManager의 참가자 정보도 업데이트 (다른 서버에서 보낸 업데이트 반영)
- 해당 챌린지에 연결된 다른 클라이언트들에게 PARTICIPANT_UPDATE 메시지 전파
- 순위는 클라이언트가 distance 기준으로 계산

---

### 채널: `challenge:expired`

#### 2.4. CHALLENGE_EXPIRED
**발행 시점**: 챌린지 종료 시간이 되었을 때  
**발행자**: 스케줄러 서버  
**구독자**: 모든 WebSocket 서버  
**구조**:
```json
{
  "challengeId": 100,
  "timestamp": 1699999999999
}
```

**처리**:
- WebSocket 서버들이 이 메시지를 받아서
- 해당 챌린지의 모든 참가자에 대해 EXPIRED 처리
- 참가자 상태를 DB에 `EXPIRED`로 업데이트
- 세션/Redis 정리, Redis Pub/Sub 발행 (USER_LEFT, reason: EXPIRED), Kafka 이벤트 발행 (LEAVE, reason: EXPIRED)

---

## 3️⃣ Kafka 메시지 (WebSocket 서버 → Stream 서버)

### 토픽: `challenge-stream`

**발행 조건**: `isBroadcast=true`인 챌린지만 발행  
**구조**:
```json
{
  "eventType": "start|running|finish|leave",
  "challengeId": 100,
  "userId": 1,
  "nickname": "러너1",
  "profileImage": "https://...",
  "distance": 2.5,
  "pace": 5,
  "ranking": 1,
  "isBroadcast": true,
  "reason": "TIMEOUT|DISCONNECTED|ERROR|QUIT|KICKED|EXPIRED",
  "timestamp": 1699999999999
}
```

#### 3.1. eventType: `start`
**발행 시점**: 첫 입장 시  
**필드**: `reason` 없음

#### 3.2. eventType: `running`
**발행 시점**: 주기적 RECORD 처리 시, 재접속 시  
**필드**: `reason` 없음

**참고**: `TIMEOUT`, `DISCONNECTED`, `ERROR`로 인한 퇴장 후 재접속 시 `running` 이벤트 발행

#### 3.3. eventType: `finish`
**발행 시점**: 완주 달성 시  
**필드**: `reason` 없음

#### 3.4. eventType: `leave`
**발행 시점**: 퇴장 시  
**필드**: `reason` 필수

**reason 값**:
- `TIMEOUT`: 무응답 타임아웃 (재접속 가능 → 재접속 시 `running` 이벤트)
- `DISCONNECTED`: 연결 끊김 (재접속 가능 → 재접속 시 `running` 이벤트)
- `ERROR`: 오류 발생 (재접속 가능 → 재접속 시 `running` 이벤트)
- `QUIT`: 자발적 포기
- `KICKED`: 강제 퇴장
- `EXPIRED`: 시간 만료
