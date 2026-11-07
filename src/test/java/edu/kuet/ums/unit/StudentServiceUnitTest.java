package edu.kuet.ums.unit;

import edu.kuet.ums.entity.Role;
import edu.kuet.ums.entity.StudentProfile;
import edu.kuet.ums.entity.User;
import edu.kuet.ums.exception.BadRequestException;
import edu.kuet.ums.exception.ResourceNotFoundException;
import edu.kuet.ums.payload.request.StudentCreateRequest;
import edu.kuet.ums.payload.request.StudentUpdateRequest;
import edu.kuet.ums.payload.response.StudentResponse;
import edu.kuet.ums.repository.StudentProfileRepository;
import edu.kuet.ums.repository.UserRepository;
import edu.kuet.ums.service.StudentService;
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
@DisplayName("Unit Tests: StudentService Business Logic")
class StudentServiceUnitTest {

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentService studentService;

    private User sampleUser;
    private StudentProfile sampleProfile;

    @BeforeEach
    void setUp() {
        sampleUser = new User("student_test", "student_test@kuet.ac.bd", "hashed_pass", Role.ROLE_STUDENT);
        sampleUser.setId(10L);
        sampleUser.setCreatedAt(LocalDateTime.now());

        sampleProfile = new StudentProfile(
                sampleUser,
                "Test Student",
                "2207999",
                "Computer Science & Engineering",
                "2K22",
                "+8801700000000"
        );
        sampleProfile.setId(1L);
    }

    @Test
    @DisplayName("Unit Test 1: getAllStudents returns mapped list of student responses")
    void testGetAllStudents_ReturnsList() {
        when(studentProfileRepository.findAll()).thenReturn(List.of(sampleProfile));

        List<StudentResponse> result = studentService.getAllStudents();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Student", result.get(0).getFullName());
        assertEquals("2207999", result.get(0).getRollNumber());
        assertEquals("ROLE_STUDENT", result.get(0).getRole());
        verify(studentProfileRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Unit Test 2: getStudentById returns student response when found")
    void testGetStudentById_Success() {
        when(studentProfileRepository.findById(1L)).thenReturn(Optional.of(sampleProfile));

        StudentResponse result = studentService.getStudentById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("student_test", result.getUsername());
        assertEquals("2207999", result.getRollNumber());
        verify(studentProfileRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Unit Test 3: getStudentById throws ResourceNotFoundException when id does not exist")
    void testGetStudentById_NotFound_ThrowsException() {
        when(studentProfileRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.getStudentById(999L));
        verify(studentProfileRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Unit Test 4: createStudent hashes password, saves entity, and returns response")
    void testCreateStudent_Success() {
        StudentCreateRequest request = new StudentCreateRequest();
        request.setUsername("student_new");
        request.setEmail("new@kuet.ac.bd");
        request.setPassword("rawPassword123");
        request.setFullName("New Student");
        request.setRollNumber("2207100");
        request.setDepartment("CSE");
        request.setSeries("2K22");
        request.setPhone("+8801711111111");

        when(userRepository.existsByUsername("student_new")).thenReturn(false);
        when(userRepository.existsByEmail("new@kuet.ac.bd")).thenReturn(false);
        when(studentProfileRepository.existsByRollNumber("2207100")).thenReturn(false);
        when(passwordEncoder.encode("rawPassword123")).thenReturn("encodedPassword123");

        User savedUser = new User("student_new", "new@kuet.ac.bd", "encodedPassword123", Role.ROLE_STUDENT);
        savedUser.setId(20L);
        savedUser.setCreatedAt(LocalDateTime.now());

        StudentProfile savedProfile = new StudentProfile(
                savedUser,
                request.getFullName(),
                request.getRollNumber(),
                request.getDepartment(),
                request.getSeries(),
                request.getPhone()
        );
        savedProfile.setId(2L);

        when(studentProfileRepository.save(any(StudentProfile.class))).thenReturn(savedProfile);

        StudentResponse response = studentService.createStudent(request);

        assertNotNull(response);
        assertEquals("New Student", response.getFullName());
        assertEquals("2207100", response.getRollNumber());
        assertEquals("ROLE_STUDENT", response.getRole());
        verify(passwordEncoder, times(1)).encode("rawPassword123");
        verify(studentProfileRepository, times(1)).save(any(StudentProfile.class));
    }

    @Test
    @DisplayName("Unit Test 5: createStudent throws BadRequestException on duplicate username")
    void testCreateStudent_DuplicateUsername_ThrowsBadRequest() {
        StudentCreateRequest request = new StudentCreateRequest();
        request.setUsername("student_test");
        request.setEmail("different@kuet.ac.bd");

        when(userRepository.existsByUsername("student_test")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> studentService.createStudent(request));
        assertTrue(ex.getMessage().contains("Username is already taken"));
        verify(studentProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("Unit Test 6: createStudent throws BadRequestException on duplicate roll number")
    void testCreateStudent_DuplicateRollNumber_ThrowsBadRequest() {
        StudentCreateRequest request = new StudentCreateRequest();
        request.setUsername("student_unique");
        request.setEmail("unique@kuet.ac.bd");
        request.setRollNumber("2207999");

        when(userRepository.existsByUsername("student_unique")).thenReturn(false);
        when(userRepository.existsByEmail("unique@kuet.ac.bd")).thenReturn(false);
        when(studentProfileRepository.existsByRollNumber("2207999")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> studentService.createStudent(request));
        assertTrue(ex.getMessage().contains("Roll number is already registered"));
        verify(studentProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("Unit Test 7: updateStudent modifies details and encodes password if supplied")
    void testUpdateStudent_Success() {
        StudentUpdateRequest request = new StudentUpdateRequest();
        request.setEmail("updated@kuet.ac.bd");
        request.setFullName("Updated Full Name");
        request.setRollNumber("2207999");
        request.setDepartment("Computer Science & Engineering");
        request.setSeries("2K22");
        request.setPhone("+8801999999999");
        request.setPassword("newSecretPass");

        when(studentProfileRepository.findById(1L)).thenReturn(Optional.of(sampleProfile));
        when(userRepository.existsByEmail("updated@kuet.ac.bd")).thenReturn(false);
        when(passwordEncoder.encode("newSecretPass")).thenReturn("hashedNewSecretPass");
        when(studentProfileRepository.save(any(StudentProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudentResponse updated = studentService.updateStudent(1L, request);

        assertNotNull(updated);
        assertEquals("Updated Full Name", updated.getFullName());
        assertEquals("+8801999999999", updated.getPhone());
        assertEquals("updated@kuet.ac.bd", updated.getEmail());
        verify(passwordEncoder, times(1)).encode("newSecretPass");
        verify(studentProfileRepository, times(1)).save(sampleProfile);
    }

    @Test
    @DisplayName("Unit Test 8: deleteStudent removes profile when found, or throws when not found")
    void testDeleteStudent_Scenarios() {
        when(studentProfileRepository.findById(1L)).thenReturn(Optional.of(sampleProfile));
        doNothing().when(studentProfileRepository).delete(sampleProfile);

        assertDoesNotThrow(() -> studentService.deleteStudent(1L));
        verify(studentProfileRepository, times(1)).delete(sampleProfile);

        when(studentProfileRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> studentService.deleteStudent(999L));
    }
}
