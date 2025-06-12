package model;
import java.util.*;
public class CourseGraph {
    private Map<String, Course> courses;
    private Map<String, List<String>> adjacencyList;

    public CourseGraph() {
        courses = new HashMap<>();
        adjacencyList = new HashMap<>();
    }

    public void addCourse(Course course) {
        courses.put(course.getCode(), course);
        adjacencyList.put(course.getCode(), new ArrayList<>());
    }

    public void addPrerequisite(String course, String prerequisite) {
        adjacencyList.get(prerequisite).add(course);
    }

    public List<Course> topologicalSort() throws Exception {
        Map<String, Integer> inDegree = new HashMap<>();
        for (String course : courses.keySet()) {
            inDegree.put(course, 0);
        }

        // Calculate in-degrees
        for (Map.Entry<String, List<String>> entry : adjacencyList.entrySet()) {
            for (String course : entry.getValue()) {
                inDegree.put(course, inDegree.get(course) + 1);
            }
        }

        // Add courses with no prerequisites to queue
        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<Course> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            String courseCode = queue.poll();
            result.add(courses.get(courseCode));

            for (String neighbor : adjacencyList.get(courseCode)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.add(neighbor);
                }
            }
        }

        if (result.size() != courses.size()) {
            throw new Exception("Ciclo detectado en los prerrequisitos");
        }

        return result;
    }
}
