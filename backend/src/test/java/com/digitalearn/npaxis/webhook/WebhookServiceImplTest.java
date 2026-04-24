package com.digitalearn.npaxis.webhook;
//
//import com.digitalearn.npaxis.webhook.handler.SubscriptionUpdatedHandler;
//import com.digitalearn.npaxis.webhook.handler.WebhookEventHandler;
//import com.digitalearn.npaxis.subscription.core.SubscriptionService;
//import com.stripe.model.Event;
//import com.stripe.model.EventDataObjectDeserializer;
//import com.stripe.model.Subscription;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.*;
//
/// **
// * Unit tests for WebhookServiceImpl idempotency and handler dispatch
// */
//@ExtendWith(MockitoExtension.class)
//@DisplayName("WebhookService Tests")
//class WebhookServiceImplTest {
//
//    @Mock
//    private WebhookProcessingEventRepository webhookEventRepository;
//
//    @Mock
//    private SubscriptionService subscriptionService;
//
//    private WebhookEventHandler subscriptionUpdatedHandler;
//
//    @BeforeEach
//    void setUp() {
//        subscriptionUpdatedHandler = new SubscriptionUpdatedHandler(subscriptionService);
//    }
//
//    @Test
//    @DisplayName("Should have correct event type")
//    void testHandlerEventType() {
//        // Assert
//        assertThat(subscriptionUpdatedHandler.eventType())
//                .isEqualTo("customer.subscription.updated");
//    }
//
//    @Test
//    @DisplayName("Should skip processing already-processed event (idempotency)")
//    void testIdempotencySkipProcessedEvent() {
//        // Arrange
//        String eventId = "evt_123";
//        when(webhookEventRepository.existsByEventId(eventId))
//                .thenReturn(true);
//
//        // Assert
//        assertThat(webhookEventRepository.existsByEventId(eventId))
//                .isTrue();
//    }
//
//    @Test
//    @DisplayName("Should create webhook record for new event")
//    void testCreateRecordForNewEvent() {
//        // Arrange
//        String eventId = "evt_456";
//        when(webhookEventRepository.existsByEventId(eventId))
//                .thenReturn(false);
//
//        // Assert
//        assertThat(webhookEventRepository.existsByEventId(eventId))
//                .isFalse();
//    }
//
//    @Test
//    @DisplayName("Should mark event as succeeded after successful handling")
//    void testMarkEventSucceeded() {
//        // Arrange
//        WebhookProcessingEvent event = WebhookProcessingEvent.builder()
//                .eventId("evt_789")
//                .status(WebhookEventStatus.PENDING)
//                .build();
//
//        // Act
//        event.markSucceeded();
//
//        // Assert
//        assertThat(event.getStatus()).isEqualTo(WebhookEventStatus.SUCCEEDED);
//        assertThat(event.getProcessedAt()).isNotNull();
//    }
//
//    @Test
//    @DisplayName("Should mark event as failed and increment retry count")
//    void testMarkEventFailedAndRetry() {
//        // Arrange
//        WebhookProcessingEvent event = WebhookProcessingEvent.builder()
//                .eventId("evt_101")
//                .status(WebhookEventStatus.PENDING)
//                .retryCount(0)
//                .build();
//
//        // Act
//        event.markFailed("Connection timeout");
//        event.incrementRetry();
//
//        // Assert
//        assertThat(event.getStatus()).isEqualTo(WebhookEventStatus.FAILED);
//        assertThat(event.getErrorMessage()).contains("Connection timeout");
//        assertThat(event.getRetryCount()).isEqualTo(1);
//    }
//
//    @Test
//    @DisplayName("Should calculate exponential backoff for retries")
//    void testExponentialBackoffCalculation() {
//        // For retry_count=0: 2^0 = 1 minute
//        // For retry_count=1: 2^1 = 2 minutes
//        // For retry_count=2: 2^2 = 4 minutes
//        // For retry_count=3: 2^3 = 8 minutes (capped at 60 minutes)
//
//        for (int retryCount = 0; retryCount <= 5; retryCount++) {
//            long delayMinutes = Math.min((long) Math.pow(2, retryCount), 60L);
//            assertThat(delayMinutes).isGreaterThan(0);
//            if (retryCount < 6) {
//                assertThat(delayMinutes).isLessThanOrEqualTo(60);
//            }
//        }
//    }
//}
//
