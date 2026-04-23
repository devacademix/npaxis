package com.digitalearn.npaxis.preceptor;

import com.digitalearn.npaxis.auditing.BaseEntity;
import com.digitalearn.npaxis.user.User;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "preceptors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Preceptor extends BaseEntity {

    @Id
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 100)
    private String name;

    @Column(length = 255)
    private String credentials;

    @Column(length = 100)
    private String specialty;

    @Column(length = 150)
    private String location;

    @Column(length = 100)
    private String setting;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "preceptor_available_days",
            joinColumns = @JoinColumn(name = "preceptor_id"))
    @Column(name = "day")
    private Set<DayOfWeekEnum> availableDays;

    @Column(length = 100)
    private String honorarium;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    // --- Monetization & Access Flags ---

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    @Column(name = "is_premium", nullable = false)
    private boolean isPremium = false;

    // --- Licensing & Compliance ---

    @Column(name = "license_number", length = 100)
    private String licenseNumber;

    @Column(name = "license_state", length = 50)
    private String licenseState;

    @Column(name = "license_file_url", length = 500)
    private String licenseFileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", length = 20)
    private VerificationStatus verificationStatus = VerificationStatus.NOT_SUBMITTED;

    @Column(name = "verification_submitted_at")
    private LocalDateTime verificationSubmittedAt;

    @Column(name = "verification_reviewed_at")
    private LocalDateTime verificationReviewedAt;

    @Column(name = "stripe_customer_id", length = 100)
    private String stripeCustomerId;

    @Column(name = "stripe_subscription_id", length = 100)
    private String stripeSubscriptionId;
}