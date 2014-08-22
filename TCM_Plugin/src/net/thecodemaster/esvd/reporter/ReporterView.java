package net.thecodemaster.esvd.reporter;

import java.util.List;
import java.util.Map;

import net.thecodemaster.esvd.Activator;
import net.thecodemaster.esvd.constant.Constant;
import net.thecodemaster.esvd.graph.BindingResolver;
import net.thecodemaster.esvd.graph.flow.DataFlow;
import net.thecodemaster.esvd.graph.flow.Flow;
import net.thecodemaster.esvd.helper.Creator;
import net.thecodemaster.esvd.helper.HelperViewDataModel;
import net.thecodemaster.esvd.logger.PluginLogger;
import net.thecodemaster.esvd.marker.MarkerManager;
import net.thecodemaster.esvd.ui.view.ViewDataModel;
import net.thecodemaster.esvd.ui.view.ViewSecurityVulnerabilities;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
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

		// 03 - Just for debugging.
		PluginLogger.logIfDebugging(String.format("01.2 - Found vulnerabilities: %d", rootVdm.getChildren().size()));
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
		// 01 - Iterate over all data flows found.
		for (DataFlow dataFlow : dataFlows) {

			// 02 - Get all vulnerable paths.
			List<List<DataFlow>> allVulnerablePaths = dataFlow.getAllVulnerablePaths();

			for (List<DataFlow> vulnerablePaths : allVulnerablePaths) {
				// 03 - Get the first element is where the vulnerability was exploited.
				DataFlow exitPointElement = dataFlow;

				// 04 - Get the last element is where the vulnerability entered into the application.
				DataFlow entryPointElement = vulnerablePaths.get(vulnerablePaths.size() - 1);

				// 05 - Get the path that lead to the vulnerability.
				String fullPath = getFullPath(vulnerablePaths);

				// 06 - Add the elements into the security view.
				addElementsToView(resource, exitPointElement, entryPointElement, fullPath);
			}
		}

	}

	private void addElementsToView(IResource resource, DataFlow exitPointElement, DataFlow entryPointElement,
			String fullPath) {
		try {
			// ** Add the exitPointElement to the view.

			// 01 - If there is a marker for this element, it means it is already into the view.
			IMarker exitPointMarker = getMarkerFromElement(exitPointElement.getRoot());

			// 02 - Get the ViewDataModel of this marker.
			List<ViewDataModel> vdms = getViewDataModels(exitPointMarker);

			String message = null;
			ViewDataModel parent = null;
			ViewDataModel currentVdm = null;

			// 04 - If the size of the list is 0, it means it is the first occurrence.
			if (vdms.size() == 0) {
				if (!exitPointElement.equals(entryPointElement)) {
					// 04.1 - Get the message that will be displayed on the marker.
					message = getMessageByNumberOfVulnerablePaths(exitPointElement, 1);

					// 04.2 - Create the element that will be add into the view.
					parent = createViewDataModelElement(resource, exitPointElement, message, null);

					// 04.3 - Add the element into the view.
					if (null != parent) {
						rootVdm.addChildren(parent);
					}
				}
			} else {
				// 04.1 - Get the parent (first) element from the list.
				parent = vdms.get(0);

				// 04.2 - Get the message that will be displayed on the marker.
				message = getMessageByNumberOfVulnerablePaths(exitPointElement, parent.getChildren().size() + 1);

				// 04.3 - Update the number of vulnerable paths.
				exitPointMarker.setAttribute(IMarker.MESSAGE, message);
				parent.setMessage(message);
			}

			// ** Add the entryPointElement to the view.
			currentVdm = createViewDataModelElement(resource, entryPointElement, entryPointElement.getMessage(), fullPath);

			if (null != currentVdm) {
				if (null != parent) {
					parent.addChildren(currentVdm);
				} else {
					rootVdm.addChildren(currentVdm);
				}
			}
		} catch (CoreException e) {
			PluginLogger.logError(e);
		}
	}

	private IMarker getMarkerFromElement(Expression expression) throws JavaModelException {
		// 01 - Get the Compilation Unit of this expression.
		CompilationUnit cUnit = BindingResolver.getCompilationUnit(expression);

		if (null != cUnit) {
			IResource resource = cUnit.getJavaElement().getCorrespondingResource();

			return MarkerManager.hasVulnerableMarkerAtPosition(cUnit, resource, expression);
		}

		return null;
	}

	private String getMessageByNumberOfVulnerablePaths(DataFlow firstElement, int nrOfChildren) {
		return HelperViewDataModel.getMessageByNumberOfVulnerablePaths(firstElement.getRoot().toString(), nrOfChildren);
	}

	private ViewDataModel createViewDataModelElement(IResource resource, DataFlow dataFlowElement, String message,
			String fullPath) {
		try {
			int startPosition = dataFlowElement.getRoot().getStartPosition();
			int endPosition = startPosition + dataFlowElement.getRoot().getLength();
			int lineNumber = 0;

			// Get the Compilation Unit of this resource.
			CompilationUnit cUnit = BindingResolver.getCompilationUnit(dataFlowElement.getRoot());
			if (null != cUnit) {
				lineNumber = cUnit.getLineNumber(startPosition);
				resource = cUnit.getJavaElement().getCorrespondingResource();
			}

			// Before I add a new Markers I have to check if this element does not already have one.
			// If the element already has one, we have to add the VulnerabilityType to it and return the same marker.
			// If the element DOES NOT have one, we create one for it.

			IMarker marker = MarkerManager.hasVulnerableMarkerAtPosition(cUnit, resource, dataFlowElement.getRoot());
			if (null == marker) {
				Map<String, Object> markerAttributes = Creator.newMap();
				markerAttributes.put(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
				markerAttributes.put(Constant.Marker.TYPE_SECURITY_VULNERABILITY, dataFlowElement.getTypeProblem());
				markerAttributes.put(IMarker.MESSAGE, message);
				markerAttributes.put(IMarker.LINE_NUMBER, lineNumber);
				markerAttributes.put(IMarker.CHAR_START, startPosition);
				markerAttributes.put(IMarker.CHAR_END, endPosition);

				marker = MarkerManager.addVulnerableMarker(resource, markerAttributes);
			}

			ViewDataModel vdm = new ViewDataModel();
			vdm.setMarker(marker);
			vdm.setExpr(dataFlowElement.getRoot());
			vdm.setTypeVulnerability(dataFlowElement.getTypeProblem());
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
