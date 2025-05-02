package cloud.yelynn.envmanager.service;

import cloud.yelynn.envmanager.model.EnvironmentVariable;
import cloud.yelynn.envmanager.model.EnvironmentVariableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the EnvironmentVariableService class.
 * 
 * Note: These tests focus on the business logic of the service and don't test
 * the persistence functionality, which would require mocking IntelliJ Platform components.
 */
public class EnvironmentVariableServiceTest {

    private EnvironmentVariableService service;

    @BeforeEach
    public void setUp() {
        service = new EnvironmentVariableService();
    }

    @Test
    public void testInitialState() {
        // Test initial state
        assertNotNull(service.getState());
        assertTrue(service.getAllSets().isEmpty());
        assertNull(service.getActiveSetId());
        assertFalse(service.getActiveSet().isPresent());
    }

    @Test
    public void testAddSet() {
        // Test adding a set
        EnvironmentVariableSet set = new EnvironmentVariableSet("Test Set");
        service.addSet(set);

        assertEquals(1, service.getAllSets().size());
        assertEquals(set, service.getAllSets().get(0));
    }

    @Test
    public void testUpdateSet() {
        // Test updating a set
        EnvironmentVariableSet set = new EnvironmentVariableSet("Test Set");
        service.addSet(set);

        // Update the set
        set.setName("Updated Set");
        EnvironmentVariable variable = new EnvironmentVariable("KEY", "VALUE");
        set.addVariable(variable);
        service.updateSet(set);

        // Verify the update
        assertEquals(1, service.getAllSets().size());
        EnvironmentVariableSet updatedSet = service.getAllSets().get(0);
        assertEquals("Updated Set", updatedSet.getName());
        assertEquals(1, updatedSet.getVariables().size());
        assertEquals(variable, updatedSet.getVariables().get(0));
    }

    @Test
    public void testUpdateNonExistentSet() {
        // Test updating a set that doesn't exist
        EnvironmentVariableSet set = new EnvironmentVariableSet("Test Set");

        // This should not throw an exception or add the set
        service.updateSet(set);

        assertTrue(service.getAllSets().isEmpty());
    }

    @Test
    public void testRemoveSet() {
        // Test removing a set
        EnvironmentVariableSet set1 = new EnvironmentVariableSet("Set 1");
        EnvironmentVariableSet set2 = new EnvironmentVariableSet("Set 2");
        service.addSet(set1);
        service.addSet(set2);

        service.removeSet(set1);

        assertEquals(1, service.getAllSets().size());
        assertEquals(set2, service.getAllSets().get(0));
    }

    @Test
    public void testRemoveActiveSet() {
        // Test removing the active set
        EnvironmentVariableSet set = new EnvironmentVariableSet("Test Set");
        service.addSet(set);
        service.setActiveSetId(set.getId());

        service.removeSet(set);

        assertTrue(service.getAllSets().isEmpty());
        assertNull(service.getActiveSetId());
        assertFalse(service.getActiveSet().isPresent());
    }

    @Test
    public void testGetSetById() {
        // Test getting a set by ID
        EnvironmentVariableSet set = new EnvironmentVariableSet("Test Set");
        service.addSet(set);

        Optional<EnvironmentVariableSet> result = service.getSetById(set.getId());

        assertTrue(result.isPresent());
        assertEquals(set, result.get());
    }

    @Test
    public void testGetSetByIdNotFound() {
        // Test getting a set by ID that doesn't exist
        Optional<EnvironmentVariableSet> result = service.getSetById("non-existent-id");

        assertFalse(result.isPresent());
    }

    @Test
    public void testGetSetByName() {
        // Test getting a set by name
        EnvironmentVariableSet set = new EnvironmentVariableSet("Test Set");
        service.addSet(set);

        Optional<EnvironmentVariableSet> result = service.getSetByName("Test Set");

        assertTrue(result.isPresent());
        assertEquals(set, result.get());
    }

    @Test
    public void testGetSetByNameNotFound() {
        // Test getting a set by name that doesn't exist
        Optional<EnvironmentVariableSet> result = service.getSetByName("Non-existent Set");

        assertFalse(result.isPresent());
    }

    @Test
    public void testSetAndGetActiveSetId() {
        // Test setting and getting the active set ID
        String activeId = "active-id";
        service.setActiveSetId(activeId);

        assertEquals(activeId, service.getActiveSetId());
    }

    @Test
    public void testGetActiveSet() {
        // Test getting the active set
        EnvironmentVariableSet set = new EnvironmentVariableSet("Test Set");
        service.addSet(set);
        service.setActiveSetId(set.getId());

        Optional<EnvironmentVariableSet> result = service.getActiveSet();

        assertTrue(result.isPresent());
        assertEquals(set, result.get());
    }

    @Test
    public void testGetActiveSetNotFound() {
        // Test getting the active set when it doesn't exist
        service.setActiveSetId("non-existent-id");

        Optional<EnvironmentVariableSet> result = service.getActiveSet();

        assertFalse(result.isPresent());
    }

    @Test
    public void testGetActiveSetWithNullId() {
        // Test getting the active set when the active ID is null
        service.setActiveSetId(null);

        Optional<EnvironmentVariableSet> result = service.getActiveSet();

        assertFalse(result.isPresent());
    }
}
