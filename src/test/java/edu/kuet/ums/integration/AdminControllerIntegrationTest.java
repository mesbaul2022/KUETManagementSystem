package edu.kuet.ums.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.kuet.ums.payload.request.AdminCreateRequest;
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
@DisplayName("Integration Tests: AdminController REST Endpoints & RBAC Enforcement")
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @DisplayName("Integration Test: Admin role can view administrators list")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminRoleCanAccessAdmins() throws Exception {
        mockMvc.perform(get("/api/admins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Integration Test: Teacher role is forbidden from admin endpoints")
    @WithMockUser(username = "teacher_mahmud", roles = {"TEACHER"})
    void testTeacherRoleForbiddenFromAdmins() throws Exception {
        mockMvc.perform(get("/api/admins"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Integration Test: Student role is forbidden from admin endpoints")
    @WithMockUser(username = "student_mesba", roles = {"STUDENT"})
    void testStudentRoleForbiddenFromAdmins() throws Exception {
        mockMvc.perform(get("/api/admins"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Integration Test: Admin can create and delete secondary admin account")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminCreateAndDeleteFlow() throws Exception {
        AdminCreateRequest request = new AdminCreateRequest();
        request.setUsername("integration_admin_extra");
        request.setEmail("integration_admin_extra@kuet.ac.bd");
        request.setPassword("adminSecret123");
        request.setFullName("Extra Admin User");
        request.setEmployeeId("KUET-ADM-EXTRA-1");
        request.setDepartment("Academic Section");
        request.setDesignation("Deputy Registrar");
        request.setPhone("+8801700998877");

        // 1. CREATE Admin
        String createResponse = mockMvc.perform(post("/api/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.employeeId", is("KUET-ADM-EXTRA-1")))
                .andReturn().getResponse().getContentAsString();

        Long newAdminId = objectMapper.readTree(createResponse).get("id").asLong();

        // 2. READ Admin
        mockMvc.perform(get("/api/admins/" + newAdminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is("Extra Admin User")));

        // 3. DELETE Admin (permitted because multiple admins exist)
        mockMvc.perform(delete("/api/admins/" + newAdminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // 4. VERIFY DELETED -> 404
        mockMvc.perform(get("/api/admins/" + newAdminId))
                .andExpect(status().isNotFound());
    }
}
