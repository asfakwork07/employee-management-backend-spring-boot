package com.asfak.employee_management_backend.holiday.service.impl;

import com.asfak.employee_management_backend.holiday.dto.HolidayRequest;
import com.asfak.employee_management_backend.holiday.dto.HolidayResponse;
import com.asfak.employee_management_backend.holiday.entity.Holiday;
import com.asfak.employee_management_backend.holiday.repository.HolidayRepository;
import com.asfak.employee_management_backend.holiday.service.HolidayService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HolidayServiceImpl
        implements HolidayService {

    private final HolidayRepository holidayRepository;

    @Override
    public HolidayResponse createHoliday(
            HolidayRequest request
    ) {

        validateRequest(request);

        if (
                holidayRepository.existsByHolidayDate(
                        request.getHolidayDate()
                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Holiday already exists for selected date"
            );
        }

        Holiday holiday =
                Holiday.builder()
                        .holidayDate(
                                request.getHolidayDate()
                        )
                        .name(
                                request.getName().trim()
                        )
                        .type(
                                request.getType()
                                        .trim()
                                        .toUpperCase()
                        )
                        .description(
                                request.getDescription()
                        )
                        .build();

        Holiday saved =
                holidayRepository.save(holiday);

        return mapToResponse(saved);
    }

    @Override
    public List<HolidayResponse> getAllHolidays() {

        return holidayRepository
                .findAllByOrderByHolidayDateAsc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<HolidayResponse> getUpcomingHolidays() {

        return holidayRepository
                .findByHolidayDateGreaterThanEqualOrderByHolidayDateAsc(
                        LocalDate.now()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public HolidayResponse updateHoliday(
            Long id,
            HolidayRequest request
    ) {

        validateRequest(request);

        Holiday holiday =
                holidayRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Holiday not found"
                                )
                        );

        if (
                holidayRepository
                        .existsByHolidayDateAndIdNot(
                                request.getHolidayDate(),
                                id
                        )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Another holiday already exists for selected date"
            );
        }

        holiday.setHolidayDate(
                request.getHolidayDate()
        );

        holiday.setName(
                request.getName().trim()
        );

        holiday.setType(
                request.getType()
                        .trim()
                        .toUpperCase()
        );

        holiday.setDescription(
                request.getDescription()
        );

        Holiday saved =
                holidayRepository.save(holiday);

        return mapToResponse(saved);
    }

    @Override
    public void deleteHoliday(
            Long id
    ) {

        Holiday holiday =
                holidayRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Holiday not found"
                                )
                        );

        holidayRepository.delete(holiday);
    }

    private void validateRequest(
            HolidayRequest request
    ) {

        if (request.getHolidayDate() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Holiday date is required"
            );
        }

        if (
                request.getName() == null ||
                        request.getName().isBlank()
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Holiday name is required"
            );
        }

        if (
                request.getType() == null ||
                        request.getType().isBlank()
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Holiday type is required"
            );
        }
    }

    private HolidayResponse mapToResponse(
            Holiday holiday
    ) {

        return HolidayResponse.builder()
                .id(
                        holiday.getId()
                )
                .holidayDate(
                        holiday.getHolidayDate()
                )
                .name(
                        holiday.getName()
                )
                .type(
                        holiday.getType()
                )
                .description(
                        holiday.getDescription()
                )
                .build();
    }
}