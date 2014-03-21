package tcm_plugin.commands;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import tcm_plugin.utils.Utils;

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
    Collection<IProject> projects = Utils.newCollection();

    IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
    ISelection selection = window.getActivePage().getSelection();

    if (selection instanceof IStructuredSelection) {
      for (Iterator<?> iter = ((IStructuredSelection) selection).iterator(); iter.hasNext();) {
        // Translate the selected object into a project.
        Object elem = iter.next();
        if (!(elem instanceof IResource)) {
          if (!(elem instanceof IAdaptable))
            continue;
          elem = ((IAdaptable) elem).getAdapter(IResource.class);
          if (!(elem instanceof IResource))
            continue;
        }
        if (!(elem instanceof IProject)) {
          elem = ((IResource) elem).getProject();
          if (!(elem instanceof IProject))
            continue;
        }
        projects.add((IProject) elem);
      }
    }

    return projects;
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
