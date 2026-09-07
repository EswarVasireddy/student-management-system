package com.klu.sms.model;

import java.util.Objects;

/**
 * A student record. Immutable identity fields (id assigned by the store)
 * plus mutable major/gpa, since those are the fields that legitimately
 * change over a student's enrollment.
 */
public class Student {

    private Integer id;
    private final String name;
    private final String email;
    private String major;
    private double gpa;

    public Student(String name, String email, String major, double gpa) {
        this(null, name, email, major, gpa);
    }

    public Student(Integer id, String name, String email, String major, double gpa) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.major = Objects.requireNonNull(major, "major must not be null");
        this.gpa = gpa;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = Objects.requireNonNull(major, "major must not be null");
    }

    public double getGpa() {
        return gpa;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student student)) return false;
        return Objects.equals(id, student.id) && email.equals(student.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "Student{id=%d, name='%s', email='%s', major='%s', gpa=%.2f}"
                .formatted(id, name, email, major, gpa);
    }
}
