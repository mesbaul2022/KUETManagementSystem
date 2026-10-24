package edu.kuet.ums.service;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentProfileRepository studentProfileRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.studentProfileRepository = studentProfileRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {
        return studentProfileRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        StudentProfile profile = studentProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
        return mapToResponse(profile);
    }

    @Transactional
    public StudentResponse createStudent(StudentCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        if (studentProfileRepository.existsByRollNumber(request.getRollNumber())) {
            throw new BadRequestException("Roll number is already registered: " + request.getRollNumber());
        }

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.ROLE_STUDENT
        );

        StudentProfile profile = new StudentProfile(
                user,
                request.getFullName(),
                request.getRollNumber(),
                request.getDepartment(),
                request.getSeries(),
                request.getPhone()
        );

        StudentProfile savedProfile = studentProfileRepository.save(profile);
        return mapToResponse(savedProfile);
    }

    @Transactional
    public StudentResponse updateStudent(Long id, StudentUpdateRequest request) {
        StudentProfile profile = studentProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        User user = profile.getUser();

        // Check email uniqueness if changed
        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already taken by another account: " + request.getEmail());
        }

        // Check rollNumber uniqueness if changed
        if (!profile.getRollNumber().equalsIgnoreCase(request.getRollNumber()) && studentProfileRepository.existsByRollNumber(request.getRollNumber())) {
            throw new BadRequestException("Roll number is already registered: " + request.getRollNumber());
        }

        user.setEmail(request.getEmail());
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        profile.setFullName(request.getFullName());
        profile.setRollNumber(request.getRollNumber());
        profile.setDepartment(request.getDepartment());
        profile.setSeries(request.getSeries());
        profile.setPhone(request.getPhone());

        StudentProfile updatedProfile = studentProfileRepository.save(profile);
        return mapToResponse(updatedProfile);
    }

    @Transactional
    public void deleteStudent(Long id) {
        StudentProfile profile = studentProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        studentProfileRepository.delete(profile);
    }

    private StudentResponse mapToResponse(StudentProfile profile) {
        User user = profile.getUser();
        return new StudentResponse(
                profile.getId(),
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                profile.getFullName(),
                profile.getRollNumber(),
                profile.getDepartment(),
                profile.getSeries(),
                profile.getPhone(),
                user.getCreatedAt()
        );
    }
}
