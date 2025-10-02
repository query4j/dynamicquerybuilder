package com.github.query4j.benchmark;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link EmployeeTable} entity class.
 * 
 * <p>Tests cover basic functionality of the EmployeeTable POJO including
 * constructor usage, getter/setter methods, and equals/hashCode contracts.</p>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
class EmployeeTableTest {

    @Test
    @DisplayName("Should create employee table with no-args constructor")
    void shouldCreateEmployeeTableWithNoArgsConstructor() {
        EmployeeTable employeeTable = new EmployeeTable();
        
        assertNotNull(employeeTable);
        assertNull(employeeTable.getId());
        assertNull(employeeTable.getFirstName());
        assertNull(employeeTable.getLastName());
        assertNull(employeeTable.getEmail());
        assertNull(employeeTable.getDepartment());
        assertNull(employeeTable.getRole());
        assertNull(employeeTable.getHireDate());
        assertNull(employeeTable.getSalary());
        assertNull(employeeTable.getActive());
        assertNull(employeeTable.getCity());
        assertNull(employeeTable.getCountry());
    }

    @Test
    @DisplayName("Should create employee table with all-args constructor")
    void shouldCreateEmployeeTableWithAllArgsConstructor() {
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
        
        EmployeeTable employeeTable = new EmployeeTable(id, firstName, lastName, email, department, 
                                                       role, hireDate, salary, active, city, country);
        
        assertEquals(id, employeeTable.getId());
        assertEquals(firstName, employeeTable.getFirstName());
        assertEquals(lastName, employeeTable.getLastName());
        assertEquals(email, employeeTable.getEmail());
        assertEquals(department, employeeTable.getDepartment());
        assertEquals(role, employeeTable.getRole());
        assertEquals(hireDate, employeeTable.getHireDate());
        assertEquals(salary, employeeTable.getSalary());
        assertEquals(active, employeeTable.getActive());
        assertEquals(city, employeeTable.getCity());
        assertEquals(country, employeeTable.getCountry());
    }

    @Test
    @DisplayName("Should support setter methods")
    void shouldSupportSetterMethods() {
        EmployeeTable employeeTable = new EmployeeTable();
        
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
        
        employeeTable.setId(id);
        employeeTable.setFirstName(firstName);
        employeeTable.setLastName(lastName);
        employeeTable.setEmail(email);
        employeeTable.setDepartment(department);
        employeeTable.setRole(role);
        employeeTable.setHireDate(hireDate);
        employeeTable.setSalary(salary);
        employeeTable.setActive(active);
        employeeTable.setCity(city);
        employeeTable.setCountry(country);
        
        assertEquals(id, employeeTable.getId());
        assertEquals(firstName, employeeTable.getFirstName());
        assertEquals(lastName, employeeTable.getLastName());
        assertEquals(email, employeeTable.getEmail());
        assertEquals(department, employeeTable.getDepartment());
        assertEquals(role, employeeTable.getRole());
        assertEquals(hireDate, employeeTable.getHireDate());
        assertEquals(salary, employeeTable.getSalary());
        assertEquals(active, employeeTable.getActive());
        assertEquals(city, employeeTable.getCity());
        assertEquals(country, employeeTable.getCountry());
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        EmployeeTable employeeTable1 = new EmployeeTable(1L, "John", "Doe", "john@company.com", 
                                                        "Engineering", "Developer", LocalDate.of(2023, 1, 1),
                                                        new BigDecimal("50000"), true, "NYC", "USA");
        
        EmployeeTable employeeTable2 = new EmployeeTable(1L, "John", "Doe", "john@company.com", 
                                                        "Engineering", "Developer", LocalDate.of(2023, 1, 1),
                                                        new BigDecimal("50000"), true, "NYC", "USA");
        
        EmployeeTable employeeTable3 = new EmployeeTable(2L, "Jane", "Smith", "jane@company.com", 
                                                        "Marketing", "Manager", LocalDate.of(2022, 6, 1),
                                                        new BigDecimal("60000"), false, "SF", "USA");
        
        // Test equals
        assertEquals(employeeTable1, employeeTable2);
        assertNotEquals(employeeTable1, employeeTable3);
        assertNotEquals(employeeTable1, null);
        assertNotEquals(employeeTable1, "not an employee table");
        
        // Test hashCode
        assertEquals(employeeTable1.hashCode(), employeeTable2.hashCode());
        assertNotEquals(employeeTable1.hashCode(), employeeTable3.hashCode());
    }

    @Test
    @DisplayName("Should provide meaningful toString representation")
    void shouldProvideMeaningfulToStringRepresentation() {
        EmployeeTable employeeTable = new EmployeeTable(1L, "John", "Doe", "john@company.com", 
                                                       "Engineering", "Developer", LocalDate.of(2023, 1, 1),
                                                       new BigDecimal("50000"), true, "NYC", "USA");
        
        String toString = employeeTable.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("EmployeeTable"));
        assertTrue(toString.contains("John"));
        assertTrue(toString.contains("Doe"));
        assertTrue(toString.contains("john@company.com"));
    }

    @Test
    @DisplayName("Should handle null values in fields")
    void shouldHandleNullValuesInFields() {
        EmployeeTable employeeTable = new EmployeeTable();
        
        // Set some fields to null explicitly
        employeeTable.setId(null);
        employeeTable.setFirstName(null);
        employeeTable.setLastName(null);
        employeeTable.setEmail(null);
        employeeTable.setDepartment(null);
        employeeTable.setRole(null);
        employeeTable.setHireDate(null);
        employeeTable.setSalary(null);
        employeeTable.setActive(null);
        employeeTable.setCity(null);
        employeeTable.setCountry(null);
        
        // Should not throw exceptions
        assertDoesNotThrow(() -> {
            employeeTable.toString();
            employeeTable.hashCode();
            employeeTable.equals(new EmployeeTable());
        });
    }

    @Test
    @DisplayName("Should demonstrate difference from Employee entity")
    void shouldDemonstrateDifferenceFromEmployeeEntity() {
        // This test ensures both classes exist and are different
        EmployeeTable employeeTable = new EmployeeTable();
        Employee employee = new Employee();
        
        assertNotNull(employeeTable);
        assertNotNull(employee);
        
        // They should not be equal even with same data
        employeeTable.setId(1L);
        employeeTable.setFirstName("John");
        
        employee.setId(1L);
        employee.setFirstName("John");
        
        // Different classes, should not be equal
        assertNotEquals(employeeTable, employee);
    }
}