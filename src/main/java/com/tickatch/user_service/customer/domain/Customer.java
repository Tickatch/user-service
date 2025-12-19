package com.tickatch.user_service.customer.domain;

import static com.tickatch.user_service.customer.domain.exception.CustomerErrorCode.GRADE_DOWNGRADE_NOT_ALLOWED;
import static com.tickatch.user_service.customer.domain.exception.CustomerErrorCode.INVALID_BIRTH_DATE;

import com.tickatch.user_service.common.domain.BaseUser;
import com.tickatch.user_service.common.domain.vo.UserProfile;
import com.tickatch.user_service.customer.domain.exception.CustomerException;
import com.tickatch.user_service.customer.domain.vo.CustomerGrade;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 고객 엔티티 (Aggregate Root).
 *
 * <p>티켓 구매 고객을 나타낸다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Entity
@Table(name = "customers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer extends BaseUser {

  /** 고객 등급. */
  @Enumerated(EnumType.STRING)
  @Column(name = "grade", nullable = false, length = 20)
  private CustomerGrade grade;

  /** 생년월일. */
  @Column(name = "birth_date")
  private LocalDate birthDate;

  private Customer(UUID id, String email, UserProfile profile, LocalDate birthDate) {
    super(id, email, profile);
    this.grade = CustomerGrade.NORMAL;
    this.birthDate = birthDate;
  }

  /**
   * 고객 생성 (정적 팩토리).
   *
   * @param authId Auth Service의 인증 ID (= 사용자 ID)
   * @param email 이메일
   * @param name 이름
   * @param phone 연락처
   * @param birthDate 생년월일 (선택)
   * @return 생성된 Customer
   */
  public static Customer create(
      UUID authId, String email, String name, String phone, LocalDate birthDate) {
    validateBirthDate(birthDate);
    UserProfile profile = UserProfile.of(name, phone);
    return new Customer(authId, email, profile, birthDate);
  }

  /**
   * 생년월일 수정.
   *
   * @param birthDate 새 생년월일
   * @throws CustomerException 유효하지 않은 생년월일인 경우
   */
  public void updateBirthDate(LocalDate birthDate) {
    validateNotWithdrawn();
    validateBirthDate(birthDate);
    this.birthDate = birthDate;
  }

  /**
   * 등급 업그레이드.
   *
   * @param newGrade 새 등급
   * @throws CustomerException 등급 하향인 경우
   */
  public void upgradeGrade(CustomerGrade newGrade) {
    validateNotWithdrawn();
    if (!this.grade.canUpgradeTo(newGrade)) {
      throw new CustomerException(GRADE_DOWNGRADE_NOT_ALLOWED);
    }
    this.grade = newGrade;
  }

  /**
   * VIP 여부 확인.
   *
   * @return VIP이면 true
   */
  public boolean isVip() {
    return this.grade.isVip();
  }

  private static void validateBirthDate(LocalDate birthDate) {
    if (birthDate == null) {
      return; // 선택 필드
    }
    if (birthDate.isAfter(LocalDate.now())) {
      throw new CustomerException(INVALID_BIRTH_DATE);
    }
    // 150세 이상은 불가
    if (birthDate.isBefore(LocalDate.now().minusYears(150))) {
      throw new CustomerException(INVALID_BIRTH_DATE);
    }
  }
}
