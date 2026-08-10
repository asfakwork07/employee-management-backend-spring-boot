package com.asfak.employee_management_backend.holiday.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "holidays",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "holiday_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "holiday_date",
            nullable = false,
            unique = true
    )
    private LocalDate holidayDate;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    private String description;
}