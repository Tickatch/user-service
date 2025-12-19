package com.tickatch.user_service.common.domain;

import static com.tickatch.user_service.common.domain.exception.UserErrorCode.USER_ALREADY_ACTIVE;
import static com.tickatch.user_service.common.domain.exception.UserErrorCode.USER_ALREADY_SUSPENDED;
import static com.tickatch.user_service.common.domain.exception.UserErrorCode.USER_ALREADY_WITHDRAWN;

import com.tickatch.user_service.common.domain.exception.UserException;
import com.tickatch.user_service.common.domain.vo.UserProfile;
import com.tickatch.user_service.common.domain.vo.UserStatus;
import com.tickatch.user_service.global.domain.AbstractAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 공통 추상 클래스.
 *
 * <p>Customer, Seller가 상속받는 공통 클래스이다. ID는 Auth Service의 authId와 동일한 값을 사용한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseUser extends AbstractAuditEntity {

  /** 사용자 ID (Auth Service의 authId와 동일). */
  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  /** 이메일 (조회용, 수정 불가). */
  @Column(name = "email", nullable = false, length = 255)
  private String email;

  /** 사용자 프로필. */
  @Embedded private UserProfile profile;

  /** 사용자 상태. */
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private UserStatus status;

  protected BaseUser(UUID id, String email, UserProfile profile) {
    this.id = id;
    this.email = email;
    this.profile = profile;
    this.status = UserStatus.ACTIVE;
  }

  /**
   * 프로필 수정.
   *
   * @param name 새 이름
   * @param phone 새 연락처
   */
  public void updateProfile(String name, String phone) {
    validateNotWithdrawn();
    this.profile = this.profile.update(name, phone);
  }

  /**
   * 사용자 정지.
   *
   * @throws UserException 이미 정지 또는 탈퇴 상태인 경우
   */
  public void suspend() {
    validateNotWithdrawn();
    if (this.status == UserStatus.SUSPENDED) {
      throw new UserException(USER_ALREADY_SUSPENDED);
    }
    this.status = UserStatus.SUSPENDED;
  }

  /**
   * 정지 해제.
   *
   * @throws UserException 이미 활성 또는 탈퇴 상태인 경우
   */
  public void activate() {
    validateNotWithdrawn();
    if (this.status == UserStatus.ACTIVE) {
      throw new UserException(USER_ALREADY_ACTIVE);
    }
    this.status = UserStatus.ACTIVE;
  }

  /**
   * 탈퇴 처리.
   *
   * @throws UserException 이미 탈퇴 상태인 경우
   */
  public void withdraw() {
    validateNotWithdrawn();
    this.status = UserStatus.WITHDRAWN;
  }

  /**
   * 활성 상태 확인.
   *
   * @return 활성 상태이면 true
   */
  public boolean isActive() {
    return this.status == UserStatus.ACTIVE;
  }

  /**
   * 정지 상태 확인.
   *
   * @return 정지 상태이면 true
   */
  public boolean isSuspended() {
    return this.status == UserStatus.SUSPENDED;
  }

  /**
   * 탈퇴 상태 확인.
   *
   * @return 탈퇴 상태이면 true
   */
  public boolean isWithdrawn() {
    return this.status == UserStatus.WITHDRAWN;
  }

  /**
   * 탈퇴 상태가 아닌지 검증.
   *
   * @throws UserException 탈퇴 상태인 경우
   */
  protected void validateNotWithdrawn() {
    if (this.status == UserStatus.WITHDRAWN) {
      throw new UserException(USER_ALREADY_WITHDRAWN);
    }
  }
}
