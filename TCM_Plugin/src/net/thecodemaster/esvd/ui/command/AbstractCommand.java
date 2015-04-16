package net.thecodemaster.esvd.ui.command;

import java.util.List;

import net.thecodemaster.esvd.helper.HelperProjects;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;

/**
 * If the current selected project is not being monitored, add it to the list.
 * 
 * @author Luciano Sampaio
 */
public abstract class AbstractCommand extends AbstractHandler {

	/**
	 * Get the list(unique elements) of selected projects by the developer. Even if he/she selected a file.
	 * 
	 * @param event
	 *          The data object to pass to the command (and its handler) as it executes.
	 * @return A list(unique elements) of selected projects by the developer.
	 */
	protected List<IProject> getSelectedProjects(ExecutionEvent event) {
		return HelperProjects.getSelectedProjects(event);
	}

	/**
	 * Returns a collection containing the projects that are being monitored by our plug-in.
	 * 
	 * @return A collection of projects' names.
	 */
	protected List<IProject> getListOfMonitoredProjects() {
		return HelperProjects.getMonitoredProjects();
	}

	/**
	 * @param projects
	 */
	protected void addProjectsToListOfMonitoredProjects(List<IProject> projects) {
		HelperProjects.addProjectsToListOfMonitoredProjects(projects);
	}

	/**
	 * @param projects
	 */
	protected void removeProjectsFromListOfMonitoredProjects(List<IProject> projects) {
		HelperProjects.removeProjectsFromListOfMonitoredProjects(projects);
	}

}
