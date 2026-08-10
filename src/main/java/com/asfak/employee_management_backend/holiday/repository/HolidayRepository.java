package com.asfak.employee_management_backend.holiday.repository;

import com.asfak.employee_management_backend.holiday.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface HolidayRepository
        extends JpaRepository<Holiday, Long> {

    boolean existsByHolidayDate(
            LocalDate holidayDate
    );

    boolean existsByHolidayDateAndIdNot(
            LocalDate holidayDate,
            Long id
    );

    List<Holiday> findAllByOrderByHolidayDateAsc();

    List<Holiday> findByHolidayDateGreaterThanEqualOrderByHolidayDateAsc(
            LocalDate date
    );
}