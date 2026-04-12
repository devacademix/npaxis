package com.digitalearn.npaxis.subscription.price;

import com.digitalearn.npaxis.auditing.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionPriceRepository extends BaseRepository<SubscriptionPrice, Long> {
}