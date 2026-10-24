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

        logger.info("Initializing KUET Management System accounts and profiles...");

        // ================= 1. Executive Administrators & Deans =================
        createAdmin("admin", "admin@kuet.ac.bd", "admin123",
                "Professor Dr. Mohammad Mashud", "KUET-ADM-001",
                "VC Office", "Vice-Chancellor & Chief Administrator", "+880-41-769468");

        createAdmin("admin_mahbub", "mahbub@ce.kuet.ac.bd", "admin123",
                "Dr. Khondoker Mahbub Hassan", "KUET-ADM-002",
                "Faculty of Civil Engineering", "Dean", "+880-1711-201001");

        createAdmin("admin_rokibul", "rokibul@eee.kuet.ac.bd", "admin123",
                "Dr. Kazi Md. Rokibul Alam", "KUET-ADM-003",
                "Faculty of Electrical and Electronic Engineering", "Dean", "+880-1711-201002");

        createAdmin("admin_sultan", "sultan@me.kuet.ac.bd", "admin123",
                "Dr. Mohammad Sultan Mahmud", "KUET-ADM-004",
                "Faculty of Mechanical Engineering", "Dean", "+880-1711-201003");

        createAdmin("admin_yousuf", "yousuf@hum.kuet.ac.bd", "admin123",
                "Dr. Mohammad Abu Yousuf", "KUET-ADM-005",
                "Faculty of Science and Humanities", "Dean", "+880-1711-201004");

        // ================= 2. Academic Faculty & Teachers =================
        createTeacher("teacher_mahmud", "al-mahmud@cse.kuet.ac.bd", "teacher123",
                "Prof. Dr. Al-Mahmud", "KUET-TCH-001",
                "Computer Science & Engineering", "Professor & Head", "+880-1711-234567",
                "Artificial Intelligence & Computer Vision");

        createTeacher("teacher_shibbir", "shibbir@eee.kuet.ac.bd", "teacher123",
                "Dr. Shibbir Ahmed", "KUET-TCH-002",
                "Electrical & Electronic Engineering", "Associate Professor", "+880-1712-345678",
                "Signal Processing & Smart Power Grids");

        createTeacher("teacher_saeed", "saeed.alam@cse.kuet.ac.bd", "teacher123",
                "Kazi Saeed Alam", "KUET-TCH-003",
                "Computer Science & Engineering", "Assistant Professor", "+880-1713-112201",
                "Data Science & Machine Learning");

        createTeacher("teacher_repon", "repon@cse.kuet.ac.bd", "teacher123",
                "Md. Repon Islam", "KUET-TCH-004",
                "Computer Science & Engineering", "Assistant Professor", "+880-1713-112202",
                "Cloud Computing & Cybersecurity");

        createTeacher("teacher_sakhawat", "sakhawat@cse.kuet.ac.bd", "teacher123",
                "Md. Sakhawat Hossain", "KUET-TCH-005",
                "Computer Science & Engineering", "Assistant Professor", "+880-1713-112203",
                "Software Engineering & IoT");

        createTeacher("teacher_shawon", "shawon@cse.kuet.ac.bd", "teacher123",
                "Md Nazirulhasan Shawon", "KUET-TCH-006",
                "Computer Science & Engineering", "Assistant Professor", "+880-1713-112204",
                "Computer Networks & Distributed Systems");

        createTeacher("teacher_isha", "isha@cse.kuet.ac.bd", "teacher123",
                "Most. Kaniz Fatema Isha", "KUET-TCH-007",
                "Computer Science & Engineering", "Lecturer", "+880-1714-223301",
                "Natural Language Processing");

        createTeacher("teacher_tajmilur", "tajmilur@cse.kuet.ac.bd", "teacher123",
                "Md Tajmilur Rahman", "KUET-TCH-008",
                "Computer Science & Engineering", "Lecturer", "+880-1714-223302",
                "Algorithms & Computational Complexity");

        createTeacher("teacher_nfs", "nfs@cse.kuet.ac.bd", "teacher123",
                "Nabil Faiyaz Sadi", "KUET-TCH-009",
                "Computer Science & Engineering", "Lecturer", "+880-1714-223303",
                "Human-Computer Interaction");

        createTeacher("teacher_nawar", "Nawar@cse.kuet.ac.bd", "teacher123",
                "Subah Nawar", "KUET-TCH-010",
                "Computer Science & Engineering", "Lecturer", "+880-1714-223304",
                "Embedded Systems & Robotics");

        createTeacher("teacher_waliul", "waliul@cse.kuet.ac.bd", "teacher123",
                "Waliul Islam Sumon", "KUET-TCH-011",
                "Computer Science & Engineering", "Lecturer", "+880-1714-223305",
                "Database Systems & Big Data Analytics");

        createTeacher("teacher_man", "man@cse.kuet.ac.bd", "teacher123",
                "Md. Mubtashim Abrar Nihal", "KUET-TCH-012",
                "Computer Science & Engineering", "Lecturer", "+880-1714-223306",
                "Computer Vision & Deep Learning");

        createTeacher("teacher_deya", "deya@cse.kuet.ac.bd", "teacher123",
                "Lamisa Bintee Mizan Deya", "KUET-TCH-013",
                "Computer Science & Engineering", "Lecturer", "+880-1714-223307",
                "Bioinformatics & Health Informatics");

        createTeacher("teacher_sadia", "sadia@cse.kuet.ac.bd", "teacher123",
                "Mst. Sadia Islam", "KUET-TCH-014",
                "Computer Science & Engineering", "Lecturer", "+880-1714-223308",
                "Web Technologies & Information Systems");

        // ================= 3. Registered Students (CGPA Removed) =================
        createStudent("student_mesba", "mesbaul.2207075@stud.kuet.ac.bd", "student123",
                "Mesbaul Islam", "2207075", "Computer Science & Engineering", "2K22", "+880-1721-750011");

        createStudent("student_evon", "evon.2207074@stud.kuet.ac.bd", "student123",
                "Ahmed Evon", "2207074", "Computer Science & Engineering", "2K22", "+880-1721-740022");

        createStudent("student_safiul", "safiul.2207068@stud.kuet.ac.bd", "student123",
                "Safiul Islam", "2207068", "Computer Science & Engineering", "2K22", "+880-1721-680033");

        createStudent("student_torikul", "torikul.2207078@stud.kuet.ac.bd", "student123",
                "Torikul Islam", "2207078", "Computer Science & Engineering", "2K22", "+880-1721-780044");

        logger.info("Successfully initialized KUET demo users with BCrypt password hashing.");
    }

    private void createAdmin(String username, String email, String password,
                             String fullName, String employeeId, String department,
                             String designation, String phone) {
        User user = new User(username, email, passwordEncoder.encode(password), Role.ROLE_ADMIN);
        AdminProfile profile = new AdminProfile(user, fullName, employeeId, department, designation, phone);
        adminProfileRepository.save(profile);
    }

    private void createTeacher(String username, String email, String password,
                               String fullName, String teacherId, String department,
                               String designation, String phone, String specialization) {
        User user = new User(username, email, passwordEncoder.encode(password), Role.ROLE_TEACHER);
        TeacherProfile profile = new TeacherProfile(user, fullName, teacherId, department, designation, phone, specialization);
        teacherProfileRepository.save(profile);
    }

    private void createStudent(String username, String email, String password,
                               String fullName, String rollNumber, String department,
                               String series, String phone) {
        User user = new User(username, email, passwordEncoder.encode(password), Role.ROLE_STUDENT);
        StudentProfile profile = new StudentProfile(user, fullName, rollNumber, department, series, phone);
        studentProfileRepository.save(profile);
    }
}
