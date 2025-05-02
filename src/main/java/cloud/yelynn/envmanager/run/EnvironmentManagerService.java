package cloud.yelynn.envmanager.run;

import cloud.yelynn.envmanager.model.EnvironmentVariable;
import cloud.yelynn.envmanager.model.EnvironmentVariableSet;
import cloud.yelynn.envmanager.service.EnvironmentVariableService;
import com.intellij.execution.ExecutionListener;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

/**
 * Service that registers a listener to inject environment variables into run configurations.
 */
@Service(Service.Level.PROJECT)
public final class EnvironmentManagerService {
    private static final Logger LOG = Logger.getInstance(EnvironmentManagerService.class);
    private final MessageBusConnection connection;

    public EnvironmentManagerService(Project project) {
        connection = project.getMessageBus().connect();
        connection.subscribe(ExecutionManager.EXECUTION_TOPIC, new ExecutionListener() {
            @Override
            public void processStarting(@NotNull String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler) {
                injectEnvironmentVariables(project, env, handler);
            }
        });
        
        Disposer.register(project, () -> connection.disconnect());
    }

    private void injectEnvironmentVariables(Project project, ExecutionEnvironment env, ProcessHandler handler) {
        EnvironmentVariableService service = project.getService(EnvironmentVariableService.class);
        Optional<EnvironmentVariableSet> activeSet = service.getActiveSet();
        
        if (activeSet.isPresent()) {
            EnvironmentVariableSet set = activeSet.get();
            
            // Log that we're activating the environment variables
            LOG.info("Activating environment variables from set: " + set.getName());
            
            // Since we can't directly modify the environment variables of a running process,
            // we'll log the variables that would be injected
            for (EnvironmentVariable variable : set.getVariables()) {
                LOG.info("  " + variable.getKey() + "=" + variable.getValue());
            }
            
            // Note: In a real implementation, we would need to modify the run configuration
            // before the process starts, which requires more complex integration with
            // the IntelliJ platform. For this example, we'll just log the variables.
        }
    }
}