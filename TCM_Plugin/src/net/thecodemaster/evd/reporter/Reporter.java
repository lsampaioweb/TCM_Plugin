package net.thecodemaster.evd.reporter;

import java.util.List;

import net.thecodemaster.evd.Activator;
import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.BindingResolver;
import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.helper.HelperProjects;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.ui.l10n.Message;
import net.thecodemaster.evd.ui.view.ViewDataModel;
import net.thecodemaster.evd.ui.view.ViewSecurityVulnerabilities;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.swt.widgets.Display;

/**
 * This class knows where and how to report the vulnerabilities.
 * 
 * @author Luciano Sampaio
 */
public class Reporter {

	private IProgressMonitor			progressMonitor;
	private static boolean				problemView;
	private static boolean				textFile;
	private static boolean				xmlFile;

	private static ViewDataModel	rootVdm;

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

		deleteFromListAndUpdateView(vdmToRemove);
	}

	public static void clearProblem(IMarker marker) {
		try {
			// Clear the Markers.
			if (null != marker) {
				marker.delete();
			}

			// Clear the Security Vulnerability View.
			clearViewDataModel(marker);
		} catch (CoreException e) {
			PluginLogger.logError(e);
		}
	}

	/**
	 * TODO - I think only one Marker will be found here, maybe add a BREAK. Delete problems of the provided marker from
	 * our Security Vulnerability View.
	 * 
	 * @param IMarker
	 *          The marker that will have all of its old problems deleted from our Security Vulnerability View.
	 */
	private static void clearViewDataModel(IMarker marker) {
		List<ViewDataModel> vdmToRemove = Creator.newList();

		// 01 - Iterate over the list to see which elements will be removed.
		ViewDataModel vdm = getViewDataModel(marker);
		if (null != vdm) {
			vdmToRemove.add(vdm);
		}

		deleteFromListAndUpdateView(vdmToRemove);
	}

	/**
	 * Get the ViewDataModel that has the provided marker.
	 * 
	 * @param marker
	 *          The marker that will be used to find the ViewDataModel.
	 * @return The ViewDataModel that has the provided marker.
	 */
	public static ViewDataModel getViewDataModel(IMarker marker) {
		return rootVdm.findByMarker(marker);
	}

	private static void deleteFromListAndUpdateView(List<ViewDataModel> vdmToRemove) {
		// 01 - Now we really remove them.
		rootVdm.getChildren().removeAll(vdmToRemove);

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
		String messageTemplate = "";

		if (allVulnerablePaths.size() == 1) {
			messageTemplate = Message.View.SINGLE_VULNERABILITY;
		} else {
			messageTemplate = Message.View.MULTIPLE_VULNERABILITIES;
		}

		return String.format(messageTemplate, firstElement.getRoot().toString(), allVulnerablePaths.size());
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

}
