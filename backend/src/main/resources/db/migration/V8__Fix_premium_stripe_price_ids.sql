UPDATE subscription_prices
SET stripe_price_id = 'price_1TKzm2GvbFqTmqQHV78dUPqK'
WHERE stripe_price_id = 'local-monthly-price'
  AND billing_interval = 'MONTHLY';

UPDATE subscription_prices
SET stripe_price_id = 'price_1TKzm2GvbFqTmqQH0J4ZpWsq'
WHERE stripe_price_id = 'local-yearly-price'
  AND billing_interval = 'YEARLY';
