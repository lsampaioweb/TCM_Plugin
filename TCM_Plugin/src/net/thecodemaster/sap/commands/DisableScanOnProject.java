package net.thecodemaster.sap.commands;

import java.util.Collection;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;

/**
 * If the current selected project is being monitored, remove it from the list.
 * 
 * @author Luciano Sampaio
 */
public class DisableScanOnProject extends TCMCommand {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    // Get the list(unique elements) of selected projects by the developer. Even if he/she selected a file.
    Collection<IProject> selectedProjects = getSelectedProjects(event);

    // If the collection is empty there is nothing to do.
    if (!selectedProjects.isEmpty()) {
      // The collection of projects that are being monitored by our plug-in.
      Collection<IProject> monitoredProjects = getListOfMonitoredProjects();

      // Removes the selected projects from the list of monitored projects.
      for (IProject project : selectedProjects) {
        monitoredProjects.remove(project);
      }

      // Save the list back to the preference store.
      saveListOfMonitoredProjects(monitoredProjects);
    }

    return null;
  }

}
