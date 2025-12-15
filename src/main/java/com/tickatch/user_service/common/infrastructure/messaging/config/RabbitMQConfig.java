package com.tickatch.user_service.common.infrastructure.messaging.config;

import io.github.tickatch.common.util.JsonUtils;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * User Service RabbitMQ 설정.
 *
 * <p>User 서비스에서 발행하는 이벤트를 위한 Exchange, Queue, Binding을 정의한다.
 *
 * <p>구성 요소:
 * <ul>
 *   <li>Exchange: tickatch.user (Topic)
 *   <li>Routing Keys: customer.*, seller.*, admin.*
 *   <li>수신 서비스: Auth Service
 * </ul>
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Configuration
public class RabbitMQConfig {

  @Value("${messaging.exchange.user:tickatch.user}")
  private String userExchange;

  // ========================================
  // Routing Keys
  // ========================================

  /** 고객 탈퇴 라우팅 키 */
  public static final String ROUTING_KEY_CUSTOMER_WITHDRAWN = "customer.withdrawn";

  /** 고객 정지 라우팅 키 */
  public static final String ROUTING_KEY_CUSTOMER_SUSPENDED = "customer.suspended";

  /** 고객 활성화 라우팅 키 */
  public static final String ROUTING_KEY_CUSTOMER_ACTIVATED = "customer.activated";

  /** 판매자 탈퇴 라우팅 키 */
  public static final String ROUTING_KEY_SELLER_WITHDRAWN = "seller.withdrawn";

  /** 판매자 정지 라우팅 키 */
  public static final String ROUTING_KEY_SELLER_SUSPENDED = "seller.suspended";

  /** 판매자 활성화 라우팅 키 */
  public static final String ROUTING_KEY_SELLER_ACTIVATED = "seller.activated";

  /** 관리자 탈퇴 라우팅 키 */
  public static final String ROUTING_KEY_ADMIN_WITHDRAWN = "admin.withdrawn";

  /** 관리자 정지 라우팅 키 */
  public static final String ROUTING_KEY_ADMIN_SUSPENDED = "admin.suspended";

  /** 관리자 활성화 라우팅 키 */
  public static final String ROUTING_KEY_ADMIN_ACTIVATED = "admin.activated";

  // ========================================
  // Queue Names (Auth Service 수신용)
  // ========================================

  /** Auth Service용 탈퇴 이벤트 큐 */
  public static final String QUEUE_USER_WITHDRAWN_AUTH = "tickatch.user.withdrawn.auth.queue";

  /** Auth Service용 정지 이벤트 큐 */
  public static final String QUEUE_USER_SUSPENDED_AUTH = "tickatch.user.suspended.auth.queue";

  /** Auth Service용 활성화 이벤트 큐 */
  public static final String QUEUE_USER_ACTIVATED_AUTH = "tickatch.user.activated.auth.queue";

  // ========================================
  // Exchange
  // ========================================

  /**
   * User 도메인 이벤트용 Topic Exchange를 생성한다.
   *
   * @return durable Topic Exchange
   */
  @Bean
  public TopicExchange userExchange() {
    return ExchangeBuilder.topicExchange(userExchange).durable(true).build();
  }

  // ========================================
  // Queues
  // ========================================

  /**
   * Auth Service용 탈퇴 이벤트 큐를 생성한다.
   *
   * @return DLQ 설정이 포함된 durable Queue
   */
  @Bean
  public Queue userWithdrawnAuthQueue() {
    return QueueBuilder.durable(QUEUE_USER_WITHDRAWN_AUTH)
        .withArgument("x-dead-letter-exchange", userExchange + ".dlx")
        .withArgument("x-dead-letter-routing-key", "dlq.withdrawn")
        .build();
  }

  /**
   * Auth Service용 정지 이벤트 큐를 생성한다.
   *
   * @return DLQ 설정이 포함된 durable Queue
   */
  @Bean
  public Queue userSuspendedAuthQueue() {
    return QueueBuilder.durable(QUEUE_USER_SUSPENDED_AUTH)
        .withArgument("x-dead-letter-exchange", userExchange + ".dlx")
        .withArgument("x-dead-letter-routing-key", "dlq.suspended")
        .build();
  }

  /**
   * Auth Service용 활성화 이벤트 큐를 생성한다.
   *
   * @return DLQ 설정이 포함된 durable Queue
   */
  @Bean
  public Queue userActivatedAuthQueue() {
    return QueueBuilder.durable(QUEUE_USER_ACTIVATED_AUTH)
        .withArgument("x-dead-letter-exchange", userExchange + ".dlx")
        .withArgument("x-dead-letter-routing-key", "dlq.activated")
        .build();
  }

  // ========================================
  // Bindings - 탈퇴 이벤트
  // ========================================

  @Bean
  public Binding customerWithdrawnBinding(Queue userWithdrawnAuthQueue, TopicExchange userExchange) {
    return BindingBuilder.bind(userWithdrawnAuthQueue)
        .to(userExchange)
        .with(ROUTING_KEY_CUSTOMER_WITHDRAWN);
  }

  @Bean
  public Binding sellerWithdrawnBinding(Queue userWithdrawnAuthQueue, TopicExchange userExchange) {
    return BindingBuilder.bind(userWithdrawnAuthQueue)
        .to(userExchange)
        .with(ROUTING_KEY_SELLER_WITHDRAWN);
  }

  @Bean
  public Binding adminWithdrawnBinding(Queue userWithdrawnAuthQueue, TopicExchange userExchange) {
    return BindingBuilder.bind(userWithdrawnAuthQueue)
        .to(userExchange)
        .with(ROUTING_KEY_ADMIN_WITHDRAWN);
  }

  // ========================================
  // Bindings - 정지 이벤트
  // ========================================

  @Bean
  public Binding customerSuspendedBinding(Queue userSuspendedAuthQueue, TopicExchange userExchange) {
    return BindingBuilder.bind(userSuspendedAuthQueue)
        .to(userExchange)
        .with(ROUTING_KEY_CUSTOMER_SUSPENDED);
  }

  @Bean
  public Binding sellerSuspendedBinding(Queue userSuspendedAuthQueue, TopicExchange userExchange) {
    return BindingBuilder.bind(userSuspendedAuthQueue)
        .to(userExchange)
        .with(ROUTING_KEY_SELLER_SUSPENDED);
  }

  @Bean
  public Binding adminSuspendedBinding(Queue userSuspendedAuthQueue, TopicExchange userExchange) {
    return BindingBuilder.bind(userSuspendedAuthQueue)
        .to(userExchange)
        .with(ROUTING_KEY_ADMIN_SUSPENDED);
  }

  // ========================================
  // Bindings - 활성화 이벤트
  // ========================================

  @Bean
  public Binding customerActivatedBinding(Queue userActivatedAuthQueue, TopicExchange userExchange) {
    return BindingBuilder.bind(userActivatedAuthQueue)
        .to(userExchange)
        .with(ROUTING_KEY_CUSTOMER_ACTIVATED);
  }

  @Bean
  public Binding sellerActivatedBinding(Queue userActivatedAuthQueue, TopicExchange userExchange) {
    return BindingBuilder.bind(userActivatedAuthQueue)
        .to(userExchange)
        .with(ROUTING_KEY_SELLER_ACTIVATED);
  }

  @Bean
  public Binding adminActivatedBinding(Queue userActivatedAuthQueue, TopicExchange userExchange) {
    return BindingBuilder.bind(userActivatedAuthQueue)
        .to(userExchange)
        .with(ROUTING_KEY_ADMIN_ACTIVATED);
  }

  // ========================================
  // Dead Letter Exchange & Queues
  // ========================================

  @Bean
  public TopicExchange userDeadLetterExchange() {
    return ExchangeBuilder.topicExchange(userExchange + ".dlx").durable(true).build();
  }

  @Bean
  public Queue userWithdrawnDlq() {
    return QueueBuilder.durable(QUEUE_USER_WITHDRAWN_AUTH + ".dlq").build();
  }

  @Bean
  public Queue userSuspendedDlq() {
    return QueueBuilder.durable(QUEUE_USER_SUSPENDED_AUTH + ".dlq").build();
  }

  @Bean
  public Queue userActivatedDlq() {
    return QueueBuilder.durable(QUEUE_USER_ACTIVATED_AUTH + ".dlq").build();
  }

  @Bean
  public Binding userWithdrawnDlqBinding(Queue userWithdrawnDlq, TopicExchange userDeadLetterExchange) {
    return BindingBuilder.bind(userWithdrawnDlq).to(userDeadLetterExchange).with("dlq.withdrawn");
  }

  @Bean
  public Binding userSuspendedDlqBinding(Queue userSuspendedDlq, TopicExchange userDeadLetterExchange) {
    return BindingBuilder.bind(userSuspendedDlq).to(userDeadLetterExchange).with("dlq.suspended");
  }

  @Bean
  public Binding userActivatedDlqBinding(Queue userActivatedDlq, TopicExchange userDeadLetterExchange) {
    return BindingBuilder.bind(userActivatedDlq).to(userDeadLetterExchange).with("dlq.activated");
  }

  // ========================================
  // Message Converter & Template
  // ========================================

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter(JsonUtils.getObjectMapper());
  }

  @Bean
  public RabbitTemplate rabbitTemplate(
      ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(jsonMessageConverter);
    return rabbitTemplate;
  }
}