package com.tickatch.user_service.common.domain.vo;

import static com.tickatch.user_service.common.domain.exception.UserErrorCode.INVALID_NAME;
import static com.tickatch.user_service.common.domain.exception.UserErrorCode.INVALID_PHONE;

import com.tickatch.user_service.common.domain.exception.UserException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 프로필 Value Object.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile {

  private static final int MAX_NAME_LENGTH = 50;
  private static final Pattern PHONE_PATTERN = Pattern.compile("^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$");

  @Column(name = "name", nullable = false, length = 50)
  private String name;

  @Column(name = "phone", length = 20)
  private String phone;

  private UserProfile(String name, String phone) {
    this.name = name;
    this.phone = phone;
  }

  /**
   * UserProfile 생성.
   *
   * @param name 이름 (필수, 50자 이하)
   * @param phone 연락처 (선택)
   * @return UserProfile 인스턴스
   * @throws UserException 유효성 검증 실패 시
   */
  public static UserProfile of(String name, String phone) {
    validateName(name);
    validatePhone(phone);
    return new UserProfile(name, normalizePhone(phone));
  }

  /**
   * 프로필 수정.
   *
   * @param name 새 이름
   * @param phone 새 연락처
   * @return 수정된 UserProfile
   */
  public UserProfile update(String name, String phone) {
    return UserProfile.of(name, phone);
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

  private static String normalizePhone(String phone) {
    if (phone == null || phone.isBlank()) {
      return null;
    }
    return phone.replaceAll("-", "");
  }
}
