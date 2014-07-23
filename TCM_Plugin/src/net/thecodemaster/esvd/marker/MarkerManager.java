package net.thecodemaster.esvd.marker;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.thecodemaster.esvd.constant.Constant;
import net.thecodemaster.esvd.graph.BindingResolver;
import net.thecodemaster.esvd.helper.Creator;
import net.thecodemaster.esvd.logger.PluginLogger;
import net.thecodemaster.esvd.ui.l10n.Message;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * This class knows where and how to report the vulnerabilities.
 * 
 * @author Luciano Sampaio
 */
public class MarkerManager {

	private static IMarker addMarkers(IResource resource, String type, Map<String, Object> markerAttributes) {
		try {
			IMarker marker = resource.createMarker(type);
			marker.setAttributes(markerAttributes);

			return marker;
		} catch (CoreException e) {
			PluginLogger.logError(e);
		}

		return null;
	}

	public static IMarker addVulnerableMarker(IResource resource, Map<String, Object> markerAttributes) {
		return addMarkers(resource, Constant.MARKER_ID, markerAttributes);
	}

	/**
	 * Add our invisible marker into the source code.
	 * 
	 * @return
	 */
	public static IMarker addInvisible(ASTNode node) {
		try {
			IResource resource = null;
			int lineNumber = 0;
			int startPosition = node.getStartPosition();
			int endPosition = startPosition + node.getLength();

			// Get the Compilation Unit of this resource.
			CompilationUnit cUnit = BindingResolver.getCompilationUnit(node);
			if (null != cUnit) {
				lineNumber = cUnit.getLineNumber(startPosition);
				resource = cUnit.getJavaElement().getCorrespondingResource();
			}

			Map<String, Object> markerAttributes = Creator.newMap();
			markerAttributes.put(IMarker.MESSAGE, Message.View.FALSE_POSITIVE);
			markerAttributes.put(IMarker.LINE_NUMBER, lineNumber);
			markerAttributes.put(IMarker.CHAR_START, startPosition);
			markerAttributes.put(IMarker.CHAR_END, endPosition);

			return addMarkers(resource, Constant.MARKER_ID_INVISIBLE, markerAttributes);
		} catch (JavaModelException e) {
			PluginLogger.logError(e);
		}

		return null;
	}

	private static IMarker hasMarkerAtPosition(CompilationUnit cUnit, IResource resource, ASTNode node, String type) {
		try {
			List<IMarker> markers = Arrays.asList(resource.findMarkers(type, false, 0));

			int startPosition = node.getStartPosition();
			int lineNumber = cUnit.getLineNumber(startPosition);
			int endPosition = startPosition + node.getLength();

			for (IMarker marker : markers) {
				int nodeLineNumber = marker.getAttribute(IMarker.LINE_NUMBER, 0);
				int nodeStartPosition = marker.getAttribute(IMarker.CHAR_START, 0);
				int nodeEndPosition = marker.getAttribute(IMarker.CHAR_END, 0);

				if ((lineNumber == nodeLineNumber) && (startPosition == nodeStartPosition) && (endPosition == nodeEndPosition)) {
					return marker;
				}
			}
		} catch (CoreException e) {
			PluginLogger.logError(e);
		}

		return null;
	}

	public static IMarker hasInvisibleMarkerAtPosition(CompilationUnit cUnit, IResource resource, ASTNode node) {
		return hasMarkerAtPosition(cUnit, resource, node, Constant.MARKER_ID_INVISIBLE);
	}

	public static IMarker hasVulnerableMarkerAtPosition(CompilationUnit cUnit, IResource resource, ASTNode node) {
		return hasMarkerAtPosition(cUnit, resource, node, Constant.MARKER_ID);
	}

}
