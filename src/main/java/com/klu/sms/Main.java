package com.klu.sms;

import com.klu.sms.dao.InMemoryStudentDAO;
import com.klu.sms.dao.StudentDAO;
import com.klu.sms.model.Student;
import com.klu.sms.service.StudentService;

import java.util.List;

/**
 * Console demo of the student management workflow. Runs against
 * InMemoryStudentDAO out of the box so it needs no database to try —
 * swap in JdbcStudentDAO (see README) to point it at real MySQL.
 */
public class Main {

    public static void main(String[] args) {
        StudentDAO dao = new InMemoryStudentDAO();
        StudentService service = new StudentService(dao);

        service.enroll("Asha Patel", "asha.patel@example.com", "Computer Science", 3.80);
        service.enroll("Rahul Mehta", "rahul.mehta@example.com", "Computer Science", 3.20);
        service.enroll("Divya Nair", "divya.nair@example.com", "Electronics", 3.95);
        service.enroll("Farhan Ali", "farhan.ali@example.com", "Mechanical", 2.90);
        service.enroll("Meera Iyer", "meera.iyer@example.com", "Computer Science", 3.55);

        System.out.println("All students:");
        for (Student s : service.allStudents()) {
            System.out.println("  " + s);
        }

        System.out.println();
        System.out.printf("Overall average GPA: %.2f%n", service.averageGpa());
        System.out.printf("Computer Science average GPA: %.2f%n",
                service.averageGpaForMajor("Computer Science"));

        System.out.println();
        System.out.println("Top 3 students by GPA:");
        List<Student> top = service.topStudents(3);
        for (int i = 0; i < top.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + top.get(i));
        }

        System.out.println();
        Student promoted = service.updateGpa(1, 3.90);
        System.out.println("Updated GPA: " + promoted);

        boolean withdrawn = service.withdraw(4);
        System.out.println("Withdrew student 4: " + withdrawn);
        System.out.println("Remaining students: " + service.allStudents().size());
    }
}
