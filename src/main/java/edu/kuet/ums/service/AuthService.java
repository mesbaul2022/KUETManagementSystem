package edu.kuet.ums.service;

import edu.kuet.ums.entity.AdminProfile;
import edu.kuet.ums.entity.Role;
import edu.kuet.ums.entity.StudentProfile;
import edu.kuet.ums.entity.TeacherProfile;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final AdminProfileRepository adminProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final JwtTokenProvider tokenProvider;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       AdminProfileRepository adminProfileRepository,
                       TeacherProfileRepository teacherProfileRepository,
                       StudentProfileRepository studentProfileRepository,
                       JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.adminProfileRepository = adminProfileRepository;
        this.teacherProfileRepository = teacherProfileRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.tokenProvider = tokenProvider;
    }

    @Transactional(readOnly = true)
    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new BadRequestException("User record not found"));

        String fullName = user.getUsername();
        Long profileId = null;

        if (user.getRole() == Role.ROLE_ADMIN) {
            AdminProfile profile = adminProfileRepository.findByUser(user).orElse(null);
            if (profile != null) {
                fullName = profile.getFullName();
                profileId = profile.getId();
            }
        } else if (user.getRole() == Role.ROLE_TEACHER) {
            TeacherProfile profile = teacherProfileRepository.findByUser(user).orElse(null);
            if (profile != null) {
                fullName = profile.getFullName();
                profileId = profile.getId();
            }
        } else if (user.getRole() == Role.ROLE_STUDENT) {
            StudentProfile profile = studentProfileRepository.findByUser(user).orElse(null);
            if (profile != null) {
                fullName = profile.getFullName();
                profileId = profile.getId();
            }
        }

        return new AuthResponse(
                jwt,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                fullName,
                profileId
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse getCurrentUser(UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new BadRequestException("User record not found"));

        String fullName = user.getUsername();
        Long profileId = null;

        if (user.getRole() == Role.ROLE_ADMIN) {
            AdminProfile profile = adminProfileRepository.findByUser(user).orElse(null);
            if (profile != null) {
                fullName = profile.getFullName();
                profileId = profile.getId();
            }
        } else if (user.getRole() == Role.ROLE_TEACHER) {
            TeacherProfile profile = teacherProfileRepository.findByUser(user).orElse(null);
            if (profile != null) {
                fullName = profile.getFullName();
                profileId = profile.getId();
            }
        } else if (user.getRole() == Role.ROLE_STUDENT) {
            StudentProfile profile = studentProfileRepository.findByUser(user).orElse(null);
            if (profile != null) {
                fullName = profile.getFullName();
                profileId = profile.getId();
            }
        }

        return new AuthResponse(
                null,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                fullName,
                profileId
        );
    }
}
