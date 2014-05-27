package net.thecodemaster.evd.reporter;

import java.util.List;
import java.util.Map;

import net.thecodemaster.evd.Activator;
import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.BindingResolver;
import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.helper.HelperAnnotation;
import net.thecodemaster.evd.helper.HelperProjects;
import net.thecodemaster.evd.helper.HelperViewDataModel;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.marker.annotation.InvisibleAnnotation;
import net.thecodemaster.evd.ui.view.ViewDataModel;
import net.thecodemaster.evd.ui.view.ViewSecurityVulnerabilities;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.swt.widgets.Display;

/**
 * This class knows where and how to report the vulnerabilities.
 * 
 * @author Luciano Sampaio
 */
public class Reporter {

	private IProgressMonitor															progressMonitor;
	private static boolean																problemView;
	private static boolean																textFile;
	private static boolean																xmlFile;

	private static ViewDataModel													rootVdm;
	private static Map<IPath, List<InvisibleAnnotation>>	invisibleAnnotationsPerFile;

	/**
	 * Default constructor.
	 * 
	 * @param problemView
	 *          If the users wants to display the vulnerabilities into our Security Vulnerability View.
	 * @param textFile
	 *          If the users wants to display the vulnerabilities into a text file.
	 * @param xmlFile
	 *          If the users wants to display the vulnerabilities into a xml file.
	 */
	public Reporter(boolean problemView, boolean textFile, boolean xmlFile) {
		Reporter.problemView = problemView;
		Reporter.textFile = textFile;
		Reporter.xmlFile = xmlFile;

		rootVdm = new ViewDataModel();
		invisibleAnnotationsPerFile = Creator.newMap();
	}

	public IProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	public void setProgressMonitor(IProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}

	/**
	 * Delete all old problems of the provided project from the Marker View and our Security Vulnerability View.
	 * 
	 * @param project
	 *          The project that will have all of its old problems deleted from the Marker View and our Security
	 *          Vulnerability View.
	 */
	public static void clearOldProblems(IProject project) {
		try {
			List<IResource> resources = Creator.newList();

			// 01 - Iterate over all members (Folder and files) of the current project.
			for (IResource resource : project.members()) {
				// 02 - We only care for the java files.
				if (HelperProjects.isToPerformDetection(resource)) {
					resources.add(resource);
				}
			}

			// 03 - Now that we have the list of all resources(java files) from the provided project, we actually delete the
			// old problems.
			clearOldProblems(resources);
		} catch (CoreException e) {
			PluginLogger.logError(e);
		}
	}

	/**
	 * Delete all old problems of the provided list from the Marker View and our Security Vulnerability View.
	 * 
	 * @param resources
	 *          The list of resources that will have all of its old problems deleted from the Marker View and our Security
	 *          Vulnerability View.
	 */
	public static void clearOldProblems(List<IResource> resources) {
		for (IResource resource : resources) {
			clearOldProblems(resource);
		}
	}

	/**
	 * Delete all old problems of the provided resource from the Marker View and our Security Vulnerability View.
	 * 
	 * @param resource
	 *          The resources that will have all of its old problems deleted from the Marker View and our Security
	 *          Vulnerability View.
	 */
	public static void clearOldProblems(IResource resource) {
		if (problemView) {
			clearProblemsFromView(resource);
		}
		if (textFile) {
			// TODO
		}
		if (xmlFile) {
			// TODO
		}
	}

	/**
	 * Delete all old problems of the provided resource from the Marker View and our Security Vulnerability View.
	 * 
	 * @param resource
	 *          The resources that will have all of its old problems deleted from the Marker View and our Security
	 *          Vulnerability View.
	 */
	private static void clearProblemsFromView(IResource resource) {
		try {
			// Clear the Markers.
			if (resource.exists()) {
				resource.deleteMarkers(Constant.MARKER_ID, true, IResource.DEPTH_INFINITE);
			}

			// Clear the Security Vulnerability View.
			clearViewDataModel(resource);
		} catch (CoreException e) {
			PluginLogger.logError(e);
		}
	}

	/**
	 * Delete all old problems of the provided resource from our Security Vulnerability View.
	 * 
	 * @param resource
	 *          The resources that will have all of its old problems deleted from our Security Vulnerability View.
	 */
	private static void clearViewDataModel(IResource resource) {
		List<ViewDataModel> vdmToRemove = Creator.newList();

		// 01 - Iterate over the list to see which elements will be removed.
		for (ViewDataModel vdm : rootVdm.getChildren()) {
			if (vdm.getResource().equals(resource)) {
				vdmToRemove.add(vdm);
			}
		}

		removeFromListAndUpdateView(vdmToRemove, false);
	}

	public static void clearProblem(ViewDataModel vdm, boolean removeChildren) {
		try {
			// Clear the Marker and the children' markers.
			if (null != vdm.getMarker()) {
				vdm.removeMarker(removeChildren);
			}

			// Clear the Security Vulnerability View.
			clearViewDataModel(vdm, removeChildren);
		} catch (CoreException e) {
			PluginLogger.logError(e);
		}
	}

	/**
	 * @param vdm
	 * @param removeChildren
	 *          True if the children elements should also be removed.
	 */
	private static void clearViewDataModel(ViewDataModel vdm, boolean removeChildren) {
		List<ViewDataModel> vdmToRemove = Creator.newList();

		vdmToRemove.add(vdm);

		removeFromListAndUpdateView(vdmToRemove, removeChildren);
	}

	/**
	 * Get the ViewDataModel that has the provided marker.
	 * 
	 * @param marker
	 *          The marker that will be used to find the ViewDataModel.
	 * @return The ViewDataModel that has the provided marker.
	 */
	public static ViewDataModel getViewDataModel(IMarker marker) {
		return rootVdm.getBy(marker);
	}

	private static void removeFromListAndUpdateView(List<ViewDataModel> vdmToRemove, boolean removeChildren) {
		// 01 - Now we really remove them.
		rootVdm.removeChildren(vdmToRemove, removeChildren);

		// 02 - Update the view so the new data can appear and the old ones can be removed.
		updateView();
	}

	/**
	 * Add the problem to one or more of the options selected by the user.
	 * 
	 * @param typeVulnerability
	 *          The type of the vulnerability.
	 * @param resource
	 *          The resource where the vulnerability was found.
	 * @param dataFlow
	 *          The data flow of the vulnerability, from where it started to where it finished. {@link DataFlow}.
	 */
	public void addProblem(int typeVulnerability, IResource resource, DataFlow dataFlow) {
		if (problemView) {
			addProblemToView(typeVulnerability, resource, dataFlow);
		}
		if (textFile) {
			// TODO
		}
		if (xmlFile) {
			// TODO
		}
	}

	private void addProblemToView(final int typeVulnerability, final IResource resource, final DataFlow dataFlow) {
		addToViewDataModel(typeVulnerability, resource, dataFlow);

		// 02 - Update the view so the new data can appear and the old ones can be removed.
		updateView();
	}

	private static ViewSecurityVulnerabilities createView() {
		ViewSecurityVulnerabilities view = new ViewSecurityVulnerabilities();

		view = new ViewSecurityVulnerabilities();
		// view.createPartControl(new Shell(Display.getDefault()));
		view.showView();

		return view;
	}

	/**
	 * Update the Security Vulnerability View so the new data can appear and the old ones can be removed.
	 */
	private static void updateView() {
		// Update the user interface asynchronously.
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				ViewSecurityVulnerabilities view = (ViewSecurityVulnerabilities) Activator.getDefault().findView(
						Constant.VIEW_ID);
				if (null == view) {
					view = createView();
				}

				view.addToView(rootVdm);
			}
		});
	}

	private void addToViewDataModel(int typeVulnerability, IResource resource, DataFlow df) {
		ViewDataModel parent = null;
		ViewDataModel currentVdm;
		// Expression root = df.getRoot();
		List<List<DataFlow>> allVulnerablePaths = df.getAllVulnerablePaths();

		for (List<DataFlow> vulnerablePaths : allVulnerablePaths) {
			// The first element is where the vulnerability was exploited.
			DataFlow firstElement = df;

			// The last element is where the vulnerability entered into the application.
			DataFlow lastElement = vulnerablePaths.get(vulnerablePaths.size() - 1);

			// The path that lead to the vulnerability.
			String fullPath = getFullPath(vulnerablePaths);

			if ((null == parent) && (firstElement != lastElement)) {
				String message = getMessageByNumberOfVulnerablePaths(allVulnerablePaths, firstElement);

				parent = createViewDataModelElement(typeVulnerability, resource, firstElement.getRoot(), message, null);
				if (null != parent) {
					rootVdm.addChildren(parent);
				}
			}

			currentVdm = createViewDataModelElement(typeVulnerability, resource, lastElement.getRoot(),
					lastElement.getMessage(), fullPath);
			if (null != currentVdm) {
				if (null != parent) {
					parent.addChildren(currentVdm);
				} else {
					rootVdm.addChildren(currentVdm);
				}
			}
		}
	}

	private String getMessageByNumberOfVulnerablePaths(List<List<DataFlow>> allVulnerablePaths, DataFlow firstElement) {
		return HelperViewDataModel.getMessageByNumberOfVulnerablePaths(firstElement.getRoot().toString(),
				allVulnerablePaths.size());
	}

	private ViewDataModel createViewDataModelElement(int typeVulnerability, IResource resource, Expression expr,
			String message, String fullPath) {
		try {
			int startPosition = expr.getStartPosition();
			int endPosition = startPosition + expr.getLength();
			int lineNumber = 0;

			// Get the Compilation Unit of this resource.
			CompilationUnit cUnit = BindingResolver.getParentCompilationUnit(expr);
			if (null != cUnit) {
				lineNumber = cUnit.getLineNumber(startPosition);
				resource = cUnit.getJavaElement().getCorrespondingResource();
			}

			IMarker marker = resource.createMarker(Constant.MARKER_ID);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
			marker.setAttribute(Constant.Marker.TYPE_SECURITY_VULNERABILITY, typeVulnerability);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			marker.setAttribute(IMarker.CHAR_START, startPosition);
			marker.setAttribute(IMarker.CHAR_END, endPosition);

			ViewDataModel vdm = new ViewDataModel();
			vdm.setMarker(marker);
			vdm.setExpr(expr);
			vdm.setTypeVulnerability(typeVulnerability);
			vdm.setMessage(message);
			vdm.setLineNumber(lineNumber);
			vdm.setResource(resource);
			vdm.setFullPath(fullPath);

			return vdm;
		} catch (CoreException e) {
			PluginLogger.logError(e);
		}
		return null;
	}

	private String getFullPath(List<DataFlow> listVulnerablePaths) {
		StringBuilder fullPath = new StringBuilder();
		for (DataFlow vulnerablePath : listVulnerablePaths) {
			if (0 != fullPath.length()) {
				fullPath.append(Constant.SEPARATOR_FULL_PATH);
			}
			fullPath.append(vulnerablePath.getRoot().toString());
		}
		return fullPath.toString();
	}

	/**
	 * Add our invisible annotation into the source code.
	 * 
	 * @param marker
	 *          The marker that will be used to add the annotation.
	 */
	public static void addInvisibleAnnotation(ASTNode node) {
		// 01 - Get the Compilation Unit which this node belongs to.
		CompilationUnit cu = BindingResolver.getParentCompilationUnit(node);
		if (null != cu) {
			InvisibleAnnotation invisibleAnnotation = HelperAnnotation.addInvisibleAnnotation(cu, node);
			// addToInternalList(getPath(cu), invisibleAnnotation);
		}
	}

	private static IPath getPath(CompilationUnit cu) {
		return (null != cu) ? cu.getJavaElement().getPath() : null;
	}

	public boolean hasAnnotationAtPosition(ASTNode node) {
		return HelperAnnotation.hasAnnotationAtPosition(node);
		// return HelperAnnotation.hasAnnotationAtPosition(node, invisibleAnnotationsPerFile);
	}

	private static void addToInternalList(IPath path, InvisibleAnnotation annotation) {
		if (null != annotation) {
			// 01 - Check if the current file is already in the list.
			if (!invisibleAnnotationsPerFile.containsKey(path)) {
				List<InvisibleAnnotation> invisibleAnnotations = Creator.newList();

				invisibleAnnotationsPerFile.put(path, invisibleAnnotations);
			}

			// 02 - Get the list of annotations in the current file.
			List<InvisibleAnnotation> invisibleAnnotations = invisibleAnnotationsPerFile.get(path);

			// 03 - Add the annotation to the list.
			if (!invisibleAnnotations.contains(annotation)) {
				invisibleAnnotations.add(annotation);
			}
		}
	}

}
