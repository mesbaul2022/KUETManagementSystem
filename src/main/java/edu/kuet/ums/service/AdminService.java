package edu.kuet.ums.service;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final AdminProfileRepository adminProfileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(AdminProfileRepository adminProfileRepository,
                        UserRepository userRepository,
                        PasswordEncoder passwordEncoder) {
        this.adminProfileRepository = adminProfileRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AdminResponse> getAllAdmins() {
        return adminProfileRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminResponse getAdminById(Long id) {
        AdminProfile profile = adminProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", id));
        return mapToResponse(profile);
    }

    @Transactional
    public AdminResponse createAdmin(AdminCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        if (adminProfileRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new BadRequestException("Employee ID is already in use: " + request.getEmployeeId());
        }

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.ROLE_ADMIN
        );

        AdminProfile profile = new AdminProfile(
                user,
                request.getFullName(),
                request.getEmployeeId(),
                request.getDepartment(),
                request.getDesignation(),
                request.getPhone()
        );

        AdminProfile savedProfile = adminProfileRepository.save(profile);
        return mapToResponse(savedProfile);
    }

    @Transactional
    public AdminResponse updateAdmin(Long id, AdminUpdateRequest request) {
        AdminProfile profile = adminProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", id));

        User user = profile.getUser();

        // Check email uniqueness if modified
        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already taken by another account: " + request.getEmail());
        }

        // Check employeeId uniqueness if modified
        if (!profile.getEmployeeId().equalsIgnoreCase(request.getEmployeeId()) && adminProfileRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new BadRequestException("Employee ID is already in use: " + request.getEmployeeId());
        }

        user.setEmail(request.getEmail());
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        profile.setFullName(request.getFullName());
        profile.setEmployeeId(request.getEmployeeId());
        profile.setDepartment(request.getDepartment());
        profile.setDesignation(request.getDesignation());
        profile.setPhone(request.getPhone());

        AdminProfile updatedProfile = adminProfileRepository.save(profile);
        return mapToResponse(updatedProfile);
    }

    @Transactional
    public void deleteAdmin(Long id) {
        AdminProfile profile = adminProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", id));

        // Prevent deleting the last administrator
        if (adminProfileRepository.count() <= 1) {
            throw new BadRequestException("Cannot delete the only remaining administrator in the system.");
        }

        adminProfileRepository.delete(profile);
    }

    private AdminResponse mapToResponse(AdminProfile profile) {
        User user = profile.getUser();
        return new AdminResponse(
                profile.getId(),
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                profile.getFullName(),
                profile.getEmployeeId(),
                profile.getDepartment(),
                profile.getDesignation(),
                profile.getPhone(),
                user.getCreatedAt()
        );
    }
}
