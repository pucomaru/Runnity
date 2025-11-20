# 🔍 Runnity WebSocket 부하 테스트 실행 가이드

---

## 🛠 테스트 전 준비 사항

### 1. 테스트용 회원 데이터 준비

**1-1. SQL 파일 생성**:
```bash
cd exec/test
python3 generate_test_members.py
```
- `test_members_insert.sql` 파일이 생성됩니다 (10,000명, 100개씩 INSERT 문으로 분리)

**1-2. DB에 회원 데이터 삽입**:
```bash
mysql -u [username] -p [database_name] < test_members_insert.sql
```

**1-3. 생성된 회원 정보**:
- 총 10,000명의 테스트 회원
- `social_uid`: `test_uid_1` ~ `test_uid_10000`
- `email`: `test1@test.com` ~ `test10000@test.com`
- `nickname`: `테스트유저1` ~ `테스트유저10000`
- `social_type`: `TEST`

**1-4. 회원 ID 확인**:
```sql
SELECT member_id, social_uid, nickname 
FROM member 
WHERE social_type = 'TEST' 
ORDER BY member_id 
LIMIT 10;
```
- `member_id`는 AUTO_INCREMENT로 자동 생성되므로, `social_uid`로 식별하거나 위 쿼리로 확인

### 2. 테스트용 챌린지 생성

**2-1. 테스트 계정 AccessToken 발급**:
- 테스트용 회원 중 하나를 사용하여 AccessToken 발급
- `POST /api/v1/auth/test-login?memberId={memberId}` 사용

**2-2. Admin 챌린지 생성 API 호출** (인원 제한 없음):
- 엔드포인트: `POST /api/v1/admin/challenges`
- 헤더: 
  - `Authorization: Bearer {accessToken}`
  - `Content-Type: application/json`
- **특징**: 권한 체크 없음, 최대 참가자 수 제한 없음 (테스트용)

**2-3. 요청 본문 예시**:
```json
{
  "title": "부하 테스트용 챌린지",
  "description": "WebSocket 부하 테스트를 위한 대규모 챌린지입니다.",
  "maxParticipants": 10000,
  "startAt": "2025-11-21T10:00:00",
  "distance": "FIVE",
  "isPrivate": false,
  "password": null,
  "isBroadcast": false
}
```

**응답 예시** (HTTP 201 Created):
```json
{
  "isSuccess": true,
  "code": 201,
  "message": "챌린지가 성공적으로 생성되었습니다.",
  "data": {
    "challengeId": 100,
    "title": "부하 테스트용 챌린지",
    "status": "RECRUITING",
    "currentParticipants": 0,
    "maxParticipants": 10000,
    "startAt": "2025-11-21T10:00:00",
    "endAt": "2025-11-21T11:00:00",
    "description": "WebSocket 부하 테스트를 위한 대규모 챌린지입니다.",
    "distance": "FIVE",
    "isPrivate": false,
    "isBroadcast": false,
    "joined": false,
    "participants": [],
    "createdAt": "2025-11-20T12:00:00",
    "updatedAt": "2025-11-20T12:00:00"
  }
}
```

**2-4. 생성된 챌린지 ID 확인**:
- 응답의 `data.challengeId` 값을 저장 (예: `100`)
- 이후 참가 신청 및 테스트에 사용

### 3. 테스트 회원 챌린지 참가 신청

**3-1. 참가 신청 API**:
- 엔드포인트: `POST /api/v1/challenges/{challengeId}/join`
- 헤더: 
  - `Authorization: Bearer {accessToken}`
  - `Content-Type: application/json`
- 요청 본문: 비밀방이 아닌 경우 `{}` 또는 생략 가능

**3-2. 요청/응답 예시**:

*요청 본문* (비밀방이 아닌 경우):
```json
{}
```

*응답* (HTTP 201 Created):
```json
{
  "isSuccess": true,
  "code": 201,
  "message": "챌린지 참가 신청이 완료되었습니다.",
  "data": {
    "participantId": 501,
    "challengeId": 100,
    "memberId": 1,
    "status": "WAITING",
    "rank": null,
    "averagePace": null
  }
}
```

**3-3. 대량 참가 신청 방법**:
- JMeter를 사용하여 모든 테스트 회원(10,000명)의 참가 신청을 대량으로 처리
- 각 회원마다 다음 순서로 진행:
  1. `POST /api/v1/auth/test-login?memberId={memberId}` → AccessToken 발급
  2. `POST /api/v1/challenges/{challengeId}/join` (Header: `Authorization: Bearer {accessToken}`) → 참가 신청

**3-4. 상태 확인**:
- 챌린지 상태: `READY` (입장 가능) 또는 `RECRUITING` (모집 중)
- 참가 상태: `WAITING` (첫 입장 가능)
- 확인 쿼리:
```sql
SELECT COUNT(*) 
FROM challenge_participation 
WHERE challenge_id = {challengeId} 
  AND status = 'WAITING';
```
- 예상 결과: 10,000명 (모든 테스트 회원)

### 4. 테스트에 필요한 API 엔드포인트 확인

**4-1. 테스트 로그인 API**:
- 엔드포인트: `POST /api/v1/auth/test-login?memberId={memberId}`
- 인증: 불필요
- 응답 구조:
```json
{
  "isSuccess": true,
  "code": 200,
  "message": "로그인 성공",
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "isNewUser": false,
    "needAdditionalInfo": false
  }
}
```

**4-2. 티켓 발급 API**:
- 엔드포인트: `POST /api/v1/challenges/{challengeId}/enter`
- 헤더: `Authorization: Bearer {accessToken}`
- 응답 구조:
```json
{
  "isSuccess": true,
  "code": 200,
  "message": "OK",
  "data": {
    "ticket": "uuid-string",
    "wsUrl": "wss://domain/ws/{serverId}",
    "challengeId": 100,
    "userId": 1,
    "expiresIn": 30
  }
}
```

**4-3. WebSocket 연결**:
- URL: `{wsUrl}?ticket={ticket}` (응답의 `data.wsUrl` 사용)
- 프로토콜: `wss://`
- 엔드포인트: `/ws` 또는 `/ws/{serverId}`
- 쿼리 파라미터: `ticket` (티켓 발급 API 응답의 `data.ticket` 사용)

### 5. JMeter 플러그인 설치

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

### 0. Prometheus & Grafana 설정 확인

**인프라 구성**:
- Prometheus: `http://localhost:9091` (또는 배포 서버 주소)
- Grafana: `http://localhost:3000` (또는 배포 서버 주소)
- cAdvisor: `http://localhost:8081` (컨테이너 메트릭)

**Actuator 메트릭 엔드포인트**:
- WebSocket 서버: `http://{websocket-server}:{port}/actuator/prometheus`
- 기본 포트: `8081`
- 예시: `http://localhost:8081/actuator/prometheus`

**Prometheus 설정 확인**:
- 설정 파일: `infra/grafana/prometheus.yml`
- WebSocket 서버를 scrape target으로 등록되어 있는지 확인

**Grafana 대시보드**:
- Prometheus 데이터 소스 연결 확인
- WebSocket 관련 대시보드가 있다면 사용
- 없으면 기본 메트릭으로 대시보드 구성

### 1. WebSocket 서버 지표

#### Active Connections
**확인 방법**:
1. **로그에서 확인**:
```bash
grep "세션 등록 완료" websocket-server.log | wc -l
```

2. **Prometheus 메트릭** (Actuator가 활성화된 경우):
```promql
# JVM 스레드 수 (간접적)
jvm_threads_live_threads{application="runnity-websocket-server"}

# 또는 커스텀 메트릭이 있다면
websocket_active_connections
```

3. **JMeter Summary Report**:
- WebSocket Open Connection 성공 수 확인

**예상 값**:
- 서버 1대: 2,000~3,000 connections
- 서버 2대: 4,000~6,000 connections (균등 분산 시)

#### CPU 사용률
**확인 방법**:
1. **Grafana 대시보드**:
   - cAdvisor 메트릭: `container_cpu_usage_seconds_total`
   - 또는 Prometheus: `process_cpu_usage`

2. **명령어**:
```bash
top -p <pid>
htop
```

**주의 사항**:
- CPU 80% 이상 지속 시 병목 가능성
- EventLoop delay 확인 필요

**Grafana 쿼리 예시**:
```promql
rate(container_cpu_usage_seconds_total{name="websocket"}[5m]) * 100
```

#### 메모리 사용량
**확인 방법**:
1. **Grafana 대시보드**:
   - JVM 힙: `jvm_memory_used_bytes{area="heap"}`
   - JVM 비힙: `jvm_memory_used_bytes{area="nonheap"}`
   - 컨테이너 메모리: `container_memory_usage_bytes{name="websocket"}`

2. **명령어**:
```bash
jstat -gc <pid>
```

**예상 값**:
- 각 WebSocket 세션: 약 1~2KB 메모리
- 3000 connections: 약 3~6MB (세션 객체만)
- 전체 힙: 서버 설정에 따라 다름

**Grafana 쿼리 예시**:
```promql
jvm_memory_used_bytes{application="runnity-websocket-server", area="heap"} / 1024 / 1024
```

#### GC (Garbage Collection)
**확인 방법**:
1. **Grafana 대시보드**:
   - GC 시간: `jvm_gc_pause_seconds_sum`
   - GC 횟수: `jvm_gc_pause_seconds_count`
   - Full GC: `jvm_gc_pause_seconds{action="end of major GC"}`

2. **JVM 옵션** (로그 파일로 확인):
```bash
-XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:gc.log
```

**주의 사항**:
- Full GC 빈도: 1분에 1회 이상이면 문제
- GC 시간: Full GC가 1초 이상 지속되면 문제

**Grafana 쿼리 예시**:
```promql
# Full GC 횟수 (1분당)
rate(jvm_gc_pause_seconds_count{action="end of major GC"}[1m])
```

#### HTTP 요청 메트릭
**확인 방법**:
- Actuator 기본 메트릭: `http_server_requests_seconds`
- 요청 수, 응답 시간, 에러율 확인 가능

**Grafana 쿼리 예시**:
```promql
# 초당 요청 수
rate(http_server_requests_seconds_count[5m])

# 평균 응답 시간
rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])
```

### 2. Redis 지표

#### Pub/Sub Latency
**확인 방법**:
1. **Redis 명령어**:
```bash
redis-cli --latency
redis-cli SLOWLOG GET 10
```

2. **Redis Exporter가 있다면** (Prometheus 메트릭):
```promql
redis_latency_milliseconds
```

**예상 값**:
- 정상: < 10ms
- 경고: 10~50ms
- 위험: > 50ms

#### Redis Memory Usage
**확인 방법**:
1. **Redis 명령어**:
```bash
redis-cli INFO memory
redis-cli MEMORY STATS
```

2. **Redis Exporter가 있다면**:
```promql
redis_memory_used_bytes
```

**주요 키**:
- `ws_ticket:*`: TTL 30초, 자동 만료
- `challenge:{id}:participants`: ZSet, 참가자 수만큼
- `challenge:{id}:participant:{userId}`: String, 참가자 수만큼
- `ws_health:*`: WebSocket 서버 헬스체크 (TTL 기반)

**예상 메모리**:
- 참가자 1명당: 약 500 bytes (ZSet + String)
- 5000명: 약 2.5MB

**키 개수 확인**:
```bash
redis-cli DBSIZE
redis-cli KEYS "challenge:*" | wc -l
```

#### Redis Connection Count
**확인 방법**:
```bash
redis-cli INFO clients
# connected_clients 값 확인
```

**주의 사항**:
- WebSocket 서버당 연결 수 확인
- Connection pool 설정 확인
- `maximum-pool-size` 초과 여부 확인

### 3. Nginx 지표

#### Upstream 분포
**확인 방법**:
1. **Nginx Access Log 분석**:
```bash
# WebSocket 업그레이드 요청 수
grep "Upgrade: websocket" access.log | wc -l

# 서버별 분산 확인
grep "Upgrade: websocket" access.log | awk '{print $NF}' | sort | uniq -c
```

2. **Nginx Status Module** (설정되어 있다면):
- `http://nginx/nginx_status` 또는 `http://nginx/stub_status`

**확인 사항**:
- WebSocket 서버 간 연결 수 분산이 균등한지
- 특정 서버로만 몰리는지

#### 499, 502 에러
**확인 방법**:
```bash
# Nginx Access Log
grep " 499 " access.log | wc -l
grep " 502 " access.log | wc -l

# 실시간 모니터링
tail -f access.log | grep -E " 499 | 502 "
```

**의미**:
- 499: 클라이언트가 연결을 끊음 (타임아웃 가능성)
- 502: Upstream 서버 오류 (WebSocket 서버 다운 가능성)

**에러율 계산**:
```bash
# 전체 요청 대비 에러 비율
total=$(wc -l < access.log)
errors=$(grep -E " 499 | 502 " access.log | wc -l)
echo "scale=2; $errors * 100 / $total" | bc
```

### 4. 비즈니스 서버 지표

#### `/api/v1/challenges/{id}/enter` 응답 시간
**확인 방법**:
1. **JMeter Summary Report**:
   - Average, Min, Max, 90th percentile 확인

2. **Actuator 메트릭** (비즈니스 서버):
```promql
# enter 엔드포인트 평균 응답 시간
rate(http_server_requests_seconds_sum{uri="/api/v1/challenges/{id}/enter"}[5m]) / 
rate(http_server_requests_seconds_count{uri="/api/v1/challenges/{id}/enter"}[5m])
```

3. **Application Log**:
```bash
grep "enterChallenge" application.log | grep "duration"
```

**예상 값**:
- 정상: < 200ms
- 경고: 200~500ms
- 위험: > 500ms

#### DB Connection Pool
**확인 방법**:
1. **HikariCP Metrics** (Actuator):
```promql
# 활성 연결 수
hikaricp_connections_active{pool="RunnityHikariPool"}

# 대기 중인 연결 수
hikaricp_connections_pending{pool="RunnityHikariPool"}

# 최대 풀 크기
hikaricp_connections_max{pool="RunnityHikariPool"}
```

2. **Application Log**:
```bash
grep "HikariPool" application.log | grep -E "Connection is not available|Pool"
```

**주의 사항**:
- Connection pool 고갈 시 새로운 연결 실패
- `maximum-pool-size: 10` 확인 (부족할 수 있음)
- `hikaricp_connections_pending` 값이 지속적으로 증가하면 풀 크기 부족

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
- [ ] 테스트용 회원 10,000명 DB에 생성 (`exec/test/generate_test_members.py` 실행)
- [ ] 테스트 계정 AccessToken 발급
- [ ] Admin API로 테스트용 챌린지 생성 (`POST /api/v1/admin/challenges`)
- [ ] 모든 테스트 회원이 챌린지에 참가 신청 완료 (`POST /api/v1/challenges/{id}/join`)
- [ ] 챌린지 상태 확인: `READY` (입장 가능)
- [ ] 참가 상태 확인: `WAITING` (첫 입장 가능)
- [ ] JMeter WebSocket 플러그인 설치
- [ ] 테스트 환경 변수 확인 (Redis, Kafka, DB)
- [ ] Prometheus & Grafana 서비스 실행 확인 (`infra/docker-compose.yml`)
- [ ] Prometheus가 WebSocket 서버 메트릭을 수집하는지 확인 (`/actuator/prometheus` 엔드포인트)
- [ ] Grafana 대시보드 준비 (Prometheus 데이터 소스 연결 확인)

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

### 문서
- 메시지 흐름: `backend/runnity-websocket/MESSAGE_FLOW.md`
- 이 가이드: `exec/test/LOAD_TEST_GUIDE.md`
