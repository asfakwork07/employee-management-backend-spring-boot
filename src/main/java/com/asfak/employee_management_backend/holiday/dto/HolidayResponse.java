package com.asfak.employee_management_backend.holiday.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolidayResponse {

    private Long id;

    private LocalDate holidayDate;

    private String name;

    private String type;

    private String description;
}