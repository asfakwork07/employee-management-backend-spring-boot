package com.asfak.employee_management_backend.ai.entity;

import com.asfak.employee_management_backend.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ai_performance_summaries",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_employee_month_year",
                        columnNames = {
                                "employee_id",
                                "summary_month",
                                "summary_year"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "employee_id",
            nullable = false
    )
    private Employee employee;

    @Column(
            name = "summary_month",
            nullable = false
    )
    private Integer month;

    @Column(
            name = "summary_year",
            nullable = false
    )
    private Integer year;

    @Column(
            name = "summary",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String summary;

    @Column(
            name = "generated_at",
            nullable = false
    )
    private LocalDateTime generatedAt;

    @PrePersist
    public void onCreate() {

        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }
}