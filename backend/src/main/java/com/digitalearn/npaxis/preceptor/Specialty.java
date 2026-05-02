package com.digitalearn.npaxis.preceptor;

import com.digitalearn.npaxis.auditing.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a Medical Specialty (e.g., Cardiology, Pediatrics, Surgery).
 * Specialties are case-insensitive and normalized to uppercase.
 */
@Entity
@Table(name = "specialties", indexes = {
        @Index(name = "idx_specialty_name_normalized", columnList = "name_normalized", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Specialty extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Original specialty name (e.g., "Cardiology", "Pediatrics")
     */
    @Column(length = 100, nullable = false)
    private String name;

    /**
     * Normalized name for case-insensitive matching (stored as uppercase).
     * This ensures "Cardiology", "cardiology", "CARDIOLOGY" are treated as duplicates.
     */
    @Column(length = 100, nullable = false)
    private String nameNormalized;

    /**
     * Optional description of the specialty
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Flag to indicate if this is a system-defined specialty
     */
    @Column(name = "is_predefined", nullable = false)
    @Builder.Default
    private boolean isPredefined = false;

    @ManyToMany(mappedBy = "specialties")
    @Builder.Default
    private Set<Preceptor> preceptors = new HashSet<>();

    /**
     * Set the name and automatically normalize it
     */
    public void setName(String name) {
        this.name = name;
        this.nameNormalized = name != null ? name.toUpperCase() : null;
    }

    @Override
    public String toString() {
        return name;
    }
}

