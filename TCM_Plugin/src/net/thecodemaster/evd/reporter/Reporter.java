package net.thecodemaster.evd.reporter;

import java.util.List;
import java.util.Map;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.BindingResolver;
import net.thecodemaster.evd.graph.VulnerabilityPath;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.logger.PluginLogger;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;

/**
 * @author Luciano Sampaio
 */
public class Reporter {

	private IProgressMonitor	progressMonitor;
	private final boolean			problemView;
	private final boolean			textFile;
	private final boolean			xmlFile;

	public Reporter(boolean problemView, boolean textFile, boolean xmlFile) {
		this.problemView = problemView;
		this.textFile = textFile;
		this.xmlFile = xmlFile;
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
	}

	public void addProblem(int typeVulnerability, IResource resource, VulnerabilityPath vp) {
		if (problemView) {
			addMarker(typeVulnerability, resource, vp);
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

	private void addMarker(int typeVulnerability, IResource resource, VulnerabilityPath vp) {
		try {
			Expression expr = vp.getRoot();
			List<List<VulnerabilityPath>> allVulnerablePaths = vp.getAllVulnerablePaths();

			for (List<VulnerabilityPath> listVulnerablePaths : allVulnerablePaths) {
				Map<String, Object> markerAttributes = Creator.newMap();
				markerAttributes.put(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
				markerAttributes.put(Constant.Marker.TYPE_SECURITY_VULNERABILITY, typeVulnerability);

				String fullPath = "";

				for (VulnerabilityPath vulnerablePath : listVulnerablePaths) {
					fullPath += vulnerablePath.getRoot().toString() + " - ";
				}
				System.out.println(fullPath);

				int indexLastElement = listVulnerablePaths.size() - 1;
				VulnerabilityPath lastElement = listVulnerablePaths.get(indexLastElement);
				expr = lastElement.getRoot();
				String message = lastElement.getMessage();
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

				// ViewSecurityVulnerabilities view = new ViewSecurityVulnerabilities();
				// view.add();
			}
		} catch (CoreException e) {
			PluginLogger.logError(e);
		}
	}

}
