package com.digitalearn.npaxis.subscription.billing.transaction;

import com.digitalearn.npaxis.auditing.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillingTransactionRepository extends BaseRepository<BillingTransaction, Long> {

    Optional<BillingTransaction> findByStripePaymentIntentId(String stripePaymentIntentId);

    Optional<BillingTransaction> findByStripeInvoiceId(String stripeInvoiceId);

    Page<BillingTransaction> findByPreceptor_UserIdOrderByTransactionAtDesc(Long preceptorId, Pageable pageable);

    long countByStatus(TransactionStatus status);

    Page<BillingTransaction> findAllByOrderByTransactionAtDesc(Pageable pageable);

    @Query("SELECT COALESCE(AVG(CAST(bt.amountInMinorUnits AS DOUBLE)), 0) FROM BillingTransaction bt WHERE bt.status = 'SUCCEEDED'")
    Double getAverageTransactionAmount();

    @Query("SELECT bt FROM BillingTransaction bt LEFT JOIN FETCH bt.preceptor p ORDER BY bt.transactionAt DESC")
    Page<BillingTransaction> findAllWithPreceptorOrderByTransactionAtDesc(Pageable pageable);
}
