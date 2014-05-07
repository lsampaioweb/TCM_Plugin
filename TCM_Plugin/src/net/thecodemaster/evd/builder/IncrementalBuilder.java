package net.thecodemaster.evd.builder;

import java.util.List;
import java.util.Map;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.ui.l10n.Messages;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

public class IncrementalBuilder extends IncrementalProjectBuilder {

	/**
	 * This mutex rule will guarantee that only one job will be running at any given time.
	 */
	private static MutexRule		rule	= new MutexRule();
	/**
	 * A list with all the projects that were full built. This is important because sometimes Eclipse might call
	 * IncrementalBuild on a project that was not first full built. We need this information for the CallGraph.
	 */
	private static List<IProject>	fullBuiltProjects;
	private BuilderJob				jobProject;
	private BuilderJob				jobDelta;

	static {
		fullBuiltProjects = Creator.newList();
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
		// Delete markers set and files created.
		getProject().deleteMarkers(Constant.ID_MARKER, true, IResource.DEPTH_INFINITE);
	}

	protected void fullBuild(final IProgressMonitor monitor) {
		addProjectToFullBuiltList(getProject());

		cancelIfNotRunning(jobProject);

		jobProject = new BuilderJob(Messages.Plugin.JOB, getProject());
		jobProject.setRule(rule);
		jobProject.run();
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) {
		if (wasProjectFullBuilt(getProject())) {
			cancelIfNotRunning(jobDelta);

			jobDelta = new BuilderJob(Messages.Plugin.JOB, delta);
			jobDelta.setRule(rule);
			jobDelta.run();
		} else {
			// Sometimes Eclipse invokes the incremental build without ever invoked the full build,
			// but we need at least one full build to create the full CallGraph. After this first time we're OK.
			fullBuild(monitor);
		}
	}

	private void addProjectToFullBuiltList(IProject project) {
		if (!fullBuiltProjects.contains(project)) {
			fullBuiltProjects.add(project);
		}
	}

	private boolean wasProjectFullBuilt(IProject project) {
		return fullBuiltProjects.contains(project);
	}

	private void cancelIfNotRunning(BuilderJob job) {
		if ((null != job) && (job.getState() != Job.RUNNING)) {
			job.cancel();
		}
	}
}