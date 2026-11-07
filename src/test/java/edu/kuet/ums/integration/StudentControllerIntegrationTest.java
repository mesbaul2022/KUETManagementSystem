package edu.kuet.ums.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.kuet.ums.payload.request.StudentCreateRequest;
import edu.kuet.ums.payload.request.StudentUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Integration Tests: StudentController REST Endpoints & RBAC")
class StudentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @DisplayName("Integration Test: Complete Student CRUD Lifecycle by Admin")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminFullStudentCrudLifecycle() throws Exception {
        StudentCreateRequest createRequest = new StudentCreateRequest();
        createRequest.setUsername("integration_student_1");
        createRequest.setEmail("integration_student_1@stud.kuet.ac.bd");
        createRequest.setPassword("testSecret123");
        createRequest.setFullName("Integration Student 1");
        createRequest.setRollNumber("2207555");
        createRequest.setDepartment("Computer Science & Engineering");
        createRequest.setSeries("2K22");
        createRequest.setPhone("+8801700555555");

        // 1. CREATE Student
        String createResponse = mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.username", is("integration_student_1")))
                .andExpect(jsonPath("$.rollNumber", is("2207555")))
                .andReturn().getResponse().getContentAsString();

        Long studentId = objectMapper.readTree(createResponse).get("id").asLong();

        // 2. READ Single Student
        mockMvc.perform(get("/api/students/" + studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is("Integration Student 1")))
                .andExpect(jsonPath("$.department", is("Computer Science & Engineering")));

        // 3. UPDATE Student
        StudentUpdateRequest updateRequest = new StudentUpdateRequest();
        updateRequest.setEmail("integration_student_1_mod@stud.kuet.ac.bd");
        updateRequest.setFullName("Integration Student 1 Modified");
        updateRequest.setRollNumber("2207555");
        updateRequest.setDepartment("Biomedical Engineering");
        updateRequest.setSeries("2K22");
        updateRequest.setPhone("+8801700999999");

        mockMvc.perform(put("/api/students/" + studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is("Integration Student 1 Modified")))
                .andExpect(jsonPath("$.department", is("Biomedical Engineering")));

        // 4. DELETE Student
        mockMvc.perform(delete("/api/students/" + studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // 5. VERIFY DELETED -> 404
        mockMvc.perform(get("/api/students/" + studentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Integration Test: Student role can access student list")
    @WithMockUser(username = "student_mesba", roles = {"STUDENT"})
    void testStudentRoleAccessAllowed() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Integration Test: Teacher role is forbidden from student endpoints")
    @WithMockUser(username = "teacher_mahmud", roles = {"TEACHER"})
    void testTeacherRoleForbiddenFromStudents() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Integration Test: POST with invalid payload returns 400 Bad Request")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateStudent_InvalidPayload_ReturnsBadRequest() throws Exception {
        // Missing required fields: email, password, fullName, rollNumber
        StudentCreateRequest invalidRequest = new StudentCreateRequest();
        invalidRequest.setUsername("short");

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
