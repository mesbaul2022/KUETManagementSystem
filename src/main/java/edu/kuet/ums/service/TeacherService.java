package edu.kuet.ums.service;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeacherService {

    private final TeacherProfileRepository teacherProfileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TeacherService(TeacherProfileRepository teacherProfileRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.teacherProfileRepository = teacherProfileRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<TeacherResponse> getAllTeachers() {
        return teacherProfileRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeacherResponse getTeacherById(Long id) {
        TeacherProfile profile = teacherProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));
        return mapToResponse(profile);
    }

    @Transactional
    public TeacherResponse createTeacher(TeacherCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        if (teacherProfileRepository.existsByTeacherId(request.getTeacherId())) {
            throw new BadRequestException("Teacher ID is already in use: " + request.getTeacherId());
        }

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.ROLE_TEACHER
        );

        TeacherProfile profile = new TeacherProfile(
                user,
                request.getFullName(),
                request.getTeacherId(),
                request.getDepartment(),
                request.getDesignation(),
                request.getPhone(),
                request.getSpecialization()
        );

        TeacherProfile savedProfile = teacherProfileRepository.save(profile);
        return mapToResponse(savedProfile);
    }

    @Transactional
    public TeacherResponse updateTeacher(Long id, TeacherUpdateRequest request) {
        TeacherProfile profile = teacherProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));

        User user = profile.getUser();

        // Check email uniqueness if changed
        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already taken by another account: " + request.getEmail());
        }

        // Check teacherId uniqueness if changed
        if (!profile.getTeacherId().equalsIgnoreCase(request.getTeacherId()) && teacherProfileRepository.existsByTeacherId(request.getTeacherId())) {
            throw new BadRequestException("Teacher ID is already in use: " + request.getTeacherId());
        }

        user.setEmail(request.getEmail());
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        profile.setFullName(request.getFullName());
        profile.setTeacherId(request.getTeacherId());
        profile.setDepartment(request.getDepartment());
        profile.setDesignation(request.getDesignation());
        profile.setPhone(request.getPhone());
        profile.setSpecialization(request.getSpecialization());

        TeacherProfile updatedProfile = teacherProfileRepository.save(profile);
        return mapToResponse(updatedProfile);
    }

    @Transactional
    public void deleteTeacher(Long id) {
        TeacherProfile profile = teacherProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));

        teacherProfileRepository.delete(profile);
    }

    private TeacherResponse mapToResponse(TeacherProfile profile) {
        User user = profile.getUser();
        return new TeacherResponse(
                profile.getId(),
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                profile.getFullName(),
                profile.getTeacherId(),
                profile.getDepartment(),
                profile.getDesignation(),
                profile.getPhone(),
                profile.getSpecialization(),
                user.getCreatedAt()
        );
    }
}
