package com.tickatch.user_service.seller.application.service.query;

import com.tickatch.user_service.seller.application.service.query.dto.SellerResponse;
import com.tickatch.user_service.seller.application.service.query.dto.SellerSearchRequest;
import com.tickatch.user_service.seller.domain.Seller;
import com.tickatch.user_service.seller.domain.SellerRepository;
import com.tickatch.user_service.seller.domain.exception.SellerErrorCode;
import com.tickatch.user_service.seller.domain.exception.SellerException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 판매자 조회 서비스.
 *
 * <p>판매자 조회 작업을 처리한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerQueryService {

  private final SellerRepository sellerRepository;

  /**
   * ID로 판매자를 조회한다.
   *
   * @param sellerId 판매자 ID
   * @return 판매자 응답
   * @throws SellerException 판매자를 찾을 수 없는 경우
   */
  public SellerResponse getSeller(UUID sellerId) {
    Seller seller =
        sellerRepository
            .findById(sellerId)
            .orElseThrow(() -> new SellerException(SellerErrorCode.SELLER_NOT_FOUND));
    return SellerResponse.from(seller);
  }

  /**
   * 이메일로 판매자를 조회한다.
   *
   * @param email 이메일
   * @return 판매자 응답
   * @throws SellerException 판매자를 찾을 수 없는 경우
   */
  public SellerResponse getSellerByEmail(String email) {
    Seller seller =
        sellerRepository
            .findByEmail(email)
            .orElseThrow(() -> new SellerException(SellerErrorCode.SELLER_NOT_FOUND));
    return SellerResponse.from(seller);
  }

  /**
   * 조건에 맞는 판매자 목록을 페이징하여 조회한다.
   *
   * @param request 검색 요청
   * @param pageable 페이징 정보
   * @return 페이징된 판매자 응답 목록
   */
  public Page<SellerResponse> searchSellers(SellerSearchRequest request, Pageable pageable) {
    return sellerRepository
        .findAllByCondition(request.toCondition(), pageable)
        .map(SellerResponse::from);
  }

  /**
   * 이메일 존재 여부를 확인한다.
   *
   * @param email 이메일
   * @return 존재하면 true
   */
  public boolean existsByEmail(String email) {
    return sellerRepository.existsByEmail(email);
  }

  /**
   * 사업자등록번호 존재 여부를 확인한다.
   *
   * @param businessNumber 사업자등록번호
   * @return 존재하면 true
   */
  public boolean existsByBusinessNumber(String businessNumber) {
    return sellerRepository.existsByBusinessNumber(businessNumber);
  }
}
