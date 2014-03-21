package tcm_plugin.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;

import tcm_plugin.Activator;
import tcm_plugin.constants.Constants;

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
  public static Collection<IProject> getListOfJavaProjectsInWorkspace() {
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
        // TODO Auto-generated catch block
        e.printStackTrace();
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
}
