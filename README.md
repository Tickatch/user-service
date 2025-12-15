# Tickatch User Service

티켓 예매 플랫폼 **Tickatch**의 사용자(User) 관리 마이크로서비스입니다.

## 프로젝트 소개

Tickatch는 콘서트, 뮤지컬, 연극, 스포츠 등 다양한 공연의 티켓 예매를 지원하는 플랫폼입니다. User Service는 구매자(Customer), 판매자(Seller), 관리자(Admin)의 프로필 및 상태 관리를 담당하며, 이벤트 기반 아키텍처를 통해 Auth Service와 통신합니다.

> 🚧 **MVP 단계** - 현재 핵심 기능 개발 중입니다.

## 기술 스택

| 분류 | 기술 |
|------|------|
| Framework | Spring Boot 3.x |
| Language | Java 21+ |
| Database | PostgreSQL |
| Messaging | RabbitMQ |
| Query | QueryDSL |
| Communication | OpenFeign |
| Security | Spring Security |

## 아키텍처

### 시스템 구성

```
┌─────────────────────────────────────────────────────────────┐
│                        Tickatch Platform                     │
├─────────────┬─────────────┬─────────────┬───────────────────┤
│    Auth     │    User     │   Product   │    Reservation    │
│   Service   │   Service   │   Service   │      Service      │
└──────┬──────┴──────┬──────┴──────┬──────┴───────────────────┘
       │             │             │
       └──────┬──────┴─────────────┘
              │
         RabbitMQ
```

### 레이어 구조

```
src/main/java
├── customer/                       # Bounded Context (Customer)
│   ├── presentation/
│   │   └── api/
│   │       ├── customer/           # 고객 본인용
│   │       │   ├── dto/
│   │       │   └── CustomerApi
│   │       └── admin/              # 관리자용
│   │           ├── dto/
│   │           └── CustomerAdminApi
│   ├── application/
│   │   └── service/
│   │       └── CustomerService
│   ├── domain/
│   │   ├── Customer                # Aggregate Root (Entity)
│   │   ├── vo/
│   │   │   └── CustomerGrade
│   │   ├── service/                # Domain Service
│   │   ├── repository/
│   │   │   └── CustomerRepository
│   │   └── exception/
│   │       ├── CustomerException
│   │       └── CustomerErrorCode
│   └── infrastructure/
│       └── external/
│
├── seller/                         # Bounded Context (Seller)
│   ├── presentation/
│   │   └── api/
│   │       ├── seller/             # 판매자 본인용
│   │       │   ├── dto/
│   │       │   └── SellerApi
│   │       └── admin/              # 관리자용 (승인/거절)
│   │           ├── dto/
│   │           └── SellerAdminApi
│   ├── application/
│   │   └── service/
│   │       ├── SellerService
│   │       └── SellerApprovalService
│   ├── domain/
│   │   ├── Seller                  # Aggregate Root (Entity)
│   │   ├── vo/
│   │   │   ├── SellerStatus
│   │   │   ├── BusinessInfo
│   │   │   └── SettlementInfo
│   │   ├── service/                # Domain Service
│   │   ├── repository/
│   │   │   └── SellerRepository
│   │   └── exception/
│   │       ├── SellerException
│   │       └── SellerErrorCode
│   └── infrastructure/
│       └── external/
│
├── admin/                          # Bounded Context (Admin)
│   ├── presentation/
│   │   └── api/
│   │       └── admin/
│   │           ├── dto/
│   │           └── AdminApi
│   ├── application/
│   │   └── service/
│   │       └── AdminService
│   ├── domain/
│   │   ├── Admin                   # Aggregate Root (Entity)
│   │   ├── vo/
│   │   │   ├── AdminRole
│   │   │   └── AdminProfile
│   │   ├── service/                # Domain Service
│   │   ├── repository/
│   │   │   └── AdminRepository
│   │   └── exception/
│   │       ├── AdminException
│   │       └── AdminErrorCode
│   └── infrastructure/
│       └── external/
│
├── common/                         # User 공통 도메인
│   └── domain/
│       ├── BaseUser                # 공통 추상 클래스
│       ├── vo/
│       │   ├── UserStatus
│       │   ├── UserProfile
│       │   └── Address
│       └── exception/
│           └── UserException
│
└── global/
    ├── exception/
    │   ├── GlobalExceptionHandler
    │   └── ErrorResponse
    ├── config/
    │   ├── SecurityConfig
    │   ├── JpaConfig
    │   └── RabbitMQConfig
    ├── utils/
    └── infrastructure/
        ├── event/
        │   └── dto/
        │       ├── CustomerWithdrawnEvent
        │       ├── SellerApprovedEvent
        │       ├── SellerRejectedEvent
        │       └── SellerWithdrawnEvent
        └── domain/
            ├── AbstractTimeEntity
            └── AbstractAuditEntity
```

### 상속 구조

```
AbstractTimeEntity (global)
    └── AbstractAuditEntity (global)
            └── BaseUser (common)
                    ├── Customer
                    ├── Seller
                    └── Admin
```

## 주요 기능

### Customer (구매자)
- 프로필 조회 / 수정
- 등급 관리 (NORMAL → VIP)
- 계정 정지 / 해제
- 회원 탈퇴

### Seller (판매자)
- 가입 신청 (승인 대기)
- 사업자 정보 관리
- 정산 정보 관리
- 승인 / 거절 처리
- 회원 탈퇴

### Admin (관리자)
- 관리자 생성 (ADMIN만 가능)
- 역할 변경 (MANAGER ↔ ADMIN)
- 판매자 승인 / 거절
- 회원 정지 / 해제

## 사용자 타입

| 타입 | 설명 | Aggregate Root |
|------|------|----------------|
| `CUSTOMER` | 일반 구매자 | Customer |
| `SELLER` | 판매자 (공연 등록) | Seller |
| `ADMIN` | 관리자 | Admin |

## 상태 흐름

### UserStatus (공통)

```
ACTIVE ←──→ SUSPENDED ──→ WITHDRAWN
```

| 상태 | 설명 |
|------|------|
| ACTIVE | 활성 |
| SUSPENDED | 정지 |
| WITHDRAWN | 탈퇴 |

### SellerStatus (판매자 승인)

```
PENDING ──→ APPROVED ──→ (공연 등록 가능)
    │
    └────→ REJECTED
```

| 상태 | 설명 |
|------|------|
| PENDING | 승인 대기 |
| APPROVED | 승인 완료 |
| REJECTED | 승인 거절 |

### CustomerGrade (고객 등급)

| 등급 | 설명 |
|------|------|
| NORMAL | 일반 |
| VIP | VIP |

### AdminRole (관리자 역할)

| 역할 | 설명 | 권한 |
|------|------|------|
| MANAGER | 매니저 | 판매자 승인, 회원 관리 |
| ADMIN | 최고 관리자 | 전체 권한 + 관리자 생성 |

## API 명세

### Customer API

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/v1/customers/me` | 내 정보 조회 | O |
| PUT | `/api/v1/customers/me` | 내 정보 수정 | O |
| DELETE | `/api/v1/customers/me` | 회원 탈퇴 | O |

### Seller API

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/v1/sellers/me` | 내 정보 조회 | O |
| PUT | `/api/v1/sellers/me` | 내 정보 수정 | O |
| PUT | `/api/v1/sellers/me/business` | 사업자 정보 수정 | O |
| PUT | `/api/v1/sellers/me/settlement` | 정산 정보 수정 | O |
| DELETE | `/api/v1/sellers/me` | 회원 탈퇴 | O |

### Admin API (관리용)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/v1/admin/customers` | 고객 목록 조회 | O (MANAGER+) |
| PUT | `/api/v1/admin/customers/{id}/suspend` | 고객 정지 | O (MANAGER+) |
| PUT | `/api/v1/admin/customers/{id}/activate` | 고객 정지 해제 | O (MANAGER+) |
| GET | `/api/v1/admin/sellers` | 판매자 목록 조회 | O (MANAGER+) |
| GET | `/api/v1/admin/sellers/pending` | 승인 대기 목록 | O (MANAGER+) |
| PUT | `/api/v1/admin/sellers/{id}/approve` | 판매자 승인 | O (MANAGER+) |
| PUT | `/api/v1/admin/sellers/{id}/reject` | 판매자 거절 | O (MANAGER+) |
| POST | `/api/v1/admin/admins` | 관리자 생성 | O (ADMIN) |
| PUT | `/api/v1/admin/admins/{id}/role` | 관리자 역할 변경 | O (ADMIN) |

## 이벤트

### 발행 이벤트

| 이벤트 | Routing Key | 대상 서비스 | Payload |
|--------|-------------|-------------|---------|
| CustomerWithdrawnEvent | `customer.withdrawn` | Auth Service | customerId |
| SellerApprovedEvent | `seller.approved` | Auth, Notification | sellerId, email |
| SellerRejectedEvent | `seller.rejected` | Notification | sellerId, email, reason |
| SellerWithdrawnEvent | `seller.withdrawn` | Auth, Product | sellerId |

### 구독 이벤트

| 이벤트 | Routing Key | 발행 서비스 | 처리 |
|--------|-------------|-------------|------|
| AuthCreatedEvent | `auth.created` | Auth Service | Customer/Seller 생성 |
| AuthWithdrawnEvent | `auth.withdrawn` | Auth Service | 사용자 상태 WITHDRAWN 변경 |

## 데이터베이스 스키마

### Joined Table 전략

```sql
-- 공통 테이블
users (id, email, user_type, name, phone, status, ...)
│
    ├── customers (user_id, grade, birth_date)
│
    ├── sellers (user_id, business_*, settlement_*, seller_status, ...)
│
    └── admins (user_id, admin_role, department)
```

## 비즈니스 규칙

### 판매자 승인
- 가입 시 `PENDING` 상태
- 관리자 승인 후 `APPROVED` → 공연 등록 가능
- 정산 정보는 `APPROVED` 후에만 수정 가능

### 관리자 생성
- 일반 회원가입 불가
- ADMIN 역할의 관리자만 생성 가능
- 최소 1명의 ADMIN 유지 필수

## 실행 방법

### 환경 변수

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tickatch_user
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  rabbitmq:
    host: localhost
    port: 5672
    username: ${RABBITMQ_USERNAME}
    password: ${RABBITMQ_PASSWORD}
```

### 실행

```bash
./gradlew bootRun
```

## 관련 서비스

- **Auth Service** - 인증 관리
- **Product Service** - 상품(공연) 관리
- **Reservation Service** - 예매 관리
- **Notification Service** - 알림 발송

---

© 2025 Tickatch Team