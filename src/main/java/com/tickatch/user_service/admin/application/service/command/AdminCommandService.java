package com.tickatch.user_service.admin.application.service.command;

import com.tickatch.user_service.admin.application.messaging.AdminLogEventPublisher;
import com.tickatch.user_service.admin.application.service.command.dto.ChangeAdminRoleCommand;
import com.tickatch.user_service.admin.application.service.command.dto.CreateAdminCommand;
import com.tickatch.user_service.admin.application.service.command.dto.UpdateAdminProfileCommand;
import com.tickatch.user_service.admin.domain.Admin;
import com.tickatch.user_service.admin.domain.AdminRepository;
import com.tickatch.user_service.admin.domain.exception.AdminErrorCode;
import com.tickatch.user_service.admin.domain.exception.AdminException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자 커맨드 서비스.
 *
 * <p>관리자 생성, 수정, 역할 변경, 상태 변경 등 상태를 변경하는 작업을 처리한다. 모든 주요 작업에 대해 성공/실패 로그를 로그 서비스로 발행한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminCommandService {

  private final AdminRepository adminRepository;
  private final AdminLogEventPublisher logEventPublisher;

  /**
   * 관리자를 생성한다.
   *
   * <p>성공 시 ADMIN_CREATED 로그를, 실패 시 ADMIN_CREATE_FAILED 로그를 발행한다.
   *
   * @param command 생성 커맨드
   * @return 생성된 관리자 ID
   * @throws AdminException 이미 존재하는 이메일인 경우
   */
  public UUID createAdmin(CreateAdminCommand command) {
    try {
      if (adminRepository.existsByEmail(command.email())) {
        throw new AdminException(AdminErrorCode.ADMIN_ALREADY_EXISTS);
      }

      Admin admin =
          Admin.create(
              command.authId(),
              command.email(),
              command.name(),
              command.phone(),
              command.department(),
              command.adminRole());

      UUID adminId = adminRepository.save(admin).getId();
      log.info("관리자 생성 완료. adminId: {}", adminId);

      logEventPublisher.publishCreated(adminId);
      return adminId;
    } catch (Exception e) {
      logEventPublisher.publishCreateFailed();
      log.error("관리자 생성 실패. email: {}, error: {}", command.email(), e.getMessage(), e);
      throw e;
    }
  }

  /**
   * 관리자 프로필을 수정한다.
   *
   * <p>성공 시 ADMIN_UPDATED 로그를, 실패 시 ADMIN_UPDATE_FAILED 로그를 발행한다.
   *
   * @param command 프로필 수정 커맨드
   * @throws AdminException 관리자를 찾을 수 없는 경우
   */
  public void updateProfile(UpdateAdminProfileCommand command) {
    try {
      Admin admin = findAdminById(command.adminId());
      admin.updateProfile(command.name(), command.phone(), command.department());
      log.info("관리자 프로필 수정 완료. adminId: {}", command.adminId());

      logEventPublisher.publishUpdated(command.adminId());
    } catch (Exception e) {
      logEventPublisher.publishUpdateFailed(command.adminId());
      log.error("관리자 프로필 수정 실패. adminId: {}, error: {}", command.adminId(), e.getMessage(), e);
      throw e;
    }
  }

  /**
   * 관리자 역할을 변경한다.
   *
   * <p>성공 시 ADMIN_UPDATED 로그를, 실패 시 ADMIN_UPDATE_FAILED 로그를 발행한다.
   *
   * @param command 역할 변경 커맨드
   * @throws AdminException 관리자를 찾을 수 없거나 역할 변경 권한이 없는 경우
   */
  public void changeRole(ChangeAdminRoleCommand command) {
    try {
      Admin targetAdmin = findAdminById(command.targetAdminId());
      Admin changerAdmin = findAdminById(command.changerAdminId());

      targetAdmin.changeRole(command.newRole(), changerAdmin);
      log.info(
          "관리자 역할 변경 완료. targetAdminId: {}, newRole: {}",
          command.targetAdminId(),
          command.newRole());

      logEventPublisher.publishUpdated(command.targetAdminId());
    } catch (Exception e) {
      logEventPublisher.publishUpdateFailed(command.targetAdminId());
      log.error(
          "관리자 역할 변경 실패. targetAdminId: {}, error: {}", command.targetAdminId(), e.getMessage(), e);
      throw e;
    }
  }

  /**
   * 관리자를 정지한다.
   *
   * <p>성공 시 ADMIN_SUSPENDED 로그를, 실패 시 ADMIN_SUSPEND_FAILED 로그를 발행한다.
   *
   * @param adminId 관리자 ID
   * @throws AdminException 관리자를 찾을 수 없거나 정지할 수 없는 상태인 경우
   */
  public void suspendAdmin(UUID adminId) {
    try {
      Admin admin = findAdminById(adminId);
      admin.suspend();
      log.info("관리자 정지 완료. adminId: {}", adminId);

      logEventPublisher.publishSuspended(adminId);
    } catch (Exception e) {
      logEventPublisher.publishSuspendFailed(adminId);
      log.error("관리자 정지 실패. adminId: {}, error: {}", adminId, e.getMessage(), e);
      throw e;
    }
  }

  /**
   * 관리자 정지를 해제한다.
   *
   * <p>성공 시 ADMIN_ACTIVATED 로그를, 실패 시 ADMIN_ACTIVATE_FAILED 로그를 발행한다.
   *
   * @param adminId 관리자 ID
   * @throws AdminException 관리자를 찾을 수 없거나 활성화할 수 없는 상태인 경우
   */
  public void activateAdmin(UUID adminId) {
    try {
      Admin admin = findAdminById(adminId);
      admin.activate();
      log.info("관리자 활성화 완료. adminId: {}", adminId);

      logEventPublisher.publishActivated(adminId);
    } catch (Exception e) {
      logEventPublisher.publishActivateFailed(adminId);
      log.error("관리자 활성화 실패. adminId: {}, error: {}", adminId, e.getMessage(), e);
      throw e;
    }
  }

  /**
   * 관리자를 탈퇴 처리한다.
   *
   * <p>성공 시 ADMIN_WITHDRAWN 로그를, 실패 시 ADMIN_WITHDRAW_FAILED 로그를 발행한다.
   *
   * @param adminId 관리자 ID
   * @throws AdminException 관리자를 찾을 수 없거나 탈퇴할 수 없는 상태인 경우
   */
  public void withdrawAdmin(UUID adminId) {
    try {
      Admin admin = findAdminById(adminId);
      admin.withdraw();
      log.info("관리자 탈퇴 완료. adminId: {}", adminId);

      logEventPublisher.publishWithdrawn(adminId);
    } catch (Exception e) {
      logEventPublisher.publishWithdrawFailed(adminId);
      log.error("관리자 탈퇴 실패. adminId: {}, error: {}", adminId, e.getMessage(), e);
      throw e;
    }
  }

  private Admin findAdminById(UUID adminId) {
    return adminRepository
        .findById(adminId)
        .orElseThrow(() -> new AdminException(AdminErrorCode.ADMIN_NOT_FOUND));
  }
}
