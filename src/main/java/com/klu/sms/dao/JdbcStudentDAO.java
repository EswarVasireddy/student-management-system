package com.klu.sms.dao;

import com.klu.sms.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * MySQL-backed implementation of {@link StudentDAO} using plain JDBC with
 * PreparedStatements throughout (no string-concatenated SQL). A
 * Supplier&lt;Connection&gt; is injected rather than a single shared
 * Connection so each call can pull a fresh connection (or one from a pool)
 * rather than assuming one long-lived connection is always valid.
 */
public class JdbcStudentDAO implements StudentDAO {

    private final Supplier<Connection> connectionSupplier;

    public JdbcStudentDAO(Supplier<Connection> connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
    }

    @Override
    public Student save(Student student) {
        String sql = "INSERT INTO students (name, email, major, gpa) VALUES (?, ?, ?, ?)";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, student.getName());
            ps.setString(2, student.getEmail());
            ps.setString(3, student.getMajor());
            ps.setDouble(4, student.getGpa());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    student.setId(keys.getInt(1));
                }
            }
            return student;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save student " + student.getEmail(), e);
        }
    }

    @Override
    public Optional<Student> findById(int id) {
        String sql = "SELECT id, name, email, major, gpa FROM students WHERE id = ?";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find student " + id, e);
        }
    }

    @Override
    public List<Student> findAll() {
        String sql = "SELECT id, name, email, major, gpa FROM students ORDER BY id";
        List<Student> results = new ArrayList<>();
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list students", e);
        }
    }

    @Override
    public List<Student> findByMajor(String major) {
        // Backed by the idx_major index in schema.sql.
        String sql = "SELECT id, name, email, major, gpa FROM students WHERE major = ? ORDER BY name";
        List<Student> results = new ArrayList<>();
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, major);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find students in major " + major, e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM students WHERE email = ? LIMIT 1";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check email " + email, e);
        }
    }

    @Override
    public void update(Student student) {
        String sql = "UPDATE students SET name = ?, email = ?, major = ?, gpa = ? WHERE id = ?";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getName());
            ps.setString(2, student.getEmail());
            ps.setString(3, student.getMajor());
            ps.setDouble(4, student.getGpa());
            ps.setInt(5, student.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update student " + student.getId(), e);
        }
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM students WHERE id = ?";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete student " + id, e);
        }
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("major"),
                rs.getDouble("gpa")
        );
    }

    /** Wraps checked SQLExceptions so DAO callers don't have to declare them. */
    public static class DataAccessException extends RuntimeException {
        public DataAccessException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
