package com.digitalearn.npaxis.user;

import com.digitalearn.npaxis.auditing.BaseEntity;
import com.digitalearn.npaxis.role.Role;
import jakarta.annotation.Nonnull;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.Collection;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"email"}),
        },
        indexes = {
                @Index(name = "idx_user_email", columnList = "email", unique = true)
        }
)
@EntityListeners(AuditingEntityListener.class)
public class User extends BaseEntity implements UserDetails, Principal {

    /**
     * Unique identifier for the entity.
     * This field is automatically generated and serves as the primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long userId;

    /**
     * The user's unique email address.
     */
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /**
     * The user's hashed password.
     */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "is_email_verified")
    private boolean isEmailVerified = false;
    /**
     * The user's display name.
     */
    @Column(name = "display_name", nullable = false)
    private String displayName;

    /**
     * The user's profile photo URL.
     */
    @Column(name = "photo_url")
    private String photoUrl;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Builder.Default
    private boolean accountLocked = false;

    @Builder.Default
    private boolean accountEnabled = false;

    @Override
    public String getName() {
        return this.displayName;
    }

    @Override
    @Nonnull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Set.of(
                new SimpleGrantedAuthority(role.getRoleName().name())
        );
    }

    @Override
    @Nonnull
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.accountEnabled && this.isEmailVerified;
    }
}