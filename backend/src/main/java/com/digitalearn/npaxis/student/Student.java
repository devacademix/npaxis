package com.digitalearn.npaxis.student;

import com.digitalearn.npaxis.auditing.BaseEntity;
import com.digitalearn.npaxis.preceptor.Preceptor;
import com.digitalearn.npaxis.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Student extends BaseEntity {

    @Id
    private Long userId; // PK is also the FK to the users table

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // Tells Hibernate to use the userId as both PK and FK
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 100)
    private String university;

    @Column(length = 100)
    private String program;

    @Column(name = "graduation_year", length = 4)
    private String graduationYear;

    @Column(length = 20)
    private String phone;

    // --- The Many-to-Many Bookmark Relationship ---
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "student_saved_preceptors",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "preceptor_id")
    )
    @Builder.Default // Ensures Lombok's builder doesn't overwrite our empty HashSet with null
    private Set<Preceptor> savedPreceptors = new HashSet<>();
}