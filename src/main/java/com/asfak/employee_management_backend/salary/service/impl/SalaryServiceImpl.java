package com.asfak.employee_management_backend.salary.service.impl;

import com.asfak.employee_management_backend.email.service.EmailService;
import com.asfak.employee_management_backend.entity.Employee;
import com.asfak.employee_management_backend.entity.User;
import com.asfak.employee_management_backend.notification.service.NotificationService;
import com.asfak.employee_management_backend.repository.EmployeeRepository;
import com.asfak.employee_management_backend.repository.UserRepository;
import com.asfak.employee_management_backend.salary.dto.GenerateSalaryRequest;
import com.asfak.employee_management_backend.salary.dto.PayslipResponse;
import com.asfak.employee_management_backend.salary.dto.SalaryResponse;
import com.asfak.employee_management_backend.salary.entity.Salary;
import com.asfak.employee_management_backend.salary.mapper.SalaryMapper;
import com.asfak.employee_management_backend.salary.repository.SalaryRepository;
import com.asfak.employee_management_backend.salary.service.SalaryService;
import com.asfak.employee_management_backend.settings.entity.SystemSettings;
import com.asfak.employee_management_backend.settings.repository.SystemSettingsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryServiceImpl implements SalaryService {

    private final SalaryRepository salaryRepository;

    private final EmployeeRepository employeeRepository;

    private final SalaryMapper salaryMapper;

    private final UserRepository userRepository;

    private final SystemSettingsRepository systemSettingsRepository;

    private final NotificationService notificationService;

    private final EmailService emailService;

    @Override
    public SalaryResponse generateSalary(
            GenerateSalaryRequest request
    ) {

        salaryRepository
                .findByEmployeeIdAndSalaryMonthAndSalaryYear(
                        request.getEmployeeId(),
                        request.getSalaryMonth(),
                        request.getSalaryYear()
                )
                .ifPresent(existingSalary -> {

                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Salary already generated for this month."
                    );
                });

        Employee employee =
                employeeRepository
                        .findById(request.getEmployeeId())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Employee not found"
                                )
                        );

        BigDecimal basic =
                employee.getSalary();

        if (basic == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Employee salary is not configured"
            );
        }

        SystemSettings settings =
                getSystemSettings();

        BigDecimal hraPercentage =
                BigDecimal.valueOf(
                        settings.getHraPercentage() != null
                                ? settings.getHraPercentage()
                                : 20.0
                );

        BigDecimal pfPercentage =
                BigDecimal.valueOf(
                        settings.getPfPercentage() != null
                                ? settings.getPfPercentage()
                                : 12.0
                );

        BigDecimal professionalTax =
                BigDecimal.valueOf(
                        settings.getProfessionalTax() != null
                                ? settings.getProfessionalTax()
                                : 200.0
                );

        BigDecimal allowance =
                BigDecimal.valueOf(
                        settings.getDefaultAllowance() != null
                                ? settings.getDefaultAllowance()
                                : 0.0
                );

        BigDecimal hra =
                basic
                        .multiply(
                                hraPercentage
                        )
                        .divide(
                                BigDecimal.valueOf(100),
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal gross =
                basic
                        .add(hra)
                        .add(allowance);

        BigDecimal pf =
                basic
                        .multiply(
                                pfPercentage
                        )
                        .divide(
                                BigDecimal.valueOf(100),
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal incomeTax =
                gross
                        .multiply(
                                BigDecimal.valueOf(0.10)
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal net =
                gross
                        .subtract(pf)
                        .subtract(professionalTax)
                        .subtract(incomeTax);

        if (
                net.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Calculated net salary cannot be negative"
            );
        }

        Salary salary =
                Salary.builder()
                        .employee(employee)
                        .salaryMonth(
                                request.getSalaryMonth()
                        )
                        .salaryYear(
                                request.getSalaryYear()
                        )
                        .basicSalary(
                                basic.setScale(
                                        2,
                                        RoundingMode.HALF_UP
                                )
                        )
                        .hra(
                                hra.setScale(
                                        2,
                                        RoundingMode.HALF_UP
                                )
                        )
                        .allowance(
                                allowance.setScale(
                                        2,
                                        RoundingMode.HALF_UP
                                )
                        )
                        .pf(
                                pf.setScale(
                                        2,
                                        RoundingMode.HALF_UP
                                )
                        )
                        .professionalTax(
                                professionalTax.setScale(
                                        2,
                                        RoundingMode.HALF_UP
                                )
                        )
                        .incomeTax(
                                incomeTax.setScale(
                                        2,
                                        RoundingMode.HALF_UP
                                )
                        )
                        .grossSalary(
                                gross.setScale(
                                        2,
                                        RoundingMode.HALF_UP
                                )
                        )
                        .netSalary(
                                net.setScale(
                                        2,
                                        RoundingMode.HALF_UP
                                )
                        )
                        .build();

        Salary saved =
                salaryRepository
                        .save(salary);

        notifyEmployeeAboutSalary(
                saved
        );

        return salaryMapper
                .toSalaryResponse(saved);
    }

    @Override
    public List<SalaryResponse> getAllSalary() {

        return salaryRepository
                .findAll()
                .stream()
                .map(
                        salaryMapper::toSalaryResponse
                )
                .toList();
    }

    @Override
    public List<SalaryResponse> getEmployeeSalary(
            Long employeeId,
            String loggedInEmail
    ) {

        User user =
                getLoggedInUser(
                        loggedInEmail
                );

        if (
                !"ADMIN".equalsIgnoreCase(
                        user.getRole()
                )
        ) {

            if (
                    user.getEmployee() == null
            ) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "User account is not linked with an employee"
                );
            }

            Long loggedInEmployeeId =
                    user.getEmployee()
                            .getId();

            if (
                    !loggedInEmployeeId.equals(
                            employeeId
                    )
            ) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You can only view your own salary records"
                );
            }
        }

        employeeRepository
                .findById(employeeId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Employee not found"
                        )
                );

        return salaryRepository
                .findByEmployeeIdOrderBySalaryYearDescSalaryMonthDesc(
                        employeeId
                )
                .stream()
                .map(
                        salaryMapper::toSalaryResponse
                )
                .toList();
    }

    @Override
    public PayslipResponse getPayslip(
            Long salaryId,
            String loggedInEmail
    ) {

        Salary salary =
                getSalaryById(
                        salaryId
                );

        validateSalaryOwnership(
                salary,
                loggedInEmail
        );

        return salaryMapper
                .toPayslipResponse(
                        salary
                );
    }

    @Override
    public byte[] generatePayslipPdf(
            Long salaryId,
            String loggedInEmail
    ) {

        Salary salary =
                getSalaryById(
                        salaryId
                );

        validateSalaryOwnership(
                salary,
                loggedInEmail
        );

        SystemSettings settings =
                getSystemSettings();

        String companyName =
                settings.getCompanyName() != null &&
                        !settings.getCompanyName().isBlank()
                        ? settings.getCompanyName()
                        : "Employee Management System";

        try (
                PDDocument document =
                        new PDDocument();

                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {

            PDPage page =
                    new PDPage();

            document.addPage(
                    page
            );

            PDType1Font regularFont =
                    new PDType1Font(
                            Standard14Fonts.FontName.HELVETICA
                    );

            PDType1Font boldFont =
                    new PDType1Font(
                            Standard14Fonts.FontName.HELVETICA_BOLD
                    );

            try (
                    PDPageContentStream content =
                            new PDPageContentStream(
                                    document,
                                    page
                            )
            ) {

                float pageWidth =
                        page.getMediaBox()
                                .getWidth();

                setFillColor(
                        content,
                        30,
                        41,
                        59
                );

                content.addRect(
                        0,
                        720,
                        pageWidth,
                        72
                );

                content.fill();

                content.beginText();

                setFillColor(
                        content,
                        255,
                        255,
                        255
                );

                content.setFont(
                        boldFont,
                        18
                );

                content.newLineAtOffset(
                        50,
                        760
                );

                content.showText(
                        sanitizePdfText(
                                companyName
                        )
                );

                content.setFont(
                        regularFont,
                        11
                );

                content.newLineAtOffset(
                        0,
                        -20
                );

                content.showText(
                        "Salary Payslip"
                );

                content.endText();

                float y =
                        680;

                setFillColor(
                        content,
                        15,
                        23,
                        42
                );

                content.beginText();

                content.setFont(
                        boldFont,
                        12
                );

                content.newLineAtOffset(
                        50,
                        y
                );

                content.showText(
                        "Employee Details"
                );

                content.endText();

                y -= 28;

                String employeeName =
                        salary.getEmployee()
                                .getFirstName()
                                + " "
                                + salary.getEmployee()
                                .getLastName();

                writeLabelValue(
                        content,
                        regularFont,
                        boldFont,
                        50,
                        y,
                        "Employee",
                        employeeName
                );

                writeLabelValue(
                        content,
                        regularFont,
                        boldFont,
                        310,
                        y,
                        "Department",
                        salary.getEmployee()
                                .getDepartment()
                );

                y -= 24;

                writeLabelValue(
                        content,
                        regularFont,
                        boldFont,
                        50,
                        y,
                        "Designation",
                        salary.getEmployee()
                                .getDesignation()
                );

                writeLabelValue(
                        content,
                        regularFont,
                        boldFont,
                        310,
                        y,
                        "Salary Period",
                        getMonthName(
                                salary.getSalaryMonth()
                        )
                                + " "
                                + salary.getSalaryYear()
                );

                y -= 30;

                setStrokeColor(
                        content,
                        203,
                        213,
                        225
                );

                content.moveTo(
                        50,
                        y
                );

                content.lineTo(
                        545,
                        y
                );

                content.stroke();

                y -= 35;

                content.beginText();

                content.setFont(
                        boldFont,
                        12
                );

                setFillColor(
                        content,
                        22,
                        163,
                        74
                );

                content.newLineAtOffset(
                        50,
                        y
                );

                content.showText(
                        "EARNINGS"
                );

                setFillColor(
                        content,
                        220,
                        38,
                        38
                );

                content.newLineAtOffset(
                        260,
                        0
                );

                content.showText(
                        "DEDUCTIONS"
                );

                content.endText();

                y -= 28;

                writeMoneyRow(
                        content,
                        regularFont,
                        boldFont,
                        y,
                        "Basic Salary",
                        salary.getBasicSalary(),
                        "PF",
                        salary.getPf()
                );

                y -= 24;

                writeMoneyRow(
                        content,
                        regularFont,
                        boldFont,
                        y,
                        "HRA",
                        salary.getHra(),
                        "Professional Tax",
                        salary.getProfessionalTax()
                );

                y -= 24;

                writeMoneyRow(
                        content,
                        regularFont,
                        boldFont,
                        y,
                        "Allowance",
                        salary.getAllowance(),
                        "Income Tax",
                        salary.getIncomeTax()
                );

                y -= 38;

                setStrokeColor(
                        content,
                        203,
                        213,
                        225
                );

                content.moveTo(
                        50,
                        y
                );

                content.lineTo(
                        545,
                        y
                );

                content.stroke();

                y -= 30;

                BigDecimal totalDeductions =
                        nullSafe(
                                salary.getPf()
                        )
                                .add(
                                        nullSafe(
                                                salary.getProfessionalTax()
                                        )
                                )
                                .add(
                                        nullSafe(
                                                salary.getIncomeTax()
                                        )
                                );

                content.beginText();

                setFillColor(
                        content,
                        15,
                        23,
                        42
                );

                content.setFont(
                        boldFont,
                        11
                );

                content.newLineAtOffset(
                        50,
                        y
                );

                content.showText(
                        "Gross Salary"
                );

                content.newLineAtOffset(
                        120,
                        0
                );

                content.showText(
                        formatMoney(
                                salary.getGrossSalary()
                        )
                );

                content.newLineAtOffset(
                        140,
                        0
                );

                content.showText(
                        "Total Deductions"
                );

                content.newLineAtOffset(
                        110,
                        0
                );

                content.showText(
                        formatMoney(
                                totalDeductions
                        )
                );

                content.endText();

                y -= 60;

                setFillColor(
                        content,
                        30,
                        64,
                        175
                );

                content.addRect(
                        50,
                        y - 10,
                        495,
                        55
                );

                content.fill();

                content.beginText();

                setFillColor(
                        content,
                        255,
                        255,
                        255
                );

                content.setFont(
                        boldFont,
                        14
                );

                content.newLineAtOffset(
                        70,
                        y + 12
                );

                content.showText(
                        "NET SALARY"
                );

                content.setFont(
                        boldFont,
                        18
                );

                content.newLineAtOffset(
                        300,
                        0
                );

                content.showText(
                        formatMoney(
                                salary.getNetSalary()
                        )
                );

                content.endText();

                content.beginText();

                setFillColor(
                        content,
                        100,
                        116,
                        139
                );

                content.setFont(
                        regularFont,
                        9
                );

                content.newLineAtOffset(
                        50,
                        70
                );

                content.showText(
                        "This is a system generated payslip and does not require a signature."
                );

                content.endText();
            }

            document.save(
                    outputStream
            );

            return outputStream
                    .toByteArray();

        } catch (Exception e) {

            log.error(
                    "Payslip PDF generation failed for salaryId: {}",
                    salaryId,
                    e
            );

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to generate payslip PDF: "
                            + e.getMessage(),
                    e
            );
        }
    }

    private Salary getSalaryById(
            Long salaryId
    ) {

        return salaryRepository
                .findById(salaryId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Salary not found"
                        )
                );
    }

    private SystemSettings getSystemSettings() {

        return systemSettingsRepository
                .findAll()
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "System settings are not configured"
                        )
                );
    }

    private void writeLabelValue(
            PDPageContentStream content,
            PDType1Font regularFont,
            PDType1Font boldFont,
            float x,
            float y,
            String label,
            String value
    ) throws IOException {

        content.beginText();

        setFillColor(
                content,
                100,
                116,
                139
        );

        content.setFont(
                regularFont,
                10
        );

        content.newLineAtOffset(
                x,
                y
        );

        content.showText(
                label + ":"
        );

        setFillColor(
                content,
                15,
                23,
                42
        );

        content.setFont(
                boldFont,
                10
        );

        content.newLineAtOffset(
                75,
                0
        );

        content.showText(
                value != null
                        ? sanitizePdfText(value)
                        : "--"
        );

        content.endText();
    }

    private void writeMoneyRow(
            PDPageContentStream content,
            PDType1Font regularFont,
            PDType1Font boldFont,
            float y,
            String earningLabel,
            BigDecimal earningValue,
            String deductionLabel,
            BigDecimal deductionValue
    ) throws IOException {

        content.beginText();

        setFillColor(
                content,
                71,
                85,
                105
        );

        content.setFont(
                regularFont,
                10
        );

        content.newLineAtOffset(
                50,
                y
        );

        content.showText(
                earningLabel
        );

        content.setFont(
                boldFont,
                10
        );

        content.newLineAtOffset(
                120,
                0
        );

        content.showText(
                formatMoney(
                        earningValue
                )
        );

        content.setFont(
                regularFont,
                10
        );

        content.newLineAtOffset(
                140,
                0
        );

        content.showText(
                deductionLabel
        );

        content.setFont(
                boldFont,
                10
        );

        content.newLineAtOffset(
                105,
                0
        );

        content.showText(
                formatMoney(
                        deductionValue
                )
        );

        content.endText();
    }

    private void setFillColor(
            PDPageContentStream content,
            int red,
            int green,
            int blue
    ) throws IOException {

        content.setNonStrokingColor(
                red / 255f,
                green / 255f,
                blue / 255f
        );
    }

    private void setStrokeColor(
            PDPageContentStream content,
            int red,
            int green,
            int blue
    ) throws IOException {

        content.setStrokingColor(
                red / 255f,
                green / 255f,
                blue / 255f
        );
    }

    private String getMonthName(
            Integer month
    ) {

        if (
                month == null ||
                        month < 1 ||
                        month > 12
        ) {

            return "--";
        }

        return Month
                .of(month)
                .getDisplayName(
                        TextStyle.FULL,
                        Locale.ENGLISH
                );
    }

    private String formatMoney(
            BigDecimal amount
    ) {

        if (
                amount == null
        ) {

            return "Rs. 0.00";
        }

        return "Rs. "
                + amount
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                )
                .toPlainString();
    }

    private BigDecimal nullSafe(
            BigDecimal value
    ) {

        return value != null
                ? value
                : BigDecimal.ZERO;
    }

    private String sanitizePdfText(
            String text
    ) {

        if (
                text == null
        ) {

            return "--";
        }

        return text
                .replace(
                        "\n",
                        " "
                )
                .replace(
                        "\r",
                        " "
                )
                .trim();
    }

    private User getLoggedInUser(
            String email
    ) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Logged-in user not found"
                        )
                );
    }

    private void validateSalaryOwnership(
            Salary salary,
            String loggedInEmail
    ) {

        User user =
                getLoggedInUser(
                        loggedInEmail
                );

        if (
                "ADMIN".equalsIgnoreCase(
                        user.getRole()
                )
        ) {

            return;
        }

        if (
                user.getEmployee() == null
        ) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User account is not linked with an employee"
            );
        }

        Long loggedInEmployeeId =
                user.getEmployee()
                        .getId();

        Long salaryEmployeeId =
                salary.getEmployee()
                        .getId();

        if (
                !loggedInEmployeeId.equals(
                        salaryEmployeeId
                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only access your own payslip"
            );
        }
    }

    private void notifyEmployeeAboutSalary(
            Salary salary
    ) {

        User employeeUser =
                userRepository
                        .findByEmployeeId(
                                salary.getEmployee()
                                        .getId()
                        )
                        .orElse(null);

        if (employeeUser == null) {
            return;
        }

        String month =
                getMonthName(
                        salary.getSalaryMonth()
                );

        String message =
                "Your salary for "
                        + month
                        + " "
                        + salary.getSalaryYear()
                        + " has been generated. Net salary: "
                        + formatMoney(
                        salary.getNetSalary()
                )
                        + ".";

        notificationService.createNotification(
                employeeUser,
                "Salary Generated",
                message,
                "SALARY_GENERATED"
        );

        if (
                employeeUser.getEmail() != null &&
                        !employeeUser.getEmail().isBlank()
        ) {

            emailService.sendEmail(
                    employeeUser.getEmail(),
                    "Salary Generated - "
                            + month
                            + " "
                            + salary.getSalaryYear(),
                    "Hello "
                            + salary.getEmployee().getFirstName()
                            + ",\n\n"
                            + "Your salary for "
                            + month
                            + " "
                            + salary.getSalaryYear()
                            + " has been generated successfully.\n\n"
                            + "Basic Salary: "
                            + formatMoney(
                            salary.getBasicSalary()
                    )
                            + "\n"
                            + "Gross Salary: "
                            + formatMoney(
                            salary.getGrossSalary()
                    )
                            + "\n"
                            + "PF: "
                            + formatMoney(
                            salary.getPf()
                    )
                            + "\n"
                            + "Professional Tax: "
                            + formatMoney(
                            salary.getProfessionalTax()
                    )
                            + "\n"
                            + "Income Tax: "
                            + formatMoney(
                            salary.getIncomeTax()
                    )
                            + "\n"
                            + "Net Salary: "
                            + formatMoney(
                            salary.getNetSalary()
                    )
                            + "\n\n"
                            + "You can view or download your payslip from the Employee Management System.\n\n"
                            + "Regards,\n"
                            + "Employee Management System"
            );
        }
    }

//    private void notifyEmployeeAboutSalary(
//            Salary salary
//    ) {
//
//        User employeeUser =
//                userRepository
//                        .findByEmployeeId(
//                                salary.getEmployee()
//                                        .getId()
//                        )
//                        .orElse(null);
//
//        if (employeeUser == null) {
//            return;
//        }
//
//        String month =
//                getMonthName(
//                        salary.getSalaryMonth()
//                );
//
//        String message =
//                "Your salary for "
//                        + month
//                        + " "
//                        + salary.getSalaryYear()
//                        + " has been generated. Net salary: "
//                        + formatMoney(
//                        salary.getNetSalary()
//                )
//                        + ".";
//
//        notificationService.createNotification(
//                employeeUser,
//                "Salary Generated",
//                message,
//                "SALARY_GENERATED"
//        );
//    }

}