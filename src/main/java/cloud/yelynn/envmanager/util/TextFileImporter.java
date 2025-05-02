package cloud.yelynn.envmanager.util;

import cloud.yelynn.envmanager.model.EnvironmentVariable;
import com.intellij.openapi.diagnostic.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for importing environment variables from text files.
 */
public class TextFileImporter {
    private static final Logger LOG = Logger.getInstance(TextFileImporter.class);

    /**
     * Imports environment variables from a text file.
     * Expected format: KEY=VALUE (one per line)
     *
     * @param file The text file to import
     * @return List of imported environment variables
     */
    public static List<EnvironmentVariable> importFromFile(File file) {
        List<EnvironmentVariable> variables = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Parse KEY=VALUE format
                int separatorIndex = line.indexOf('=');
                if (separatorIndex > 0) {
                    String key = line.substring(0, separatorIndex).trim();
                    String value = line.substring(separatorIndex + 1).trim();
                    
                    if (!key.isEmpty()) {
                        variables.add(new EnvironmentVariable(key, value));
                    }
                } else {
                    LOG.warn("Skipping invalid line in environment file: " + line);
                }
            }
        } catch (IOException e) {
            LOG.error("Error reading environment file: " + file.getPath(), e);
        }

        return variables;
    }
}