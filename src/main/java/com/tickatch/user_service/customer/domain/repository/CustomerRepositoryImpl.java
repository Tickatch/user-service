package com.tickatch.user_service.customer.domain.repository;

import static com.tickatch.user_service.customer.domain.QCustomer.customer;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tickatch.user_service.common.domain.vo.UserStatus;
import com.tickatch.user_service.customer.domain.Customer;
import com.tickatch.user_service.customer.domain.CustomerRepository;
import com.tickatch.user_service.customer.domain.repository.dto.CustomerSearchCondition;
import com.tickatch.user_service.customer.domain.vo.CustomerGrade;
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
 * Customer 리포지토리 구현체.
 *
 * <p>JPA와 QueryDSL을 사용하여 Customer 데이터를 조회/저장한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepository {

  private final CustomerJpaRepository customerJpaRepository;
  private final JPAQueryFactory queryFactory;

  @Override
  public Customer save(Customer customer) {
    return customerJpaRepository.save(customer);
  }

  @Override
  public Optional<Customer> findById(UUID id) {
    return customerJpaRepository.findById(id);
  }

  @Override
  public Optional<Customer> findByEmail(String email) {
    return customerJpaRepository.findByEmail(email);
  }

  @Override
  public boolean existsByEmail(String email) {
    return customerJpaRepository.existsByEmail(email);
  }

  @Override
  public Page<Customer> findAllByCondition(CustomerSearchCondition condition, Pageable pageable) {
    List<Customer> content =
        queryFactory
            .selectFrom(customer)
            .where(
                emailContains(condition.getEmail()),
                nameContains(condition.getName()),
                phoneContains(condition.getPhone()),
                statusEq(condition.getStatus()),
                gradeEq(condition.getGrade()))
            .orderBy(getOrderSpecifiers(pageable.getSort()))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    JPAQuery<Long> countQuery =
        queryFactory
            .select(customer.count())
            .from(customer)
            .where(
                emailContains(condition.getEmail()),
                nameContains(condition.getName()),
                phoneContains(condition.getPhone()),
                statusEq(condition.getStatus()),
                gradeEq(condition.getGrade()));

    return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
  }

  private BooleanExpression emailContains(String email) {
    return StringUtils.hasText(email) ? customer.email.containsIgnoreCase(email) : null;
  }

  private BooleanExpression nameContains(String name) {
    return StringUtils.hasText(name) ? customer.profile.name.containsIgnoreCase(name) : null;
  }

  private BooleanExpression phoneContains(String phone) {
    return StringUtils.hasText(phone) ? customer.profile.phone.contains(phone) : null;
  }

  private BooleanExpression statusEq(UserStatus status) {
    return status != null ? customer.status.eq(status) : null;
  }

  private BooleanExpression gradeEq(CustomerGrade grade) {
    return grade != null ? customer.grade.eq(grade) : null;
  }

  private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
    List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

    sort.forEach(
        order -> {
          Order direction = order.isAscending() ? Order.ASC : Order.DESC;
          String property = order.getProperty();

          OrderSpecifier<?> orderSpecifier =
              switch (property) {
                case "email" -> new OrderSpecifier<>(direction, customer.email);
                case "name" -> new OrderSpecifier<>(direction, customer.profile.name);
                case "status" -> new OrderSpecifier<>(direction, customer.status);
                case "grade" -> new OrderSpecifier<>(direction, customer.grade);
                case "createdAt" -> new OrderSpecifier<>(direction, customer.createdAt);
                case "updatedAt" -> new OrderSpecifier<>(direction, customer.updatedAt);
                default -> new OrderSpecifier<>(direction, customer.createdAt);
              };
          orderSpecifiers.add(orderSpecifier);
        });

    if (orderSpecifiers.isEmpty()) {
      orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, customer.createdAt));
    }

    return orderSpecifiers.toArray(new OrderSpecifier[0]);
  }
}
