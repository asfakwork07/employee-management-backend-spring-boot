package com.asfak.employee_management_backend.holiday.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class HolidayRequest {

    private LocalDate holidayDate;

    private String name;

    private String type;

    private String description;
}