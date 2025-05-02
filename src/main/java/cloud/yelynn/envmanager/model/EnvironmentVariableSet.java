package cloud.yelynn.envmanager.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a set of environment variables (e.g., dev, staging, prod).
 */
public class EnvironmentVariableSet {
    private String id;
    private String name;
    private List<EnvironmentVariable> variables;

    // Default constructor for serialization
    public EnvironmentVariableSet() {
        this.id = UUID.randomUUID().toString();
        this.variables = new ArrayList<>();
    }

    public EnvironmentVariableSet(String name) {
        this();
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<EnvironmentVariable> getVariables() {
        return variables;
    }

    public void setVariables(List<EnvironmentVariable> variables) {
        this.variables = variables != null ? variables : new ArrayList<>();
    }

    public void addVariable(EnvironmentVariable variable) {
        if (variables == null) {
            variables = new ArrayList<>();
        }
        variables.add(variable);
    }

    public void removeVariable(EnvironmentVariable variable) {
        if (variables != null) {
            variables.remove(variable);
        }
    }

    public void removeVariableByKey(String key) {
        if (variables != null) {
            variables.removeIf(var -> Objects.equals(var.getKey(), key));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnvironmentVariableSet that = (EnvironmentVariableSet) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name;
    }
}