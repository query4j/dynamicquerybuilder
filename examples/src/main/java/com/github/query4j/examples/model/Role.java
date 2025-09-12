package com.github.query4j.examples.model;

/**
 * Example Role entity for Query4j documentation and examples.
 */
public class Role {
    
    private Long id;
    private String name;
    private String description;
    private Boolean active;
    
    // Constructors
    public Role() {}
    
    public Role(String name) {
        this.name = name;
        this.active = true;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    @Override
    public String toString() {
        return String.format("Role{id=%d, name='%s'}", id, name);
    }
}