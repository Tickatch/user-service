package com.tickatch.user_service.admin.application.service.command;

import com.tickatch.user_service.admin.application.service.command.dto.ChangeAdminRoleCommand;
import com.tickatch.user_service.admin.application.service.command.dto.CreateAdminCommand;
import com.tickatch.user_service.admin.application.service.command.dto.UpdateAdminProfileCommand;
import com.tickatch.user_service.admin.domain.Admin;
import com.tickatch.user_service.admin.domain.AdminRepository;
import com.tickatch.user_service.admin.domain.exception.AdminErrorCode;
import com.tickatch.user_service.admin.domain.exception.AdminException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자 커맨드 서비스.
 *
 * <p>관리자 생성, 수정, 역할 변경, 상태 변경 등 상태를 변경하는 작업을 처리한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AdminCommandService {

  private final AdminRepository adminRepository;

  /**
   * 관리자를 생성한다.
   *
   * @param command 생성 커맨드
   * @return 생성된 관리자 ID
   * @throws AdminException 이미 존재하는 이메일인 경우
   */
  public UUID createAdmin(CreateAdminCommand command) {
    if (adminRepository.existsByEmail(command.email())) {
      throw new AdminException(AdminErrorCode.ADMIN_ALREADY_EXISTS);
    }

    Admin admin = Admin.create(
        command.authId(),
        command.email(),
        command.name(),
        command.phone(),
        command.department(),
        command.adminRole()
    );

    return adminRepository.save(admin).getId();
  }

  /**
   * 관리자 프로필을 수정한다.
   *
   * @param command 프로필 수정 커맨드
   * @throws AdminException 관리자를 찾을 수 없는 경우
   */
  public void updateProfile(UpdateAdminProfileCommand command) {
    Admin admin = findAdminById(command.adminId());
    admin.updateProfile(command.name(), command.phone(), command.department());
  }

  /**
   * 관리자 역할을 변경한다.
   *
   * @param command 역할 변경 커맨드
   * @throws AdminException 관리자를 찾을 수 없거나 역할 변경 권한이 없는 경우
   */
  public void changeRole(ChangeAdminRoleCommand command) {
    Admin targetAdmin = findAdminById(command.targetAdminId());
    Admin changerAdmin = findAdminById(command.changerAdminId());

    targetAdmin.changeRole(command.newRole(), changerAdmin);
  }

  /**
   * 관리자를 정지한다.
   *
   * @param adminId 관리자 ID
   * @throws AdminException 관리자를 찾을 수 없거나 정지할 수 없는 상태인 경우
   */
  public void suspendAdmin(UUID adminId) {
    Admin admin = findAdminById(adminId);
    admin.suspend();
  }

  /**
   * 관리자 정지를 해제한다.
   *
   * @param adminId 관리자 ID
   * @throws AdminException 관리자를 찾을 수 없거나 활성화할 수 없는 상태인 경우
   */
  public void activateAdmin(UUID adminId) {
    Admin admin = findAdminById(adminId);
    admin.activate();
  }

  /**
   * 관리자를 탈퇴 처리한다.
   *
   * @param adminId 관리자 ID
   * @throws AdminException 관리자를 찾을 수 없거나 탈퇴할 수 없는 상태인 경우
   */
  public void withdrawAdmin(UUID adminId) {
    Admin admin = findAdminById(adminId);
    admin.withdraw();
  }

  private Admin findAdminById(UUID adminId) {
    return adminRepository.findById(adminId)
        .orElseThrow(() -> new AdminException(AdminErrorCode.ADMIN_NOT_FOUND));
  }
}