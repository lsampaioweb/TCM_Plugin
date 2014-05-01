package net.thecodemaster.sap.builders;

import java.util.List;
import java.util.Map;

import net.thecodemaster.sap.Manager;
import net.thecodemaster.sap.graph.CallGraph;
import net.thecodemaster.sap.loggers.PluginLogger;
import net.thecodemaster.sap.ui.l10n.Messages;
import net.thecodemaster.sap.utils.Creator;
import net.thecodemaster.sap.utils.Timer;
import net.thecodemaster.sap.visitors.CallGraphVisitor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author Luciano Sampaio
 */
public class BuilderJob extends Job {

	private IProject						project;
	private IResourceDelta					delta;

	/**
	 * This object contains all the methods, variables and their interactions, on the project that is being analyzed. At
	 * any given time, we should only have on call graph of the code per project.
	 */
	private static Map<IProject, CallGraph>	mapCallGraphs;

	static {
		mapCallGraphs = Creator.newMap();
	}

	private BuilderJob(String name) {
		super(name);
	}

	public BuilderJob(String name, IProject project) {
		this(name);

		this.project = project;
		addProjectToList(project);
	}

	public BuilderJob(String name, IResourceDelta delta) {
		this(name);

		this.delta = delta;
	}

	private void addProjectToList(IProject project) {
		if (!mapCallGraphs.containsKey(project)) {
			mapCallGraphs.put(project, new CallGraph());
		}
	}

	private CallGraph getCallGraph() {
		if (null != delta) {
			return mapCallGraphs.get(delta.getResource().getProject());
		} else {
			return mapCallGraphs.get(project);
		}
	}

	public void run() {
		schedule();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		Timer timerCP = (new Timer("01 - Complete Process: ")).start();
		try {
			CallGraph callGraph = getCallGraph();
			if (null == callGraph) {
				String projectName = (null != project) ? project.getName() : "";
				PluginLogger.logError(String.format(Messages.Error.CALL_GRAPH_DOES_NOT_CONTAIN_PROJECT, projectName),
						null);
			} else {
				monitor.beginTask(Messages.Plugin.TASK, IProgressMonitor.UNKNOWN);

				CallGraphVisitor callGraphVisitor = new CallGraphVisitor(callGraph);
				List<IResource> updatedResources = Creator.newList();

				if (null != delta) {
					Timer timerD = (new Timer("01.1 - Call Graph Delta: ")).start();
					updatedResources = callGraphVisitor.run(delta);
					PluginLogger.logInfo(timerD.stop().toString());
				}

				if (null != project) {
					Timer timerP = (new Timer("01.1 - Call Graph Project: ")).start();
					updatedResources = callGraphVisitor.run(project);
					PluginLogger.logInfo(timerP.stop().toString());
				}

				if ((null != monitor) && (!monitor.isCanceled())) {
					Timer timerPV = (new Timer("01.2 - Plugin verifications: ")).start();
					Manager manager = Manager.getInstance();
					manager.setProgressMonitor(monitor);
					manager.run(updatedResources, callGraph);
					PluginLogger.logInfo(timerPV.stop().toString());
				} else {
					// The user canceled the operation.
					return Status.CANCEL_STATUS;
				}
			}
		} catch (CoreException e) {
			PluginLogger.logError(e);
			return e.getStatus();
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}

		PluginLogger.logInfo(timerCP.stop().toString());
		return Status.OK_STATUS;
	}
}
