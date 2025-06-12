package util;

import model.Course;
import java.io.*;
import java.util.*;

public class CSVHandler {
    public static List<Course> readCoursesFromCSV(String filename) throws IOException {
        List<Course> courses = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            // Skip header
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Course course = new Course(values[0].trim(), values[1].trim());

                if (values.length > 2 && !values[2].trim().isEmpty()) {
                    String[] prerequisites = values[2].split(";");
                    for (String prereq : prerequisites) {
                        course.addPrerequisite(prereq.trim());
                    }
                }

                courses.add(course);
            }
        }

        return courses;
    }
}
