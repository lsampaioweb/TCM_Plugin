package net.thecodemaster.sap.ui.commands;

import java.util.Collection;

import net.thecodemaster.sap.utils.Utils;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;

/**
 * If the current selected project is not being monitored, add it to the list.
 * 
 * @author Luciano Sampaio
 */
public abstract class TCMCommand extends AbstractHandler {

  /**
   * Get the list(unique elements) of selected projects by the developer. Even if he/she selected a file.
   * 
   * @param event The data object to pass to the command (and its handler) as it executes.
   * @return A list(unique elements) of selected projects by the developer.
   */
  protected Collection<IProject> getSelectedProjects(ExecutionEvent event) {
    return Utils.getSelectedProjects(event);
  }

  /**
   * Returns a collection containing the projects that are being monitored by our plug-in.
   * 
   * @return A collection of projects' names.
   */
  protected Collection<IProject> getListOfMonitoredProjects() {
    return Utils.getListOfMonitoredProjects();
  }

  /**
   * @param monitoredProjects
   */
  protected void saveListOfMonitoredProjects(Collection<IProject> monitoredProjects) {
    Utils.saveListOfMonitoredProjects(monitoredProjects);
  }

}
