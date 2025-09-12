package com.github.query4j.examples.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Example User entity for Query4j documentation and examples.
 * 
 * This class represents a typical JPA entity that would be used
 * with the Query4j Dynamic Query Builder.
 */
public class User {
    
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private String role;
    private Double salary;
    private Boolean active;
    private Boolean vipStatus;
    private LocalDate joinDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String country;
    private String region;
    
    // Associations
    private List<Role> roles;
    private List<Order> orders;
    
    // Constructors
    public User() {}
    
    public User(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public Boolean getVipStatus() { return vipStatus; }
    public void setVipStatus(Boolean vipStatus) { this.vipStatus = vipStatus; }
    
    public LocalDate getJoinDate() { return joinDate; }
    public void setJoinDate(LocalDate joinDate) { this.joinDate = joinDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    
    public List<Role> getRoles() { return roles; }
    public void setRoles(List<Role> roles) { this.roles = roles; }
    
    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }
    
    @Override
    public String toString() {
        return String.format("User{id=%d, firstName='%s', lastName='%s', email='%s', department='%s'}",
            id, firstName, lastName, email, department);
    }
}