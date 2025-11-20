<div align="center">

<h1 style="background-color: #3958bd; color: white; padding: 20px; border-radius: 10px; margin: 0; display: inline-block;">Runnity</h1>

</div>

<br>

## 📚 목차

1.  [**프로젝트 개요**](#-프로젝트-개요)

2.  [**핵심 기능 소개**](#-핵심-기능-소개)

3.  [**서비스 소개**](#-서비스-소개)

4.  [**프로젝트 설계**](#-프로젝트-설계)

5.  [**기술 스택**](#-기술-스택)

6.  [**팀원 소개**](#-팀원-소개)

<br/>

## 📄 프로젝트 개요

> **개발 기간** : `2025.10.10 ~ 2025.11.20`

**Runnity**는 러너들이 실시간으로 운동 데이터를 공유하며 함께 달릴 수 있는 **러닝 챌린지 플랫폼**입니다.
개인 러닝과 그룹 챌린지를 지원하며, WebSocket 기반 실시간 데이터 공유와 AI 중계 기능으로 몰입감을 높였습니다.

<br>

## ✨ 핵심 기능 소개

### 1. 실시간 러닝 챌린지

- **챌린지 생성/참여**: 거리(3km, 5km, 10km 등)와 공개 범위를 설정해 챌린지를 만들고 참여할 수 있습니다.

- **운동 데이터 실시간 공유**: WebSocket으로 거리, 페이스, 시간 등 운동 데이터를 주고받으며 함께 달립니다.

- **실시간 랭킹**: 거리와 페이스를 기반으로 순위를 계산해 경쟁 요소를 제공합니다.

<br>

### 2. 중계방 스트리밍

- **라이브 스트리밍**: 챌린지 상황을 실시간으로 중계해 시청자들이 참가자들의 러닝을 관전할 수 있습니다.

- **AI 하이라이트**: Kafka로 수집한 데이터를 분석해 러닝 중 주요 순간을 자동 생성합니다.

<br>

### 3. 러닝 기록 및 통계

- **기록 관리**: 개인 러닝과 챌린지 기록을 구분해 거리, 페이스, 랩 타임 등 상세 정보를 제공합니다.
- **통계 대시보드**: 주/월/년 데이터를 시각화해 러닝 추이를 확인할 수 있습니다.

<br>

### 4. Wear OS 지원

- **스마트워치 연동**: Wear OS 기기로 러닝 중에도 실시간 데이터를 확인하고 간편하게 제어할 수 있습니다.

<br>

### 5. 소셜 기능

- **소셜 로그인**: Google, Kakao 로그인을 지원합니다.

- **프로필 관리**: 프로필 이미지, 닉네임 등 개인 정보를 관리할 수 있습니다.

<br/>

<br/>

## 💻 서비스 소개

### 1️⃣ **온보딩 및 로그인**

> 소셜 로그인으로 간편하게 시작할 수 있습니다.

| 온보딩 화면 |
| :---: |
| <img src="exec/image/온보딩.png" width="300"> |

<br>

### 2️⃣ **챌린지 생성 및 참여**

> 다양한 옵션으로 챌린지를 만들고 참여할 수 있습니다.

| 챌린지 목록 | 챌린지 상세 |
| :---: | :---: |
| <img src="exec/image/챌린지_목록.png" width="300"> | <img src="exec/image/챌린지_상세.png" width="300"> |

| 챌린지 생성 | 챌린지 결과 |
| :---: | :---: |
| <img src="exec/image/챌린지_생성.png" width="300"> | <img src="exec/image/챌린지_결과.png" width="300"> |

<br>

### 3️⃣ **실시간 러닝 데이터 공유**

> WebSocket으로 거리, 시간, 페이스 등 운동 데이터를 실시간 주고받습니다.

| 메인 홈 | 개인 러닝 목표 설정 |
| :---: | :---: |
| <img src="exec/image/메인_홈.png" width="300"> | <img src="exec/image/개인_러닝_목표_설정.png" width="300"> |

| 개인 러닝 시작 전 | 개인 러닝 진행 중 |
| :---: | :---: |
| <img src="exec/image/개인_러닝_시작_전.png" width="300"> | <img src="exec/image/개인_러닝_진행_중.png" width="300"> |

| 개인 러닝 결과 |
| :---: |
| <img src="exec/image/개인_러닝_결과.png" width="300"> |

<br>

### 4️⃣ **중계방 스트리밍**

> 챌린지 진행 상황을 실시간 중계하며, AI가 중계 메세지를 생성합니다.

| 중계방 스트리밍 |
| :---: |
| <p align="center">중계방 기능 화면 (추가 예정)</p> |

<br>

### 5️⃣ **러닝 기록 및 통계**

> 달린 기록을 확인하고 나만의 통계를 관리할 수 있습니다.

| 러닝 기록 및 통계 |
| :---: |
| <p align="center">러닝 기록 및 통계 화면 (추가 예정)</p> |

<br>

### 6️⃣ **마이페이지**

> 사용자 정보를 확인하고 수정할 수 있습니다.

| 마이페이지 |
| :---: |
| <p align="center">마이페이지 화면 (추가 예정)</p> |

<br/>

<br/>

## 🛠️ 프로젝트 설계

### 시스템 아키텍처

<img width="800px" src="exec/image/아키텍처.png">

### 주요 컴포넌트

1. **Backend Server**: API 제공, 사용자 및 챌린지 관리
2. **WebSocket Server**: 운동 데이터 실시간 송수신
3. **Stream Server**: 라이브 중계 처리
4. **AI Server**: 운동 데이터 기반 하이라이트 생성
5. **Redis**: 캐싱 및 Pub/Sub 메시지 처리
6. **Kafka**: 서버 간 이벤트 스트리밍

<br>

## ⚙️ 기술 스택

| 분류 | 기술 스택 |
| :---: | :--- |
| **Frontend** | ![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white) ![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white) ![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpack-compose&logoColor=white) ![Material3](https://img.shields.io/badge/Material3-757575?style=for-the-badge&logo=material-design&logoColor=white) |
| **Backend** | ![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white) ![Spring WebSocket](https://img.shields.io/badge/Spring_WebSocket-6DB33F?style=for-the-badge&logo=spring&logoColor=white) |
| **AI** | ![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white) ![FastAPI](https://img.shields.io/badge/FastAPI-009688?style=for-the-badge&logo=fastapi&logoColor=white) |
| **Database** | ![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white) ![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white) ![Kafka](https://img.shields.io/badge/Kafka-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white) |
| **Infra** | ![Ubuntu](https://img.shields.io/badge/Ubuntu-E95420?style=for-the-badge&logo=ubuntu&logoColor=white) ![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white) ![Jenkins](https://img.shields.io/badge/Jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white) ![Nginx](https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white) ![AWS](https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white) |

### 상세 기술 스택

#### Frontend
- Kotlin, Jetpack Compose (MVVM)
- Retrofit, OkHttp, Coroutines, Flow
- Hilt, Kakao Map SDK
- Google/Kakao 로그인, FCM 알림, Wear OS 연동

#### Backend
- Java 17, Spring Boot 3.5.7
- Gradle 8.14.3, JPA, Hibernate
- JWT, OAuth2 인증
- Swagger 문서화, AWS S3 연동

#### AI Server
- Python 3.11, FastAPI, Uvicorn
- Kafka (kafka-python), Threading

#### Infrastructure
- Ubuntu 24.04, Docker, Docker Compose
- Jenkins, Nginx (SSL, Reverse Proxy)
- Let's Encrypt 인증서
- Prometheus, Grafana 모니터링

<br/>

<br/>

## 👨‍👩‍👧‍👦 팀원 소개

<table>
<tr>
<td align="center" width="33%">
<img src="https://via.placeholder.com/120" width="120" height="120" style="border-radius: 50%;">
<br/>
<b>신상원</b>
<br/>
<i>팀장 / FE</i>
</td>
<td align="center" width="33%">
<img src="https://via.placeholder.com/120" width="120" height="120" style="border-radius: 50%;">
<br/>
<b>김동휘</b>
<br/>
<i>BE</i>
</td>
<td align="center" width="33%">
<img src="https://via.placeholder.com/120" width="120" height="120" style="border-radius: 50%;">
<br/>
<b>김상경</b>
<br/>
<i>BE</i>
</td>
</tr>
<tr>
<td align="center" width="33%">
<img src="https://via.placeholder.com/120" width="120" height="120" style="border-radius: 50%;">
<br/>
<b>윤선호</b>
<br/>
<i>BE</i>
</td>
<td align="center" width="33%">
<img src="https://via.placeholder.com/120" width="120" height="120" style="border-radius: 50%;">
<br/>
<b>이상민</b>
<br/>
<i>INFRA</i>
</td>
<td align="center" width="33%">
<img src="https://via.placeholder.com/120" width="120" height="120" style="border-radius: 50%;">
<br/>
<b>이다예</b>
<br/>
<i>FE</i>
</td>
</tr>
<tr>
<td align="center" width="33%">
<img src="https://via.placeholder.com/120" width="120" height="120" style="border-radius: 50%;">
<br/>
<b>조예림</b>
<br/>
<i>BE</i>
</td>
<td align="center" width="33%">
</td>
<td align="center" width="33%">
</td>
</tr>
</table>

<br>
<br>