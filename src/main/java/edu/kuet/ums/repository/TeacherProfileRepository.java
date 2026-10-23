package edu.kuet.ums.repository;

import edu.kuet.ums.entity.TeacherProfile;
import edu.kuet.ums.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherProfileRepository extends JpaRepository<TeacherProfile, Long> {
    Optional<TeacherProfile> findByUser(User user);
    Optional<TeacherProfile> findByUserId(Long userId);
    Optional<TeacherProfile> findByTeacherId(String teacherId);
    boolean existsByTeacherId(String teacherId);
}
