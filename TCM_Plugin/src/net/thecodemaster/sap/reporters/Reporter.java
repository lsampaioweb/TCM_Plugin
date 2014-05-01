package net.thecodemaster.sap.reporters;

import java.util.Map;

import net.thecodemaster.sap.constants.Constants;
import net.thecodemaster.sap.graph.BindingResolver;
import net.thecodemaster.sap.graph.VulnerabilityPath;
import net.thecodemaster.sap.loggers.PluginLogger;
import net.thecodemaster.sap.utils.Creator;

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

	public void clearProblems(IResource resource) {
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
			resource.deleteMarkers(Constants.MARKER_ID, false, IResource.DEPTH_INFINITE);
			return true;
		} catch (CoreException e) {
			PluginLogger.logError(e);
			return false;
		}
	}

	private void addMarker(int typeVulnerability, IResource resource, VulnerabilityPath vp) {
		try {
			Map<String, Object> markerAttributes = Creator.newMap();

			Expression expr = vp.getRoot();
			String message = vp.getMessage();

			markerAttributes.put(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
			markerAttributes.put(Constants.Marker.TYPE_SECURITY_VULNERABILITY, typeVulnerability);
			markerAttributes.put(IMarker.MESSAGE, message);

			// Get the Compilation Unit of this resource.
			CompilationUnit cUnit = BindingResolver.findParentCompilationUnit(expr);

			int startPosition = expr.getStartPosition();
			int endPosition = startPosition + expr.getLength();
			int lineNumber = cUnit.getLineNumber(startPosition);

			markerAttributes.put(IMarker.LINE_NUMBER, lineNumber);
			markerAttributes.put(IMarker.CHAR_START, startPosition);
			markerAttributes.put(IMarker.CHAR_END, endPosition);

			IMarker marker = resource.createMarker(Constants.MARKER_ID);
			marker.setAttributes(markerAttributes);
		} catch (CoreException e) {
			PluginLogger.logError(e);
		}
	}

}
