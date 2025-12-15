package com.tickatch.user_service.admin.domain.repository;

import static com.tickatch.user_service.admin.domain.QAdmin.admin;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tickatch.user_service.admin.domain.Admin;
import com.tickatch.user_service.admin.domain.AdminRepository;
import com.tickatch.user_service.admin.domain.repository.dto.AdminSearchCondition;
import com.tickatch.user_service.admin.domain.vo.AdminRole;
import com.tickatch.user_service.common.domain.vo.UserStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * Admin 리포지토리 구현체.
 *
 * <p>JPA와 QueryDSL을 사용하여 Admin 데이터를 조회/저장한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class AdminRepositoryImpl implements AdminRepository {

  private final AdminJpaRepository adminJpaRepository;
  private final JPAQueryFactory queryFactory;

  @Override
  public Admin save(Admin admin) {
    return adminJpaRepository.save(admin);
  }

  @Override
  public Optional<Admin> findById(UUID id) {
    return adminJpaRepository.findById(id);
  }

  @Override
  public Optional<Admin> findByEmail(String email) {
    return adminJpaRepository.findByEmail(email);
  }

  @Override
  public boolean existsByEmail(String email) {
    return adminJpaRepository.existsByEmail(email);
  }

  @Override
  public long countActiveByRole(AdminRole role) {
    return adminJpaRepository.countByAdminRoleAndStatus(role, UserStatus.ACTIVE);
  }

  @Override
  public Page<Admin> findAllByCondition(AdminSearchCondition condition, Pageable pageable) {
    List<Admin> content = queryFactory
        .selectFrom(admin)
        .where(
            emailContains(condition.getEmail()),
            nameContains(condition.getName()),
            statusEq(condition.getStatus()),
            adminRoleEq(condition.getAdminRole()),
            departmentContains(condition.getDepartment()))
        .orderBy(getOrderSpecifiers(pageable.getSort()))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(admin.count())
        .from(admin)
        .where(
            emailContains(condition.getEmail()),
            nameContains(condition.getName()),
            statusEq(condition.getStatus()),
            adminRoleEq(condition.getAdminRole()),
            departmentContains(condition.getDepartment()));

    return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
  }

  private BooleanExpression emailContains(String email) {
    return StringUtils.hasText(email) ? admin.email.containsIgnoreCase(email) : null;
  }

  private BooleanExpression nameContains(String name) {
    return StringUtils.hasText(name) ? admin.profile.name.containsIgnoreCase(name) : null;
  }

  private BooleanExpression statusEq(UserStatus status) {
    return status != null ? admin.status.eq(status) : null;
  }

  private BooleanExpression adminRoleEq(AdminRole role) {
    return role != null ? admin.adminRole.eq(role) : null;
  }

  private BooleanExpression departmentContains(String department) {
    return StringUtils.hasText(department)
        ? admin.profile.department.containsIgnoreCase(department) : null;
  }

  private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
    List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

    sort.forEach(order -> {
      Order direction = order.isAscending() ? Order.ASC : Order.DESC;
      String property = order.getProperty();

      OrderSpecifier<?> orderSpecifier = switch (property) {
        case "email" -> new OrderSpecifier<>(direction, admin.email);
        case "name" -> new OrderSpecifier<>(direction, admin.profile.name);
        case "status" -> new OrderSpecifier<>(direction, admin.status);
        case "adminRole" -> new OrderSpecifier<>(direction, admin.adminRole);
        case "department" -> new OrderSpecifier<>(direction, admin.profile.department);
        case "createdAt" -> new OrderSpecifier<>(direction, admin.createdAt);
        case "updatedAt" -> new OrderSpecifier<>(direction, admin.updatedAt);
        default -> new OrderSpecifier<>(direction, admin.createdAt);
      };
      orderSpecifiers.add(orderSpecifier);
    });

    if (orderSpecifiers.isEmpty()) {
      orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, admin.createdAt));
    }

    return orderSpecifiers.toArray(new OrderSpecifier[0]);
  }
}