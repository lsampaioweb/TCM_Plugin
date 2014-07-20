package net.thecodemaster.evd.reporter;

import java.util.List;
import java.util.Map;

import net.thecodemaster.evd.Activator;
import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.BindingResolver;
import net.thecodemaster.evd.graph.flow.DataFlow;
import net.thecodemaster.evd.graph.flow.Flow;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.helper.HelperViewDataModel;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.marker.MarkerManager;
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
	public void addProblem(IResource resource, List<DataFlow> allVulnerablePaths) {
		addToViewDataModel(resource, allVulnerablePaths);

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
		try {
			// Update the user interface asynchronously.
			Display defaultDisplay = Display.getDefault();
			if ((null != defaultDisplay) && (!defaultDisplay.isDisposed())) {
				defaultDisplay.asyncExec(new Runnable() {
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
		} catch (Exception e) {
			PluginLogger.logError(e);
		}
	}

	private void addToViewDataModel(IResource resource, List<DataFlow> dataFlows) {
		for (DataFlow dataFlow : dataFlows) {
			ViewDataModel parent = null;
			ViewDataModel currentVdm;

			List<List<DataFlow>> allVulnerablePaths = dataFlow.getAllVulnerablePaths();

			for (List<DataFlow> vulnerablePaths : allVulnerablePaths) {
				// The first element is where the vulnerability was exploited.
				DataFlow firstElement = dataFlow;

				// The last element is where the vulnerability entered into the application.
				DataFlow lastElement = vulnerablePaths.get(vulnerablePaths.size() - 1);

				// The path that lead to the vulnerability.
				String fullPath = getFullPath(vulnerablePaths);

				if ((null == parent) && (!firstElement.equals(lastElement))) {
					String message = getMessageByNumberOfVulnerablePaths(allVulnerablePaths, firstElement);

					parent = createViewDataModelElement(resource, firstElement.getTypeProblem(), firstElement.getRoot(), message,
							null);
					if (null != parent) {
						rootVdm.addChildren(parent);
					}
				}

				currentVdm = createViewDataModelElement(resource, lastElement.getTypeProblem(), lastElement.getRoot(),
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

	}

	private String getMessageByNumberOfVulnerablePaths(List<List<DataFlow>> allVulnerablePaths, DataFlow firstElement) {
		return HelperViewDataModel.getMessageByNumberOfVulnerablePaths(firstElement.getRoot().toString(),
				allVulnerablePaths.size());
	}

	private ViewDataModel createViewDataModelElement(IResource resource, int typeProblem, Expression expr,
			String message, String fullPath) {
		try {
			int startPosition = expr.getStartPosition();
			int endPosition = startPosition + expr.getLength();
			int lineNumber = 0;

			// Get the Compilation Unit of this resource.
			CompilationUnit cUnit = BindingResolver.getCompilationUnit(expr);
			if (null != cUnit) {
				lineNumber = cUnit.getLineNumber(startPosition);
				resource = cUnit.getJavaElement().getCorrespondingResource();
			}

			// Before I add a new Markers I have to check if this element does not already have one.
			// If the element already has one, we have to add the VulnerabilityType to it and return the same marker.
			// If the element DOES NOT have one, we create one for it.

			IMarker marker = MarkerManager.hasVulnerableMarkerAtPosition(cUnit, resource, expr);
			if (null == marker) {
				Map<String, Object> markerAttributes = Creator.newMap();
				markerAttributes.put(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
				markerAttributes.put(Constant.Marker.TYPE_SECURITY_VULNERABILITY, typeProblem);
				markerAttributes.put(IMarker.MESSAGE, message);
				markerAttributes.put(IMarker.LINE_NUMBER, lineNumber);
				markerAttributes.put(IMarker.CHAR_START, startPosition);
				markerAttributes.put(IMarker.CHAR_END, endPosition);

				marker = MarkerManager.addVulnerableMarker(resource, markerAttributes);
			}

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
		List<String> fullPath = Creator.newList();
		boolean hasAddedFullPath = false;
		for (DataFlow vulnerablePath : listVulnerablePaths) {
			Flow flow = vulnerablePath.getFullPath();

			if ((!hasAddedFullPath) && (null != flow)) {
				fullPath.add(0, flow.getFullPath(fullPath));
				hasAddedFullPath = true;
			}

			Expression root = vulnerablePath.getRoot();
			if (null != root) {
				fullPath.add(root.toString());
			}
		}

		StringBuilder sb = new StringBuilder();
		for (String value : fullPath) {
			if (0 != sb.length()) {
				sb.append(Constant.SEPARATOR_FULL_PATH);
			}

			sb.append(value);
		}

		return sb.toString();
	}

	/**
	 * Get the ViewDataModel that has the provided marker.
	 * 
	 * @param marker
	 *          The marker that will be used to find the ViewDataModel.
	 * @return The ViewDataModel that has the provided marker.
	 */
	public List<ViewDataModel> getViewDataModels(IMarker marker) {
		return rootVdm.getBy(marker);
	}

}
