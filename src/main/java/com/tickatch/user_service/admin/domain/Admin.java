package com.tickatch.user_service.admin.domain;

import static com.tickatch.user_service.admin.domain.exception.AdminErrorCode.CANNOT_CHANGE_OWN_ROLE;
import static com.tickatch.user_service.admin.domain.exception.AdminErrorCode.ONLY_ADMIN_CAN_CHANGE_ROLE;

import com.tickatch.user_service.admin.domain.exception.AdminException;
import com.tickatch.user_service.admin.domain.vo.AdminProfile;
import com.tickatch.user_service.admin.domain.vo.AdminRole;
import com.tickatch.user_service.common.domain.vo.UserStatus;
import com.tickatch.user_service.global.domain.AbstractAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자 엔티티 (Aggregate Root).
 *
 * <p>시스템 관리자를 나타낸다. BaseUser를 상속하지 않고 별도로 구현 (AdminProfile 사용).
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Entity
@Table(name = "admins")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin extends AbstractAuditEntity {

  /** 관리자 ID (Auth Service의 authId와 동일). */
  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  /** 이메일 (조회용, 수정 불가). */
  @Column(name = "email", nullable = false, length = 255)
  private String email;

  /** 관리자 프로필. */
  @Embedded private AdminProfile profile;

  /** 관리자 상태. */
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private UserStatus status;

  /** 관리자 역할. */
  @Enumerated(EnumType.STRING)
  @Column(name = "admin_role", nullable = false, length = 20)
  private AdminRole adminRole;

  private Admin(UUID id, String email, AdminProfile profile, AdminRole adminRole) {
    this.id = id;
    this.email = email;
    this.profile = profile;
    this.status = UserStatus.ACTIVE;
    this.adminRole = adminRole;
  }

  /**
   * 관리자 생성 (정적 팩토리).
   *
   * @param authId Auth Service의 인증 ID (= 사용자 ID)
   * @param email 이메일
   * @param name 이름
   * @param phone 연락처
   * @param department 부서
   * @param adminRole 역할
   * @return 생성된 Admin
   */
  public static Admin create(
      UUID authId,
      String email,
      String name,
      String phone,
      String department,
      AdminRole adminRole) {
    AdminProfile profile = AdminProfile.of(name, phone, department);
    return new Admin(authId, email, profile, adminRole);
  }

  /**
   * 프로필 수정.
   *
   * @param name 새 이름
   * @param phone 새 연락처
   * @param department 새 부서
   */
  public void updateProfile(String name, String phone, String department) {
    this.profile = this.profile.update(name, phone, department);
  }

  /**
   * 역할 변경 (ADMIN만 가능, 자기 자신 불가).
   *
   * @param newRole 새 역할
   * @param changedBy 변경자 Admin
   * @throws AdminException 권한 없거나 자기 자신 변경인 경우
   */
  public void changeRole(AdminRole newRole, Admin changedBy) {
    if (!changedBy.getAdminRole().canChangeRole()) {
      throw new AdminException(ONLY_ADMIN_CAN_CHANGE_ROLE);
    }
    if (this.id.equals(changedBy.getId())) {
      throw new AdminException(CANNOT_CHANGE_OWN_ROLE);
    }
    this.adminRole = newRole;
  }

  /** 관리자 정지. */
  public void suspend() {
    this.status = UserStatus.SUSPENDED;
  }

  /** 정지 해제. */
  public void activate() {
    this.status = UserStatus.ACTIVE;
  }

  /** 탈퇴 처리. */
  public void withdraw() {
    this.status = UserStatus.WITHDRAWN;
  }

  /**
   * 활성 상태 확인.
   *
   * @return 활성 상태이면 true
   */
  public boolean isActive() {
    return this.status.isActive();
  }

  /**
   * 정지 상태 확인.
   *
   * @return 정지 상태이면 true
   */
  public boolean isSuspended() {
    return this.status.isSuspended();
  }

  /**
   * 탈퇴 상태 확인.
   *
   * @return 탈퇴 상태이면 true
   */
  public boolean isWithdrawn() {
    return this.status.isWithdrawn();
  }

  /**
   * ADMIN 역할인지 확인.
   *
   * @return ADMIN이면 true
   */
  public boolean isAdmin() {
    return this.adminRole.isAdmin();
  }

  /**
   * MANAGER 역할인지 확인.
   *
   * @return MANAGER이면 true
   */
  public boolean isManager() {
    return this.adminRole.isManager();
  }

  /**
   * 특정 권한이 있는지 확인.
   *
   * @param requiredRole 필요한 역할
   * @return 권한이 있으면 true
   */
  public boolean hasPermission(AdminRole requiredRole) {
    return this.adminRole.isHigherOrEqualThan(requiredRole);
  }

  /**
   * 관리자 생성 권한이 있는지 확인.
   *
   * @return 권한 있으면 true
   */
  public boolean canCreateAdmin() {
    return this.adminRole.canCreateAdmin();
  }

  /**
   * 판매자 승인 권한이 있는지 확인.
   *
   * @return 권한 있으면 true
   */
  public boolean canApproveSeller() {
    return this.adminRole.canApproveSeller();
  }
}
