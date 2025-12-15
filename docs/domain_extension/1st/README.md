# User Service 설계 계획서

> User Service 구현을 위한 설계 방향과 구현 가이드입니다.
> Auth Service 구현 완료 후, 동일한 패턴을 적용하여 구현합니다.

---

## 1. 서비스 개요

| 항목 | 내용 |
|------|------|
| **책임** | 사용자 프로필 및 상태 관리 (구매자, 판매자, 관리자) |
| **Aggregate Root** | Customer, Seller, Admin (3개) |
| **공통 추상 클래스** | BaseUser (common/domain) |
| **주요 특징** | Auth Service 이벤트 수신하여 사용자 생성 |

---

## 2. 패키지 구조

```
src/main/java/tickatch/user_service
├── customer/                           # Bounded Context
│   ├── presentation/
│   │   └── api/
│   │       ├── customer/               # 고객 본인용
│   │       │   ├── dto/
│   │       │   └── CustomerApi.java
│   │       └── admin/                  # 관리자용
│   │           ├── dto/
│   │           └── CustomerAdminApi.java
│   ├── application/
│   │   └── service/
│   │       └── CustomerService.java
│   ├── domain/
│   │   ├── Customer.java               # Aggregate Root
│   │   ├── vo/
│   │   │   └── CustomerGrade.java
│   │   ├── repository/
│   │   │   └── CustomerRepository.java
│   │   └── exception/
│   │       ├── CustomerErrorCode.java
│   │       └── CustomerException.java
│   └── infrastructure/
│       └── external/
│
├── seller/                             # Bounded Context
│   ├── presentation/
│   │   └── api/
│   │       ├── seller/                 # 판매자 본인용
│   │       │   ├── dto/
│   │       │   └── SellerApi.java
│   │       └── admin/                  # 관리자용 (승인/거절)
│   │           ├── dto/
│   │           └── SellerAdminApi.java
│   ├── application/
│   │   └── service/
│   │       ├── SellerService.java
│   │       └── SellerApprovalService.java
│   ├── domain/
│   │   ├── Seller.java                 # Aggregate Root
│   │   ├── vo/
│   │   │   ├── SellerStatus.java
│   │   │   ├── BusinessInfo.java
│   │   │   └── SettlementInfo.java
│   │   ├── repository/
│   │   │   └── SellerRepository.java
│   │   └── exception/
│   │       ├── SellerErrorCode.java
│   │       └── SellerException.java
│   └── infrastructure/
│       └── external/
│
├── admin/                              # Bounded Context
│   ├── presentation/
│   │   └── api/
│   │       └── admin/
│   │           ├── dto/
│   │           └── AdminApi.java
│   ├── application/
│   │   └── service/
│   │       └── AdminService.java
│   ├── domain/
│   │   ├── Admin.java                  # Aggregate Root
│   │   ├── vo/
│   │   │   ├── AdminRole.java
│   │   │   └── AdminProfile.java
│   │   ├── repository/
│   │   │   └── AdminRepository.java
│   │   └── exception/
│   │       ├── AdminErrorCode.java
│   │       └── AdminException.java
│   └── infrastructure/
│       └── external/
│
├── common/                             # User 공통 도메인
│   └── domain/
│       ├── BaseUser.java               # 공통 추상 클래스
│       ├── vo/
│       │   ├── UserStatus.java
│       │   ├── UserProfile.java
│       │   └── Address.java
│       └── exception/
│           ├── UserErrorCode.java
│           └── UserException.java
│
└── global/
    ├── exception/
    ├── config/
    ├── utils/
    └── infrastructure/
        ├── event/
        │   └── dto/
        │       ├── CustomerWithdrawnEvent.java
        │       ├── SellerApprovedEvent.java
        │       ├── SellerRejectedEvent.java
        │       └── SellerWithdrawnEvent.java
        └── domain/
            ├── AbstractTimeEntity.java
            └── AbstractAuditEntity.java
```

---

## 3. 상속 구조

```
AbstractTimeEntity (global)
    └── AbstractAuditEntity (global)
            └── BaseUser (common)
                    ├── Customer
                    ├── Seller
                    └── Admin
```

---

## 4. 도메인 설계

### 4.1 BaseUser (공통 추상 클래스)

> Customer, Seller, Admin이 상속받는 공통 클래스 (Aggregate Root 아님)

**속성**

| 필드 | 타입 | 필수 | 설명 |
|------|------|:----:|------|
| `id` | `UUID` | O | PK (authId와 동일) |
| `email` | `String` | O | 이메일 (조회용) |
| `profile` | `UserProfile` | O | 기본 프로필 VO |
| `status` | `UserStatus` | O | ACTIVE / SUSPENDED / WITHDRAWN |

**공통 행위**
- `updateProfile()` - 프로필 수정
- `suspend()` - 정지
- `activate()` - 정지 해제
- `withdraw()` - 탈퇴
- `isActive()` - 상태 확인

### 4.2 Customer (Aggregate Root)

**추가 속성**

| 필드 | 타입 | 설명 |
|------|------|------|
| `grade` | `CustomerGrade` | NORMAL / VIP |
| `birthDate` | `LocalDate` | 생년월일 |

**행위**
- `create()` - 생성 (정적 팩토리)
- `updateBirthDate()` - 생년월일 수정
- `upgradeGrade()` - 등급 변경

### 4.3 Seller (Aggregate Root)

**추가 속성**

| 필드 | 타입 | 설명 |
|------|------|------|
| `businessInfo` | `BusinessInfo` | 사업자 정보 VO |
| `settlementInfo` | `SettlementInfo` | 정산 정보 VO |
| `sellerStatus` | `SellerStatus` | PENDING / APPROVED / REJECTED |
| `approvedAt` | `LocalDateTime` | 승인 일시 |
| `approvedBy` | `String` | 승인자 |
| `rejectedReason` | `String` | 거절 사유 |

**행위**
- `create()` - 생성 (PENDING 상태)
- `approve()` - 승인
- `reject()` - 거절
- `updateBusinessInfo()` - 사업자 정보 수정
- `updateSettlementInfo()` - 정산 정보 수정 (APPROVED만 가능)
- `canRegisterPerformance()` - 공연 등록 가능 여부

### 4.4 Admin (Aggregate Root)

**추가 속성**

| 필드 | 타입 | 설명 |
|------|------|------|
| `profile` | `AdminProfile` | 관리자 프로필 VO (department 포함) |
| `adminRole` | `AdminRole` | MANAGER / ADMIN |

**행위**
- `create()` - 생성 (ADMIN만 가능)
- `changeRole()` - 역할 변경
- `hasPermission()` - 권한 확인

### 4.5 Value Objects

| VO | 위치 | 필드 |
|----|------|------|
| `UserStatus` | common | enum: ACTIVE, SUSPENDED, WITHDRAWN |
| `UserProfile` | common | name, phone |
| `Address` | common | zipCode, address1, address2 |
| `CustomerGrade` | customer | enum: NORMAL, VIP |
| `SellerStatus` | seller | enum: PENDING, APPROVED, REJECTED |
| `BusinessInfo` | seller | businessName, businessNumber, representativeName, businessAddress |
| `SettlementInfo` | seller | bankCode, accountNumber, accountHolder |
| `AdminRole` | admin | enum: MANAGER, ADMIN |
| `AdminProfile` | admin | name, phone, department |

---

## 5. 예외 설계

### 5.1 공통 (UserErrorCode)

```
# 검증 - 프로필 (400)
- INVALID_NAME
- INVALID_PHONE
- INVALID_ADDRESS

# 상태 (422)
- USER_ALREADY_SUSPENDED
- USER_ALREADY_ACTIVE
- USER_ALREADY_WITHDRAWN

# 이벤트 (503)
- EVENT_PUBLISH_FAILED
```

### 5.2 Customer (CustomerErrorCode)

```
# 조회 (404)
- CUSTOMER_NOT_FOUND

# 검증 (400)
- INVALID_BIRTH_DATE
- INVALID_CUSTOMER_GRADE

# 권한 (403)
- CUSTOMER_SUSPENDED
- CUSTOMER_WITHDRAWN

# 비즈니스 규칙 (422)
- CANNOT_WITHDRAW_WITH_ACTIVE_RESERVATIONS
- GRADE_DOWNGRADE_NOT_ALLOWED
```

### 5.3 Seller (SellerErrorCode)

```
# 조회 (404)
- SELLER_NOT_FOUND

# 검증 - 사업자 정보 (400)
- INVALID_BUSINESS_NAME
- INVALID_BUSINESS_NUMBER
- INVALID_REPRESENTATIVE_NAME
- INVALID_BUSINESS_ADDRESS
- DUPLICATE_BUSINESS_NUMBER

# 검증 - 정산 정보 (400)
- INVALID_BANK_CODE
- INVALID_ACCOUNT_NUMBER
- INVALID_ACCOUNT_HOLDER

# 검증 - 승인 (400)
- INVALID_REJECTION_REASON

# 권한 (403)
- SELLER_NOT_APPROVED
- SELLER_SUSPENDED
- SELLER_WITHDRAWN

# 비즈니스 규칙 (422)
- SELLER_NOT_PENDING
- SELLER_ALREADY_APPROVED
- SELLER_ALREADY_REJECTED
- CANNOT_UPDATE_SETTLEMENT_BEFORE_APPROVAL
- CANNOT_WITHDRAW_WITH_ACTIVE_PRODUCTS
- CANNOT_REGISTER_PERFORMANCE
```

### 5.4 Admin (AdminErrorCode)

```
# 조회 (404)
- ADMIN_NOT_FOUND

# 검증 (400)
- INVALID_ADMIN_ROLE
- INVALID_DEPARTMENT

# 권한 (403)
- ADMIN_PERMISSION_DENIED
- ONLY_ADMIN_CAN_CREATE_ADMIN
- ONLY_ADMIN_CAN_CHANGE_ROLE
- ADMIN_SUSPENDED

# 비즈니스 규칙 (422)
- CANNOT_CHANGE_OWN_ROLE
- CANNOT_DELETE_LAST_ADMIN
- CANNOT_DEACTIVATE_LAST_ADMIN
```

### 5.5 메시지 파일 (messages_user.properties)

```properties
# ========================================
# Common
# ========================================
INVALID_NAME=이름은 필수이며 50자 이하여야 합니다.
INVALID_PHONE=연락처 형식이 올바르지 않습니다.
INVALID_ADDRESS=주소 정보가 유효하지 않습니다.
USER_ALREADY_SUSPENDED=이미 정지된 사용자입니다.
USER_ALREADY_ACTIVE=이미 활성화된 사용자입니다.
USER_ALREADY_WITHDRAWN=이미 탈퇴한 사용자입니다.
EVENT_PUBLISH_FAILED=이벤트 발행에 실패했습니다. (사용자 ID: {0})

# ========================================
# Customer
# ========================================
CUSTOMER_NOT_FOUND=고객 {0}을(를) 찾을 수 없습니다.
INVALID_BIRTH_DATE=생년월일이 유효하지 않습니다.
INVALID_CUSTOMER_GRADE=고객 등급이 유효하지 않습니다.
CUSTOMER_SUSPENDED=정지된 고객입니다.
CUSTOMER_WITHDRAWN=탈퇴한 고객입니다.
CANNOT_WITHDRAW_WITH_ACTIVE_RESERVATIONS=진행 중인 예매가 있어 탈퇴할 수 없습니다.
GRADE_DOWNGRADE_NOT_ALLOWED=등급 하향은 허용되지 않습니다.

# ========================================
# Seller
# ========================================
SELLER_NOT_FOUND=판매자 {0}을(를) 찾을 수 없습니다.
INVALID_BUSINESS_NAME=상호명은 필수이며 200자 이하여야 합니다.
INVALID_BUSINESS_NUMBER=사업자등록번호가 유효하지 않습니다. 10자리 숫자를 입력해주세요.
INVALID_REPRESENTATIVE_NAME=대표자명은 필수입니다.
INVALID_BUSINESS_ADDRESS=사업장 주소가 유효하지 않습니다.
DUPLICATE_BUSINESS_NUMBER=이미 등록된 사업자등록번호입니다.
INVALID_BANK_CODE=은행 코드가 유효하지 않습니다.
INVALID_ACCOUNT_NUMBER=계좌번호가 유효하지 않습니다.
INVALID_ACCOUNT_HOLDER=예금주명은 필수입니다.
INVALID_REJECTION_REASON=거절 사유는 필수입니다.
SELLER_NOT_APPROVED=승인되지 않은 판매자입니다.
SELLER_SUSPENDED=정지된 판매자입니다.
SELLER_WITHDRAWN=탈퇴한 판매자입니다.
SELLER_NOT_PENDING=승인 대기 상태가 아닙니다.
SELLER_ALREADY_APPROVED=이미 승인된 판매자입니다.
SELLER_ALREADY_REJECTED=이미 거절된 판매자입니다.
CANNOT_UPDATE_SETTLEMENT_BEFORE_APPROVAL=승인 전에는 정산 정보를 수정할 수 없습니다.
CANNOT_WITHDRAW_WITH_ACTIVE_PRODUCTS=진행 중인 공연이 있어 탈퇴할 수 없습니다.
CANNOT_REGISTER_PERFORMANCE=공연을 등록할 수 없는 상태입니다. 승인 완료 후 이용해주세요.

# ========================================
# Admin
# ========================================
ADMIN_NOT_FOUND=관리자 {0}을(를) 찾을 수 없습니다.
INVALID_ADMIN_ROLE=관리자 역할이 유효하지 않습니다.
INVALID_DEPARTMENT=부서 정보가 유효하지 않습니다.
ADMIN_PERMISSION_DENIED=권한이 없습니다.
ONLY_ADMIN_CAN_CREATE_ADMIN=관리자 생성은 ADMIN 역할만 가능합니다.
ONLY_ADMIN_CAN_CHANGE_ROLE=역할 변경은 ADMIN 역할만 가능합니다.
ADMIN_SUSPENDED=정지된 관리자입니다.
CANNOT_CHANGE_OWN_ROLE=자신의 역할은 변경할 수 없습니다.
CANNOT_DELETE_LAST_ADMIN=마지막 ADMIN은 삭제할 수 없습니다.
CANNOT_DEACTIVATE_LAST_ADMIN=마지막 ADMIN은 비활성화할 수 없습니다.
```

---

## 6. 이벤트 설계

### 6.1 구독 이벤트 (Auth Service로부터)

| 이벤트 | 처리 |
|--------|------|
| `AuthCreatedEvent` (userType=CUSTOMER) | Customer 생성 |
| `AuthCreatedEvent` (userType=SELLER) | Seller 생성 (PENDING) |
| `AuthCreatedEvent` (userType=ADMIN) | Admin 생성 |
| `AuthWithdrawnEvent` | 해당 사용자 상태 WITHDRAWN 변경 |

### 6.2 발행 이벤트

| 이벤트 | 발행 시점 | Routing Key | Payload |
|--------|----------|-------------|---------|
| `CustomerWithdrawnEvent` | 고객 탈퇴 | `customer.withdrawn` | customerId |
| `SellerApprovedEvent` | 판매자 승인 | `seller.approved` | sellerId, email |
| `SellerRejectedEvent` | 판매자 거절 | `seller.rejected` | sellerId, email, reason |
| `SellerWithdrawnEvent` | 판매자 탈퇴 | `seller.withdrawn` | sellerId |

---

## 7. API 설계

### 7.1 Customer API

**고객 본인용**

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/customers/me` | 내 정보 조회 |
| PUT | `/api/v1/customers/me` | 내 정보 수정 |
| DELETE | `/api/v1/customers/me` | 회원 탈퇴 |

**관리자용**

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/admin/customers` | 고객 목록 조회 |
| GET | `/api/v1/admin/customers/{id}` | 고객 상세 조회 |
| PUT | `/api/v1/admin/customers/{id}/suspend` | 고객 정지 |
| PUT | `/api/v1/admin/customers/{id}/activate` | 고객 정지 해제 |

### 7.2 Seller API

**판매자 본인용**

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/sellers/me` | 내 정보 조회 |
| PUT | `/api/v1/sellers/me` | 내 정보 수정 |
| PUT | `/api/v1/sellers/me/business` | 사업자 정보 수정 |
| PUT | `/api/v1/sellers/me/settlement` | 정산 정보 수정 |
| DELETE | `/api/v1/sellers/me` | 회원 탈퇴 |

**관리자용**

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/admin/sellers` | 판매자 목록 조회 |
| GET | `/api/v1/admin/sellers/pending` | 승인 대기 목록 |
| GET | `/api/v1/admin/sellers/{id}` | 판매자 상세 조회 |
| PUT | `/api/v1/admin/sellers/{id}/approve` | 판매자 승인 |
| PUT | `/api/v1/admin/sellers/{id}/reject` | 판매자 거절 |
| PUT | `/api/v1/admin/sellers/{id}/suspend` | 판매자 정지 |
| PUT | `/api/v1/admin/sellers/{id}/activate` | 판매자 정지 해제 |

### 7.3 Admin API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/admin/admins` | 관리자 목록 조회 |
| GET | `/api/v1/admin/admins/{id}` | 관리자 상세 조회 |
| POST | `/api/v1/admin/admins` | 관리자 생성 (ADMIN만) |
| PUT | `/api/v1/admin/admins/{id}` | 관리자 정보 수정 |
| PUT | `/api/v1/admin/admins/{id}/role` | 관리자 역할 변경 (ADMIN만) |
| PUT | `/api/v1/admin/admins/{id}/deactivate` | 관리자 비활성화 |

---

## 8. 비즈니스 규칙 요약

### Customer
| 규칙 | 설명 |
|------|------|
| 생성 | AuthCreatedEvent 수신 시 자동 생성 |
| 등급 | 기본 NORMAL, 관리자/시스템에 의해 VIP 승급 |
| 탈퇴 | 진행 중인 예매 없어야 가능 |

### Seller
| 규칙 | 설명 |
|------|------|
| 생성 | AuthCreatedEvent 수신 시 PENDING 상태로 생성 |
| 승인 | 관리자 승인 후 APPROVED → 공연 등록 가능 |
| 정산 정보 | APPROVED 상태에서만 수정 가능 |
| 탈퇴 | 진행 중인 공연 없어야 가능 |

### Admin
| 규칙 | 설명 |
|------|------|
| 생성 | ADMIN 역할만 생성 가능 (일반 가입 불가) |
| 역할 변경 | ADMIN만 가능, 자기 자신 변경 불가 |
| 삭제 보호 | 최소 1명의 ADMIN 유지 필수 |

---

## 9. 데이터베이스 스키마

### Joined Table 전략

```sql
-- users 테이블 (공통)
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    user_type VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    -- AbstractAuditEntity 필드
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100)
);

-- customers 테이블
CREATE TABLE customers (
    user_id UUID PRIMARY KEY REFERENCES users(id),
    grade VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    birth_date DATE
);

-- sellers 테이블
CREATE TABLE sellers (
    user_id UUID PRIMARY KEY REFERENCES users(id),
    -- BusinessInfo
    business_name VARCHAR(200) NOT NULL,
    business_number VARCHAR(20) NOT NULL,
    representative_name VARCHAR(100) NOT NULL,
    business_zip_code VARCHAR(10),
    business_address1 VARCHAR(200),
    business_address2 VARCHAR(200),
    -- SettlementInfo
    bank_code VARCHAR(10),
    account_number VARCHAR(50),
    account_holder VARCHAR(100),
    -- 승인 관련
    seller_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_at TIMESTAMP,
    approved_by VARCHAR(100),
    rejected_reason VARCHAR(500)
);

-- admins 테이블
CREATE TABLE admins (
    user_id UUID PRIMARY KEY REFERENCES users(id),
    department VARCHAR(100),
    admin_role VARCHAR(20) NOT NULL
);

-- 인덱스
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_user_type ON users(user_type);
CREATE INDEX idx_sellers_seller_status ON sellers(seller_status);
CREATE INDEX idx_sellers_business_number ON sellers(business_number);
```

---

## 10. 구현 체크리스트

### Phase 1: 기본 구조
- [ ] 패키지 구조 생성
- [ ] global 공통 클래스 복사 (Auth Service에서)
- [ ] common/domain 구현 (BaseUser, UserStatus, UserProfile, Address)
- [ ] 각 도메인별 ErrorCode, Exception 구현
- [ ] messages_user.properties 작성

### Phase 2: Customer 도메인
- [ ] Customer Entity
- [ ] CustomerGrade VO
- [ ] CustomerRepository
- [ ] CustomerService
- [ ] CustomerApi, CustomerAdminApi

### Phase 3: Seller 도메인
- [ ] Seller Entity
- [ ] SellerStatus, BusinessInfo, SettlementInfo VO
- [ ] SellerRepository
- [ ] SellerService, SellerApprovalService
- [ ] SellerApi, SellerAdminApi

### Phase 4: Admin 도메인
- [ ] Admin Entity
- [ ] AdminRole, AdminProfile VO
- [ ] AdminRepository
- [ ] AdminService
- [ ] AdminApi

### Phase 5: 이벤트 연동
- [ ] AuthCreatedEvent 구독 → Customer/Seller/Admin 생성
- [ ] AuthWithdrawnEvent 구독 → 상태 변경
- [ ] 발행 이벤트 구현 (SellerApproved, SellerRejected 등)

### Phase 6: 테스트
- [ ] 단위 테스트 (Domain)
- [ ] 통합 테스트 (Service)
- [ ] API 테스트 (Controller)