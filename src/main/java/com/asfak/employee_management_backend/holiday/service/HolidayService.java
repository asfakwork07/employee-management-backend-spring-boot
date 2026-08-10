package com.asfak.employee_management_backend.holiday.service;

import com.asfak.employee_management_backend.holiday.dto.HolidayRequest;
import com.asfak.employee_management_backend.holiday.dto.HolidayResponse;

import java.util.List;

public interface HolidayService {

    HolidayResponse createHoliday(
            HolidayRequest request
    );

    List<HolidayResponse> getAllHolidays();

    List<HolidayResponse> getUpcomingHolidays();

    HolidayResponse updateHoliday(
            Long id,
            HolidayRequest request
    );

    void deleteHoliday(
            Long id
    );
}