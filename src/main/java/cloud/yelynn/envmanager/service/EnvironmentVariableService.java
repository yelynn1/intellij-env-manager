package cloud.yelynn.envmanager.service;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import cloud.yelynn.envmanager.model.EnvironmentVariableSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing environment variable sets.
 * Uses PersistentStateComponent to persist data between IDE restarts.
 */
@Service(Service.Level.PROJECT)
@State(
    name = "EnvironmentVariableService",
    storages = {@Storage("environmentVariables.xml")}
)
public final class EnvironmentVariableService implements PersistentStateComponent<EnvironmentVariableService.State> {
    private State myState = new State();
    private String activeSetId;

    public static class State {
        public List<EnvironmentVariableSet> environmentVariableSets = new ArrayList<>();
    }

    @Override
    public @Nullable State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        XmlSerializerUtil.copyBean(state, myState);
    }

    public List<EnvironmentVariableSet> getAllSets() {
        return myState.environmentVariableSets;
    }

    public void addSet(EnvironmentVariableSet set) {
        myState.environmentVariableSets.add(set);
    }

    public void updateSet(EnvironmentVariableSet set) {
        for (int i = 0; i < myState.environmentVariableSets.size(); i++) {
            if (myState.environmentVariableSets.get(i).getId().equals(set.getId())) {
                myState.environmentVariableSets.set(i, set);
                return;
            }
        }
    }

    public void removeSet(EnvironmentVariableSet set) {
        myState.environmentVariableSets.removeIf(s -> s.getId().equals(set.getId()));
        if (set.getId().equals(activeSetId)) {
            activeSetId = null;
        }
    }

    public Optional<EnvironmentVariableSet> getSetById(String id) {
        return myState.environmentVariableSets.stream()
                .filter(set -> set.getId().equals(id))
                .findFirst();
    }

    public Optional<EnvironmentVariableSet> getSetByName(String name) {
        return myState.environmentVariableSets.stream()
                .filter(set -> set.getName().equals(name))
                .findFirst();
    }

    public String getActiveSetId() {
        return activeSetId;
    }

    public void setActiveSetId(String activeSetId) {
        this.activeSetId = activeSetId;
    }

    public Optional<EnvironmentVariableSet> getActiveSet() {
        if (activeSetId == null) {
            return Optional.empty();
        }
        return getSetById(activeSetId);
    }
}