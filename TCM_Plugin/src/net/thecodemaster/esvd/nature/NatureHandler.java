package net.thecodemaster.esvd.nature;

import java.util.Collection;

import net.thecodemaster.esvd.constant.Constant;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Luciano Sampaio
 */
public class NatureHandler {

	/**
	 * Add the plug-in nature to all the projects in the list.
	 * 
	 * @param projects
	 *          Collection of projects to have the nature added.
	 * @throws CoreException
	 */
	public void add(Collection<IProject> projects) throws CoreException {
		for (IProject project : projects) {
			add(project);
		}
	}

	/**
	 * Add the plug-in nature to the project.
	 * 
	 * @param project
	 *          The project to have the nature added.
	 * @throws CoreException
	 */
	public void add(IProject project) throws CoreException {
		if ((project.isAccessible()) && (project.isOpen())) {
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();

			for (int i = 0; i < natures.length; ++i) {
				if (Constant.NATURE_ID.equals(natures[i])) {
					// If the project already has the nature, there is nothing else to do.
					return;
				}
			}

			// Add the nature.
			String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);
			newNatures[natures.length] = Constant.NATURE_ID;
			description.setNatureIds(newNatures);
			project.setDescription(description, null);
		}
	}

	/**
	 * Remove the plug-in nature from all the projects in the list.
	 * 
	 * @param projects
	 *          Collection of projects to have the nature removed.
	 * @throws CoreException
	 */
	public void remove(Collection<IProject> projects) throws CoreException {
		for (IProject project : projects) {
			remove(project);
		}
	}

	/**
	 * Remove the plug-in nature from the project.
	 * 
	 * @param project
	 *          The project to have the nature removed.
	 * @throws CoreException
	 */
	public void remove(IProject project) throws CoreException {
		if ((project.isAccessible()) && (project.isOpen())) {
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();

			for (int i = 0; i < natures.length; ++i) {
				if (Constant.NATURE_ID.equals(natures[i])) {
					// Remove the nature.
					String[] newNatures = new String[natures.length - 1];
					System.arraycopy(natures, 0, newNatures, 0, i);
					System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);
					description.setNatureIds(newNatures);
					project.setDescription(description, null);
					return;
				}
			}
		}
	}

}
