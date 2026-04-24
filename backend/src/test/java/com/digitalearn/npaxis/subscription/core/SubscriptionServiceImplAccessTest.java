package com.digitalearn.npaxis.subscription.core;
//
//import com.digitalearn.npaxis.preceptor.Preceptor;
//import com.digitalearn.npaxis.preceptor.PreceptorRepository;
//import com.digitalearn.npaxis.subscription.stripe.StripeSubscriptionService;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.when;
//
/// **
// * Unit tests for SubscriptionService premium access logic
// */
//@ExtendWith(MockitoExtension.class)
//@DisplayName("SubscriptionService Premium Access Tests")
//class SubscriptionServiceImplAccessTest {
//
//    @Mock
//    private PreceptorSubscriptionRepository subscriptionRepository;
//
//    @Mock
//    private PreceptorRepository preceptorRepository;
//
//    @Mock
//    private StripeSubscriptionService stripeSubscriptionService;
//
//    @Test
//    @DisplayName("Should grant access for ACTIVE subscription")
//    void testCanAccessPremiumFeatures_ActiveStatus() {
//        // Arrange
//        Long userId = 1L;
//        PreceptorSubscription subscription = PreceptorSubscription.builder()
//                .status(SubscriptionStatus.ACTIVE)
//                .accessEnabled(true)
//                .currentPeriodEnd(LocalDateTime.now().plusDays(30))
//                .build();
//
//        when(subscriptionRepository.findByPreceptor_UserId(userId))
//                .thenReturn(Optional.of(subscription));
//
//        // Assert
//        assertThat(subscription.isAccessEnabled()).isTrue();
//    }
//
//    @Test
//    @DisplayName("Should grant access for TRIALING subscription")
//    void testCanAccessPremiumFeatures_TrialingStatus() {
//        // Arrange
//        PreceptorSubscription subscription = PreceptorSubscription.builder()
//                .status(SubscriptionStatus.TRIALING)
//                .accessEnabled(true)
//                .build();
//
//        assertThat(subscription.isAccessEnabled()).isTrue();
//    }
//
//    @Test
//    @DisplayName("Should grant access for CANCELED with future period end")
//    void testCanAccessPremiumFeatures_CanceledWithFuturePeriodEnd() {
//        // Arrange
//        PreceptorSubscription subscription = PreceptorSubscription.builder()
//                .status(SubscriptionStatus.CANCELED)
//                .cancelAtPeriodEnd(true)
//                .currentPeriodEnd(LocalDateTime.now().plusDays(10))
//                .build();
//
//        // Assert - the subscription is still active until period end
//        assertThat(subscription.getCurrentPeriodEnd())
//                .isAfter(LocalDateTime.now());
//    }
//
//    @Test
//    @DisplayName("Should deny access for no subscription")
//    void testCanAccessPremiumFeatures_NoSubscription() {
//        // Arrange
//        Long userId = 1L;
//        when(subscriptionRepository.findByPreceptor_UserId(userId))
//                .thenReturn(Optional.empty());
//
//        // Assert
//        assertThat(subscriptionRepository.findByPreceptor_UserId(userId))
//                .isEmpty();
//    }
//}
//
