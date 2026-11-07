package edu.kuet.ums.unit;

import edu.kuet.ums.entity.AdminProfile;
import edu.kuet.ums.entity.Role;
import edu.kuet.ums.entity.User;
import edu.kuet.ums.exception.BadRequestException;
import edu.kuet.ums.exception.ResourceNotFoundException;
import edu.kuet.ums.payload.request.AdminCreateRequest;
import edu.kuet.ums.payload.request.AdminUpdateRequest;
import edu.kuet.ums.payload.response.AdminResponse;
import edu.kuet.ums.repository.AdminProfileRepository;
import edu.kuet.ums.repository.UserRepository;
import edu.kuet.ums.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests: AdminService Business Logic")
class AdminServiceUnitTest {

    @Mock
    private AdminProfileRepository adminProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    private User sampleAdminUser;
    private AdminProfile sampleAdminProfile;

    @BeforeEach
    void setUp() {
        sampleAdminUser = new User("admin_test", "admin_test@kuet.ac.bd", "hashed_pass", Role.ROLE_ADMIN);
        sampleAdminUser.setId(100L);
        sampleAdminUser.setCreatedAt(LocalDateTime.now());

        sampleAdminProfile = new AdminProfile(
                sampleAdminUser,
                "Dr. Administrator",
                "KUET-ADM-999",
                "VC Office",
                "Vice-Chancellor",
                "+8801700000000"
        );
        sampleAdminProfile.setId(10L);
    }

    @Test
    @DisplayName("Unit Test: getAllAdmins returns mapped list of admin DTOs")
    void testGetAllAdmins_ReturnsList() {
        when(adminProfileRepository.findAll()).thenReturn(List.of(sampleAdminProfile));

        List<AdminResponse> list = adminService.getAllAdmins();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Dr. Administrator", list.get(0).getFullName());
        assertEquals("KUET-ADM-999", list.get(0).getEmployeeId());
        assertEquals("ROLE_ADMIN", list.get(0).getRole());
        verify(adminProfileRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Unit Test: getAdminById returns admin DTO when found")
    void testGetAdminById_Success() {
        when(adminProfileRepository.findById(10L)).thenReturn(Optional.of(sampleAdminProfile));

        AdminResponse admin = adminService.getAdminById(10L);

        assertNotNull(admin);
        assertEquals(10L, admin.getId());
        assertEquals("admin_test", admin.getUsername());
        verify(adminProfileRepository, times(1)).findById(10L);
    }

    @Test
    @DisplayName("Unit Test: getAdminById throws ResourceNotFoundException when not found")
    void testGetAdminById_NotFound_ThrowsException() {
        when(adminProfileRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.getAdminById(999L));
        verify(adminProfileRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Unit Test: createAdmin successfully encodes password and persists admin")
    void testCreateAdmin_Success() {
        AdminCreateRequest request = new AdminCreateRequest();
        request.setUsername("admin_dean");
        request.setEmail("dean@kuet.ac.bd");
        request.setPassword("secureAdminPass");
        request.setFullName("Prof. Dr. Dean");
        request.setEmployeeId("KUET-ADM-005");
        request.setDepartment("Faculty of EEE");
        request.setDesignation("Dean");
        request.setPhone("+8801711223344");

        when(userRepository.existsByUsername("admin_dean")).thenReturn(false);
        when(userRepository.existsByEmail("dean@kuet.ac.bd")).thenReturn(false);
        when(adminProfileRepository.existsByEmployeeId("KUET-ADM-005")).thenReturn(false);
        when(passwordEncoder.encode("secureAdminPass")).thenReturn("hashedPass");

        User savedUser = new User("admin_dean", "dean@kuet.ac.bd", "hashedPass", Role.ROLE_ADMIN);
        savedUser.setId(101L);
        savedUser.setCreatedAt(LocalDateTime.now());

        AdminProfile savedProfile = new AdminProfile(
                savedUser,
                request.getFullName(),
                request.getEmployeeId(),
                request.getDepartment(),
                request.getDesignation(),
                request.getPhone()
        );
        savedProfile.setId(11L);

        when(adminProfileRepository.save(any(AdminProfile.class))).thenReturn(savedProfile);

        AdminResponse response = adminService.createAdmin(request);

        assertNotNull(response);
        assertEquals("Prof. Dr. Dean", response.getFullName());
        assertEquals("ROLE_ADMIN", response.getRole());
        verify(passwordEncoder, times(1)).encode("secureAdminPass");
        verify(adminProfileRepository, times(1)).save(any(AdminProfile.class));
    }

    @Test
    @DisplayName("Unit Test: createAdmin throws BadRequestException on duplicate employeeId")
    void testCreateAdmin_DuplicateEmployeeId_ThrowsBadRequest() {
        AdminCreateRequest request = new AdminCreateRequest();
        request.setUsername("admin_new");
        request.setEmail("admin_new@kuet.ac.bd");
        request.setEmployeeId("KUET-ADM-999");

        when(userRepository.existsByUsername("admin_new")).thenReturn(false);
        when(userRepository.existsByEmail("admin_new@kuet.ac.bd")).thenReturn(false);
        when(adminProfileRepository.existsByEmployeeId("KUET-ADM-999")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> adminService.createAdmin(request));
        assertTrue(ex.getMessage().contains("Employee ID is already in use"));
        verify(adminProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("Unit Test: deleteAdmin throws BadRequestException when trying to delete the last admin")
    void testDeleteAdmin_LastAdmin_ThrowsBadRequest() {
        when(adminProfileRepository.findById(10L)).thenReturn(Optional.of(sampleAdminProfile));
        when(adminProfileRepository.count()).thenReturn(1L);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> adminService.deleteAdmin(10L));
        assertTrue(ex.getMessage().contains("Cannot delete the only remaining administrator"));
        verify(adminProfileRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Unit Test: deleteAdmin successfully removes profile when more than 1 admin exists")
    void testDeleteAdmin_Success() {
        when(adminProfileRepository.findById(10L)).thenReturn(Optional.of(sampleAdminProfile));
        when(adminProfileRepository.count()).thenReturn(3L);
        doNothing().when(adminProfileRepository).delete(sampleAdminProfile);

        assertDoesNotThrow(() -> adminService.deleteAdmin(10L));
        verify(adminProfileRepository, times(1)).delete(sampleAdminProfile);
    }
}
