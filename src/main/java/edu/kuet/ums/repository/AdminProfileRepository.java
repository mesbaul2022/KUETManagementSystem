package edu.kuet.ums.repository;

import edu.kuet.ums.entity.AdminProfile;
import edu.kuet.ums.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminProfileRepository extends JpaRepository<AdminProfile, Long> {
    Optional<AdminProfile> findByUser(User user);
    Optional<AdminProfile> findByUserId(Long userId);
    Optional<AdminProfile> findByEmployeeId(String employeeId);
    boolean existsByEmployeeId(String employeeId);
}
