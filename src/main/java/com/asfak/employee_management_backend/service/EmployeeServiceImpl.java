//package com.asfak.employee_management_backend.service;
//
//import com.asfak.employee_management_backend.entity.Employee;
//import com.asfak.employee_management_backend.exception.ResourceNotFoundException;
//import com.asfak.employee_management_backend.helper.ExcelHelper;
//import com.asfak.employee_management_backend.repository.EmployeeRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//
//@Service
//public class EmployeeServiceImpl implements EmployeeService {
//
//    private final EmployeeRepository employeeRepository;
//
//    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
//        this.employeeRepository = employeeRepository;
//    }
//
//    @Override
//    public Employee saveEmployee(Employee employee) {
//        return employeeRepository.save(employee);
//    }
//
//    @Override
//    public List<Employee> getAllEmployees() {
//        return employeeRepository.findAll();
//    }
//
//    @Override
//    public Employee getEmployeeById(Long id) {
//        return employeeRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id : " + id));
//    }
//
//    @Override
//    public Employee updateEmployee(Long id, Employee employee) {
//
//        Employee existingEmployee = getEmployeeById(id);
//
//        existingEmployee.setFirstName(employee.getFirstName());
//        existingEmployee.setLastName(employee.getLastName());
//        existingEmployee.setEmail(employee.getEmail());
//        existingEmployee.setPhone(employee.getPhone());
//        existingEmployee.setDepartment(employee.getDepartment());
//        existingEmployee.setDesignation(employee.getDesignation());
//        existingEmployee.setSalary(employee.getSalary());
//        existingEmployee.setJoiningDate(employee.getJoiningDate());
//        existingEmployee.setStatus(employee.getStatus());
//
//        return employeeRepository.save(existingEmployee);
//    }
//
//    @Override
//    public void deleteEmployee(Long id) {
//
//        Employee employee = getEmployeeById(id);
//
//        employeeRepository.delete(employee);
//    }
//
//    @Override
//    public void importEmployees(MultipartFile file) {
//
//        List<Employee> employees =
//                ExcelHelper.excelToEmployees(file);
//
//        employeeRepository.saveAll(employees);
//
//    }
//}

package com.asfak.employee_management_backend.service;

import com.asfak.employee_management_backend.entity.Employee;
import com.asfak.employee_management_backend.exception.ResourceNotFoundException;
import com.asfak.employee_management_backend.helper.ExcelHelper;
import com.asfak.employee_management_backend.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Employee saveEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    @Override
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @Override
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with id : " + id));
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

        return employeeRepository.save(existingEmployee);
    }

    @Override
    public void deleteEmployee(Long id) {

        Employee employee = getEmployeeById(id);

        employeeRepository.delete(employee);
    }

    @Override
    public String importEmployees(MultipartFile file) {

        List<Employee> employees = ExcelHelper.excelToEmployees(file);

        List<Employee> employeesToSave = new ArrayList<>();

        int skipped = 0;

        for (Employee employee : employees) {

            boolean emailExists =
                    employeeRepository.existsByEmail(employee.getEmail());

            boolean phoneExists =
                    employeeRepository.existsByPhone(employee.getPhone());

            if (!emailExists && !phoneExists) {
                employeesToSave.add(employee);
            } else {
                skipped++;
            }
        }

        employeeRepository.saveAll(employeesToSave);

        return "Successfully Imported : "
                + employeesToSave.size()
                + " employee(s), Skipped : "
                + skipped;
    }
}