package edu.kuet.ums.unit;

import edu.kuet.ums.entity.Role;
import edu.kuet.ums.entity.StudentProfile;
import edu.kuet.ums.entity.User;
import edu.kuet.ums.exception.BadRequestException;
import edu.kuet.ums.payload.request.LoginRequest;
import edu.kuet.ums.payload.response.AuthResponse;
import edu.kuet.ums.repository.AdminProfileRepository;
import edu.kuet.ums.repository.StudentProfileRepository;
import edu.kuet.ums.repository.TeacherProfileRepository;
import edu.kuet.ums.repository.UserRepository;
import edu.kuet.ums.security.JwtTokenProvider;
import edu.kuet.ums.security.UserPrincipal;
import edu.kuet.ums.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests: AuthService Authentication Logic")
class AuthServiceUnitTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdminProfileRepository adminProfileRepository;

    @Mock
    private TeacherProfileRepository teacherProfileRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Unit Test: Authenticate user successfully returns AuthResponse with JWT and profile info")
    void testAuthenticateUser_StudentSuccess() {
        LoginRequest loginRequest = new LoginRequest("student_mesba", "student123");

        User user = new User("student_mesba", "mesbaul@stud.kuet.ac.bd", "hashed", Role.ROLE_STUDENT);
        user.setId(50L);

        UserPrincipal principal = UserPrincipal.create(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("mocked-jwt-token");
        when(userRepository.findById(50L)).thenReturn(Optional.of(user));

        StudentProfile studentProfile = new StudentProfile(user, "Mesbaul Islam", "2207075", "CSE", "2K22", "+8801700000000");
        studentProfile.setId(25L);
        when(studentProfileRepository.findByUser(user)).thenReturn(Optional.of(studentProfile));

        AuthResponse authResponse = authService.authenticateUser(loginRequest);

        assertNotNull(authResponse);
        assertEquals("mocked-jwt-token", authResponse.getAccessToken());
        assertEquals("student_mesba", authResponse.getUsername());
        assertEquals("Mesbaul Islam", authResponse.getFullName());
        assertEquals(25L, authResponse.getProfileId());
        assertEquals("ROLE_STUDENT", authResponse.getRole());
    }

    @Test
    @DisplayName("Unit Test: getCurrentUser throws BadRequestException when user ID is not found in database")
    void testGetCurrentUser_UserNotFound_ThrowsException() {
        User user = new User("ghost_user", "ghost@kuet.ac.bd", "pass", Role.ROLE_STUDENT);
        user.setId(999L);
        UserPrincipal principal = UserPrincipal.create(user);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> authService.getCurrentUser(principal));
    }
}
