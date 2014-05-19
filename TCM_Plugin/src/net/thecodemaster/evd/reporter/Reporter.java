package net.thecodemaster.evd.reporter;

import java.util.List;
import java.util.Map;

import net.thecodemaster.evd.Activator;
import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.BindingResolver;
import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.ui.view.ViewDataModel;
import net.thecodemaster.evd.ui.view.ViewSecurityVulnerabilities;

import org.eclipse.core.resources.IMarker;
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
	private final boolean					problemView;
	private final boolean					textFile;
	private final boolean					xmlFile;

	private static ViewDataModel	rootVdm;

	public Reporter(boolean problemView, boolean textFile, boolean xmlFile) {
		this.problemView = problemView;
		this.textFile = textFile;
		this.xmlFile = xmlFile;

		rootVdm = new ViewDataModel();
	}

	public IProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	public void setProgressMonitor(IProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}

	public void clearOldProblems(List<IResource> resources) {
		for (IResource resource : resources) {
			clearOldProblems(resource);
		}
	}

	private void clearOldProblems(IResource resource) {
		if (problemView) {
			clearMarkers(resource);
		}
		if (textFile) {
			// TODO
		}
		if (xmlFile) {
			// TODO
		}

		clearViewDataModel(resource);
	}

	public void addProblem(int typeVulnerability, IResource resource, DataFlow dataFlow) {
		if (problemView) {
			addMarker(typeVulnerability, resource, dataFlow);
		}
		if (textFile) {
			// TODO
		}
		if (xmlFile) {
			// TODO
		}
	}

	private boolean clearMarkers(IResource resource) {
		try {
			resource.deleteMarkers(Constant.MARKER_ID, true, IResource.DEPTH_INFINITE);
			return true;
		} catch (CoreException e) {
			PluginLogger.logError(e);
			return false;
		}
	}

	private void clearViewDataModel(IResource resource) {
		List<ViewDataModel> vdmToRemove = Creator.newList();

		// 01 - First we iterate over the list to see which elements will be removed.
		for (ViewDataModel vdm : rootVdm.getChildren()) {
			if (vdm.getResource().equals(resource)) {
				vdmToRemove.add(vdm);
			}
		}

		// 02 - Now we really remove them.
		rootVdm.getChildren().removeAll(vdmToRemove);
	}

	private void addMarker(final int typeVulnerability, final IResource resource, final DataFlow dataFlow) {
		add(typeVulnerability, resource, dataFlow);

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

	private ViewSecurityVulnerabilities createView() {
		ViewSecurityVulnerabilities view = new ViewSecurityVulnerabilities();

		view = new ViewSecurityVulnerabilities();
		// view.createPartControl(new Shell(Display.getDefault()));
		view.showView();

		return view;
	}

	public void add(int typeVulnerability, IResource resource, DataFlow df) {
		ViewDataModel parent = null;
		ViewDataModel currentVdm;
		Expression root = df.getRoot();
		List<List<DataFlow>> allVulnerablePaths = df.getAllVulnerablePaths();

		if (allVulnerablePaths.size() > 1) {
			String message = String.format("%s has %d vulnerabilities.", root.toString(), allVulnerablePaths.size());

			parent = add(typeVulnerability, resource, root, message, null);
			if (null != parent) {
				rootVdm.addChildren(parent);
			}
		}

		for (List<DataFlow> vulnerablePaths : allVulnerablePaths) {
			// The last element is the element that have the vulnerability message.
			DataFlow lastElement = vulnerablePaths.get(vulnerablePaths.size() - 1);

			// The path that lead to the vulnerability.
			String fullPath = getFullPath(vulnerablePaths);

			currentVdm = add(typeVulnerability, resource, lastElement.getRoot(), lastElement.getMessage(), fullPath);
			if (null != currentVdm) {
				if (null != parent) {
					parent.addChildren(currentVdm);
				} else {
					rootVdm.addChildren(currentVdm);
				}
			}
		}
	}

	private ViewDataModel add(int typeVulnerability, IResource resource, Expression expr, String message, String fullPath) {
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
