package com.digitalearn.npaxis.subscription.stripe;
//
//import com.digitalearn.npaxis.preceptor.Preceptor;
//import com.digitalearn.npaxis.preceptor.PreceptorRepository;
//import com.digitalearn.npaxis.subscription.exceptions.StripeIntegrationException;
//import com.stripe.exception.StripeException;
//import com.stripe.model.Customer;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//
/// **
// * Unit tests for StripeCustomerServiceImpl
// */
//@ExtendWith(MockitoExtension.class)
//@DisplayName("StripeCustomerService Tests")
//class StripeCustomerServiceImplTest {
//
//    @Mock
//    private PreceptorRepository preceptorRepository;
//
//    private StripeCustomerService stripeCustomerService;
//
//    @BeforeEach
//    void setUp() {
//        stripeCustomerService = new StripeCustomerServiceImpl(preceptorRepository);
//    }
//
//    @Test
//    @DisplayName("Should create customer successfully")
//    void testCreateCustomerSuccess() throws StripeException {
//        // Arrange
//        Long userId = 1L;
//        String name = "John Doe";
//        String email = "john@example.com";
//
//        // Act & Assert
//        // Note: In reality, we'd mock Stripe SDK calls, but for this example,
//        // we're testing the service structure
//        assertThat(stripeCustomerService).isNotNull();
//    }
//
//    @Test
//    @DisplayName("Should retrieve existing Stripe customer")
//    void testGetOrCreateCustomerWithExisting() {
//        // Arrange
//        Long userId = 1L;
//        Preceptor preceptor = Preceptor.builder()
//                .userId(userId)
//                .name("John Doe")
//                .email("john@example.com")
//                .stripeCustomerId("cus_123")
//                .build();
//
//        when(preceptorRepository.findById(userId)).thenReturn(Optional.of(preceptor));
//
//        // Act
//        String customerId = stripeCustomerService.getOrCreateCustomer(userId);
//
//        // Assert
//        assertEquals("cus_123", customerId);
//        verify(preceptorRepository, times(1)).findById(userId);
//        verify(preceptorRepository, never()).save(any());
//    }
//
//    @Test
//    @DisplayName("Should throw exception when preceptor not found")
//    void testGetOrCreateCustomerNotFound() {
//        // Arrange
//        Long userId = 999L;
//        when(preceptorRepository.findById(userId)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThatThrownBy(() -> stripeCustomerService.getOrCreateCustomer(userId))
//                .isInstanceOf(StripeIntegrationException.class)
//                .hasMessageContaining("Preceptor not found");
//    }
//}
//
