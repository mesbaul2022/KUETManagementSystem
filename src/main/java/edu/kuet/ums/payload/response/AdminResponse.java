package edu.kuet.ums.payload.response;

import java.time.LocalDateTime;

public class AdminResponse {

    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String role;
    private String fullName;
    private String employeeId;
    private String department;
    private String designation;
    private String phone;
    private LocalDateTime createdAt;

    public AdminResponse() {
    }

    public AdminResponse(Long id, Long userId, String username, String email, String role, String fullName,
                         String employeeId, String department, String designation, String phone, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.fullName = fullName;
        this.employeeId = employeeId;
        this.department = department;
        this.designation = designation;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
