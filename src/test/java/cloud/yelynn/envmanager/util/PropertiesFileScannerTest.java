package cloud.yelynn.envmanager.util;

import cloud.yelynn.envmanager.model.EnvironmentVariable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the PropertiesFileScanner utility class.
 */
public class PropertiesFileScannerTest {

    @TempDir
    Path tempDir;
    
    private File propertiesFile;
    private File yamlFile;
    private File invalidFile;

    @BeforeEach
    public void setUp() throws IOException {
        // Create a temporary properties file
        propertiesFile = tempDir.resolve("test.properties").toFile();
        try (FileWriter writer = new FileWriter(propertiesFile)) {
            writer.write("# This is a comment\n");
            writer.write("key1=value1\n");
            writer.write("key2 = value2\n");
            writer.write("key3:value3\n");
            writer.write("key4 : value4\n");
            writer.write("\n");  // Empty line
            writer.write("# Another comment\n");
            writer.write("key5=value with spaces\n");
        }

        // Create a temporary YAML file
        yamlFile = tempDir.resolve("test.yaml").toFile();
        try (FileWriter writer = new FileWriter(yamlFile)) {
            writer.write("# YAML comment\n");
            writer.write("key1: value1\n");
            writer.write("key2: value2\n");
            writer.write("nested:\n");
            writer.write("  key3: value3\n");
            writer.write("  key4: value4\n");
            writer.write("  deeper:\n");
            writer.write("    key5: value5\n");
            writer.write("\n");  // Empty line
            writer.write("# Another comment\n");
            writer.write("list_parent:\n");
            writer.write("  key6: value6\n");
        }

        // Create an invalid file
        invalidFile = tempDir.resolve("invalid.txt").toFile();
        try (FileWriter writer = new FileWriter(invalidFile)) {
            writer.write("This is not a properties or YAML file\n");
        }
    }

    @Test
    public void testScanPropertiesFile() {
        List<EnvironmentVariable> variables = PropertiesFileScanner.scanPropertiesFile(propertiesFile);
        
        assertEquals(5, variables.size());
        
        // Check that all keys and values are correctly parsed
        assertContainsVariable(variables, "key1", "value1");
        assertContainsVariable(variables, "key2", "value2");
        assertContainsVariable(variables, "key3", "value3");
        assertContainsVariable(variables, "key4", "value4");
        assertContainsVariable(variables, "key5", "value with spaces");
    }

    @Test
    public void testScanYamlFile() {
        List<EnvironmentVariable> variables = PropertiesFileScanner.scanYamlFile(yamlFile);
        
        assertEquals(6, variables.size());
        
        // Check that all keys and values are correctly parsed
        assertContainsVariable(variables, "key1", "value1");
        assertContainsVariable(variables, "key2", "value2");
        assertContainsVariable(variables, "nested.key3", "value3");
        assertContainsVariable(variables, "nested.key4", "value4");
        assertContainsVariable(variables, "nested.deeper.key5", "value5");
        assertContainsVariable(variables, "list_parent.key6", "value6");
    }

    @Test
    public void testScanFile() {
        // Test with properties file
        List<EnvironmentVariable> propertiesVariables = PropertiesFileScanner.scanFile(propertiesFile);
        assertEquals(5, propertiesVariables.size());
        
        // Test with YAML file
        List<EnvironmentVariable> yamlVariables = PropertiesFileScanner.scanFile(yamlFile);
        assertEquals(6, yamlVariables.size());
        
        // Test with invalid file
        List<EnvironmentVariable> invalidVariables = PropertiesFileScanner.scanFile(invalidFile);
        assertTrue(invalidVariables.isEmpty());
    }

    @Test
    public void testScanNonExistentFile() {
        File nonExistentFile = new File(tempDir.toFile(), "non-existent.properties");
        List<EnvironmentVariable> variables = PropertiesFileScanner.scanPropertiesFile(nonExistentFile);
        
        assertTrue(variables.isEmpty());
    }

    @Test
    public void testScanInvalidPropertiesFile() throws IOException {
        // Create a file with invalid properties format
        File invalidPropertiesFile = tempDir.resolve("invalid.properties").toFile();
        try (FileWriter writer = new FileWriter(invalidPropertiesFile)) {
            writer.write("This is not a valid key-value pair\n");
            writer.write("Also not valid\n");
        }
        
        List<EnvironmentVariable> variables = PropertiesFileScanner.scanPropertiesFile(invalidPropertiesFile);
        
        assertTrue(variables.isEmpty());
    }

    @Test
    public void testScanInvalidYamlFile() throws IOException {
        // Create a file with invalid YAML format
        File invalidYamlFile = tempDir.resolve("invalid.yaml").toFile();
        try (FileWriter writer = new FileWriter(invalidYamlFile)) {
            writer.write("This is not a valid YAML file\n");
        }
        
        List<EnvironmentVariable> variables = PropertiesFileScanner.scanYamlFile(invalidYamlFile);
        
        assertTrue(variables.isEmpty());
    }

    /**
     * Helper method to check if a list of environment variables contains a variable with the given key and value.
     */
    private void assertContainsVariable(List<EnvironmentVariable> variables, String key, String value) {
        boolean found = false;
        for (EnvironmentVariable variable : variables) {
            if (variable.getKey().equals(key) && variable.getValue().equals(value)) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Variable with key '" + key + "' and value '" + value + "' not found");
    }
}