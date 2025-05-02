package cloud.yelynn.envmanager.run;

import cloud.yelynn.envmanager.model.EnvironmentVariable;
import cloud.yelynn.envmanager.model.EnvironmentVariableSet;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Service that registers a listener to inject environment variables into run configurations.
 */
@Service(Service.Level.PROJECT)
public final class EnvironmentManagerService {
    private static final Logger LOG = Logger.getInstance(EnvironmentManagerService.class);

    public void injectEnvironmentVariables(EnvironmentVariableSet envSet) {
        try {
            if (envSet == null) {
                LOG.warn("Environment variable set is null, skipping injection.");
                return;
            }
             // Convert environment variables from the set to a Map<String, String>
            Map<String, String> envVars = new HashMap<>();
            for (EnvironmentVariable variable : envSet.getVariables()) {
                envVars.put(variable.getKey(), variable.getValue());
            }

            // Log the variables being injected
            for (Map.Entry<String, String> entry : envVars.entrySet()) {
                LOG.info("Injecting environment variable: " + entry.getKey() + "=" + entry.getValue());
            }

            // Set the active environment variables in the injector
            EnvironmentVariableInjector.setActiveEnvironmentVariables(envVars);

        } catch (Exception e) {
            // Log any exceptions that occur
            LOG.error("Error injecting environment variables", e);
        }
    }

}
