package com.asfak.employee_management_backend.service;

import com.asfak.employee_management_backend.attendance.repository.AttendanceRepository;
import com.asfak.employee_management_backend.dto.EmployeeResponse;
import com.asfak.employee_management_backend.entity.Employee;
import com.asfak.employee_management_backend.entity.User;
import com.asfak.employee_management_backend.exception.ResourceNotFoundException;
import com.asfak.employee_management_backend.helper.ExcelHelper;
import com.asfak.employee_management_backend.leave.repository.LeaveRequestRepository;
import com.asfak.employee_management_backend.notification.repository.NotificationRepository;
import com.asfak.employee_management_backend.repository.EmployeeRepository;
import com.asfak.employee_management_backend.repository.UserRepository;
import com.asfak.employee_management_backend.salary.repository.SalaryRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    private final UserRepository userRepository;

    private final AttendanceRepository attendanceRepository;

    private final LeaveRequestRepository leaveRequestRepository;

    private final SalaryRepository salaryRepository;

    private final NotificationRepository notificationRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, UserRepository userRepository, AttendanceRepository attendanceRepository, LeaveRequestRepository leaveRequestRepository, SalaryRepository salaryRepository, NotificationRepository notificationRepository) {

        this.employeeRepository = employeeRepository;

        this.userRepository = userRepository;

        this.attendanceRepository = attendanceRepository;

        this.leaveRequestRepository = leaveRequestRepository;

        this.salaryRepository = salaryRepository;

        this.notificationRepository = notificationRepository;
    }

    @Override
    public Employee saveEmployee(Employee employee) {

        return employeeRepository.save(employee);
    }

    @Override
    public List<EmployeeResponse> getAllEmployees() {

        return employeeRepository.findAll().stream().map(this::mapToEmployeeResponse).toList();
    }

    @Override
    public Employee getEmployeeById(Long id) {

        return employeeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Employee not found with id : " + id));
    }

    @Override
    public Employee updateEmployee(Long id, Employee employee) {

        Employee existingEmployee = getEmployeeById(id);

        existingEmployee.setFirstName(employee.getFirstName());

        existingEmployee.setLastName(employee.getLastName());

        existingEmployee.setEmail(employee.getEmail());

        existingEmployee.setPhone(employee.getPhone());

        existingEmployee.setDepartment(employee.getDepartment());

        existingEmployee.setDesignation(employee.getDesignation());

        existingEmployee.setSalary(employee.getSalary());

        existingEmployee.setJoiningDate(employee.getJoiningDate());

        existingEmployee.setStatus(employee.getStatus());

        User linkedUser = userRepository.findByEmployeeId(id).orElse(null);

        if (linkedUser != null) {

            linkedUser.setName(existingEmployee.getFirstName() + " " + existingEmployee.getLastName());

            linkedUser.setEmail(existingEmployee.getEmail());

            userRepository.save(linkedUser);
        }

        return employeeRepository.save(existingEmployee);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {

        Employee employee = getEmployeeById(id);

        Optional<User> userOptional = userRepository.findByEmployeeId(id);

        if (userOptional.isPresent()) {

            User user = userOptional.get();

            notificationRepository.deleteByUserId(user.getId());
        }

        attendanceRepository.deleteByEmployeeId(id);

        leaveRequestRepository.deleteByEmployeeId(id);

        salaryRepository.deleteByEmployeeId(id);

        userRepository.deleteByEmployeeId(id);

        employeeRepository.delete(employee);
    }

    @Override
    public String importEmployees(MultipartFile file) {

        List<Employee> employees = ExcelHelper.excelToEmployees(file);

        List<Employee> employeesToSave = new ArrayList<>();

        int skipped = 0;

        for (Employee employee : employees) {

            boolean emailExists = employeeRepository.existsByEmail(employee.getEmail());

            boolean phoneExists = employeeRepository.existsByPhone(employee.getPhone());

            if (!emailExists && !phoneExists) {

                employeesToSave.add(employee);

            } else {

                skipped++;
            }
        }

        employeeRepository.saveAll(employeesToSave);

        return "Successfully Imported : " + employeesToSave.size() + " employee(s), Skipped : " + skipped;
    }

    @Override
    public EmployeeResponse getMyProfile(String loggedInEmail) {

        User user = userRepository.findByEmail(loggedInEmail).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Logged-in user not found"));

        if (user.getEmployee() == null) {

            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee profile is not linked with this account");
        }

        Employee employee = employeeRepository.findById(user.getEmployee().getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        return mapToEmployeeResponse(employee);
    }

    private EmployeeResponse mapToEmployeeResponse(Employee employee) {

        Optional<User> userOptional = userRepository.findByEmployeeId(employee.getId());

        boolean loginEnabled = userOptional.isPresent();

        boolean accountEnabled = userOptional.map(User::isEnabled).orElse(false);

        return EmployeeResponse.builder().id(employee.getId()).firstName(employee.getFirstName()).lastName(employee.getLastName()).email(employee.getEmail()).phone(employee.getPhone()).department(employee.getDepartment()).designation(employee.getDesignation()).salary(employee.getSalary()).joiningDate(employee.getJoiningDate()).status(employee.getStatus()).loginEnabled(loginEnabled).accountEnabled(accountEnabled).build();
    }
}