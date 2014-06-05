package net.thecodemaster.evd.marker;

import java.util.Arrays;
import java.util.List;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.BindingResolver;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.ui.l10n.Message;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * This class knows where and how to report the vulnerabilities.
 * 
 * @author Luciano Sampaio
 */
public class MarkerManager {

	/**
	 * Add our invisible marker into the source code.
	 */
	public static void addInvisible(ASTNode node) {
		try {
			IResource resource = null;
			int lineNumber = 0;
			int startPosition = node.getStartPosition();
			int endPosition = startPosition + node.getLength();

			// Get the Compilation Unit of this resource.
			CompilationUnit cUnit = BindingResolver.getParentCompilationUnit(node);
			if (null != cUnit) {
				lineNumber = cUnit.getLineNumber(startPosition);
				resource = cUnit.getJavaElement().getCorrespondingResource();
			}

			IMarker marker = resource.createMarker(Constant.MARKER_ID_INVISIBLE);

			marker.setAttribute(IMarker.MESSAGE, Message.View.FALSE_POSITIVE);
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			marker.setAttribute(IMarker.CHAR_START, startPosition);
			marker.setAttribute(IMarker.CHAR_END, endPosition);

		} catch (CoreException e) {
			PluginLogger.logError(e);
		}
	}

	public static boolean hasMarkerAtPosition(CompilationUnit cUnit, IResource resource, ASTNode node) {
		try {
			List<IMarker> markers = Arrays.asList(resource.findMarkers(Constant.MARKER_ID_INVISIBLE, false, 0));

			int startPosition = node.getStartPosition();
			int lineNumber = cUnit.getLineNumber(startPosition);
			int endPosition = startPosition + node.getLength();

			for (IMarker marker : markers) {
				int nodeLineNumber = marker.getAttribute(IMarker.LINE_NUMBER, 0);
				int nodeStartPosition = marker.getAttribute(IMarker.CHAR_START, 0);
				int nodeEndPosition = marker.getAttribute(IMarker.CHAR_END, 0);

				if ((lineNumber == nodeLineNumber) && (startPosition == nodeStartPosition) && (endPosition == nodeEndPosition)) {
					return true;
				}
			}
		} catch (CoreException e) {
			PluginLogger.logError(e);
		}

		return false;
	}

}
