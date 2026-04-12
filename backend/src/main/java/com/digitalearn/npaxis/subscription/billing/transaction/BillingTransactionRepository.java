package com.digitalearn.npaxis.subscription.billing.transaction;

import com.digitalearn.npaxis.auditing.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillingTransactionRepository extends BaseRepository<BillingTransaction, Long> {
}