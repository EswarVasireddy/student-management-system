package com.klu.sms.dao;

import com.klu.sms.model.Student;

import java.util.List;
import java.util.Optional;

/**
 * Persistence boundary for Student records. Kept as an interface so the
 * service layer (and its unit tests) can run against an in-memory
 * implementation without a real database, while production code uses the
 * JDBC-backed implementation.
 */
public interface StudentDAO {

    Student save(Student student);

    Optional<Student> findById(int id);

    List<Student> findAll();

    List<Student> findByMajor(String major);

    boolean existsByEmail(String email);

    void update(Student student);

    boolean deleteById(int id);
}
