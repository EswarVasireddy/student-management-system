package com.klu.sms.service;

import com.klu.sms.dao.StudentDAO;
import com.klu.sms.model.Student;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Business rules for managing students, on top of a {@link StudentDAO}.
 * Validation (email format, GPA range, duplicate emails) lives here rather
 * than in the DAO, so it applies no matter which DAO implementation is
 * wired in.
 */
public class StudentService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private final StudentDAO studentDAO;

    public StudentService(StudentDAO studentDAO) {
        this.studentDAO = studentDAO;
    }

    public Student enroll(String name, String email, String major, double gpa) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Name must not be blank");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Invalid email: " + email);
        }
        if (major == null || major.isBlank()) {
            throw new ValidationException("Major must not be blank");
        }
        if (gpa < 0.0 || gpa > 4.0) {
            throw new ValidationException("GPA must be between 0.0 and 4.0, got " + gpa);
        }
        if (studentDAO.existsByEmail(email)) {
            throw new DuplicateEmailException("A student with email " + email + " already exists");
        }
        return studentDAO.save(new Student(name, email, major, gpa));
    }

    public Student updateGpa(int studentId, double newGpa) {
        if (newGpa < 0.0 || newGpa > 4.0) {
            throw new ValidationException("GPA must be between 0.0 and 4.0, got " + newGpa);
        }
        Student student = studentDAO.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("No student with id " + studentId));
        student.setGpa(newGpa);
        studentDAO.update(student);
        return student;
    }

    public Student transferMajor(int studentId, String newMajor) {
        if (newMajor == null || newMajor.isBlank()) {
            throw new ValidationException("Major must not be blank");
        }
        Student student = studentDAO.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("No student with id " + studentId));
        student.setMajor(newMajor);
        studentDAO.update(student);
        return student;
    }

    public boolean withdraw(int studentId) {
        return studentDAO.deleteById(studentId);
    }

    public List<Student> allStudents() {
        return studentDAO.findAll();
    }

    public List<Student> studentsInMajor(String major) {
        return studentDAO.findByMajor(major);
    }

    /** Average GPA across all enrolled students, or 0.0 if none are enrolled. */
    public double averageGpa() {
        List<Student> all = studentDAO.findAll();
        if (all.isEmpty()) {
            return 0.0;
        }
        return all.stream().mapToDouble(Student::getGpa).average().orElse(0.0);
    }

    /** Average GPA within a single major, or 0.0 if no students are in it. */
    public double averageGpaForMajor(String major) {
        List<Student> inMajor = studentDAO.findByMajor(major);
        if (inMajor.isEmpty()) {
            return 0.0;
        }
        return inMajor.stream().mapToDouble(Student::getGpa).average().orElse(0.0);
    }

    /** The top {@code n} students by GPA, highest first. */
    public List<Student> topStudents(int n) {
        return studentDAO.findAll().stream()
                .sorted((a, b) -> Double.compare(b.getGpa(), a.getGpa()))
                .limit(Math.max(0, n))
                .toList();
    }

    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }

    public static class DuplicateEmailException extends RuntimeException {
        public DuplicateEmailException(String message) {
            super(message);
        }
    }

    public static class StudentNotFoundException extends RuntimeException {
        public StudentNotFoundException(String message) {
            super(message);
        }
    }
}
