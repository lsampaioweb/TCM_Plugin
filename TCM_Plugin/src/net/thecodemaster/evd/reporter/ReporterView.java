package net.thecodemaster.evd.reporter;

import java.util.List;

import net.thecodemaster.evd.Activator;
import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.BindingResolver;
import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.helper.HelperViewDataModel;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.ui.view.ViewDataModel;
import net.thecodemaster.evd.ui.view.ViewSecurityVulnerabilities;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.swt.widgets.Display;

/**
 * This class knows where and how to report the vulnerabilities.
 * 
 * @author Luciano Sampaio
 */
public class ReporterView implements IReporter {

	private static ViewDataModel	rootVdm;

	/**
	 * Default constructor.
	 */
	public ReporterView() {
		rootVdm = new ViewDataModel();
	}

	@Override
	public int getType() {
		return IReporter.SECURITY_VIEW;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearOldProblems(List<IResource> resources) {
		for (IResource resource : resources) {
			clearOldProblems(resource);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearOldProblems(IResource resource) {
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

	public void clearProblem(ViewDataModel vdm, boolean removeChildren) {
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
	 * Delete all old problems of the provided resource from our Security Vulnerability View.
	 * 
	 * @param resource
	 *          The resources that will have all of its old problems deleted from our Security Vulnerability View.
	 */
	private void clearViewDataModel(IResource resource) {
		List<ViewDataModel> vdmToRemove = Creator.newList();

		// 01 - Iterate over the list to see which elements will be removed.
		for (ViewDataModel vdm : rootVdm.getChildren()) {
			if (vdm.getResource().equals(resource)) {
				vdmToRemove.add(vdm);
			}
		}

		removeFromListAndUpdateView(vdmToRemove, false);
	}

	private void removeFromListAndUpdateView(List<ViewDataModel> vdmToRemove, boolean removeChildren) {
		// 01 - Now we really remove them.
		rootVdm.removeChildren(vdmToRemove, removeChildren);

		// 02 - Update the view so the new data can appear and the old ones can be removed.
		updateView();
	}

	/**
	 * @param vdm
	 * @param removeChildren
	 *          True if the children elements should also be removed.
	 */
	private void clearViewDataModel(ViewDataModel vdm, boolean removeChildren) {
		List<ViewDataModel> vdmToRemove = Creator.newList();

		vdmToRemove.add(vdm);

		removeFromListAndUpdateView(vdmToRemove, removeChildren);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addProblem(int typeProblem, IResource resource, DataFlow dataFlow) {
		addToViewDataModel(typeProblem, resource, dataFlow);

		// 02 - Update the view so the new data can appear and the old ones can be removed.
		updateView();
	}

	private ViewSecurityVulnerabilities createView() {
		ViewSecurityVulnerabilities view = new ViewSecurityVulnerabilities();

		view = new ViewSecurityVulnerabilities();
		// view.createPartControl(new Shell(Display.getDefault()));
		view.showView();

		return view;
	}

	/**
	 * Update the Security Vulnerability View so the new data can appear and the old ones can be removed.
	 */
	private void updateView() {
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

	private void addToViewDataModel(int typeProblem, IResource resource, DataFlow df) {
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

				parent = createViewDataModelElement(typeProblem, resource, firstElement.getRoot(), message, null);
				if (null != parent) {
					rootVdm.addChildren(parent);
				}
			}

			currentVdm = createViewDataModelElement(lastElement.getTypeProblem(), resource, lastElement.getRoot(),
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

	private ViewDataModel createViewDataModelElement(int typeProblem, IResource resource, Expression expr,
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
			marker.setAttribute(Constant.Marker.TYPE_SECURITY_VULNERABILITY, typeProblem);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			marker.setAttribute(IMarker.CHAR_START, startPosition);
			marker.setAttribute(IMarker.CHAR_END, endPosition);

			ViewDataModel vdm = new ViewDataModel();
			vdm.setMarker(marker);
			vdm.setExpr(expr);
			vdm.setTypeVulnerability(typeProblem);
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
	 * Get the ViewDataModel that has the provided marker.
	 * 
	 * @param marker
	 *          The marker that will be used to find the ViewDataModel.
	 * @return The ViewDataModel that has the provided marker.
	 */
	public ViewDataModel getViewDataModel(IMarker marker) {
		return rootVdm.getBy(marker);
	}

}
