package com.asfak.employee_management_backend.holiday.controller;

import com.asfak.employee_management_backend.holiday.dto.HolidayRequest;
import com.asfak.employee_management_backend.holiday.dto.HolidayResponse;
import com.asfak.employee_management_backend.holiday.service.HolidayService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayService holidayService;

    @PostMapping
    public ResponseEntity<HolidayResponse> createHoliday(
            @RequestBody HolidayRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        holidayService.createHoliday(request)
                );
    }

    @GetMapping
    public ResponseEntity<List<HolidayResponse>>
    getAllHolidays() {

        return ResponseEntity.ok(
                holidayService.getAllHolidays()
        );
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<HolidayResponse>>
    getUpcomingHolidays() {

        return ResponseEntity.ok(
                holidayService.getUpcomingHolidays()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<HolidayResponse> updateHoliday(
            @PathVariable Long id,
            @RequestBody HolidayRequest request
    ) {

        return ResponseEntity.ok(
                holidayService.updateHoliday(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHoliday(
            @PathVariable Long id
    ) {

        holidayService.deleteHoliday(id);

        return ResponseEntity.noContent().build();
    }
}