package com.klu.sms.dao;

import com.klu.sms.model.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory implementation of {@link StudentDAO}. Used by the unit tests
 * (see StudentServiceTest) and by Main when run without a database
 * configured, so the business logic in StudentService can be exercised
 * without standing up MySQL.
 */
public class InMemoryStudentDAO implements StudentDAO {

    private final Map<Integer, Student> store = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    @Override
    public Student save(Student student) {
        int id = nextId.getAndIncrement();
        student.setId(id);
        store.put(id, student);
        return student;
    }

    @Override
    public Optional<Student> findById(int id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Student> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Student> findByMajor(String major) {
        return store.values().stream()
                .filter(s -> s.getMajor().equalsIgnoreCase(major))
                .toList();
    }

    @Override
    public boolean existsByEmail(String email) {
        return store.values().stream().anyMatch(s -> s.getEmail().equalsIgnoreCase(email));
    }

    @Override
    public void update(Student student) {
        if (student.getId() == null || !store.containsKey(student.getId())) {
            throw new IllegalArgumentException("Cannot update a student that hasn't been saved: " + student);
        }
        store.put(student.getId(), student);
    }

    @Override
    public boolean deleteById(int id) {
        return store.remove(id) != null;
    }
}
