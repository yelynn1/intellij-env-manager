package cloud.yelynn.envmanager.util;

import cloud.yelynn.envmanager.model.EnvironmentVariable;
import com.intellij.openapi.diagnostic.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Utility class for scanning and parsing application.properties and application.yaml files.
 */
public class PropertiesFileScanner {
    private static final Logger LOG = Logger.getInstance(PropertiesFileScanner.class);

    /**
     * Scans and parses an application.properties file.
     *
     * @param file The properties file to scan
     * @return List of environment variables extracted from the file
     */
    public static List<EnvironmentVariable> scanPropertiesFile(File file) {
        List<EnvironmentVariable> variables = new ArrayList<>();

        // Check if file exists before trying to read it
        if (!file.exists() || !file.isFile()) {
            LOG.warn("Properties file does not exist or is not a file: " + file.getPath());
            return variables;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Parse KEY=VALUE or KEY: VALUE format
                int separatorIndex = line.indexOf('=');
                if (separatorIndex <= 0) {
                    separatorIndex = line.indexOf(':');
                }

                if (separatorIndex > 0) {
                    String key = line.substring(0, separatorIndex).trim();
                    String value = line.substring(separatorIndex + 1).trim();

                    if (!key.isEmpty()) {
                        variables.add(new EnvironmentVariable(key, value));
                    }
                } else {
                    LOG.warn("Skipping invalid line in properties file: " + line);
                }
            }
        } catch (IOException e) {
            LOG.error("Error reading properties file: " + file.getPath(), e);
        }

        return variables;
    }

    /**
     * Scans and parses an application.yaml file.
     *
     * @param file The YAML file to scan
     * @return List of environment variables extracted from the file
     */
    public static List<EnvironmentVariable> scanYamlFile(File file) {
        List<EnvironmentVariable> variables = new ArrayList<>();
        Map<String, String> flattenedProperties = new HashMap<>();

        // Check if file exists before trying to read it
        if (!file.exists() || !file.isFile()) {
            LOG.warn("YAML file does not exist or is not a file: " + file.getPath());
            return variables;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int currentIndentation = 0;
            List<String> currentPath = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                // Skip empty lines and comments
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }

                // Calculate indentation level
                int indentation = 0;
                while (indentation < line.length() && line.charAt(indentation) == ' ') {
                    indentation++;
                }

                // Skip if line is only whitespace
                if (indentation == line.length()) {
                    continue;
                }

                // Adjust current path based on indentation
                if (indentation < currentIndentation) {
                    int levelsToRemove = (currentIndentation - indentation) / 2;
                    for (int i = 0; i < levelsToRemove && !currentPath.isEmpty(); i++) {
                        currentPath.remove(currentPath.size() - 1);
                    }
                }

                currentIndentation = indentation;
                String trimmedLine = line.trim();

                // Parse key-value pair
                int separatorIndex = trimmedLine.indexOf(':');
                if (separatorIndex > 0) {
                    String key = trimmedLine.substring(0, separatorIndex).trim();
                    String value = separatorIndex < trimmedLine.length() - 1 ? 
                                  trimmedLine.substring(separatorIndex + 1).trim() : "";

                    if (!value.isEmpty()) {
                        // This is a key-value pair
                        StringBuilder fullKey = new StringBuilder();
                        for (String pathPart : currentPath) {
                            fullKey.append(pathPart).append(".");
                        }
                        fullKey.append(key);

                        flattenedProperties.put(fullKey.toString(), value);
                    } else {
                        // This is a nested structure
                        currentPath.add(key);
                    }
                }
            }

            // Convert flattened properties to environment variables
            for (Map.Entry<String, String> entry : flattenedProperties.entrySet()) {
                variables.add(new EnvironmentVariable(entry.getKey(), entry.getValue()));
            }

        } catch (IOException e) {
            LOG.error("Error reading YAML file: " + file.getPath(), e);
        }

        return variables;
    }

    /**
     * Scans and parses either a properties or YAML file based on the file extension.
     *
     * @param file The file to scan
     * @return List of environment variables extracted from the file
     */
    public static List<EnvironmentVariable> scanFile(File file) {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".properties") || fileName.endsWith(".yml") || fileName.endsWith(".yaml")) {
            if (fileName.endsWith(".properties")) {
                return scanPropertiesFile(file);
            } else {
                return scanYamlFile(file);
            }
        } else {
            LOG.warn("Unsupported file type: " + fileName);
            return new ArrayList<>();
        }
    }
}
