# User Service Error Messages 한글 매핑

## UserErrorCode (Common)

### 검증 - 프로필 (400)

| 코드 | 메시지 |
|------|--------|
| `INVALID_NAME` | 이름은 필수이며 50자 이하여야 합니다. |
| `INVALID_PHONE` | 연락처 형식이 올바르지 않습니다. |
| `INVALID_ADDRESS` | 주소 정보가 유효하지 않습니다. |

### 상태 (422)

| 코드 | 메시지 |
|------|--------|
| `USER_ALREADY_SUSPENDED` | 이미 정지된 사용자입니다. |
| `USER_ALREADY_ACTIVE` | 이미 활성화된 사용자입니다. |
| `USER_ALREADY_WITHDRAWN` | 이미 탈퇴한 사용자입니다. |

### 이벤트 (503)

| 코드 | 메시지 |
|------|--------|
| `EVENT_PUBLISH_FAILED` | 이벤트 발행에 실패했습니다. (사용자 ID: {0}) |

---

## CustomerErrorCode

### 조회 (404)

| 코드 | 메시지 |
|------|--------|
| `CUSTOMER_NOT_FOUND` | 고객 {0}을(를) 찾을 수 없습니다. |

### 검증 (400)

| 코드 | 메시지 |
|------|--------|
| `INVALID_BIRTH_DATE` | 생년월일이 유효하지 않습니다. |
| `INVALID_CUSTOMER_GRADE` | 고객 등급이 유효하지 않습니다. |

### 권한 (403)

| 코드 | 메시지 |
|------|--------|
| `CUSTOMER_SUSPENDED` | 정지된 고객입니다. |
| `CUSTOMER_WITHDRAWN` | 탈퇴한 고객입니다. |

### 비즈니스 규칙 (422)

| 코드 | 메시지 |
|------|--------|
| `CANNOT_WITHDRAW_WITH_ACTIVE_RESERVATIONS` | 진행 중인 예매가 있어 탈퇴할 수 없습니다. |
| `GRADE_DOWNGRADE_NOT_ALLOWED` | 등급 하향은 허용되지 않습니다. |

---

## SellerErrorCode

### 조회 (404)

| 코드 | 메시지 |
|------|--------|
| `SELLER_NOT_FOUND` | 판매자 {0}을(를) 찾을 수 없습니다. |

### 검증 - 사업자 정보 (400)

| 코드 | 메시지 |
|------|--------|
| `INVALID_BUSINESS_NAME` | 상호명은 필수이며 200자 이하여야 합니다. |
| `INVALID_BUSINESS_NUMBER` | 사업자등록번호가 유효하지 않습니다. 10자리 숫자를 입력해주세요. |
| `INVALID_REPRESENTATIVE_NAME` | 대표자명은 필수입니다. |
| `INVALID_BUSINESS_ADDRESS` | 사업장 주소가 유효하지 않습니다. |
| `DUPLICATE_BUSINESS_NUMBER` | 이미 등록된 사업자등록번호입니다. |

### 검증 - 정산 정보 (400)

| 코드 | 메시지 |
|------|--------|
| `INVALID_BANK_CODE` | 은행 코드가 유효하지 않습니다. |
| `INVALID_ACCOUNT_NUMBER` | 계좌번호가 유효하지 않습니다. |
| `INVALID_ACCOUNT_HOLDER` | 예금주명은 필수입니다. |

### 검증 - 승인 (400)

| 코드 | 메시지 |
|------|--------|
| `INVALID_REJECTION_REASON` | 거절 사유는 필수입니다. |

### 권한 (403)

| 코드 | 메시지 |
|------|--------|
| `SELLER_NOT_APPROVED` | 승인되지 않은 판매자입니다. |
| `SELLER_SUSPENDED` | 정지된 판매자입니다. |
| `SELLER_WITHDRAWN` | 탈퇴한 판매자입니다. |

### 비즈니스 규칙 (422)

| 코드 | 메시지 |
|------|--------|
| `SELLER_NOT_PENDING` | 승인 대기 상태가 아닙니다. |
| `SELLER_ALREADY_APPROVED` | 이미 승인된 판매자입니다. |
| `SELLER_ALREADY_REJECTED` | 이미 거절된 판매자입니다. |
| `CANNOT_UPDATE_SETTLEMENT_BEFORE_APPROVAL` | 승인 전에는 정산 정보를 수정할 수 없습니다. |
| `CANNOT_WITHDRAW_WITH_ACTIVE_PRODUCTS` | 진행 중인 공연이 있어 탈퇴할 수 없습니다. |
| `CANNOT_REGISTER_PERFORMANCE` | 공연을 등록할 수 없는 상태입니다. 승인 완료 후 이용해주세요. |

---

## AdminErrorCode

### 조회 (404)

| 코드 | 메시지 |
|------|--------|
| `ADMIN_NOT_FOUND` | 관리자 {0}을(를) 찾을 수 없습니다. |

### 검증 (400)

| 코드 | 메시지 |
|------|--------|
| `INVALID_ADMIN_ROLE` | 관리자 역할이 유효하지 않습니다. |
| `INVALID_DEPARTMENT` | 부서 정보가 유효하지 않습니다. |

### 권한 (403)

| 코드 | 메시지 |
|------|--------|
| `ADMIN_PERMISSION_DENIED` | 권한이 없습니다. |
| `ONLY_ADMIN_CAN_CREATE_ADMIN` | 관리자 생성은 ADMIN 역할만 가능합니다. |
| `ONLY_ADMIN_CAN_CHANGE_ROLE` | 역할 변경은 ADMIN 역할만 가능합니다. |
| `ADMIN_SUSPENDED` | 정지된 관리자입니다. |

### 비즈니스 규칙 (422)

| 코드 | 메시지 |
|------|--------|
| `CANNOT_CHANGE_OWN_ROLE` | 자신의 역할은 변경할 수 없습니다. |
| `CANNOT_DELETE_LAST_ADMIN` | 마지막 ADMIN은 삭제할 수 없습니다. |
| `CANNOT_DEACTIVATE_LAST_ADMIN` | 마지막 ADMIN은 비활성화할 수 없습니다. |

---

## 정책 참고

### 사용자 상태

| 상태 | 설명 |
|------|------|
| `ACTIVE` | 활성 상태 - 정상 이용 가능 |
| `SUSPENDED` | 정지 상태 - 관리자에 의해 정지됨 |
| `WITHDRAWN` | 탈퇴 상태 - 탈퇴 처리됨 |

### 고객 등급

| 등급 | 설명 |
|------|------|
| `NORMAL` | 일반 등급 - 기본 등급 |
| `VIP` | VIP 등급 - 혜택 제공 |

### 판매자 승인 상태

| 상태 | 설명 |
|------|------|
| `PENDING` | 승인 대기 - 가입 후 관리자 승인 대기 |
| `APPROVED` | 승인 완료 - 공연 등록 가능 |
| `REJECTED` | 승인 거절 - 거절 사유와 함께 거절됨 |

### 관리자 역할

| 역할 | 설명 |
|------|------|
| `MANAGER` | 매니저 - 일반 관리 권한 |
| `ADMIN` | 어드민 - 최고 관리 권한 (관리자 생성, 역할 변경 가능) |

### 비즈니스 규칙

| 도메인 | 규칙 |
|--------|------|
| Customer | 등급 하향 불가, 진행 중인 예매 있으면 탈퇴 불가 |
| Seller | 승인 전 정산정보 수정 불가, 진행 중인 공연 있으면 탈퇴 불가 |
| Admin | 자기 역할 변경 불가, 마지막 ADMIN 삭제/비활성화 불가 |