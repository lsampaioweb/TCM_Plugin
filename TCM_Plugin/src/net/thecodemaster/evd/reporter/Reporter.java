package net.thecodemaster.evd.reporter;

import java.util.List;

import net.thecodemaster.evd.Manager;
import net.thecodemaster.evd.graph.flow.DataFlow;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.helper.HelperProjects;
import net.thecodemaster.evd.logger.PluginLogger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This class knows where and how to report the vulnerabilities.
 * 
 * @author Luciano Sampaio
 */
public class Reporter implements IReporter {

	private static Reporter				instance	= null;
	private IProgressMonitor			progressMonitor;
	private final List<IReporter>	reporters;

	/**
	 * Default constructor.
	 */
	private Reporter() {
		reporters = Creator.newList();
	}

	/**
	 * The user has changed some options from the tool. It is necessary to reset the reporter.
	 */
	public static void reset() {
		instance = null;
	}

	/**
	 * Creates one instance of the Manager class if it was not created before. <br/>
	 * After that always return the same instance of the Manager class.
	 * 
	 * @return Return the same instance of the Manager class.
	 */
	public static Reporter getInstance() {
		if (instance == null) {
			synchronized (Manager.class) {
				if (instance == null) {
					instance = new Reporter();
				}
			}
		}
		return instance;
	}

	@Override
	public int getType() {
		return 0;
	}

	public IProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	public void setProgressMonitor(IProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}

	public void addReporter(IReporter reporter) {
		reporters.add(reporter);
	}

	private List<IReporter> getReporters() {
		return reporters;
	}

	public IReporter getReporter(int type) {
		for (IReporter reporter : getReporters()) {
			if (reporter.getType() == type) {
				return reporter;
			}
		}

		return null;
	}

	/**
	 * Delete all old problems of the provided project from the Marker View and our Security Vulnerability View.
	 * 
	 * @param project
	 *          The project that will have all of its old problems deleted from the Marker View and our Security
	 *          Vulnerability View.
	 */
	public void clearOldProblems(IProject project) {
		try {
			List<IResource> resources = Creator.newList();

			// 01 - Iterate over all members (Folder and files) of the current project.
			for (IResource resource : project.members()) {
				// 02 - We only care for the java files.
				if (HelperProjects.isToPerformDetection(resource)) {
					resources.add(resource);
				}
			}

			// 03 - Now that we have the list of all resources(java files) from the provided project,
			// we actually delete the old problems.
			clearOldProblems(resources);
		} catch (CoreException e) {
			PluginLogger.logError(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearOldProblems(List<IResource> resources) {
		for (IReporter reporter : getReporters()) {
			reporter.clearOldProblems(resources);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearOldProblems(IResource resource) {
		for (IReporter reporter : getReporters()) {
			reporter.clearOldProblems(resource);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addProblem(IResource resource, int typeProblem, DataFlow dataFlow) {
		for (IReporter reporter : getReporters()) {
			reporter.addProblem(resource, typeProblem, dataFlow);
		}
	}

}
