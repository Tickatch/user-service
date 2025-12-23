# Tickatch User Service

티켓 예매 플랫폼 **Tickatch**의 사용자(User) 관리 마이크로서비스입니다.

구매자(Customer), 판매자(Seller), 관리자(Admin)의 프로필 및 상태 관리를 담당하며, 이벤트 기반 아키텍처를 통해 Auth Service 및 Log Service와 통신합니다.

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Framework | Spring Boot 3.x |
| Language | Java 21 |
| Database | PostgreSQL 16 |
| Messaging | RabbitMQ, Kafka |
| Query | QueryDSL |
| Communication | OpenFeign |
| Security | Spring Security + common-lib |
| Tracing | Micrometer + Zipkin |

---

## 아키텍처

### 시스템 구성

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           Tickatch Platform                              │
├─────────────┬─────────────┬─────────────┬─────────────┬─────────────────┤
│    Auth     │    User     │   Product   │ Reservation │      Log        │
│   Service   │   Service   │   Service   │   Service   │    Service      │
└──────┬──────┴──────┬──────┴──────┬──────┴──────┬──────┴────────┬────────┘
       │             │             │             │               │
       └──────┬──────┴─────────────┴─────────────┘               │
              │                                                   │
         RabbitMQ ◄───────────────────────────────────────────────┘
              │
           Kafka
```

### 패키지 구조

```
src/main/java/com/tickatch/user_service
├── admin/                              # Admin Bounded Context
│   ├── application/
│   │   ├── messaging/                  # 이벤트 발행 인터페이스
│   │   │   └── AdminLogEventPublisher.java
│   │   └── service/
│   │       ├── command/                # 상태 변경 서비스
│   │       │   ├── AdminCommandService.java
│   │       │   └── dto/                # Command DTO
│   │       └── query/                  # 조회 서비스
│   │           ├── AdminQueryService.java
│   │           └── dto/                # Query DTO
│   ├── domain/
│   │   ├── Admin.java                  # Aggregate Root
│   │   ├── AdminRepository.java
│   │   ├── exception/
│   │   │   ├── AdminException.java
│   │   │   └── AdminErrorCode.java
│   │   ├── repository/dto/             # Repository 조회 DTO
│   │   └── vo/
│   │       └── AdminRole.java          # MANAGER, ADMIN
│   ├── infrastructure/
│   │   ├── external/                   # 외부 연동
│   │   └── messaging/
│   │       └── publisher/              # 이벤트 발행 구현
│   │           └── RabbitAdminLogPublisher.java
│   └── presentation/
│       └── api/
│           ├── AdminApi.java           # Admin API Controller
│           └── dto/                    # 요청/응답 DTO
│
├── common/                             # 공통 도메인
│   ├── domain/
│   │   ├── BaseUser.java               # 사용자 공통 추상 클래스
│   │   ├── exception/
│   │   │   ├── UserException.java
│   │   │   └── UserErrorCode.java
│   │   └── vo/
│   │       ├── UserStatus.java         # ACTIVE, SUSPENDED, WITHDRAWN
│   │       ├── UserProfile.java
│   │       └── Address.java
│   └── infrastructure/
│       └── messaging/
│           ├── config/                 # RabbitMQ 설정
│           │   └── RabbitMQConfig.java
│           └── event/                  # 이벤트 클래스
│               ├── UserActionType.java
│               ├── UserLogEvent.java
│               └── UserStatusChangedEvent.java
│
├── customer/                           # Customer Bounded Context
│   ├── application/
│   │   ├── messaging/
│   │   │   └── CustomerLogEventPublisher.java
│   │   └── service/
│   │       ├── command/
│   │       │   ├── CustomerCommandService.java
│   │       │   └── dto/
│   │       └── query/
│   │           ├── CustomerQueryService.java
│   │           └── dto/
│   ├── domain/
│   │   ├── Customer.java               # Aggregate Root
│   │   ├── CustomerRepository.java
│   │   ├── exception/
│   │   │   ├── CustomerException.java
│   │   │   └── CustomerErrorCode.java
│   │   ├── repository/dto/
│   │   └── vo/
│   │       └── CustomerGrade.java      # NORMAL, VIP
│   ├── infrastructure/
│   │   └── messaging/
│   │       └── publisher/
│   │           └── RabbitCustomerLogPublisher.java
│   └── presentation/
│       └── api/
│           ├── CustomerApi.java        # Customer API Controller
│           └── dto/
│
├── global/                             # 글로벌 설정
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── AsyncConfig.java
│   │   ├── ActorExtractor.java
│   │   ├── FeignConfig.java
│   │   ├── KafkaConsumerConfig.java
│   │   ├── KafkaProducerConfig.java
│   │   └── QueryDslConfig.java
│   ├── domain/
│   │   ├── AbstractTimeEntity.java
│   │   └── AbstractAuditEntity.java
│   └── feign/
│       ├── FeignErrorDecoder.java
│       └── FeignRequestInterceptor.java
│
└── seller/                             # Seller Bounded Context
    ├── application/
    │   ├── messaging/
    │   │   └── SellerLogEventPublisher.java
    │   └── service/
    │       ├── command/
    │       │   ├── SellerCommandService.java
    │       │   └── dto/
    │       └── query/
    │           ├── SellerQueryService.java
    │           └── dto/
    ├── domain/
    │   ├── Seller.java                 # Aggregate Root
    │   ├── SellerRepository.java
    │   ├── exception/
    │   │   ├── SellerException.java
    │   │   └── SellerErrorCode.java
    │   ├── repository/dto/
    │   └── vo/
    │       ├── SellerStatus.java       # PENDING, APPROVED, REJECTED
    │       ├── BusinessInfo.java
    │       └── SettlementInfo.java
    ├── infrastructure/
    │   ├── external/                   # 외부 연동
    │   └── messaging/
    │       └── publisher/
    │           └── RabbitSellerLogPublisher.java
    └── presentation/
        └── api/
            ├── SellerApi.java          # Seller API Controller
            └── dto/
```

### 엔티티 상속 구조

```
AbstractTimeEntity (createdAt, updatedAt)
    └── AbstractAuditEntity (createdBy, updatedBy, deletedBy, deletedAt)
            └── BaseUser (id, email, profile, status)
                    ├── Customer (grade, birthDate)
                    ├── Seller (businessInfo, settlementInfo, sellerStatus)
                    └── Admin (adminRole, department)
```

---

## 사용자 타입

| 타입 | 설명 | Aggregate Root |
|------|------|----------------|
| `CUSTOMER` | 일반 구매자 | Customer |
| `SELLER` | 판매자 (공연 등록) | Seller |
| `ADMIN` | 관리자 | Admin |

---

## 상태 흐름

### UserStatus (공통)

```
ACTIVE ◄──► SUSPENDED ───► WITHDRAWN
```

| 상태 | 설명 |
|------|------|
| `ACTIVE` | 활성 상태 |
| `SUSPENDED` | 정지 상태 |
| `WITHDRAWN` | 탈퇴 상태 (최종) |

### SellerStatus (판매자 승인)

```
PENDING ───► APPROVED ───► (공연 등록 가능)
    │
    └──────► REJECTED
```

| 상태 | 설명 |
|------|------|
| `PENDING` | 승인 대기 |
| `APPROVED` | 승인 완료 |
| `REJECTED` | 승인 거절 |

### CustomerGrade (고객 등급)

| 등급 | 레벨 | 설명 |
|------|------|------|
| `NORMAL` | 0 | 일반 등급 |
| `VIP` | 1 | VIP 등급 (등급 하향 불가) |

### AdminRole (관리자 역할)

| 역할 | 설명 |
|------|------|
| `MANAGER` | 매니저 - 판매자 승인, 회원 관리 |
| `ADMIN` | 최고 관리자 - 전체 권한 + 관리자 생성 |

---

## API 명세

### Customer API

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | `/api/v1/user/customers` | 고객 목록 조회 | 인증 |
| GET | `/api/v1/user/customers/{id}` | 고객 단건 조회 | 인증 |
| GET | `/api/v1/user/customers/me` | 내 정보 조회 | 인증 |
| POST | `/api/v1/user/customers` | 고객 생성 | 인증 |
| PUT | `/api/v1/user/customers/{id}/profile` | 프로필 수정 | 인증 |
| PUT | `/api/v1/user/customers/{id}/grade` | 등급 변경 | 인증 |
| POST | `/api/v1/user/customers/{id}/suspend` | 고객 정지 | 관리자 |
| POST | `/api/v1/user/customers/{id}/activate` | 고객 활성화 | 관리자 |
| DELETE | `/api/v1/user/customers/{id}` | 고객 탈퇴 | 인증 |

### Seller API

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | `/api/v1/user/sellers` | 판매자 목록 조회 | 인증 |
| GET | `/api/v1/user/sellers/{id}` | 판매자 단건 조회 | 인증 |
| GET | `/api/v1/user/sellers/me` | 내 정보 조회 | 인증 |
| POST | `/api/v1/user/sellers` | 판매자 생성 (PENDING) | 인증 |
| PUT | `/api/v1/user/sellers/{id}/profile` | 프로필 수정 | 인증 |
| PUT | `/api/v1/user/sellers/{id}/settlement` | 정산 정보 수정 | 인증 (승인 후) |
| POST | `/api/v1/user/sellers/{id}/approve` | 판매자 승인 | 관리자 |
| POST | `/api/v1/user/sellers/{id}/reject` | 판매자 거절 | 관리자 |
| POST | `/api/v1/user/sellers/{id}/suspend` | 판매자 정지 | 관리자 |
| POST | `/api/v1/user/sellers/{id}/activate` | 판매자 활성화 | 관리자 |
| DELETE | `/api/v1/user/sellers/{id}` | 판매자 탈퇴 | 인증 |

### Admin API

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | `/api/v1/user/admins` | 관리자 목록 조회 | MANAGER+ |
| GET | `/api/v1/user/admins/{id}` | 관리자 단건 조회 | MANAGER+ |
| POST | `/api/v1/user/admins` | 관리자 생성 | ADMIN |
| PUT | `/api/v1/user/admins/{id}/profile` | 프로필 수정 | MANAGER+ |
| PUT | `/api/v1/user/admins/{id}/role` | 역할 변경 | ADMIN |
| POST | `/api/v1/user/admins/{id}/suspend` | 관리자 정지 | ADMIN |
| POST | `/api/v1/user/admins/{id}/activate` | 관리자 활성화 | ADMIN |
| DELETE | `/api/v1/user/admins/{id}` | 관리자 탈퇴 | ADMIN |

---

## 이벤트

### 발행 이벤트 (RabbitMQ)

#### 도메인 이벤트 (tickatch.user Exchange)

| 이벤트 | Routing Key | 수신 서비스 | 설명 |
|--------|-------------|-------------|------|
| UserStatusChangedEvent | `customer.withdrawn` | Auth Service | 고객 탈퇴 |
| UserStatusChangedEvent | `customer.suspended` | Auth Service | 고객 정지 |
| UserStatusChangedEvent | `customer.activated` | Auth Service | 고객 활성화 |
| UserStatusChangedEvent | `seller.withdrawn` | Auth Service | 판매자 탈퇴 |
| UserStatusChangedEvent | `seller.suspended` | Auth Service | 판매자 정지 |
| UserStatusChangedEvent | `seller.activated` | Auth Service | 판매자 활성화 |
| UserStatusChangedEvent | `admin.withdrawn` | Auth Service | 관리자 탈퇴 |
| UserStatusChangedEvent | `admin.suspended` | Auth Service | 관리자 정지 |
| UserStatusChangedEvent | `admin.activated` | Auth Service | 관리자 활성화 |

#### 로그 이벤트 (tickatch.log Exchange)

| 이벤트 | Routing Key | 수신 서비스 | 설명 |
|--------|-------------|-------------|------|
| UserLogEvent | `user.log` | Log Service | 사용자 활동 로그 |

### 로그 액션 타입 (UserActionType)

```java
// Customer
CUSTOMER_CREATED, CUSTOMER_CREATE_FAILED
    CUSTOMER_UPDATED, CUSTOMER_UPDATE_FAILED
CUSTOMER_WITHDRAWN, CUSTOMER_WITHDRAW_FAILED
    CUSTOMER_SUSPENDED, CUSTOMER_SUSPEND_FAILED
CUSTOMER_ACTIVATED, CUSTOMER_ACTIVATE_FAILED

// Seller
    SELLER_CREATED, SELLER_CREATE_FAILED
SELLER_UPDATED, SELLER_UPDATE_FAILED
    SELLER_WITHDRAWN, SELLER_WITHDRAW_FAILED
SELLER_SUSPENDED, SELLER_SUSPEND_FAILED
    SELLER_ACTIVATED, SELLER_ACTIVATE_FAILED
SELLER_APPROVED, SELLER_APPROVE_FAILED
    SELLER_REJECTED, SELLER_REJECT_FAILED

// Admin
ADMIN_CREATED, ADMIN_CREATE_FAILED
    ADMIN_UPDATED, ADMIN_UPDATE_FAILED
ADMIN_WITHDRAWN, ADMIN_WITHDRAW_FAILED
    ADMIN_SUSPENDED, ADMIN_SUSPEND_FAILED
ADMIN_ACTIVATED, ADMIN_ACTIVATE_FAILED
```

### 메시지 발행 구조

```
CommandService
    │
    ├── 비즈니스 로직 수행
    │
    ├── 성공 시 → LogEventPublisher.publishXxx()
    │              └── RabbitMQ (tickatch.log) → Log Service
    │
    └── 상태 변경 시 → EventPublisher.publishXxx()
                       └── RabbitMQ (tickatch.user) → Auth Service
```

---

## RabbitMQ 설정

### Exchange

| Exchange | Type | 용도 |
|----------|------|------|
| `tickatch.user` | Topic | 도메인 이벤트 (상태 변경) |
| `tickatch.log` | Topic | 로그 이벤트 |
| `tickatch.user.dlx` | Topic | 도메인 이벤트 DLX |
| `tickatch.log.dlx` | Topic | 로그 이벤트 DLX |

### Queue

| Queue | Binding Key | 설명 |
|-------|-------------|------|
| `tickatch.user.withdrawn.auth.queue` | `*.withdrawn` | Auth Service 탈퇴 이벤트 |
| `tickatch.user.suspended.auth.queue` | `*.suspended` | Auth Service 정지 이벤트 |
| `tickatch.user.activated.auth.queue` | `*.activated` | Auth Service 활성화 이벤트 |
| `tickatch.user.log.queue` | `user.log` | Log Service 로그 이벤트 |

---

## 데이터베이스 스키마

### 테이블 구조

```sql
-- customers 테이블
CREATE TABLE customers (
                           id              UUID PRIMARY KEY,
                           email           VARCHAR(255) NOT NULL,
                           name            VARCHAR(50) NOT NULL,
                           phone           VARCHAR(20),
                           status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                           grade           VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
                           birth_date      DATE,
                           created_at      TIMESTAMP NOT NULL,
                           created_by      VARCHAR(100),
                           updated_at      TIMESTAMP,
                           updated_by      VARCHAR(100),
                           deleted_at      TIMESTAMP,
                           deleted_by      VARCHAR(100)
);

-- sellers 테이블
CREATE TABLE sellers (
                         id                  UUID PRIMARY KEY,
                         email               VARCHAR(255) NOT NULL,
                         name                VARCHAR(50) NOT NULL,
                         phone               VARCHAR(20),
                         status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                         seller_status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                         business_name       VARCHAR(200) NOT NULL,
                         business_number     VARCHAR(20) NOT NULL,
                         representative_name VARCHAR(100) NOT NULL,
                         business_zip_code   VARCHAR(10),
                         business_address1   VARCHAR(200),
                         business_address2   VARCHAR(200),
                         bank_code           VARCHAR(10),
                         account_number      VARCHAR(50),
                         account_holder      VARCHAR(100),
                         approved_at         TIMESTAMP,
                         approved_by         VARCHAR(100),
                         rejected_reason     VARCHAR(500),
                         created_at          TIMESTAMP NOT NULL,
    ...
);

-- admins 테이블
CREATE TABLE admins (
                        id          UUID PRIMARY KEY,
                        email       VARCHAR(255) NOT NULL,
                        name        VARCHAR(50) NOT NULL,
                        phone       VARCHAR(20),
                        status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                        admin_role  VARCHAR(20) NOT NULL DEFAULT 'MANAGER',
                        department  VARCHAR(100),
                        created_at  TIMESTAMP NOT NULL,
    ...
);
```

---

## 비즈니스 규칙

### Customer

| 규칙 | 설명 |
|------|------|
| 등급 하향 불가 | VIP → NORMAL 변경 불가 |
| 탈퇴 제한 | 진행 중인 예매가 있으면 탈퇴 불가 |

### Seller

| 규칙 | 설명 |
|------|------|
| 정산 정보 수정 | APPROVED 상태에서만 가능 |
| 공연 등록 | APPROVED + ACTIVE 상태에서만 가능 |
| 탈퇴 제한 | 진행 중인 공연이 있으면 탈퇴 불가 |
| 사업자등록번호 | 10자리 숫자, 중복 불가 |

### Admin

| 규칙 | 설명 |
|------|------|
| 관리자 생성 | ADMIN 역할만 가능 |
| 역할 변경 | ADMIN 역할만 가능, 자기 자신 변경 불가 |
| 마지막 ADMIN | 삭제/비활성화 불가 |

---

## 환경변수

### .env.example

```env
# ========================================
# Tickatch User Service 환경 변수
# ========================================

# ===== 애플리케이션 기본 설정 =====
APP_NAME=user-service
APP_PROFILE=default
APP_VERSION=1.0.0
SERVER_PORT=8089

# ===== 환경 식별 =====
ENVIRONMENT=production

# ===== Eureka 설정 =====
# Eureka 서버 URL (HA 구성)
EUREKA_DEFAULT_ZONE=https://your-domain.com/eureka1/eureka/,https://your-domain.com/eureka2/eureka/
# 이 서비스 인스턴스의 호스트명
EUREKA_INSTANCE_HOSTNAME=your-instance-ip

# ===== Config Server 설정 =====
CONFIG_SERVER_URL=https://your-domain.com/config

# ===== Kafka 설정 =====
KAFKA_BOOTSTRAP_SERVERS=your-kafka-host:9094

# ===== RabbitMQ 설정 =====
RABBITMQ_HOST=your-rabbitmq-host
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=your-username
RABBITMQ_PASSWORD=your-password
RABBITMQ_VHOST=/

# ===== 데이터베이스 설정 =====
DB_HOST=your-db-host
DB_PORT=5432
DB_NAME=tickatch
DB_USERNAME=your-db-username
DB_PASSWORD=your-db-password

# ===== 로깅 설정 =====
LOG_LEVEL=INFO
HIBERNATE_LOG_LEVEL=WARN

# ===== JPA 설정 =====
JPA_DDL_AUTO=validate
JPA_SHOW_SQL=false

# ===== 트레이싱 설정 =====
TRACING_PROBABILITY=0.1
ZIPKIN_ENDPOINT=https://your-domain.com/zipkin/api/v2/spans
```

### 환경변수 설명

| 변수 | 설명 | 예시 |
|------|------|------|
| `APP_NAME` | 애플리케이션 이름 | `user-service` |
| `SERVER_PORT` | 서버 포트 | `8089` |
| `EUREKA_DEFAULT_ZONE` | Eureka 서버 URL (HA) | `https://domain/eureka1/eureka/` |
| `EUREKA_INSTANCE_HOSTNAME` | 인스턴스 호스트명/IP | `192.168.0.48` |
| `CONFIG_SERVER_URL` | Config Server URL | `https://domain/config` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka 브로커 주소 | `host:9094` |
| `RABBITMQ_HOST` | RabbitMQ 호스트 | `host` |
| `RABBITMQ_PORT` | RabbitMQ 포트 | `5672` (또는 매핑된 포트) |
| `DB_HOST` | PostgreSQL 호스트 | `host` |
| `DB_PORT` | PostgreSQL 포트 | `5432` |
| `DB_NAME` | 데이터베이스 이름 | `tickatch` |
| `TRACING_PROBABILITY` | 트레이싱 샘플링 비율 | `0.1` (10%) |
| `ZIPKIN_ENDPOINT` | Zipkin 엔드포인트 | `https://domain/zipkin/api/v2/spans` |

---

## 의존성 (common-lib)

User Service는 `common-lib`의 다음 기능을 사용합니다:

| 기능 | 클래스 | 용도 |
|------|--------|------|
| API 응답 | `ApiResponse`, `PageResponse` | 표준 응답 포맷 |
| 에러 처리 | `BusinessException`, `ErrorCode` | 예외 처리 |
| 보안 | `BaseSecurityConfig`, `LoginFilter` | 인증/인가 |
| 이벤트 | `DomainEvent`, `IntegrationEvent` | 이벤트 래핑 |
| 유틸리티 | `JsonUtils` | JSON 직렬화 |
| 로깅 | `MdcUtils` | 분산 추적 |

---

## 실행 방법

### 로컬 실행

```bash
# 환경변수 설정
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=tickatch
export DB_USERNAME=tickatch
export DB_PASSWORD=password
export RABBITMQ_HOST=localhost
export RABBITMQ_PORT=5672
export RABBITMQ_USERNAME=guest
export RABBITMQ_PASSWORD=guest

# 실행
./gradlew bootRun
```

### Docker 실행

```bash
docker run -d \
  --name user-service \
  --network tickatch-network \
  -e DB_HOST=tickatch-postgres \
  -e RABBITMQ_HOST=rabbitmq \
  ghcr.io/tickatch/user-service:latest
```

---

## 관련 서비스

| 서비스 | 연동 방식 | 설명 |
|--------|-----------|------|
| Auth Service | RabbitMQ | 상태 변경 이벤트 수신 |
| Log Service | RabbitMQ | 로그 이벤트 수신 |
| Product Service | Feign | 판매자 검증 |
| Reservation Service | Feign | 고객 검증 |

---

## 에러 코드

### UserErrorCode (공통)

| 코드 | HTTP | 메시지 |
|------|------|--------|
| `INVALID_NAME` | 400 | 이름은 필수이며 50자 이하여야 합니다 |
| `INVALID_PHONE` | 400 | 연락처 형식이 올바르지 않습니다 |
| `INVALID_ADDRESS` | 400 | 주소 정보가 유효하지 않습니다 |
| `USER_ALREADY_SUSPENDED` | 422 | 이미 정지된 사용자입니다 |
| `USER_ALREADY_ACTIVE` | 422 | 이미 활성화된 사용자입니다 |
| `USER_ALREADY_WITHDRAWN` | 422 | 이미 탈퇴한 사용자입니다 |
| `EVENT_PUBLISH_FAILED` | 503 | 이벤트 발행에 실패했습니다 |

### CustomerErrorCode

| 코드 | HTTP | 메시지 |
|------|------|--------|
| `CUSTOMER_NOT_FOUND` | 404 | 고객을 찾을 수 없습니다 |
| `CUSTOMER_ALREADY_EXISTS` | 409 | 이미 존재하는 고객입니다 |
| `INVALID_BIRTH_DATE` | 400 | 생년월일이 유효하지 않습니다 |
| `GRADE_DOWNGRADE_NOT_ALLOWED` | 422 | 등급 하향은 허용되지 않습니다 |

### SellerErrorCode

| 코드 | HTTP | 메시지 |
|------|------|--------|
| `SELLER_NOT_FOUND` | 404 | 판매자를 찾을 수 없습니다 |
| `SELLER_ALREADY_EXISTS` | 409 | 이미 존재하는 판매자입니다 |
| `BUSINESS_NUMBER_ALREADY_EXISTS` | 409 | 이미 등록된 사업자등록번호입니다 |
| `INVALID_BUSINESS_NAME` | 400 | 상호명은 필수이며 200자 이하여야 합니다 |
| `INVALID_BUSINESS_NUMBER` | 400 | 사업자등록번호가 유효하지 않습니다 |
| `SELLER_NOT_PENDING` | 422 | 승인 대기 상태가 아닙니다 |
| `SELLER_ALREADY_APPROVED` | 422 | 이미 승인된 판매자입니다 |
| `CANNOT_UPDATE_SETTLEMENT_BEFORE_APPROVAL` | 422 | 승인 전에는 정산 정보를 수정할 수 없습니다 |
| `CANNOT_REGISTER_PERFORMANCE` | 422 | 공연을 등록할 수 없는 상태입니다 |

### AdminErrorCode

| 코드 | HTTP | 메시지 |
|------|------|--------|
| `ADMIN_NOT_FOUND` | 404 | 관리자를 찾을 수 없습니다 |
| `ADMIN_PERMISSION_DENIED` | 403 | 권한이 없습니다 |
| `ONLY_ADMIN_CAN_CREATE_ADMIN` | 403 | 관리자 생성은 ADMIN 역할만 가능합니다 |
| `CANNOT_CHANGE_OWN_ROLE` | 422 | 자신의 역할은 변경할 수 없습니다 |
| `CANNOT_DELETE_LAST_ADMIN` | 422 | 마지막 ADMIN은 삭제할 수 없습니다 |

---

© 2025 Tickatch Team