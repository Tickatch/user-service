package com.tickatch.user_service.seller.domain.repository;

import static com.tickatch.user_service.seller.domain.QSeller.seller;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tickatch.user_service.common.domain.vo.UserStatus;
import com.tickatch.user_service.seller.domain.Seller;
import com.tickatch.user_service.seller.domain.SellerRepository;
import com.tickatch.user_service.seller.domain.repository.dto.SellerSearchCondition;
import com.tickatch.user_service.seller.domain.vo.SellerStatus;
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
 * Seller 리포지토리 구현체.
 *
 * <p>JPA와 QueryDSL을 사용하여 Seller 데이터를 조회/저장한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class SellerRepositoryImpl implements SellerRepository {

  private final SellerJpaRepository sellerJpaRepository;
  private final JPAQueryFactory queryFactory;

  @Override
  public Seller save(Seller seller) {
    return sellerJpaRepository.save(seller);
  }

  @Override
  public Optional<Seller> findById(UUID id) {
    return sellerJpaRepository.findById(id);
  }

  @Override
  public Optional<Seller> findByEmail(String email) {
    return sellerJpaRepository.findByEmail(email);
  }

  @Override
  public boolean existsByEmail(String email) {
    return sellerJpaRepository.existsByEmail(email);
  }

  @Override
  public boolean existsByBusinessNumber(String businessNumber) {
    return sellerJpaRepository.existsByBusinessNumber(businessNumber);
  }

  @Override
  public Page<Seller> findAllByCondition(SellerSearchCondition condition, Pageable pageable) {
    List<Seller> content = queryFactory
        .selectFrom(seller)
        .where(
            emailContains(condition.getEmail()),
            nameContains(condition.getName()),
            statusEq(condition.getStatus()),
            sellerStatusEq(condition.getSellerStatus()),
            businessNameContains(condition.getBusinessName()),
            businessNumberEq(condition.getBusinessNumber()))
        .orderBy(getOrderSpecifiers(pageable.getSort()))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(seller.count())
        .from(seller)
        .where(
            emailContains(condition.getEmail()),
            nameContains(condition.getName()),
            statusEq(condition.getStatus()),
            sellerStatusEq(condition.getSellerStatus()),
            businessNameContains(condition.getBusinessName()),
            businessNumberEq(condition.getBusinessNumber()));

    return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
  }

  private BooleanExpression emailContains(String email) {
    return StringUtils.hasText(email) ? seller.email.containsIgnoreCase(email) : null;
  }

  private BooleanExpression nameContains(String name) {
    return StringUtils.hasText(name) ? seller.profile.name.containsIgnoreCase(name) : null;
  }

  private BooleanExpression statusEq(UserStatus status) {
    return status != null ? seller.status.eq(status) : null;
  }

  private BooleanExpression sellerStatusEq(SellerStatus sellerStatus) {
    return sellerStatus != null ? seller.sellerStatus.eq(sellerStatus) : null;
  }

  private BooleanExpression businessNameContains(String businessName) {
    return StringUtils.hasText(businessName)
        ? seller.businessInfo.businessName.containsIgnoreCase(businessName) : null;
  }

  private BooleanExpression businessNumberEq(String businessNumber) {
    return StringUtils.hasText(businessNumber)
        ? seller.businessInfo.businessNumber.eq(businessNumber) : null;
  }

  private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
    List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

    sort.forEach(order -> {
      Order direction = order.isAscending() ? Order.ASC : Order.DESC;
      String property = order.getProperty();

      OrderSpecifier<?> orderSpecifier = switch (property) {
        case "email" -> new OrderSpecifier<>(direction, seller.email);
        case "name" -> new OrderSpecifier<>(direction, seller.profile.name);
        case "status" -> new OrderSpecifier<>(direction, seller.status);
        case "sellerStatus" -> new OrderSpecifier<>(direction, seller.sellerStatus);
        case "businessName" -> new OrderSpecifier<>(direction, seller.businessInfo.businessName);
        case "approvedAt" -> new OrderSpecifier<>(direction, seller.approvedAt);
        case "createdAt" -> new OrderSpecifier<>(direction, seller.createdAt);
        case "updatedAt" -> new OrderSpecifier<>(direction, seller.updatedAt);
        default -> new OrderSpecifier<>(direction, seller.createdAt);
      };
      orderSpecifiers.add(orderSpecifier);
    });

    if (orderSpecifiers.isEmpty()) {
      orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, seller.createdAt));
    }

    return orderSpecifiers.toArray(new OrderSpecifier[0]);
  }
}