package net.thecodemaster.esvd.builder;

import java.util.List;
import java.util.Map;

import net.thecodemaster.esvd.helper.Creator;
import net.thecodemaster.esvd.logger.PluginLogger;
import net.thecodemaster.esvd.reporter.Reporter;
import net.thecodemaster.esvd.ui.l10n.Message;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

/**
 * This class is invoked when Eclipse is going to compile(build) the files in a project that is being edited by the
 * developer.
 * 
 * @Author: Luciano Sampaio
 * @Date: 2014-05-07
 * @Version: 01
 */
public class IncrementalBuilder extends IncrementalProjectBuilder {

	/**
	 * It will guarantee that only one job will be running at any given time.
	 */
	private static MutexRule			rule	= new MutexRule();

	/**
	 * A list with all the projects that were full built. This is important because sometimes Eclipse might call
	 * IncrementalBuild on a project that was not first full built. We need this information for the CallGraph.
	 */
	private static List<IProject>	fullBuiltProjects;
	private BuilderJob						builderJob;

	static {
		reset();
	}

	/**
	 * In case the user change some settings, we have to reset this list and start over.
	 */
	public static void reset() {
		fullBuiltProjects = Creator.newList();
	}

	/**
	 * Add the project to the list of full built projects.
	 * 
	 * @param project
	 *          The project that will be added to the list.
	 */
	private void addProjectToFullBuiltList(IProject project) {
		if (!wasProjectFullBuilt(project)) {
			fullBuiltProjects.add(project);
		}
	}

	/**
	 * Checks if the project was already full built.
	 * 
	 * @param project
	 *          The project that will be checked.
	 * @return True if the project was already full built, otherwise false.
	 */
	private boolean wasProjectFullBuilt(IProject project) {
		return fullBuiltProjects.contains(project);
	}

	/**
	 * Cancel the job if the state is different from RUNNING, which means the job is sleeping or waiting.
	 * 
	 * @param job
	 *          The job that will be canceled if it is not running.
	 */
	private void cancelIfNotRunning(BuilderJob job) {
		if ((null != job) && (job.getState() != Job.RUNNING)) {
			job.cancel();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		try {
			if (kind == FULL_BUILD) {
				fullBuild(monitor);
			} else if (kind == CLEAN_BUILD) {
				clean(monitor);
			} else {
				IResourceDelta delta = getDelta(getProject());
				if (null == delta) {
					fullBuild(monitor);
				} else {
					incrementalBuild(delta, monitor);
				}
			}
		} catch (CoreException e) {
			PluginLogger.logError(e);
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		// Delete old files, markers and etc related to this project.
		Reporter.getInstance().clearOldProblems(getProject());
	}

	protected void fullBuild(final IProgressMonitor monitor) {
		addProjectToFullBuiltList(getProject());

		cancelIfNotRunning(builderJob);

		builderJob = new BuilderJob(Message.Plugin.JOB, getProject());
		builderJob.setRule(rule);
		builderJob.run();
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) {
		if (wasProjectFullBuilt(getProject())) {
			cancelIfNotRunning(builderJob);

			builderJob = new BuilderJob(Message.Plugin.JOB, delta);
			builderJob.setRule(rule);
			builderJob.run();
		} else {
			// Sometimes Eclipse invokes the incremental build without ever invoked the full build,
			// but we need at least one full build to create the full CallGraph. After this first time we're OK.
			fullBuild(monitor);
		}
	}
}