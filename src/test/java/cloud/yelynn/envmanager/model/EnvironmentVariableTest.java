package cloud.yelynn.envmanager.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the EnvironmentVariable class.
 */
public class EnvironmentVariableTest {

    @Test
    public void testConstructorAndGetters() {
        // Test the parameterized constructor and getters
        EnvironmentVariable variable = new EnvironmentVariable("KEY", "VALUE");
        assertEquals("KEY", variable.getKey());
        assertEquals("VALUE", variable.getValue());
    }

    @Test
    public void testDefaultConstructor() {
        // Test the default constructor
        EnvironmentVariable variable = new EnvironmentVariable();
        assertNull(variable.getKey());
        assertNull(variable.getValue());
    }

    @Test
    public void testSetters() {
        // Test the setters
        EnvironmentVariable variable = new EnvironmentVariable();
        variable.setKey("NEW_KEY");
        variable.setValue("NEW_VALUE");
        assertEquals("NEW_KEY", variable.getKey());
        assertEquals("NEW_VALUE", variable.getValue());
    }

    @Test
    public void testEquals() {
        // Test equals method
        EnvironmentVariable variable1 = new EnvironmentVariable("KEY", "VALUE");
        EnvironmentVariable variable2 = new EnvironmentVariable("KEY", "VALUE");
        EnvironmentVariable variable3 = new EnvironmentVariable("DIFFERENT", "VALUE");
        EnvironmentVariable variable4 = new EnvironmentVariable("KEY", "DIFFERENT");

        // Same key and value should be equal
        assertEquals(variable1, variable2);
        
        // Different key should not be equal
        assertNotEquals(variable1, variable3);
        
        // Different value should not be equal
        assertNotEquals(variable1, variable4);
        
        // Different object type should not be equal
        assertNotEquals(variable1, "Not an EnvironmentVariable");
        
        // Null should not be equal
        assertNotEquals(variable1, null);
        
        // Same object should be equal to itself
        assertEquals(variable1, variable1);
    }

    @Test
    public void testHashCode() {
        // Test hashCode method
        EnvironmentVariable variable1 = new EnvironmentVariable("KEY", "VALUE");
        EnvironmentVariable variable2 = new EnvironmentVariable("KEY", "VALUE");
        
        // Same key and value should have same hash code
        assertEquals(variable1.hashCode(), variable2.hashCode());
    }

    @Test
    public void testToString() {
        // Test toString method
        EnvironmentVariable variable = new EnvironmentVariable("KEY", "VALUE");
        assertEquals("KEY=VALUE", variable.toString());
    }
}