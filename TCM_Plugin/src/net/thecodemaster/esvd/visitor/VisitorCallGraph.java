package net.thecodemaster.esvd.visitor;

import java.util.List;

import net.thecodemaster.esvd.graph.BindingResolver;
import net.thecodemaster.esvd.graph.CallGraph;
import net.thecodemaster.esvd.helper.Creator;
import net.thecodemaster.esvd.helper.HelperProjects;
import net.thecodemaster.esvd.logger.PluginLogger;
import net.thecodemaster.esvd.reporter.Reporter;
import net.thecodemaster.esvd.ui.l10n.Message;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * This class creates a CompilationUnit from the current resource and uses the VISITOR pattern to create the call graph.
 * 
 * @Author: Luciano Sampaio
 * @Date: 2014-05-07
 * @Version: 01
 */
public class VisitorCallGraph implements IResourceVisitor, IResourceDeltaVisitor {

	/**
	 * The resource files that were updated since the last build.
	 */
	private final List<IResource>	resourcesUpdated;

	/**
	 * This object contains all the methods, variables and their interactions, on the project that is being analyzed. At
	 * any given time, we should only have one call graph of the code.
	 */
	private final CallGraph				callGraph;
	private IProgressMonitor			progressMonitor;

	public VisitorCallGraph(CallGraph callGraph, IProgressMonitor monitor) {
		resourcesUpdated = Creator.newList();
		this.callGraph = callGraph;
		setProgressMonitor(monitor);
	}

	private CallGraph getCallGraph() {
		return callGraph;
	}

	private final void setProgressMonitor(IProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}

	private IProgressMonitor getProgressMonitor() {
		return progressMonitor;
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

	/**
	 * Notifies that a subtask of the main task is beginning.
	 * 
	 * @param taskName
	 *          The text that will be displayed to the user.
	 */
	private void setSubTask(String taskName) {
		if (null != getProgressMonitor()) {
			getProgressMonitor().subTask(taskName);
		}
	}

	public List<IResource> run(IProject project) throws CoreException {
		project.accept(this);

		return resourcesUpdated;
	}

	public List<IResource> run(IResourceDelta delta) throws CoreException {
		delta.accept(this);

		return resourcesUpdated;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		IResource resource = delta.getResource();

		switch (delta.getKind()) {
			case IResourceDelta.REMOVED:
				// Delete old files, markers and etc related to this project.
				Reporter.getInstance().clearOldProblems(resource);
				break;
			case IResourceDelta.ADDED:
			case IResourceDelta.CHANGED:
				return visit(resource);
		}
		// Return true to continue visiting children.
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean visit(IResource resource) throws CoreException {
		if (!userCanceledProcess(getProgressMonitor())) {
			if (HelperProjects.isToPerformDetection(resource)) {
				try {
					ICompilationUnit cu = JavaCore.createCompilationUnitFrom((IFile) resource);

					if (cu.isStructureKnown()) {
						setSubTask(Message.Plugin.VISITOR_CALL_GRAPH_SUB_TASK + resource.getName());
						// Creates the AST for the ICompilationUnits.
						// Timer timer = (new Timer("01.1.1 - Parsing: " + resource.getName())).start();
						CompilationUnit cUnit = BindingResolver.parse(cu);
						// PluginLogger.logIfDebugging(timer.stop().toString());

						// Visit the compilation unit.
						// timer = (new Timer("01.1.2 - Visiting: " + resource.getName())).start();

						// Remove old interactions of this resource.
						getCallGraph().remove(resource);

						cUnit.accept(new VisitorCompilationUnit(resource, getCallGraph()));
						// PluginLogger.logIfDebugging(timer.stop().toString());

						// Add this resource to the list of updated resources.
						resourcesUpdated.add(resource);
					}
				} catch (JavaModelException e) {
					String resourceName = (null != resource) ? resource.getName() : "";
					PluginLogger.logError(resourceName, e);
				}
			}
			// Returns true to continue visiting children.
			return true;
		} else {
			// The user has canceled the process.
			// Returns false to stop visiting the files.
			return false;
		}
	}

}
