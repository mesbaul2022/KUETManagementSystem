package edu.kuet.ums.service;

import edu.kuet.ums.entity.*;
import edu.kuet.ums.repository.AdminProfileRepository;
import edu.kuet.ums.repository.StudentProfileRepository;
import edu.kuet.ums.repository.TeacherProfileRepository;
import edu.kuet.ums.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final AdminProfileRepository adminProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           AdminProfileRepository adminProfileRepository,
                           TeacherProfileRepository teacherProfileRepository,
                           StudentProfileRepository studentProfileRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.adminProfileRepository = adminProfileRepository;
        this.teacherProfileRepository = teacherProfileRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            logger.info("Database already contains users. Skipping demo data initialization.");
            return;
        }

        logger.info("Initializing KUET Management System demo accounts and profiles...");

        // 1. Initial Admin (Vice-Chancellor)
        User adminUser = new User(
                "admin",
                "admin@kuet.ac.bd",
                passwordEncoder.encode("admin123"),
                Role.ROLE_ADMIN
        );
        AdminProfile adminProfile = new AdminProfile(
                adminUser,
                "Prof. Dr. Mihir Ranjan Halder",
                "KUET-ADM-001",
                "Office of the Vice-Chancellor",
                "Vice-Chancellor & Chief Administrator",
                "+880-41-769468"
        );
        adminProfileRepository.save(adminProfile);

        // 2. Initial Teacher 1 (CSE Professor)
        User teacherUser1 = new User(
                "teacher_rokib",
                "rokibul@cse.kuet.ac.bd",
                passwordEncoder.encode("teacher123"),
                Role.ROLE_TEACHER
        );
        TeacherProfile teacherProfile1 = new TeacherProfile(
                teacherUser1,
                "Prof. Dr. Kazi Md. Rokibul Hasan",
                "KUET-TCH-001",
                "Computer Science & Engineering",
                "Professor & Head",
                "+880-1711-234567",
                "Artificial Intelligence & Distributed Computing"
        );
        teacherProfileRepository.save(teacherProfile1);

        // 3. Initial Teacher 2 (EEE Associate Professor)
        User teacherUser2 = new User(
                "teacher_shibbir",
                "shibbir@eee.kuet.ac.bd",
                passwordEncoder.encode("teacher123"),
                Role.ROLE_TEACHER
        );
        TeacherProfile teacherProfile2 = new TeacherProfile(
                teacherUser2,
                "Dr. Shibbir Ahmed",
                "KUET-TCH-002",
                "Electrical & Electronic Engineering",
                "Associate Professor",
                "+880-1712-345678",
                "Signal Processing & Smart Power Grids"
        );
        teacherProfileRepository.save(teacherProfile2);

        // 4. Initial Student 1 (CSE 2K20)
        User studentUser1 = new User(
                "student_tanvir",
                "tanvir.2007001@stud.kuet.ac.bd",
                passwordEncoder.encode("student123"),
                Role.ROLE_STUDENT
        );
        StudentProfile studentProfile1 = new StudentProfile(
                studentUser1,
                "Tanvir Ahmed",
                "2007001",
                "Computer Science & Engineering",
                "2K20",
                3.89,
                "+880-1811-345678"
        );
        studentProfileRepository.save(studentProfile1);

        // 5. Initial Student 2 (CSE 2K21)
        User studentUser2 = new User(
                "student_nusrat",
                "nusrat.2107042@stud.kuet.ac.bd",
                passwordEncoder.encode("student123"),
                Role.ROLE_STUDENT
        );
        StudentProfile studentProfile2 = new StudentProfile(
                studentUser2,
                "Nusrat Jahan",
                "2107042",
                "Computer Science & Engineering",
                "2K21",
                3.94,
                "+880-1812-456789"
        );
        studentProfileRepository.save(studentProfile2);

        logger.info("Successfully initialized KUET demo users with BCrypt password hashing.");
    }
}
