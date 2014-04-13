package net.thecodemaster.sap.utils;

import java.util.Collection;
import java.util.Iterator;

import net.thecodemaster.sap.Activator;
import net.thecodemaster.sap.Manager;
import net.thecodemaster.sap.constants.Constants;
import net.thecodemaster.sap.logger.PluginLogger;
import net.thecodemaster.sap.natures.NatureHandler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author Luciano Sampaio
 */
public abstract class UtilProjects {

  /**
   * Returns the collection of projects which exist under this root. <br/>
   * This collection has only Java projects and which are accessible and opened.
   * 
   * @return An list of projects.
   */
  public static Collection<IProject> getProjectsInWorkspace() {
    // Returns the collection of projects which exist under this root. The projects can be open or closed.
    IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    // This collection only has Java projects which are also accessible and opened.
    Collection<IProject> javaProjects = Creator.newCollection(allProjects.length);

    for (IProject project : allProjects) {
      try {
        if ((project.isAccessible()) && (project.isOpen()) && (project.isNatureEnabled(Constants.JDT_NATURE))) {
          javaProjects.add(project);
        }
      }
      catch (CoreException e) {
        PluginLogger.logError(e);
      }
    }

    // Return the list of all projects in the current workspace.
    return javaProjects;
  }

  /**
   * Returns a collection containing the projects that are being monitored by our plug-in.
   * 
   * @return A collection of projects' names.
   */
  public static Collection<IProject> getMonitoredProjects() {
    IPreferenceStore store = Activator.getDefault().getPreferenceStore();

    // Get the list of monitored projects split by the SEPARATOR constant.
    String storedMonitoredProjects =
      store.getString(Constants.SecurityVulnerabilities.FIELD_MONITORED_PROJECTS);

    // Extract a collection (unique elements) from the string.
    Collection<String> projectsNames = Convert.fromStringToList(storedMonitoredProjects, Constants.SEPARATOR);

    // The list with the projects that are being monitored by our plug-in.
    Collection<IProject> listMonitoredProjects = Creator.newCollection();

    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    for (String projectName : projectsNames) {
      listMonitoredProjects.add(root.getProject(projectName));
    }

    return listMonitoredProjects;
  }

  /**
   * @param projects
   */
  public static void setProjectsToListOfMonitoredProjects(Collection<IProject> projects) {
    // The collection of projects that are being monitored by our plug-in.
    Collection<IProject> monitoredProjects = getMonitoredProjects();

    IPreferenceStore store = Activator.getDefault().getPreferenceStore();

    StringBuilder projectsToSave = new StringBuilder();
    for (IProject project : projects) {
      projectsToSave.append(project.getName()).append(Constants.SEPARATOR);
    }

    store.putValue(Constants.SecurityVulnerabilities.FIELD_MONITORED_PROJECTS, projectsToSave.toString());

    // Add or remove the nature to the each project.
    updateNatureOnProjects(monitoredProjects, projects);

    // Reset the list of analyzers from the Manager.
    Manager.resetManager();
  }

  private static void updateNatureOnProjects(Collection<IProject> oldProjects,
    Collection<IProject> newProjects) {
    // Create a difference from the old and the new list.
    Collection<IProject> projectsToAdd = Creator.newCollection();
    projectsToAdd.addAll(newProjects);
    projectsToAdd.removeAll(oldProjects);

    Collection<IProject> projectsToRemove = Creator.newCollection();
    projectsToRemove.addAll(oldProjects);
    projectsToRemove.removeAll(newProjects);

    try {
      NatureHandler handler = new NatureHandler();
      handler.add(projectsToAdd);
      handler.remove(projectsToRemove);
    }
    catch (CoreException e) {
      PluginLogger.logError(e);
    }
  }

  /**
   * @param projects
   */
  public static void addProjectsToListOfMonitoredProjects(Collection<IProject> projects) {
    // If the collection is empty there is nothing to do.
    if ((null != projects) && (!projects.isEmpty())) {
      // The collection of projects that are being monitored by our plug-in.
      Collection<IProject> monitoredProjects = getMonitoredProjects();

      // Adds the selected projects to the list of monitored projects.
      // Because it is a HashSet collection, it will not allow repeated elements.
      for (IProject project : projects) {
        monitoredProjects.add(project);
      }

      // Save the list back to the preference store.
      setProjectsToListOfMonitoredProjects(monitoredProjects);
    }
  }

  /**
   * @param projects
   */
  public static void removeProjectsFromListOfMonitoredProjects(Collection<IProject> projects) {
    // If the collection is empty there is nothing to do.
    if ((null != projects) && (!projects.isEmpty())) {
      // The collection of projects that are being monitored by our plug-in.
      Collection<IProject> monitoredProjects = getMonitoredProjects();

      // Removes the selected projects from the list of monitored projects.
      for (IProject project : projects) {
        monitoredProjects.remove(project);
      }

      // Save the list back to the preference store.
      setProjectsToListOfMonitoredProjects(monitoredProjects);
    }
  }

  /**
   * Get the list(unique elements) of selected projects by the developer. Even if he/she selected a file.
   * 
   * @param event The data object to pass to the command (and its handler) as it executes.
   * @return A list(unique elements) of selected projects by the developer.
   */
  public static Collection<IProject> getSelectedProjects(ExecutionEvent event) {
    IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
    ISelection selection = window.getActivePage().getSelection();

    return getSelectedProjects(selection);
  }

  /**
   * Get the list(unique elements) of selected projects by the developer. Even if he/she selected a file.
   * 
   * @param selection The objection that contains the current selection.
   * @return A list(unique elements) of selected projects by the developer.
   */
  public static Collection<IProject> getSelectedProjects(ISelection selection) {
    Collection<IProject> projects = Creator.newCollection();

    if (selection instanceof IStructuredSelection) {
      for (Iterator<?> iter = ((IStructuredSelection) selection).iterator(); iter.hasNext();) {
        // Translate the selected object into a project.
        IProject element = Convert.fromResourceToProject(iter.next());

        if (null != element) {
          projects.add(element);
        }
      }
    }

    return projects;
  }

  /**
   * Get the resource types that will be perform the early security vulnerability detection.
   * 
   * @return A list of resource types.
   */
  public static Collection<String> getResourceTypesToPerformDetection() {
    Collection<String> resourceTypes =
      Convert.fromStringToList(Constants.RESOURCE_TYPE_TO_PERFORM_DETECTION, Constants.SEPARATOR);

    return resourceTypes;
  }

}
