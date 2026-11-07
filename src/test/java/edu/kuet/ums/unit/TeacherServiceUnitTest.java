package edu.kuet.ums.unit;

import edu.kuet.ums.entity.Role;
import edu.kuet.ums.entity.TeacherProfile;
import edu.kuet.ums.entity.User;
import edu.kuet.ums.exception.BadRequestException;
import edu.kuet.ums.exception.ResourceNotFoundException;
import edu.kuet.ums.payload.request.TeacherCreateRequest;
import edu.kuet.ums.payload.request.TeacherUpdateRequest;
import edu.kuet.ums.payload.response.TeacherResponse;
import edu.kuet.ums.repository.TeacherProfileRepository;
import edu.kuet.ums.repository.UserRepository;
import edu.kuet.ums.service.TeacherService;
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
@DisplayName("Unit Tests: TeacherService Business Logic")
class TeacherServiceUnitTest {

    @Mock
    private TeacherProfileRepository teacherProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TeacherService teacherService;

    private User sampleTeacherUser;
    private TeacherProfile sampleTeacherProfile;

    @BeforeEach
    void setUp() {
        sampleTeacherUser = new User("teacher_test", "teacher_test@cse.kuet.ac.bd", "hashed_pass", Role.ROLE_TEACHER);
        sampleTeacherUser.setId(15L);
        sampleTeacherUser.setCreatedAt(LocalDateTime.now());

        sampleTeacherProfile = new TeacherProfile(
                sampleTeacherUser,
                "Dr. Test Teacher",
                "KUET-TCH-999",
                "CSE",
                "Associate Professor",
                "+8801800000000",
                "Artificial Intelligence"
        );
        sampleTeacherProfile.setId(5L);
    }

    @Test
    @DisplayName("Unit Test: getAllTeachers returns list of teacher DTOs")
    void testGetAllTeachers_ReturnsList() {
        when(teacherProfileRepository.findAll()).thenReturn(List.of(sampleTeacherProfile));

        List<TeacherResponse> teachers = teacherService.getAllTeachers();

        assertNotNull(teachers);
        assertEquals(1, teachers.size());
        assertEquals("Dr. Test Teacher", teachers.get(0).getFullName());
        assertEquals("KUET-TCH-999", teachers.get(0).getTeacherId());
        assertEquals("ROLE_TEACHER", teachers.get(0).getRole());
        verify(teacherProfileRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Unit Test: getTeacherById returns teacher DTO when present")
    void testGetTeacherById_Success() {
        when(teacherProfileRepository.findById(5L)).thenReturn(Optional.of(sampleTeacherProfile));

        TeacherResponse teacher = teacherService.getTeacherById(5L);

        assertNotNull(teacher);
        assertEquals("KUET-TCH-999", teacher.getTeacherId());
        assertEquals("CSE", teacher.getDepartment());
        verify(teacherProfileRepository, times(1)).findById(5L);
    }

    @Test
    @DisplayName("Unit Test: getTeacherById throws ResourceNotFoundException when absent")
    void testGetTeacherById_NotFound_ThrowsException() {
        when(teacherProfileRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teacherService.getTeacherById(100L));
        verify(teacherProfileRepository, times(1)).findById(100L);
    }

    @Test
    @DisplayName("Unit Test: createTeacher successfully creates user with ROLE_TEACHER and profile")
    void testCreateTeacher_Success() {
        TeacherCreateRequest request = new TeacherCreateRequest();
        request.setUsername("teacher_alam");
        request.setEmail("alam@cse.kuet.ac.bd");
        request.setPassword("pass12345");
        request.setFullName("Kazi Saeed Alam");
        request.setTeacherId("KUET-TCH-100");
        request.setDepartment("CSE");
        request.setDesignation("Assistant Professor");
        request.setPhone("+8801722222222");
        request.setSpecialization("Software Engineering");

        when(userRepository.existsByUsername("teacher_alam")).thenReturn(false);
        when(userRepository.existsByEmail("alam@cse.kuet.ac.bd")).thenReturn(false);
        when(teacherProfileRepository.existsByTeacherId("KUET-TCH-100")).thenReturn(false);
        when(passwordEncoder.encode("pass12345")).thenReturn("encodedPass");

        User savedUser = new User("teacher_alam", "alam@cse.kuet.ac.bd", "encodedPass", Role.ROLE_TEACHER);
        savedUser.setId(30L);
        savedUser.setCreatedAt(LocalDateTime.now());

        TeacherProfile savedProfile = new TeacherProfile(
                savedUser,
                request.getFullName(),
                request.getTeacherId(),
                request.getDepartment(),
                request.getDesignation(),
                request.getPhone(),
                request.getSpecialization()
        );
        savedProfile.setId(12L);

        when(teacherProfileRepository.save(any(TeacherProfile.class))).thenReturn(savedProfile);

        TeacherResponse response = teacherService.createTeacher(request);

        assertNotNull(response);
        assertEquals("Kazi Saeed Alam", response.getFullName());
        assertEquals("ROLE_TEACHER", response.getRole());
        assertEquals("Software Engineering", response.getSpecialization());
        verify(passwordEncoder, times(1)).encode("pass12345");
        verify(teacherProfileRepository, times(1)).save(any(TeacherProfile.class));
    }

    @Test
    @DisplayName("Unit Test: createTeacher throws BadRequestException on duplicate teacherId")
    void testCreateTeacher_DuplicateTeacherId_ThrowsBadRequest() {
        TeacherCreateRequest request = new TeacherCreateRequest();
        request.setUsername("teacher_unique");
        request.setEmail("unique@cse.kuet.ac.bd");
        request.setTeacherId("KUET-TCH-999");

        when(userRepository.existsByUsername("teacher_unique")).thenReturn(false);
        when(userRepository.existsByEmail("unique@cse.kuet.ac.bd")).thenReturn(false);
        when(teacherProfileRepository.existsByTeacherId("KUET-TCH-999")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> teacherService.createTeacher(request));
        assertTrue(ex.getMessage().contains("Teacher ID is already in use"));
        verify(teacherProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("Unit Test: deleteTeacher deletes profile when found")
    void testDeleteTeacher_Success() {
        when(teacherProfileRepository.findById(5L)).thenReturn(Optional.of(sampleTeacherProfile));
        doNothing().when(teacherProfileRepository).delete(sampleTeacherProfile);

        assertDoesNotThrow(() -> teacherService.deleteTeacher(5L));
        verify(teacherProfileRepository, times(1)).delete(sampleTeacherProfile);
    }
}
