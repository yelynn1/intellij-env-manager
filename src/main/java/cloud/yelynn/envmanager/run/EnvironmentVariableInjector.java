package cloud.yelynn.envmanager.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.RunConfigurationExtension;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunnerSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;


public class EnvironmentVariableInjector extends RunConfigurationExtension {

    private static Map<String, String> activeEnvironmentVariables = new HashMap<>();

    public static void setActiveEnvironmentVariables(Map<String, String> variables) {
        activeEnvironmentVariables = variables != null ? new HashMap<>(variables) : new HashMap<>();
    }

    @Override
    public <T extends RunConfigurationBase<?>> void updateJavaParameters(
            @NotNull T configuration,
            @NotNull JavaParameters params,
            @Nullable RunnerSettings runnerSettings
    ) throws ExecutionException {
        if (activeEnvironmentVariables != null && !activeEnvironmentVariables.isEmpty()) {
            Map<String, String> env = params.getEnv();
            env.putAll(activeEnvironmentVariables);
            params.setEnv(env);
        }
    }

    @Override
    public boolean isApplicableFor(@NotNull RunConfigurationBase<?> configuration) {
        return true;
    }

    @Override
    public boolean isEnabledFor(@NotNull RunConfigurationBase applicableConfiguration,
                                @Nullable RunnerSettings runnerSettings) {
        return true;
    }
}