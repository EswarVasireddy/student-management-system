package com.klu.sms.service;

import com.klu.sms.dao.InMemoryStudentDAO;
import com.klu.sms.dao.StudentDAO;
import com.klu.sms.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentServiceTest {

    private StudentDAO dao;
    private StudentService service;

    @BeforeEach
    void setUp() {
        dao = new InMemoryStudentDAO();
        service = new StudentService(dao);
    }

    @Test
    void enrollAssignsAnIdAndPersists() {
        Student s = service.enroll("Asha Patel", "asha.patel@example.com", "Computer Science", 3.8);

        assertEquals(1, s.getId());
        assertEquals(1, service.allStudents().size());
    }

    @Test
    void enrollRejectsInvalidEmail() {
        assertThrows(StudentService.ValidationException.class,
                () -> service.enroll("Bad Email", "not-an-email", "CS", 3.0));
    }

    @Test
    void enrollRejectsGpaOutOfRange() {
        assertThrows(StudentService.ValidationException.class,
                () -> service.enroll("Too Good", "too.good@example.com", "CS", 4.5));
        assertThrows(StudentService.ValidationException.class,
                () -> service.enroll("Too Bad", "too.bad@example.com", "CS", -1.0));
    }

    @Test
    void enrollRejectsDuplicateEmail() {
        service.enroll("First", "dup@example.com", "CS", 3.0);

        assertThrows(StudentService.DuplicateEmailException.class,
                () -> service.enroll("Second", "dup@example.com", "Math", 3.5));
    }

    @Test
    void updateGpaChangesExistingStudent() {
        Student s = service.enroll("Rahul Mehta", "rahul.mehta@example.com", "CS", 3.2);

        Student updated = service.updateGpa(s.getId(), 3.9);

        assertEquals(3.9, updated.getGpa(), 0.0001);
    }

    @Test
    void updateGpaOnUnknownStudentThrows() {
        assertThrows(StudentService.StudentNotFoundException.class,
                () -> service.updateGpa(999, 3.0));
    }

    @Test
    void averageGpaAcrossAllStudents() {
        service.enroll("A", "a@example.com", "CS", 3.0);
        service.enroll("B", "b@example.com", "CS", 4.0);

        assertEquals(3.5, service.averageGpa(), 0.0001);
    }

    @Test
    void averageGpaIsZeroWhenNoStudents() {
        assertEquals(0.0, service.averageGpa(), 0.0001);
    }

    @Test
    void averageGpaForMajorOnlyIncludesThatMajor() {
        service.enroll("A", "a@example.com", "Computer Science", 3.0);
        service.enroll("B", "b@example.com", "Computer Science", 4.0);
        service.enroll("C", "c@example.com", "Electronics", 2.0);

        assertEquals(3.5, service.averageGpaForMajor("Computer Science"), 0.0001);
        assertEquals(2.0, service.averageGpaForMajor("Electronics"), 0.0001);
    }

    @Test
    void topStudentsReturnsHighestGpaFirst() {
        service.enroll("Low", "low@example.com", "CS", 2.5);
        service.enroll("High", "high@example.com", "CS", 3.9);
        service.enroll("Mid", "mid@example.com", "CS", 3.2);

        List<Student> top2 = service.topStudents(2);

        assertEquals(2, top2.size());
        assertEquals("High", top2.get(0).getName());
        assertEquals("Mid", top2.get(1).getName());
    }

    @Test
    void withdrawRemovesStudent() {
        Student s = service.enroll("Leaving", "leaving@example.com", "CS", 3.0);

        boolean removed = service.withdraw(s.getId());

        assertTrue(removed);
        assertEquals(0, service.allStudents().size());
    }

    @Test
    void withdrawUnknownStudentReturnsFalse() {
        assertFalse(service.withdraw(999));
    }

    @Test
    void transferMajorUpdatesStudent() {
        Student s = service.enroll("Switching", "switching@example.com", "Undeclared", 3.0);

        Student updated = service.transferMajor(s.getId(), "Computer Science");

        assertEquals("Computer Science", updated.getMajor());
        assertEquals(1, service.studentsInMajor("Computer Science").size());
    }
}
