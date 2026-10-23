package edu.kuet.ums.payload.response;

import java.time.LocalDateTime;

public class TeacherResponse {

    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String role;
    private String fullName;
    private String teacherId;
    private String department;
    private String designation;
    private String phone;
    private String specialization;
    private LocalDateTime createdAt;

    public TeacherResponse() {
    }

    public TeacherResponse(Long id, Long userId, String username, String email, String role, String fullName,
                           String teacherId, String department, String designation, String phone,
                           String specialization, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.fullName = fullName;
        this.teacherId = teacherId;
        this.department = department;
        this.designation = designation;
        this.phone = phone;
        this.specialization = specialization;
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

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
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

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
