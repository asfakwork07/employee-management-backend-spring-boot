package com.asfak.employee_management_backend.helper;

import com.asfak.employee_management_backend.entity.Employee;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExcelHelper {

    public static List<Employee> excelToEmployees(MultipartFile file) {

        try {

            InputStream is = file.getInputStream();

            Workbook workbook = WorkbookFactory.create(is);

            Sheet sheet = workbook.getSheetAt(0);

            List<Employee> employees = new ArrayList<>();

            DataFormatter formatter = new DataFormatter();

            boolean firstRow = true;

            for (Row row : sheet) {

                if (firstRow) {
                    firstRow = false;
                    continue;
                }

                Employee employee = new Employee();

                employee.setFirstName(formatter.formatCellValue(row.getCell(0)));

                employee.setLastName(formatter.formatCellValue(row.getCell(1)));

                employee.setEmail(formatter.formatCellValue(row.getCell(2)));

                employee.setPhone(formatter.formatCellValue(row.getCell(3)));

                employee.setDepartment(formatter.formatCellValue(row.getCell(4)));

                employee.setDesignation(formatter.formatCellValue(row.getCell(5)));

                employee.setSalary(
                        BigDecimal.valueOf(
                                Double.parseDouble(
                                        formatter.formatCellValue(row.getCell(6))
                                )
                        )
                );

                employee.setJoiningDate(
                        LocalDate.parse(
                                formatter.formatCellValue(row.getCell(7))
                        )
                );

                employee.setStatus(formatter.formatCellValue(row.getCell(8)));

                employees.add(employee);

            }

            workbook.close();

            return employees;

        } catch (Exception e) {

            throw new RuntimeException(e);

        }

    }

}