package edu.kuet.ums.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.kuet.ums.payload.request.TeacherCreateRequest;
import edu.kuet.ums.payload.request.TeacherUpdateRequest;
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
@DisplayName("Integration Tests: TeacherController REST Endpoints & RBAC")
class TeacherControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @DisplayName("Integration Test: Complete Teacher CRUD Lifecycle by Admin")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminFullTeacherCrudLifecycle() throws Exception {
        TeacherCreateRequest request = new TeacherCreateRequest();
        request.setUsername("integration_teacher_1");
        request.setEmail("integration_teacher_1@cse.kuet.ac.bd");
        request.setPassword("passTeacher123");
        request.setFullName("Dr. Integration Teacher");
        request.setTeacherId("KUET-TCH-INT-1");
        request.setDepartment("CSE");
        request.setDesignation("Assistant Professor");
        request.setPhone("+8801811000000");
        request.setSpecialization("Cloud Computing");

        // 1. CREATE Teacher
        String createResponse = mockMvc.perform(post("/api/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.teacherId", is("KUET-TCH-INT-1")))
                .andExpect(jsonPath("$.specialization", is("Cloud Computing")))
                .andReturn().getResponse().getContentAsString();

        Long teacherId = objectMapper.readTree(createResponse).get("id").asLong();

        // 2. READ Single Teacher
        mockMvc.perform(get("/api/teachers/" + teacherId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is("Dr. Integration Teacher")));

        // 3. UPDATE Teacher
        TeacherUpdateRequest updateRequest = new TeacherUpdateRequest();
        updateRequest.setEmail("integration_teacher_1_mod@cse.kuet.ac.bd");
        updateRequest.setFullName("Dr. Integration Teacher Mod");
        updateRequest.setTeacherId("KUET-TCH-INT-1");
        updateRequest.setDepartment("CSE");
        updateRequest.setDesignation("Associate Professor");
        updateRequest.setPhone("+8801811999999");
        updateRequest.setSpecialization("Distributed Systems");

        mockMvc.perform(put("/api/teachers/" + teacherId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.designation", is("Associate Professor")))
                .andExpect(jsonPath("$.specialization", is("Distributed Systems")));

        // 4. DELETE Teacher
        mockMvc.perform(delete("/api/teachers/" + teacherId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // 5. VERIFY DELETED -> 404
        mockMvc.perform(get("/api/teachers/" + teacherId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Integration Test: Teacher role can access teacher list")
    @WithMockUser(username = "teacher_mahmud", roles = {"TEACHER"})
    void testTeacherRoleCanAccessTeachers() throws Exception {
        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Integration Test: Student role is forbidden from teacher endpoints")
    @WithMockUser(username = "student_mesba", roles = {"STUDENT"})
    void testStudentRoleForbiddenFromTeachers() throws Exception {
        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isForbidden());
    }
}
