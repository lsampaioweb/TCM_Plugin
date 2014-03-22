package net.thecodemaster.sap.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import net.thecodemaster.sap.Activator;
import net.thecodemaster.sap.constants.Constants;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author Luciano Sampaio
 */
public abstract class Utils {

  /**
   * Returns the collection of projects which exist under this root. <br/>
   * This collection has only Java projects and which are accessible and opened.
   * 
   * @return An list of projects.
   */
  public static Collection<IProject> getListOfProjectsInWorkspace() {
    // Returns the collection of projects which exist under this root. The projects can be open or closed.
    IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    // This collection only has Java projects which are also accessible and opened.
    Collection<IProject> javaProjects = Utils.newCollection(allProjects.length);

    for (IProject project : allProjects) {
      try {
        if ((project.isAccessible()) && (project.isOpen()) && (project.isNatureEnabled(Constants.JDT_NATURE))) {
          javaProjects.add(project);
        }
      }
      catch (CoreException e) {
        PluginLog.logError(e);
      }
    }

    // Return the list of all projects in the current workspace.
    return javaProjects;
  }

  /**
   * Returns a collection of strings that was inside the string passed as parameter.
   * 
   * @param content The string containing its content separated by the SEPARATOR constant.
   * @param separator The separator that was used between each value.
   * @return A collection of strings;
   */
  public static Collection<String> getListFromString(String content, String separator) {
    Collection<String> collection = newCollection();

    if ((null != content) && (content.length() > 0)) {
      collection = Arrays.asList(content.split(separator));
    }

    return collection;
  }

  /**
   * Returns a collection containing the projects that are being monitored by our plug-in.
   * 
   * @return A collection of projects' names.
   */
  public static Collection<IProject> getListOfMonitoredProjects() {
    IPreferenceStore store = Activator.getDefault().getPreferenceStore();

    // Get the list of monitored projects split by the SEPARATOR constant.
    String storedMonitoredProjects = store.getString(Constants.SecurityVulnerabilities.FIELD_MONITORED_PROJECTS);

    // Extract a collection (unique elements) from the string.
    Collection<String> projectsNames = getListFromString(storedMonitoredProjects, Constants.SEPARATOR);

    // The list with the projects that are being monitored by our plug-in.
    Collection<IProject> listMonitoredProjects = newCollection();

    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    for (String projectName : projectsNames) {
      listMonitoredProjects.add(root.getProject(projectName));
    }

    return listMonitoredProjects;
  }

  /**
   * @param monitoredProjects
   */
  public static void saveListOfMonitoredProjects(Collection<IProject> monitoredProjects) {
    IPreferenceStore store = Activator.getDefault().getPreferenceStore();

    StringBuilder projectsToSave = new StringBuilder();
    for (IProject project : monitoredProjects) {
      projectsToSave.append(project.getName()).append(Constants.SEPARATOR);
    }

    store.putValue(Constants.SecurityVulnerabilities.FIELD_MONITORED_PROJECTS, projectsToSave.toString());
  }

  /**
   * Return a Collection of type T.
   * 
   * @return Collection<T>
   */
  public static <T> Collection<T> newCollection() {
    return new HashSet<T>();
  }

  /**
   * Return a Collection of type T.
   * 
   * @param initialCapacity the initial capacity of the Collection.
   * @return Collection<T>
   */
  public static <T> Collection<T> newCollection(int initialCapacity) {
    return new HashSet<T>(initialCapacity);
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
    Collection<IProject> projects = newCollection();

    if (selection instanceof IStructuredSelection) {
      for (Iterator<?> iter = ((IStructuredSelection) selection).iterator(); iter.hasNext();) {
        // Translate the selected object into a project.
        IProject element = extractProjectFromResource(iter.next());

        if (null != element) {
          projects.add(element);
        }
      }
    }

    return projects;
  }

  /**
   * Extract the project where this resource is under.
   * 
   * @param element the element that will be used to extract the project.
   * @return The project where this resource is under.
   */
  public static IProject extractProjectFromResource(Object element) {
    if (!(element instanceof IResource)) {
      if (!(element instanceof IAdaptable)) {
        return null;
      }
      element = ((IAdaptable) element).getAdapter(IResource.class);
      if (!(element instanceof IResource)) {
        return null;
      }
    }
    if (!(element instanceof IProject)) {
      element = ((IResource) element).getProject();
      if (!(element instanceof IProject)) {
        return null;
      }
    }

    return (IProject) element;
  }
}
