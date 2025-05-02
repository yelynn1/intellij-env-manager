package cloud.yelynn.envmanager.ui;

import cloud.yelynn.envmanager.model.EnvironmentVariable;
import cloud.yelynn.envmanager.model.EnvironmentVariableSet;
import cloud.yelynn.envmanager.run.EnvironmentManagerService;
import cloud.yelynn.envmanager.service.EnvironmentVariableService;
import cloud.yelynn.envmanager.util.TextFileImporter;
import cloud.yelynn.envmanager.util.TextFileExporter;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Content for the Environment Manager tool window.
 */
public class EnvironmentManagerToolWindowContent {
    private final Project project;
    private final ToolWindow toolWindow;
    private final EnvironmentVariableService environmentVariableService;
    private final EnvironmentManagerService environmentManagerService;

    private JPanel mainPanel;
    private JBList<EnvironmentVariableSet> setsList;
    private JBTable variablesTable;
    private VariablesTableModel tableModel;
    private DefaultListModel<EnvironmentVariableSet> listModel;

    public EnvironmentManagerToolWindowContent(Project project, ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;
        this.environmentVariableService = project.getService(EnvironmentVariableService.class);
        this.environmentManagerService = project.getService(EnvironmentManagerService.class);

        createUI();
    }

    public JComponent getContent() {
        return mainPanel;
    }

    private void createUI() {
        mainPanel = new JPanel(new BorderLayout());

        // Create sets list
        listModel = new DefaultListModel<>();
        setsList = new JBList<>(listModel);
        setsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateVariablesTable();
            }
        });

        // Set custom cell renderer to show active set
        setsList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof EnvironmentVariableSet) {
                    EnvironmentVariableSet set = (EnvironmentVariableSet) value;
                    Optional<EnvironmentVariableSet> activeSet = environmentVariableService.getActiveSet();
                    boolean isActive = activeSet.isPresent() && activeSet.get().getId().equals(set.getId());

                    if (isActive) {
                        setText(set.getName() + " (active)");
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else {
                        setText(set.getName());
                        setFont(getFont().deriveFont(Font.PLAIN));
                    }
                }

                return renderer;
            }
        });

        // Create variables table
        tableModel = new VariablesTableModel();
        variablesTable = new JBTable(tableModel);

        // Create split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JBScrollPane(setsList),
                new JBScrollPane(variablesTable));
        splitPane.setDividerLocation(200);

        // Create toolbar
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new AddSetAction());
        actionGroup.add(new EditSetAction());
        actionGroup.add(new RemoveSetAction());
        actionGroup.add(new CloneSetAction());
        actionGroup.addSeparator();
        actionGroup.add(new AddVariableAction());
        actionGroup.add(new EditVariableAction());
        actionGroup.add(new RemoveVariableAction());
        actionGroup.add(new ImportVariablesAction());
        actionGroup.add(new ExportSetAction());
        actionGroup.addSeparator();
        actionGroup.add(new ActivateSetAction());

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(
                "EnvironmentManagerToolbar", actionGroup, true);

        // Create panel
        SimpleToolWindowPanel panel = new SimpleToolWindowPanel(true, true);
        panel.setToolbar(toolbar.getComponent());
        panel.setContent(splitPane);

        mainPanel.add(panel, BorderLayout.CENTER);

        // Load data
        loadSets();
    }

    private void loadSets() {
        listModel.clear();
        for (EnvironmentVariableSet set : environmentVariableService.getAllSets()) {
            listModel.addElement(set);
        }
    }

    private void updateVariablesTable() {
        EnvironmentVariableSet selectedSet = setsList.getSelectedValue();
        if (selectedSet != null) {
            tableModel.setVariables(selectedSet.getVariables());
        } else {
            tableModel.setVariables(new ArrayList<>());
        }
    }

    private class VariablesTableModel extends AbstractTableModel {
        private final String[] COLUMN_NAMES = {"Key", "Value"};
        private List<EnvironmentVariable> variables = new ArrayList<>();

        public void setVariables(List<EnvironmentVariable> variables) {
            this.variables = variables;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return variables.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            EnvironmentVariable variable = variables.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return variable.getKey();
                case 1:
                    return variable.getValue();
                default:
                    return null;
            }
        }
    }

    private class AddSetAction extends AnAction {
        public AddSetAction() {
            super("Add Set", "Add a new environment variable set", AllIcons.General.Add);
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            String name = Messages.showInputDialog(
                    project,
                    "Enter name for the new environment variable set:",
                    "Add Environment Variable Set",
                    null);

            if (name != null && !name.trim().isEmpty()) {
                EnvironmentVariableSet newSet = new EnvironmentVariableSet(name.trim());
                environmentVariableService.addSet(newSet);
                loadSets();
                setsList.setSelectedValue(newSet, true);
            }
        }
    }

    private class EditSetAction extends AnAction {
        public EditSetAction() {
            super("Edit Set", "Edit the selected environment variable set", AllIcons.Actions.Edit);
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(setsList.getSelectedValue() != null);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            EnvironmentVariableSet selectedSet = setsList.getSelectedValue();
            if (selectedSet != null) {
                String name = Messages.showInputDialog(
                        project,
                        "Enter new name for the environment variable set:",
                        "Edit Environment Variable Set",
                        null,
                        selectedSet.getName(),
                        null);

                if (name != null && !name.trim().isEmpty()) {
                    selectedSet.setName(name.trim());
                    environmentVariableService.updateSet(selectedSet);
                    loadSets();
                    setsList.setSelectedValue(selectedSet, true);
                }
            }
        }
    }

    private class RemoveSetAction extends AnAction {
        public RemoveSetAction() {
            super("Remove Set", "Remove the selected environment variable set", AllIcons.General.Remove);
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(setsList.getSelectedValue() != null);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            EnvironmentVariableSet selectedSet = setsList.getSelectedValue();
            if (selectedSet != null) {
                int result = Messages.showYesNoDialog(
                        project,
                        "Are you sure you want to remove the environment variable set '" + selectedSet.getName() + "'?",
                        "Remove Environment Variable Set",
                        null);

                if (result == Messages.YES) {
                    environmentVariableService.removeSet(selectedSet);
                    loadSets();
                    updateVariablesTable();
                }
            }
        }
    }

    private class CloneSetAction extends AnAction {
        public CloneSetAction() {
            super("Clone Set", "Create a copy of the selected environment variable set", AllIcons.Actions.Copy);
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(setsList.getSelectedValue() != null);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            EnvironmentVariableSet selectedSet = setsList.getSelectedValue();
            if (selectedSet != null) {
                // Create a new set with a copy of the name
                String newName = "Copy of " + selectedSet.getName();
                EnvironmentVariableSet newSet = new EnvironmentVariableSet(newName);

                // Copy all variables from the selected set
                for (EnvironmentVariable variable : selectedSet.getVariables()) {
                    newSet.addVariable(new EnvironmentVariable(variable.getKey(), variable.getValue()));
                }

                // Add the new set to the service
                environmentVariableService.addSet(newSet);

                // Refresh the UI
                loadSets();
                setsList.setSelectedValue(newSet, true);
            }
        }
    }

    /**
     * Dialog for adding or editing environment variables
     */
    private class EnvironmentVariableDialog extends DialogWrapper {
        private final JBTextField keyField;
        private final JBTextField valueField;

        public EnvironmentVariableDialog(String title, String key, String value) {
            super(project);
            setTitle(title);

            keyField = new JBTextField(key, 20);
            valueField = new JBTextField(value, 20);

            init();
        }

        @Override
        protected @Nullable JComponent createCenterPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();

            // Key label
            c.gridx = 0;
            c.gridy = 0;
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets(0, 0, 5, 5);
            panel.add(new JBLabel("Key:"), c);

            // Key field
            c.gridx = 1;
            c.gridy = 0;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            panel.add(keyField, c);

            // Value label
            c.gridx = 0;
            c.gridy = 1;
            c.weightx = 0.0;
            c.fill = GridBagConstraints.NONE;
            panel.add(new JBLabel("Value:"), c);

            // Value field
            c.gridx = 1;
            c.gridy = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            panel.add(valueField, c);

            return panel;
        }

        public String getKey() {
            return keyField.getText().trim();
        }

        public String getValue() {
            return valueField.getText();
        }

        @Override
        public @Nullable JComponent getPreferredFocusedComponent() {
            return keyField;
        }
    }

    private class AddVariableAction extends AnAction {
        public AddVariableAction() {
            super("Add Variable", "Add a new environment variable", AllIcons.General.Add);
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(setsList.getSelectedValue() != null);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            EnvironmentVariableSet selectedSet = setsList.getSelectedValue();
            if (selectedSet != null) {
                EnvironmentVariableDialog dialog = new EnvironmentVariableDialog(
                        "Add Environment Variable", "", "");

                if (dialog.showAndGet()) {
                    String key = dialog.getKey();
                    String value = dialog.getValue();

                    if (!key.isEmpty()) {
                        EnvironmentVariable variable = new EnvironmentVariable(key, value);
                        selectedSet.addVariable(variable);
                        environmentVariableService.updateSet(selectedSet);
                        updateVariablesTable();
                    }
                }
            }
        }
    }

    private class EditVariableAction extends AnAction {
        public EditVariableAction() {
            super("Edit Variable", "Edit the selected environment variable", AllIcons.Actions.Edit);
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(setsList.getSelectedValue() != null && 
                    variablesTable.getSelectedRow() != -1);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            EnvironmentVariableSet selectedSet = setsList.getSelectedValue();
            int selectedRow = variablesTable.getSelectedRow();

            if (selectedSet != null && selectedRow != -1) {
                EnvironmentVariable variable = selectedSet.getVariables().get(selectedRow);

                EnvironmentVariableDialog dialog = new EnvironmentVariableDialog(
                        "Edit Environment Variable", 
                        variable.getKey(), 
                        variable.getValue());

                if (dialog.showAndGet()) {
                    String key = dialog.getKey();
                    String value = dialog.getValue();

                    if (!key.isEmpty()) {
                        variable.setKey(key);
                        variable.setValue(value);
                        environmentVariableService.updateSet(selectedSet);
                        updateVariablesTable();
                    }
                }
            }
        }
    }

    private class RemoveVariableAction extends AnAction {
        public RemoveVariableAction() {
            super("Remove Variable", "Remove the selected environment variable", AllIcons.General.Remove);
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(setsList.getSelectedValue() != null && 
                    variablesTable.getSelectedRow() != -1);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            EnvironmentVariableSet selectedSet = setsList.getSelectedValue();
            int selectedRow = variablesTable.getSelectedRow();

            if (selectedSet != null && selectedRow != -1) {
                EnvironmentVariable variable = selectedSet.getVariables().get(selectedRow);

                int result = Messages.showYesNoDialog(
                        project,
                        "Are you sure you want to remove the environment variable '" + variable.getKey() + "'?",
                        "Remove Environment Variable",
                        null);

                if (result == Messages.YES) {
                    selectedSet.removeVariable(variable);
                    environmentVariableService.updateSet(selectedSet);
                    updateVariablesTable();
                }
            }
        }
    }

    private class ImportVariablesAction extends AnAction {
        public ImportVariablesAction() {
            super("Import Variables", "Import environment variables from a text file", AllIcons.Actions.MenuOpen);
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(setsList.getSelectedValue() != null);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            EnvironmentVariableSet selectedSet = setsList.getSelectedValue();
            if (selectedSet != null) {
                FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false)
                        .withTitle("Import Environment Variables")
                        .withDescription("Select a text file with environment variables (KEY=VALUE format)");
//                        .withFileFilter(file -> file.getExtension() != null && file.getExtension().equalsIgnoreCase("txt"));

                FileChooser.chooseFile(descriptor, project, null, file -> {
                    java.io.File ioFile = VfsUtil.virtualToIoFile(file);
                    List<EnvironmentVariable> importedVariables = TextFileImporter.importFromFile(ioFile);

                    if (importedVariables.isEmpty()) {
                        Messages.showWarningDialog(
                                project,
                                "No valid environment variables found in the selected file.",
                                "Import Environment Variables");
                        return;
                    }

                    int result = Messages.showYesNoDialog(
                            project,
                            "Found " + importedVariables.size() + " environment variables. Do you want to import them?",
                            "Import Environment Variables",
                            null);

                    if (result == Messages.YES) {
                        for (EnvironmentVariable variable : importedVariables) {
                            // Check if variable with same key already exists
                            boolean exists = selectedSet.getVariables().stream()
                                    .anyMatch(v -> v.getKey().equals(variable.getKey()));

                            if (exists) {
                                // Ask user if they want to overwrite
                                int overwriteResult = Messages.showYesNoDialog(
                                        project,
                                        "Variable '" + variable.getKey() + "' already exists. Do you want to overwrite it?",
                                        "Import Environment Variables",
                                        null);

                                if (overwriteResult == Messages.YES) {
                                    selectedSet.removeVariableByKey(variable.getKey());
                                    selectedSet.addVariable(variable);
                                }
                            } else {
                                selectedSet.addVariable(variable);
                            }
                        }

                        environmentVariableService.updateSet(selectedSet);
                        updateVariablesTable();

                        Messages.showInfoMessage(
                                project,
                                "Environment variables imported successfully.",
                                "Import Environment Variables");
                    }
                });
            }
        }
    }

    private class ExportSetAction extends AnAction {
        public ExportSetAction() {
            super("Export Set", "Export environment variables to a text file", AllIcons.Actions.MenuSaveall);
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(setsList.getSelectedValue() != null);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            EnvironmentVariableSet selectedSet = setsList.getSelectedValue();
            if (selectedSet != null) {
                // Create a file chooser descriptor for saving files
                FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false)
                        .withTitle("Export Environment Variables")
                        .withDescription("Select a directory to save the environment variables file");

                FileChooser.chooseFile(descriptor, project, null, directory -> {
                    // Create a file in the selected directory with the set name
                    String fileName = selectedSet.getName().replaceAll("[^a-zA-Z0-9.-]", "_") + ".env";
                    java.io.File file = new java.io.File(VfsUtil.virtualToIoFile(directory), fileName);

                    // Export the variables to the file
                    boolean success = TextFileExporter.exportToFile(selectedSet, file);

                    if (success) {
                        Messages.showInfoMessage(
                                project,
                                "Environment variables exported successfully to " + file.getPath(),
                                "Export Environment Variables");
                    } else {
                        Messages.showErrorDialog(
                                project,
                                "Failed to export environment variables to " + file.getPath(),
                                "Export Environment Variables");
                    }
                });
            }
        }
    }

    private class ActivateSetAction extends AnAction {
        public ActivateSetAction() {
            super("Activate Set", "Activate the selected environment variable set", AllIcons.Actions.Execute);
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(setsList.getSelectedValue() != null);
            EnvironmentVariableSet selectedSet = setsList.getSelectedValue();

            environmentManagerService.injectEnvironmentVariables(selectedSet);
            if (selectedSet != null) {
                Optional<EnvironmentVariableSet> activeSet = environmentVariableService.getActiveSet();
                boolean isActive = activeSet.isPresent() && activeSet.get().getId().equals(selectedSet.getId());
                e.getPresentation().setText(isActive ? "Deactivate Set" : "Activate Set");
            }
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            EnvironmentVariableSet selectedSet = setsList.getSelectedValue();
            if (selectedSet != null) {
                Optional<EnvironmentVariableSet> activeSet = environmentVariableService.getActiveSet();
                boolean isActive = activeSet.isPresent() && activeSet.get().getId().equals(selectedSet.getId());

                if (isActive) {
                    environmentVariableService.setActiveSetId(null);
                    Messages.showInfoMessage(
                            project,
                            "Environment variable set '" + selectedSet.getName() + "' has been deactivated.",
                            "Environment Variable Set Deactivated");
                } else {
                    environmentVariableService.setActiveSetId(selectedSet.getId());
                    Messages.showInfoMessage(
                            project,
                            "Environment variable set '" + selectedSet.getName() + "' has been activated.",
                            "Environment Variable Set Activated");
                }

                // Refresh the list to update the active set display
                loadSets();
                setsList.setSelectedValue(selectedSet, true);

                // Refresh the action's presentation
                e.getPresentation().setText(isActive ? "Activate Set" : "Deactivate Set");
            }
        }
    }
}
