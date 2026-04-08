package com.digitalearn.npaxis.subscription.entity;

import com.digitalearn.npaxis.auditing.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "webhook_events")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class WebhookEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "stripe_event_id", nullable = false, unique = true, length = 100)
    private String stripeEventId;

    @Column(name = "type", nullable = false, length = 100)
    private String type;

    @Lob
    @Column(name = "payload", nullable = false)
    private String payload;

    @Column(name = "processed", nullable = false)
    private boolean processed = false;
}
