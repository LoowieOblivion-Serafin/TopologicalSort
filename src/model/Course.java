package model;
import java.util.ArrayList;
import java.util.List;

public class Course {
    private String code;
    private String name;
    private List<String> prerequisites;

    public Course(String code, String name) {
        this.code = code;
        this.name = name;
        this.prerequisites = new ArrayList<>();
    }

    // Getters and setters
    public String getCode() { return code; }
    public String getName() { return name; }
    public List<String> getPrerequisites() { return prerequisites; }
    public void addPrerequisite(String prerequisite) {
        prerequisites.add(prerequisite);
    }

    @Override
    public String toString() {
        return code + " - " + name;
    }
}
