package edu.kuet.ums;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kuet.ums.payload.request.LoginRequest;
import edu.kuet.ums.payload.request.StudentCreateRequest;
import edu.kuet.ums.payload.request.TeacherCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class KuetManagementSystemApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @Test
    void contextLoads() {
    }

    @Test
    @DisplayName("Login with valid Admin credentials returns JWT token")
    void testAdminLoginSuccess() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin", "admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.role", is("ROLE_ADMIN")))
                .andExpect(jsonPath("$.username", is("admin")));
    }

    @Test
    @DisplayName("Login with invalid password returns 401 Unauthorized")
    void testLoginWithInvalidCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin", "wrong_password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    @DisplayName("Unauthenticated request to protected endpoint returns 401")
    void testUnauthenticatedAccessReturns401() throws Exception {
        mockMvc.perform(get("/api/admins"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("RBAC: Admin has permission to access Admin, Teacher, and Student resources")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminCanAccessAllResources() throws Exception {
        mockMvc.perform(get("/api/admins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("RBAC: Teacher can access Teacher resources, but FORBIDDEN from Admins and Students")
    @WithMockUser(username = "teacher_mahmud", roles = {"TEACHER"})
    void testTeacherCanOnlyManageTeachers() throws Exception {
        // Allowed:
        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isOk());

        // Forbidden:
        mockMvc.perform(get("/api/admins"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RBAC: Student can access Student resources, but FORBIDDEN from Admins and Teachers")
    @WithMockUser(username = "student_mesba", roles = {"STUDENT"})
    void testStudentCanOnlyManageStudents() throws Exception {
        // Allowed:
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk());

        // Forbidden:
        mockMvc.perform(get("/api/admins"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin can create a new student and retrieve it (CRUD)")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testStudentCrudFlow() throws Exception {
        StudentCreateRequest request = new StudentCreateRequest();
        request.setUsername("student_test_crud");
        request.setEmail("test_crud@stud.kuet.ac.bd");
        request.setPassword("password123");
        request.setFullName("Test CRUD Student");
        request.setRollNumber("2007999");
        request.setDepartment("Computer Science & Engineering");
        request.setSeries("2K20");
        request.setPhone("+8801700000000");

        // 1. Create Student (201 Created)
        String responseContent = mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.rollNumber", is("2007999")))
                .andReturn().getResponse().getContentAsString();

        Long createdId = objectMapper.readTree(responseContent).get("id").asLong();

        // 2. Read Single Student (200 OK)
        mockMvc.perform(get("/api/students/" + createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is("Test CRUD Student")));

        // 3. Delete Student (200 OK)
        mockMvc.perform(delete("/api/students/" + createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // 4. Verify Deleted (404 Not Found)
        mockMvc.perform(get("/api/students/" + createdId))
                .andExpect(status().isNotFound());
    }
}
