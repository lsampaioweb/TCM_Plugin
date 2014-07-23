package net.thecodemaster.esvd.helper;

import java.util.Iterator;
import java.util.List;

import net.thecodemaster.esvd.Activator;
import net.thecodemaster.esvd.Manager;
import net.thecodemaster.esvd.builder.IncrementalBuilder;
import net.thecodemaster.esvd.constant.Constant;
import net.thecodemaster.esvd.logger.PluginLogger;
import net.thecodemaster.esvd.nature.NatureHandler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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
public abstract class HelperProjects {

	/**
	 * The resource types that we want to visit.
	 */
	private static List<String>	resourceTypesWanted;

	/**
	 * Returns the collection of projects which exist under this root. <br/>
	 * This collection has only Java projects and which are accessible and opened.
	 * 
	 * @return An list of projects.
	 */
	public static List<IProject> getProjectsInWorkspace() {
		// Returns the collection of projects which exist under this root. The projects can be open or closed.
		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		// This collection only has Java projects which are also accessible and opened.
		List<IProject> javaProjects = Creator.newList(allProjects.length);

		for (IProject project : allProjects) {
			try {
				if ((project.isAccessible()) && (project.isOpen()) && (project.isNatureEnabled(Constant.JDT_NATURE))) {
					javaProjects.add(project);
				}
			} catch (CoreException e) {
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
	public static List<IProject> getMonitoredProjects() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		// Get the list of monitored projects split by the SEPARATOR constant.
		String storedMonitoredProjects = store.getString(Constant.PrefPageSecurityVulnerability.FIELD_MONITORED_PROJECTS);

		// Extract a collection (unique elements) from the string.
		List<String> projectsNames = Convert.fromStringToList(storedMonitoredProjects, Constant.SEPARATOR_RESOURCES_TYPE);

		// The list with the projects that are being monitored by our plug-in.
		List<IProject> listMonitoredProjects = Creator.newList();

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		for (String projectName : projectsNames) {
			listMonitoredProjects.add(root.getProject(projectName));
		}

		return listMonitoredProjects;
	}

	/**
	 * @param projects
	 */
	public static void setProjectsToListOfMonitoredProjects(List<IProject> projects) {
		// The collection of projects that are being monitored by our plug-in.
		List<IProject> monitoredProjects = getMonitoredProjects();

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		StringBuilder projectsToSave = new StringBuilder();
		for (IProject project : projects) {
			projectsToSave.append(project.getName()).append(Constant.SEPARATOR_RESOURCES_TYPE);
		}

		store.putValue(Constant.PrefPageSecurityVulnerability.FIELD_MONITORED_PROJECTS, projectsToSave.toString());

		// Add or remove the nature to the each project.
		updateNatureOnProjects(monitoredProjects, projects);

		resetPluginState();
	}

	public static void resetPluginState() {
		// Reset the state of the plug-in to its initial state.
		IncrementalBuilder.reset();
		Manager.reset();
	}

	private static void updateNatureOnProjects(List<IProject> oldProjects, List<IProject> newProjects) {
		// Create a difference from the old and the new list.
		List<IProject> projectsToAdd = Creator.newList();
		projectsToAdd.addAll(newProjects);
		projectsToAdd.removeAll(oldProjects);

		List<IProject> projectsToRemove = Creator.newList();
		projectsToRemove.addAll(oldProjects);
		projectsToRemove.removeAll(newProjects);

		try {
			NatureHandler handler = new NatureHandler();
			handler.add(projectsToAdd);
			handler.remove(projectsToRemove);
		} catch (CoreException e) {
			PluginLogger.logError(e);
		}
	}

	/**
	 * @param projects
	 */
	public static void addProjectsToListOfMonitoredProjects(List<IProject> projects) {
		// If the collection is empty there is nothing to do.
		if ((null != projects) && (!projects.isEmpty())) {
			// The collection of projects that are being monitored by our plug-in.
			List<IProject> monitoredProjects = getMonitoredProjects();

			// Adds the selected projects to the list of monitored projects.
			// It should not allow repeated elements.
			for (IProject project : projects) {
				if (!monitoredProjects.contains(project)) {
					monitoredProjects.add(project);
				}
			}

			// Save the list back to the preference store.
			setProjectsToListOfMonitoredProjects(monitoredProjects);
		}
	}

	/**
	 * @param projects
	 */
	public static void removeProjectsFromListOfMonitoredProjects(List<IProject> projects) {
		// If the collection is empty there is nothing to do.
		if ((null != projects) && (!projects.isEmpty())) {
			// The collection of projects that are being monitored by our plug-in.
			List<IProject> monitoredProjects = getMonitoredProjects();

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
	 * @param event
	 *          The data object to pass to the command (and its handler) as it executes.
	 * @return A list(unique elements) of selected projects by the developer.
	 */
	public static List<IProject> getSelectedProjects(ExecutionEvent event) {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		ISelection selection = window.getActivePage().getSelection();

		return getSelectedProjects(selection);
	}

	/**
	 * Get the list(unique elements) of selected projects by the developer. Even if he/she selected a file.
	 * 
	 * @param selection
	 *          The objection that contains the current selection.
	 * @return A list(unique elements) of selected projects by the developer.
	 */
	public static List<IProject> getSelectedProjects(ISelection selection) {
		List<IProject> projects = Creator.newList();

		if (selection instanceof IStructuredSelection) {
			for (Iterator<?> iter = ((IStructuredSelection) selection).iterator(); iter.hasNext();) {
				// Translate the selected object into a project.
				IProject project = Convert.fromResourceToProject(iter.next());

				if ((null != project) && (!projects.contains(project))) {
					projects.add(project);
				}
			}
		}

		return projects;
	}

	/**
	 * Check if the detection should be performed in this resource or not.
	 * 
	 * @param resource
	 *          The resource that will be tested.
	 * @return True if the detection should be performed in this resource, otherwise false.
	 */
	public static boolean isToPerformDetection(IResource resource) {
		if (resource instanceof IFile) {
			if (null == resourceTypesWanted) {
				resourceTypesWanted = getResourceTypesToPerformDetection();
			}

			String fileExtension = (null != resource.getFileExtension()) ? resource.getFileExtension() : "";
			return resourceTypesWanted.contains(fileExtension.toLowerCase());
		}

		// If it reaches this point, it means that the detection should not be performed in this resource.
		return false;
	}

	/**
	 * Get the resource types that will be perform the early security vulnerability detection.
	 * 
	 * @return A list of resource types.
	 */
	public static List<String> getResourceTypesToPerformDetection() {
		List<String> resourceTypes = Convert.fromStringToList(Constant.RESOURCE_TYPE_TO_PERFORM_DETECTION,
				Constant.SEPARATOR_RESOURCES_TYPE);

		return resourceTypes;
	}

}
