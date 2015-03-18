package net.thecodemaster.esvd.ui.command;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;

/**
 * If the current selected project is being monitored, remove it from the list.
 * 
 * @author Luciano Sampaio
 */
public class DisableScanOnProject extends AbstractCommand {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    // Get the list(unique elements) of selected projects by the developer. Even if he/she selected a file.
    List<IProject> selectedProjects = getSelectedProjects(event);

    // If the collection is empty there is nothing to do.
    if (!selectedProjects.isEmpty()) {
      // Save the list back to the preference store.
      removeProjectsFromListOfMonitoredProjects(selectedProjects);
    }

    return null;
  }

}
