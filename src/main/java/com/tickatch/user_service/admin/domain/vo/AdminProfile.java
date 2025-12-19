package com.tickatch.user_service.admin.domain.vo;

import static com.tickatch.user_service.admin.domain.exception.AdminErrorCode.INVALID_DEPARTMENT;
import static com.tickatch.user_service.common.domain.exception.UserErrorCode.INVALID_NAME;
import static com.tickatch.user_service.common.domain.exception.UserErrorCode.INVALID_PHONE;

import com.tickatch.user_service.admin.domain.exception.AdminException;
import com.tickatch.user_service.common.domain.exception.UserException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자 프로필 Value Object.
 *
 * <p>UserProfile과 유사하지만 department 필드가 추가됨.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminProfile {

  private static final int MAX_NAME_LENGTH = 50;
  private static final int MAX_DEPARTMENT_LENGTH = 100;
  private static final Pattern PHONE_PATTERN = Pattern.compile("^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$");

  @Column(name = "name", nullable = false, length = 50)
  private String name;

  @Column(name = "phone", length = 20)
  private String phone;

  @Column(name = "department", length = 100)
  private String department;

  private AdminProfile(String name, String phone, String department) {
    this.name = name;
    this.phone = phone;
    this.department = department;
  }

  /**
   * AdminProfile 생성.
   *
   * @param name 이름 (필수, 50자 이하)
   * @param phone 연락처 (선택)
   * @param department 부서 (선택)
   * @return AdminProfile 인스턴스
   * @throws UserException 이름/연락처 유효성 검증 실패 시
   * @throws AdminException 부서 유효성 검증 실패 시
   */
  public static AdminProfile of(String name, String phone, String department) {
    validateName(name);
    validatePhone(phone);
    validateDepartment(department);
    return new AdminProfile(name, normalizePhone(phone), department);
  }

  /**
   * 프로필 수정.
   *
   * @param name 새 이름
   * @param phone 새 연락처
   * @param department 새 부서
   * @return 수정된 AdminProfile
   */
  public AdminProfile update(String name, String phone, String department) {
    return AdminProfile.of(name, phone, department);
  }

  private static void validateName(String name) {
    if (name == null || name.isBlank()) {
      throw new UserException(INVALID_NAME);
    }
    if (name.length() > MAX_NAME_LENGTH) {
      throw new UserException(INVALID_NAME);
    }
  }

  private static void validatePhone(String phone) {
    if (phone == null || phone.isBlank()) {
      return; // 선택 필드
    }
    String normalized = phone.replaceAll("-", "");
    if (!PHONE_PATTERN.matcher(phone).matches() && !normalized.matches("^01[0-9][0-9]{7,8}$")) {
      throw new UserException(INVALID_PHONE);
    }
  }

  private static void validateDepartment(String department) {
    if (department == null || department.isBlank()) {
      return; // 선택 필드
    }
    if (department.length() > MAX_DEPARTMENT_LENGTH) {
      throw new AdminException(INVALID_DEPARTMENT);
    }
  }

  private static String normalizePhone(String phone) {
    if (phone == null || phone.isBlank()) {
      return null;
    }
    return phone.replaceAll("-", "");
  }
}
