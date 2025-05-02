package cloud.yelynn.envmanager.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Factory class for creating the Environment Manager tool window.
 */
public class EnvironmentManagerToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        EnvironmentManagerToolWindowContent toolWindowContent = new EnvironmentManagerToolWindowContent(project, toolWindow);
        Content content = ContentFactory.getInstance().createContent(
                toolWindowContent.getContent(),
                "",
                false
        );
        toolWindow.getContentManager().addContent(content);
    }
}