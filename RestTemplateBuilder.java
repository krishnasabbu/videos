package com.chat.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RestTemplateBuilder {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.base.url}")
    private String baseUrl;

    @Value("${api.employee.endpoint}")
    private String employeeEndpoint;

    @Value("${api.employee.delete.endpoint}")
    private String employeeDeleteEndpoint;

    // GET All Employees
    public List<Employee1> getAllEmployees() {
        String url = baseUrl + employeeEndpoint;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<Employee[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Employee[].class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                this.buildEmployeeList(Arrays.asList(response.getBody()));
            }
        } catch (RestClientException e) {
            throw new ServiceException("Error occurred while fetching employees", e);
        }
        return Collections.emptyList();
    }

    public List<Employee1> buildEmployeeList(List<Employee> eployeeList) {
        List<Employee1> employeeList = new ArrayList<>();
        for (Employee source : eployeeList) {
            Employee1 employee = new Employee1();
            employee.setName(source.getFullName());
            employee.setAge(source.getBirth());
            employee.setLocation(source.getAddress());
            employeeList.add(employee);
        }
        return employeeList;
    }

    // POST Employee
    public Employee1 postEmployee(Student student) {
        String url = baseUrl + employeeEndpoint;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "application/json");

        Employee employee = this.buildEmployee(student);

        HttpEntity<Employee> requestEntity = new HttpEntity<>(employee, headers);
        try {
            ResponseEntity<Employee1> response = restTemplate.postForEntity(url, requestEntity, Employee1.class);

            if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new ServiceException("Failed to create Employee");
            }
        } catch (RestClientException e) {
            throw new ServiceException("Error occurred while creating Employee", e);
        }
    }

    public Employee buildEmployee(Student source) {
        Employee employee = new Employee();
        employee.setAddress(source.getDetails().getAddress());
        employee.setFullName(source.getProfile().getName());
        employee.setBirth(source.getProfile().getAge());
        return employee;
    }



    // DELETE Employee
    public void deleteEmployee(Long employeeId) {
        String url = baseUrl + employeeDeleteEndpoint;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class, employeeId);
        } catch (RestClientException e) {
            throw new ServiceException("Error occurred while deleting Employee", e);
        }
    }
}
