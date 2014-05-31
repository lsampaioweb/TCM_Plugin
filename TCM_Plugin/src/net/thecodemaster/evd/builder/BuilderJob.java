package net.thecodemaster.evd.builder;

import java.util.List;
import java.util.Map;

import net.thecodemaster.evd.Manager;
import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.helper.Timer;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.ui.l10n.Message;
import net.thecodemaster.evd.visitor.VisitorCallGraph;
import net.thecodemaster.evd.visitor.VisitorPointsToAnalysis;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * This class will perform its operations in a different(new) thread from the UI Thread. So the user will not be
 * blocked.
 * 
 * @Author: Luciano Sampaio
 * @Date: 2014-05-07
 * @Version: 01
 */
public class BuilderJob extends Job {

	private IProject												project;
	private IResourceDelta									delta;

	/**
	 * This object contains all the methods, variables and their interactions, on the project that is being analyzed. At
	 * any given time, we should only have one call graph per project.
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

	/**
	 * Add the project to the list of call graphs.
	 * 
	 * @param project
	 *          The project that will be added to the list.
	 */
	private void addProjectToList(IProject project) {
		if (!mapCallGraphs.containsKey(project)) {
			mapCallGraphs.put(project, new CallGraph());
		}
	}

	/**
	 * Returns the call graph of the current project.
	 * 
	 * @return The call graph of the current project.
	 */
	private CallGraph getCallGraph() {
		if (null != delta) {
			return mapCallGraphs.get(delta.getResource().getProject());
		} else {
			return mapCallGraphs.get(project);
		}
	}

	/**
	 * Returns whether cancellation of current operation has been requested
	 * 
	 * @param reporter
	 * @return true if cancellation has been requested, and false otherwise.
	 */
	private boolean userCanceledProcess(IProgressMonitor monitor) {
		return ((null != monitor) && (monitor.isCanceled()));
	}

	public void run() {
		schedule();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			// 01 - The manager knows what are the actions that should be performed.
			Manager manager = Manager.getInstance();
			if (manager.shouldPerformVerifications()) {
				Timer timerCP = (new Timer("01 - Complete Process: ")).start();

				// 02 - Get the CallGraph instance for this project.
				CallGraph callGraph = getCallGraph();
				if (null == callGraph) {
					String projectName = (null != project) ? project.getName() : "";
					PluginLogger.logError(String.format(Message.Error.CALL_GRAPH_DOES_NOT_CONTAIN_PROJECT, projectName), null);
				} else {
					monitor.beginTask(Message.Plugin.TASK, IProgressMonitor.UNKNOWN);

					VisitorCallGraph visitorCallGraph = new VisitorCallGraph(callGraph);
					List<IResource> resourcesUpdated = Creator.newList();

					if (null != delta) {
						Timer timerD = (new Timer("01.1 - Call Graph Delta: ")).start();
						// 03 - Use the VISITOR pattern to create/populate the call graph.
						resourcesUpdated = visitorCallGraph.run(delta);
						PluginLogger.logIfDebugging(timerD.stop().toString());
					} else if (null != project) {
						Timer timerP = (new Timer("01.1 - Call Graph Project: ")).start();
						// 03 - Use the VISITOR pattern to create/populate the call graph.
						resourcesUpdated = visitorCallGraph.run(project);
						PluginLogger.logIfDebugging(timerP.stop().toString());
					}

					if (!userCanceledProcess(monitor)) {
						Timer timerPA = (new Timer("01.2 - : Points-to Analysis: ")).start();
						// 04 - Link variables and methods to content.
						VisitorPointsToAnalysis pointToAnalysis = new VisitorPointsToAnalysis();
						pointToAnalysis.run(resourcesUpdated, callGraph);
						PluginLogger.logIfDebugging(timerPA.stop().toString());
					}

					if (!userCanceledProcess(monitor)) {
						Timer timerPV = (new Timer("01.3 - Plug-in verifications: ")).start();
						// 05 - Perform the plug-in's verifications.
						manager.run(monitor, resourcesUpdated, callGraph);
						PluginLogger.logIfDebugging(timerPV.stop().toString());
					} else {
						// The user canceled the operation.
						return Status.CANCEL_STATUS;
					}
				}
				PluginLogger.logIfDebugging(timerCP.stop().toString());
			}
		} catch (CoreException e) {
			PluginLogger.logError(e);
			return e.getStatus();
		} catch (Exception e) {
			PluginLogger.logError(e);
			return Status.CANCEL_STATUS;
		} finally {
			if (null != monitor) {
				monitor.done();
			}
		}

		return Status.OK_STATUS;
	}
}
