package net.thecodemaster.sap.ui.commands;

import java.util.Collection;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;

/**
 * If the current selected project is not being monitored, add it to the list.
 * 
 * @author Luciano Sampaio
 */
public class EnableScanOnProject extends AbstracCommand {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    // Get the list(unique elements) of selected projects by the developer. Even if he/she selected a file.
    Collection<IProject> selectedProjects = getSelectedProjects(event);

    // If the collection is empty there is nothing to do.
    if (!selectedProjects.isEmpty()) {
      // The collection of projects that are being monitored by our plug-in.
      Collection<IProject> monitoredProjects = getListOfMonitoredProjects();

      // Adds the selected projects to the list of monitored projects.
      // Because it is a HashSet collection, it will not allow repeated elements.
      for (IProject project : selectedProjects) {
        monitoredProjects.add(project);
      }

      // Save the list back to the preference store.
      saveListOfMonitoredProjects(monitoredProjects);
    }

    return null;
  }

}
