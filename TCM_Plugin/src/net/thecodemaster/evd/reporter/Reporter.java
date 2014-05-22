package net.thecodemaster.evd.reporter;

import java.util.List;
import java.util.Map;

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
 * @author Luciano Sampaio
 */
public class Reporter {

	private IProgressMonitor			progressMonitor;
	private static boolean				problemView;
	private static boolean				textFile;
	private static boolean				xmlFile;

	private static ViewDataModel	rootVdm;

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

	public static void clearOldProblems(IProject project) {
		try {
			List<IResource> resources = Creator.newList();

			for (IResource resource : project.members()) {
				if (HelperProjects.isToPerformDetection(resource)) {
					resources.add(resource);
				}
			}

			clearOldProblems(resources);
		} catch (CoreException e) {
			PluginLogger.logError(e);
		}
	}

	public static void clearOldProblems(List<IResource> resources) {
		for (IResource resource : resources) {
			clearOldProblems(resource);
		}
	}

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

	private static void clearProblemsFromView(IResource resource) {
		try {
			// Clear the Markers.
			if (resource.exists()) {
				resource.deleteMarkers(Constant.MARKER_ID, true, IResource.DEPTH_INFINITE);
			}

			clearViewDataModel(resource);
		} catch (CoreException e) {
			PluginLogger.logError(e);
		}
	}

	private static void clearViewDataModel(IResource resource) {
		List<ViewDataModel> vdmToRemove = Creator.newList();

		// 01 - First we iterate over the list to see which elements will be removed.
		for (ViewDataModel vdm : rootVdm.getChildren()) {
			if (vdm.getResource().equals(resource)) {
				vdmToRemove.add(vdm);
			}
		}

		// 02 - Now we really remove them.
		rootVdm.getChildren().removeAll(vdmToRemove);
		updateView();
	}

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

	private static ViewSecurityVulnerabilities createView() {
		ViewSecurityVulnerabilities view = new ViewSecurityVulnerabilities();

		view = new ViewSecurityVulnerabilities();
		// view.createPartControl(new Shell(Display.getDefault()));
		view.showView();

		return view;
	}

	private void addProblemToView(final int typeVulnerability, final IResource resource, final DataFlow dataFlow) {
		addToViewDataModel(typeVulnerability, resource, dataFlow);

		updateView();
	}

	public void addToViewDataModel(int typeVulnerability, IResource resource, DataFlow df) {
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

		if (allVulnerablePaths.size() > 1) {
			messageTemplate = Message.View.MULTIPLE_VULNERABILITIES;
		} else {
			messageTemplate = Message.View.SINGLE_VULNERABILITY;
		}

		return String.format(messageTemplate, firstElement.getRoot().toString(), allVulnerablePaths.size());
	}

	private ViewDataModel createViewDataModelElement(int typeVulnerability, IResource resource, Expression expr,
			String message, String fullPath) {
		try {
			Map<String, Object> markerAttributes = Creator.newMap();
			markerAttributes.put(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
			markerAttributes.put(Constant.Marker.TYPE_SECURITY_VULNERABILITY, typeVulnerability);
			markerAttributes.put(IMarker.MESSAGE, message);

			// Get the Compilation Unit of this resource.
			CompilationUnit cUnit = BindingResolver.findParentCompilationUnit(expr);

			int startPosition = expr.getStartPosition();
			int endPosition = startPosition + expr.getLength();
			int lineNumber = cUnit.getLineNumber(startPosition);

			markerAttributes.put(IMarker.LINE_NUMBER, lineNumber);
			markerAttributes.put(IMarker.CHAR_START, startPosition);
			markerAttributes.put(IMarker.CHAR_END, endPosition);

			IMarker marker = resource.createMarker(Constant.MARKER_ID);
			marker.setAttributes(markerAttributes);

			ViewDataModel vdm = new ViewDataModel();
			vdm.setExpr(expr);
			vdm.setMessage(message);
			vdm.setTypeVulnerability(typeVulnerability);
			vdm.setLineNumber(lineNumber);
			vdm.setResource(resource);
			vdm.setFullPath(fullPath);
			vdm.setMarker(marker);

			return vdm;
		} catch (CoreException e) {
			PluginLogger.logError(e);
		}
		return null;
	}

	private String getFullPath(List<DataFlow> listVulnerablePaths) {
		String SEPARATOR = " - ";
		StringBuilder fullPath = new StringBuilder();
		for (DataFlow vulnerablePath : listVulnerablePaths) {
			if (fullPath.length() != 0) {
				fullPath.append(SEPARATOR);
			}
			fullPath.append(vulnerablePath.getRoot().toString());
		}
		return fullPath.toString();
	}

}
