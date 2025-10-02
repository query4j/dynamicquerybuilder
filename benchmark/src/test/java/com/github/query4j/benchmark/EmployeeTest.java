package com.github.query4j.benchmark;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Employee} entity class.
 * 
 * <p>Tests cover basic functionality of the Employee POJO including
 * constructor usage, getter/setter methods, and equals/hashCode contracts.</p>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
class EmployeeTest {

    @Test
    @DisplayName("Should create employee with no-args constructor")
    void shouldCreateEmployeeWithNoArgsConstructor() {
        Employee employee = new Employee();
        
        assertNotNull(employee);
        assertNull(employee.getId());
        assertNull(employee.getFirstName());
        assertNull(employee.getLastName());
        assertNull(employee.getEmail());
        assertNull(employee.getDepartment());
        assertNull(employee.getRole());
        assertNull(employee.getHireDate());
        assertNull(employee.getSalary());
        assertNull(employee.getActive());
        assertNull(employee.getCity());
        assertNull(employee.getCountry());
    }

    @Test
    @DisplayName("Should create employee with all-args constructor")
    void shouldCreateEmployeeWithAllArgsConstructor() {
        Long id = 1L;
        String firstName = "John";
        String lastName = "Doe";
        String email = "john.doe@company.com";
        String department = "Engineering";
        String role = "Software Developer";
        LocalDate hireDate = LocalDate.of(2023, 1, 15);
        BigDecimal salary = new BigDecimal("75000.00");
        Boolean active = true;
        String city = "New York";
        String country = "USA";
        
        Employee employee = new Employee(id, firstName, lastName, email, department, 
                                       role, hireDate, salary, active, city, country);
        
        assertEquals(id, employee.getId());
        assertEquals(firstName, employee.getFirstName());
        assertEquals(lastName, employee.getLastName());
        assertEquals(email, employee.getEmail());
        assertEquals(department, employee.getDepartment());
        assertEquals(role, employee.getRole());
        assertEquals(hireDate, employee.getHireDate());
        assertEquals(salary, employee.getSalary());
        assertEquals(active, employee.getActive());
        assertEquals(city, employee.getCity());
        assertEquals(country, employee.getCountry());
    }

    @Test
    @DisplayName("Should support setter methods")
    void shouldSupportSetterMethods() {
        Employee employee = new Employee();
        
        Long id = 2L;
        String firstName = "Jane";
        String lastName = "Smith";
        String email = "jane.smith@company.com";
        String department = "Marketing";
        String role = "Marketing Manager";
        LocalDate hireDate = LocalDate.of(2022, 6, 1);
        BigDecimal salary = new BigDecimal("85000.00");
        Boolean active = false;
        String city = "San Francisco";
        String country = "USA";
        
        employee.setId(id);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(email);
        employee.setDepartment(department);
        employee.setRole(role);
        employee.setHireDate(hireDate);
        employee.setSalary(salary);
        employee.setActive(active);
        employee.setCity(city);
        employee.setCountry(country);
        
        assertEquals(id, employee.getId());
        assertEquals(firstName, employee.getFirstName());
        assertEquals(lastName, employee.getLastName());
        assertEquals(email, employee.getEmail());
        assertEquals(department, employee.getDepartment());
        assertEquals(role, employee.getRole());
        assertEquals(hireDate, employee.getHireDate());
        assertEquals(salary, employee.getSalary());
        assertEquals(active, employee.getActive());
        assertEquals(city, employee.getCity());
        assertEquals(country, employee.getCountry());
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        Employee employee1 = new Employee(1L, "John", "Doe", "john@company.com", 
                                        "Engineering", "Developer", LocalDate.of(2023, 1, 1),
                                        new BigDecimal("50000"), true, "NYC", "USA");
        
        Employee employee2 = new Employee(1L, "John", "Doe", "john@company.com", 
                                        "Engineering", "Developer", LocalDate.of(2023, 1, 1),
                                        new BigDecimal("50000"), true, "NYC", "USA");
        
        Employee employee3 = new Employee(2L, "Jane", "Smith", "jane@company.com", 
                                        "Marketing", "Manager", LocalDate.of(2022, 6, 1),
                                        new BigDecimal("60000"), false, "SF", "USA");
        
        // Test equals
        assertEquals(employee1, employee2);
        assertNotEquals(employee1, employee3);
        assertNotEquals(employee1, null);
        assertNotEquals(employee1, "not an employee");
        
        // Test hashCode
        assertEquals(employee1.hashCode(), employee2.hashCode());
        assertNotEquals(employee1.hashCode(), employee3.hashCode());
    }

    @Test
    @DisplayName("Should provide meaningful toString representation")
    void shouldProvideMeaningfulToStringRepresentation() {
        Employee employee = new Employee(1L, "John", "Doe", "john@company.com", 
                                       "Engineering", "Developer", LocalDate.of(2023, 1, 1),
                                       new BigDecimal("50000"), true, "NYC", "USA");
        
        String toString = employee.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("Employee"));
        assertTrue(toString.contains("John"));
        assertTrue(toString.contains("Doe"));
        assertTrue(toString.contains("john@company.com"));
    }

    @Test
    @DisplayName("Should handle null values in fields")
    void shouldHandleNullValuesInFields() {
        Employee employee = new Employee();
        
        // Set some fields to null explicitly
        employee.setId(null);
        employee.setFirstName(null);
        employee.setLastName(null);
        employee.setEmail(null);
        employee.setDepartment(null);
        employee.setRole(null);
        employee.setHireDate(null);
        employee.setSalary(null);
        employee.setActive(null);
        employee.setCity(null);
        employee.setCountry(null);
        
        // Should not throw exceptions
        assertDoesNotThrow(() -> {
            employee.toString();
            employee.hashCode();
            employee.equals(new Employee());
        });
    }
}