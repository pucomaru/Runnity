# 🔍 Runnity WebSocket 부하 테스트 검증 및 실행 가이드

---

## 코드 분석 기반 수정 사항

### 1. 테스트용 로그인 엔드포인트 구현 필요

**현재 상태**: `/auth/test-login` 엔드포인트 없음

**필요한 구현**:
```java
// MemberController에 추가 필요
@PostMapping("/auth/test-login")
@Operation(summary = "테스트용 로그인", description = "테스트용 회원 ID로 AccessToken을 즉시 발급합니다")
public ResponseEntity<ApiResponse<LoginResponseDto>> testLogin(
    @RequestParam Long memberId  // 또는 @RequestBody로 memberId 받기
) {
    // memberId로 Member 조회
    // AccessToken 발급 (JwtTokenProvider 사용)
    // LoginResponseDto 반환
}
```

**SecurityConfig 수정 필요**:
- `/api/v1/auth/test-login` 경로를 `permitAll()`에 추가

### 2. 실제 API 엔드포인트

**인증 플로우**:
1. `POST /api/v1/auth/test-login?memberId={id}` → `{accessToken}`
2. `POST /api/v1/challenges/{challengeId}/enter` (Header: `Authorization: Bearer {accessToken}`) → `{ticket, wsUrl}`
3. `WebSocket Connect: {wsUrl}?ticket={ticket}`

**WebSocket 연결**:
- 엔드포인트: `/ws/{serverId}`
- 쿼리 파라미터: `?ticket={ticket}`
- 프로토콜: `wss://`

### 3. Sticky Session 동작

**구현 방식**:
```java
ChallengeService.selectWebSocketServer()
String hashKey = memberId + ":" + challengeId;
int primaryIndex = Math.abs(hashKey.hashCode()) % count;
```

### 4. Redis 구조 확인

**티켓 저장소**:
- Key: `ws_ticket:{ticket}`
- TTL: 30초
- Value: JSON (userId, challengeId, ticketType, nickname, profileImage)

**세션 데이터**:
- `challenge:{challengeId}:participants` (ZSet, score=distance)
- `challenge:{challengeId}:participant:{userId}` (String/JSON)
- `challenge:{challengeId}:user:{userId}:lastRecord` (String, timestamp)

**Pub/Sub 채널**:
- `challenge:enter`
- `challenge:leave`
- `challenge:update`
- `challenge:done`

---

## 🛠 테스트 전 준비 사항

### 1. 테스트용 회원 데이터 준비

**DB에 테스트용 회원 생성**:
```sql
-- 예시: 5000명의 테스트 회원 생성
INSERT INTO runnity_member (email, social_uid, social_type, nickname, profile_image, created_at, updated_at)
VALUES 
  ('test1@test.com', 'test_uid_1', 'TEST', '테스트유저1', NULL, NOW(), NOW()),
  ('test2@test.com', 'test_uid_2', 'TEST', '테스트유저2', NULL, NOW(), NOW()),
  -- ... 5000개
;
```

**필수 필드**:
- `member_id`: 고유 ID
- `nickname`: WebSocket 연결 시 필요
- `profile_image`: nullable

### 2. 테스트용 챌린지 생성

**하나의 대규모 챌린지 생성**:
```sql
-- 테스트용 챌린지 생성 (최대 참가자 수 충분히 크게)
INSERT INTO runnity_challenge (...)
VALUES (...);
```

**참가 신청 데이터**:
```sql
-- 모든 테스트 회원이 챌린지에 참가 신청한 상태로 만들기
INSERT INTO runnity_challenge_participation (challenge_id, member_id, status, ...)
VALUES 
  ({challengeId}, {memberId1}, 'WAITING', ...),
  ({challengeId}, {memberId2}, 'WAITING', ...),
  -- ... 모든 테스트 회원
;
```

**상태 확인**:
- 챌린지 상태: `READY` (입장 가능)
- 참가 상태: `WAITING` (첫 입장 가능)

### 3. `/auth/test-login` 엔드포인트 구현

**구현 위치**: `MemberController.java`

**구현 내용**:
- `@PostMapping("/auth/test-login")` 추가
- `@RequestParam Long memberId` 받기
- `MemberRepository.findById(memberId)` 조회
- `JwtTokenProvider.createAccessToken(member)` 발급
- `LoginResponseDto` 반환 (refreshToken은 빈 문자열 또는 null 가능)

**SecurityConfig 수정**:
```java
.requestMatchers(
    "/swagger-ui/**",
    "/v3/api-docs/**",
    "/api/v1/auth/login/**",
    "/api/v1/auth/token",
    "/api/v1/auth/test-login"  // 추가
).permitAll()
```

### 4. JMeter 플러그인 설치

**필수 플러그인**:
- **WebSocket Samplers by Peter Doornbosch**
  - 설치: JMeter → Options → Plugins Manager → "WebSocket Samplers" 검색 후 설치

**대안**:
- **JMeter WebSocket Plugin** (다른 플러그인)
- 또는 **Java WebSocket Client**를 사용한 커스텀 스크립트

---

## 📊 JMeter 시나리오 상세

### 시나리오 플로우

```
1. HTTP Request: POST /api/v1/auth/test-login?memberId=${__threadNum}
   → Response: {accessToken}
   → Extract: accessToken → ${accessToken}

2. HTTP Request: POST /api/v1/challenges/${challengeId}/enter
   Headers:
     - Authorization: Bearer ${accessToken}
   → Response: {ticket, wsUrl, challengeId, userId, expiresIn}
   → Extract: ticket → ${ticket}, wsUrl → ${wsUrl}

3. WebSocket Open Connection
   URL: ${wsUrl}?ticket=${ticket}
   → Connection Established

4. WebSocket Single Read Sampler
   → Wait for CONNECTED message
   → Validate: type == "CONNECTED"

5. Loop Controller (20~30분 유지)
   - WebSocket Single Write Sampler: PING 메시지 전송
   - WebSocket Single Read Sampler: PONG 메시지 수신 대기
   - Timer: 30초 대기
```

### JMeter 테스트 계획 구조

```
Test Plan
├── Thread Group (500 connections)
│   ├── HTTP Request: test-login
│   ├── HTTP Request: enter challenge
│   ├── WebSocket Open Connection
│   ├── WebSocket Single Read (CONNECTED)
│   └── Loop Controller (600 iterations = 30분)
│       ├── WebSocket Single Write (PING)
│       ├── WebSocket Single Read (PONG)
│       └── Constant Timer (30000ms)
├── Thread Group (500 connections)  # PC 2
├── Thread Group (500 connections)  # PC 3
└── ... (총 6~7개 PC, 각 500~1500 connections)
```

### JMeter 설정 값

**Thread Group**:
- Number of Threads: 500 (PC당)
- Ramp-up Period: 300초 (5분에 걸쳐 점진적 연결)
- Loop Count: 1 (외부 Loop Controller 사용)

**HTTP Request - test-login**:
- Method: POST
- Path: `/api/v1/auth/test-login`
- Parameters: `memberId=${__threadNum}` 또는 CSV Data Set Config 사용

**HTTP Request - enter challenge**:
- Method: POST
- Path: `/api/v1/challenges/${challengeId}/enter`
- Headers:
  - `Authorization: Bearer ${accessToken}`
  - `Content-Type: application/json`

**WebSocket Open Connection**:
- Server Name or IP: `${wsUrl}`에서 추출
- Port Number: URL에서 추출 (wss://domain:port)
- Path: `/ws?ticket=${ticket}` 또는 `/ws/{serverId}?ticket=${ticket}`

**WebSocket Single Write (PING)**:
```json
{
  "type": "PING",
  "timestamp": ${__time()}
}
```

**WebSocket Single Read (PONG)**:
- Timeout: 5000ms
- Expected: `"type":"PONG"`

### CSV Data Set Config (선택)

**테스트 회원 ID 목록 파일** (`test_members.csv`):
```csv
memberId,challengeId
1,100
2,100
3,100
...
```

**JMeter 설정**:
- Filename: `test_members.csv`
- Variable Names: `memberId,challengeId`
- Delimiter: `,`
- Recycle on EOF: `true`
- Stop thread on EOF: `false`

---

## 📈 모니터링 지표 및 확인 방법

### 1. WebSocket 서버 지표

#### Active Connections
**확인 방법**:
```bash
# 로그에서 확인
grep "세션 등록 완료" websocket-server.log | wc -l

# 또는 JMeter Summary Report에서 확인
```

**예상 값**:
- 서버 1대: 2,000~3,000 connections
- 서버 2대: 4,000~6,000 connections (균등 분산 시)

#### CPU 사용률
**확인 방법**:
- Grafana 대시보드
- 또는 `top`, `htop` 명령어

**주의 사항**:
- CPU 80% 이상 지속 시 병목 가능성
- EventLoop delay 확인 필요

#### EventLoop Delay
**확인 방법**:
- Spring Boot Actuator Metrics
- 또는 커스텀 메트릭 구현

**예상 값**:
- 정상: < 100ms
- 경고: 100~500ms
- 위험: > 500ms

#### GC (Garbage Collection)
**확인 방법**:
```bash
# JVM 옵션 추가
-XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:gc.log

# 또는 JMX로 모니터링
```

**주의 사항**:
- Full GC 빈도: 1분에 1회 이상이면 문제
- GC 시간: Full GC가 1초 이상 지속되면 문제

#### 메모리 사용량
**확인 방법**:
- JVM 힙 메모리: `jstat -gc <pid>`
- 또는 Grafana

**예상 값**:
- 각 WebSocket 세션: 약 1~2KB 메모리
- 3000 connections: 약 3~6MB (세션 객체만)
- 전체 힙: 서버 설정에 따라 다름

### 2. Redis 지표

#### Pub/Sub Latency
**확인 방법**:
```bash
# Redis 명령어
redis-cli --latency

# 또는 Redis Slowlog
redis-cli SLOWLOG GET 10
```

**예상 값**:
- 정상: < 10ms
- 경고: 10~50ms
- 위험: > 50ms

#### Redis Memory Usage
**확인 방법**:
```bash
redis-cli INFO memory
```

**주요 키**:
- `ws_ticket:*`: TTL 30초, 자동 만료
- `challenge:{id}:participants`: ZSet, 참가자 수만큼
- `challenge:{id}:participant:{userId}`: String, 참가자 수만큼

**예상 메모리**:
- 참가자 1명당: 약 500 bytes (ZSet + String)
- 5000명: 약 2.5MB

#### Redis Connection Count
**확인 방법**:
```bash
redis-cli INFO clients
```

**주의 사항**:
- WebSocket 서버당 연결 수 확인
- Connection pool 설정 확인

### 3. Nginx 지표

#### Upstream 분포
**확인 방법**:
- Nginx Access Log 분석
- 또는 Nginx Status Module

**확인 사항**:
- WebSocket 서버 간 연결 수 분산이 균등한지
- 특정 서버로만 몰리는지

#### 499, 502 에러
**확인 방법**:
```bash
# Nginx Access Log
grep " 499 " access.log | wc -l
grep " 502 " access.log | wc -l
```

**의미**:
- 499: 클라이언트가 연결을 끊음 (타임아웃 가능성)
- 502: Upstream 서버 오류 (WebSocket 서버 다운 가능성)

### 4. 비즈니스 서버 지표

#### `/api/v1/challenges/{id}/enter` 응답 시간
**확인 방법**:
- JMeter Summary Report
- 또는 Application Log

**예상 값**:
- 정상: < 200ms
- 경고: 200~500ms
- 위험: > 500ms

#### DB Connection Pool
**확인 방법**:
- HikariCP Metrics
- 또는 Application Log

**주의 사항**:
- Connection pool 고갈 시 새로운 연결 실패
- `maximum-pool-size: 10` 확인 (부족할 수 있음)

### 5. JMeter 지표

#### Response Time
- test-login: < 100ms
- enter challenge: < 200ms
- WebSocket 연결: < 1초

#### Error Rate
- 목표: < 0.1% (1000개 중 1개 미만)
- 경고: 0.1~1%
- 위험: > 1%

#### Throughput
- 초당 연결 수: 목표 100~200 connections/sec
- 총 연결 시간: 5분 (ramp-up) + 30분 (유지) = 35분

---

## ⚠️ 예상 이슈 및 대응 방안

### 1. Ticket 만료 (30초 TTL)

**증상**:
- WebSocket 연결 시 `INVALID_TICKET` 에러

**원인**:
- Ticket 발급 후 30초 이내에 연결하지 않음
- 또는 네트워크 지연

**대응**:
- Ticket 발급 직후 즉시 WebSocket 연결
- JMeter에서 `enter` 요청 후 1초 이내 연결
- 필요 시 `WEBSOCKET_TICKET_TTL` 환경 변수로 TTL 증가 (테스트용)

### 2. DB Connection Pool 고갈

**증상**:
- `enter` 요청 실패
- `HikariPool - Connection is not available` 에러

**원인**:
- `maximum-pool-size: 10`이 부족
- 트랜잭션 처리 시간이 길어서 연결이 반환되지 않음

**대응**:
- 테스트 시 Connection Pool 크기 임시 증가
- 또는 Connection Pool 모니터링 후 필요 시 조정

### 3. Redis Pub/Sub 지연

**증상**:
- 다른 서버의 참가자 입장/퇴장 이벤트가 늦게 전파됨
- 또는 이벤트 누락

**원인**:
- Redis 부하
- 또는 네트워크 지연

**대응**:
- Redis 성능 모니터링
- 필요 시 Redis 인스턴스 분리 (Cache/PubSub)

### 4. WebSocket 서버 메모리 부족

**증상**:
- OutOfMemoryError
- 또는 GC 빈번 발생

**원인**:
- 세션 수가 예상보다 많음
- 또는 메모리 누수

**대응**:
- JVM 힙 메모리 증가
- GC 튜닝
- 세션 정리 로직 확인

### 5. Sticky Session 불일치

**증상**:
- 동일 사용자가 다른 서버로 라우팅됨
- 또는 서버 간 참가자 목록 불일치

**원인**:
- 서버 수 변경
- 또는 해시 알고리즘 변경

**대응**:
- 서버 수 고정 (테스트 중)
- 해시 알고리즘 일관성 확인

### 6. 타임아웃 발생

**증상**:
- 60초 무응답 시 자동 퇴장
- `TIMEOUT` reason으로 연결 종료

**원인**:
- PING 메시지 전송 실패
- 또는 서버 처리 지연

**대응**:
- JMeter에서 PING 주기 확인 (30초)
- 서버 로그에서 타임아웃 원인 확인

### 7. Nginx 502 에러

**증상**:
- WebSocket 연결 실패
- 502 Bad Gateway

**원인**:
- WebSocket 서버 다운
- 또는 Health Check 실패

**대응**:
- WebSocket 서버 상태 확인
- Health Check 로직 확인 (`ws_health:*` Redis Key)

---

## 📝 테스트 체크리스트

### 테스트 전
- [ ] 테스트용 회원 5000명 이상 DB에 생성
- [ ] 테스트용 챌린지 생성 및 모든 회원 참가 신청 완료
- [ ] `/auth/test-login` 엔드포인트 구현 및 배포
- [ ] SecurityConfig에 `/auth/test-login` 경로 추가
- [ ] JMeter WebSocket 플러그인 설치
- [ ] 테스트 환경 변수 확인 (Redis, Kafka, DB)
- [ ] Grafana 대시보드 준비 (선택)

### 테스트 중
- [ ] PC별 연결 수 분배 확인
- [ ] Ramp-up 기간 동안 연결 수 모니터링
- [ ] 에러 로그 실시간 확인
- [ ] CPU, Memory, GC 모니터링
- [ ] Redis Pub/Sub 지연 확인
- [ ] Nginx upstream 분산 확인

### 테스트 후
- [ ] 최대 안정 연결 수 기록
- [ ] 에러 발생 시점 및 원인 분석
- [ ] 서버별 연결 수 분산 확인
- [ ] Redis 메모리 사용량 확인
- [ ] GC 로그 분석
- [ ] 테스트 결과 리포트 작성

---

## 🎯 최종 목표 달성 기준

### 성공 기준
1. **연결 수**: 4,000~6,000 connections 안정 유지 (20~30분)
2. **에러율**: < 0.1% (1000개 중 1개 미만)
3. **응답 시간**: 
   - test-login: < 100ms
   - enter: < 200ms
   - WebSocket 연결: < 1초
4. **서버 안정성**:
   - CPU: < 80%
   - Memory: OutOfMemoryError 없음
   - GC: Full GC 1분에 1회 미만

### 실패 기준
1. 연결 수가 4,000 미만에서 불안정
2. 에러율 > 1%
3. 서버 다운 또는 OutOfMemoryError
4. Redis Pub/Sub 지연 > 100ms

---

## 📚 참고 자료

### 코드 위치
- WebSocket Handler: `backend/runnity-websocket/src/main/java/com/runnity/websocket/handler/ChallengeWebSocketHandler.java`
- Ticket Service: `backend/runnity/src/main/java/com/runnity/global/service/WebSocketTicketService.java`
- Challenge Enter: `backend/runnity/src/main/java/com/runnity/challenge/service/ChallengeService.java`
- Redis Pub/Sub: `backend/runnity-websocket/src/main/java/com/runnity/websocket/service/RedisPubSubService.java`
- Session Manager: `backend/runnity-websocket/src/main/java/com/runnity/websocket/manager/SessionManager.java`

### 문서
- 메시지 흐름: `backend/runnity-websocket/MESSAGE_FLOW.md`
- 이 가이드: `backend/runnity-websocket/LOAD_TEST_GUIDE.md`

