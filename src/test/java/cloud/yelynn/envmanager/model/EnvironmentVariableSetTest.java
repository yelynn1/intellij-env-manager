package cloud.yelynn.envmanager.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for the EnvironmentVariableSet class.
 */
public class EnvironmentVariableSetTest {

    @Test
    public void testConstructorAndGetters() {
        // Test the parameterized constructor and getters
        EnvironmentVariableSet set = new EnvironmentVariableSet("Test Set");
        assertEquals("Test Set", set.getName());
        assertNotNull(set.getId());
        assertNotNull(set.getVariables());
        assertTrue(set.getVariables().isEmpty());
    }

    @Test
    public void testDefaultConstructor() {
        // Test the default constructor
        EnvironmentVariableSet set = new EnvironmentVariableSet();
        assertNull(set.getName());
        assertNotNull(set.getId());
        assertNotNull(set.getVariables());
        assertTrue(set.getVariables().isEmpty());
    }

    @Test
    public void testSetters() {
        // Test the setters
        EnvironmentVariableSet set = new EnvironmentVariableSet();
        set.setName("New Name");
        set.setId("custom-id");
        
        assertEquals("New Name", set.getName());
        assertEquals("custom-id", set.getId());
    }

    @Test
    public void testSetVariables() {
        // Test setting the variables list
        EnvironmentVariableSet set = new EnvironmentVariableSet("Test Set");
        List<EnvironmentVariable> variables = new ArrayList<>();
        variables.add(new EnvironmentVariable("KEY1", "VALUE1"));
        variables.add(new EnvironmentVariable("KEY2", "VALUE2"));
        
        set.setVariables(variables);
        
        assertEquals(2, set.getVariables().size());
        assertEquals("KEY1", set.getVariables().get(0).getKey());
        assertEquals("VALUE1", set.getVariables().get(0).getValue());
        assertEquals("KEY2", set.getVariables().get(1).getKey());
        assertEquals("VALUE2", set.getVariables().get(1).getValue());
    }

    @Test
    public void testSetVariablesWithNull() {
        // Test setting the variables list to null
        EnvironmentVariableSet set = new EnvironmentVariableSet("Test Set");
        set.setVariables(null);
        
        assertNotNull(set.getVariables());
        assertTrue(set.getVariables().isEmpty());
    }

    @Test
    public void testAddVariable() {
        // Test adding a variable
        EnvironmentVariableSet set = new EnvironmentVariableSet("Test Set");
        EnvironmentVariable variable = new EnvironmentVariable("KEY", "VALUE");
        
        set.addVariable(variable);
        
        assertEquals(1, set.getVariables().size());
        assertEquals(variable, set.getVariables().get(0));
    }

    @Test
    public void testAddVariableWithNullVariables() {
        // Test adding a variable when variables is null
        EnvironmentVariableSet set = new EnvironmentVariableSet("Test Set");
        set.setVariables(null);
        EnvironmentVariable variable = new EnvironmentVariable("KEY", "VALUE");
        
        set.addVariable(variable);
        
        assertEquals(1, set.getVariables().size());
        assertEquals(variable, set.getVariables().get(0));
    }

    @Test
    public void testRemoveVariable() {
        // Test removing a variable
        EnvironmentVariableSet set = new EnvironmentVariableSet("Test Set");
        EnvironmentVariable variable1 = new EnvironmentVariable("KEY1", "VALUE1");
        EnvironmentVariable variable2 = new EnvironmentVariable("KEY2", "VALUE2");
        
        set.addVariable(variable1);
        set.addVariable(variable2);
        set.removeVariable(variable1);
        
        assertEquals(1, set.getVariables().size());
        assertEquals(variable2, set.getVariables().get(0));
    }

    @Test
    public void testRemoveVariableWithNullVariables() {
        // Test removing a variable when variables is null
        EnvironmentVariableSet set = new EnvironmentVariableSet("Test Set");
        set.setVariables(null);
        EnvironmentVariable variable = new EnvironmentVariable("KEY", "VALUE");
        
        // This should not throw an exception
        set.removeVariable(variable);
    }

    @Test
    public void testRemoveVariableByKey() {
        // Test removing a variable by key
        EnvironmentVariableSet set = new EnvironmentVariableSet("Test Set");
        EnvironmentVariable variable1 = new EnvironmentVariable("KEY1", "VALUE1");
        EnvironmentVariable variable2 = new EnvironmentVariable("KEY2", "VALUE2");
        
        set.addVariable(variable1);
        set.addVariable(variable2);
        set.removeVariableByKey("KEY1");
        
        assertEquals(1, set.getVariables().size());
        assertEquals(variable2, set.getVariables().get(0));
    }

    @Test
    public void testRemoveVariableByKeyWithNullVariables() {
        // Test removing a variable by key when variables is null
        EnvironmentVariableSet set = new EnvironmentVariableSet("Test Set");
        set.setVariables(null);
        
        // This should not throw an exception
        set.removeVariableByKey("KEY");
    }

    @Test
    public void testEquals() {
        // Test equals method
        EnvironmentVariableSet set1 = new EnvironmentVariableSet("Set 1");
        set1.setId("id-1");
        
        EnvironmentVariableSet set2 = new EnvironmentVariableSet("Set 2");
        set2.setId("id-1");
        
        EnvironmentVariableSet set3 = new EnvironmentVariableSet("Set 3");
        set3.setId("id-3");
        
        // Same ID should be equal, even with different names
        assertEquals(set1, set2);
        
        // Different ID should not be equal
        assertNotEquals(set1, set3);
        
        // Different object type should not be equal
        assertNotEquals(set1, "Not an EnvironmentVariableSet");
        
        // Null should not be equal
        assertNotEquals(set1, null);
        
        // Same object should be equal to itself
        assertEquals(set1, set1);
    }

    @Test
    public void testHashCode() {
        // Test hashCode method
        EnvironmentVariableSet set1 = new EnvironmentVariableSet("Set 1");
        set1.setId("id-1");
        
        EnvironmentVariableSet set2 = new EnvironmentVariableSet("Set 2");
        set2.setId("id-1");
        
        // Same ID should have same hash code
        assertEquals(set1.hashCode(), set2.hashCode());
    }

    @Test
    public void testToString() {
        // Test toString method
        EnvironmentVariableSet set = new EnvironmentVariableSet("Test Set");
        assertEquals("Test Set", set.toString());
    }
}