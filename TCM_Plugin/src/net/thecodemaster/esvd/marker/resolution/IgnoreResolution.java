package net.thecodemaster.esvd.marker.resolution;

import java.util.List;

import net.thecodemaster.esvd.logger.PluginLogger;
import net.thecodemaster.esvd.marker.MarkerManager;
import net.thecodemaster.esvd.ui.view.ViewDataModel;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * @author Luciano Sampaio
 */
public class IgnoreResolution extends AbstractResolution {

	private final ResolutionMessage	resolutionMessage;

	public IgnoreResolution(ResolutionMessage resolutionMessage, IMarker marker) {
		this.resolutionMessage = resolutionMessage;

		// 03 - Get the ViewDataModel of this marker.
		List<ViewDataModel> vdms = getViewDataModelsFromMarker(marker);

		ViewDataModel vdm = vdms.get(0);

		String expression = (null != vdm.getExpr()) ? vdm.getExpr().toString() : "";
		String fullPath = getFullPath(vdm);
		String description = String.format(resolutionMessage.getDescription(), expression, fullPath);

		setDescription(description);
	}

	@Override
	public String getLabel() {
		return resolutionMessage.getLabel();
	}

	@Override
	public void run(IMarker marker) {
		try {
			// 01 - Get the ViewDataModel of this marker.
			List<ViewDataModel> vdms = getViewDataModelsFromMarker(marker);

			if (vdms.size() > 0) {
				// 02 - Add our invisible annotation into the source code for this element.
				MarkerManager.addInvisible(vdms.get(0).getExpr());

				for (ViewDataModel vdm : vdms) {
					if (null != vdm) {
						// We have 02 cases: The user clicked on the EntryPoint or on the ExitPoint.
						if (0 >= getNrChildren(vdm)) {
							// 01 - EntryPoint.
							handleEntryPoint(vdm);
						} else {
							// 02 - ExitPoint.
							handleExitPoint(vdm);
						}
					}
				}
			}
		} catch (Exception e) {
			PluginLogger.logError(e);
		}
	}

	private void handleEntryPoint(ViewDataModel vdm) {
		// We have to check if the exit point of this element has more children.
		// Case 1: The parent is the Entry and Exit Point.
		// Case 2: The parent has only one child (this element).
		// Case 3: The parent has other children.
		ViewDataModel vdmToRemove = vdm;
		int nrChildren = getNrChildren(vdm.getParent());
		if (1 == nrChildren) {
			// Case 2: Remove the parent and the clicked element.
			vdmToRemove = vdm.getParent();
		} else if (1 < nrChildren) {
			// Case 3: Update the message that contains the number of vulnerable paths.
			vdm.getParent().updateMessage();
		}

		// 01 - The element that will receive the invisible annotation will always be the one that was clicked.
		runIgnoreResolution(vdmToRemove, vdm.getExpr(), true);
	}

	private void handleExitPoint(ViewDataModel vdm) {
		runIgnoreResolution(vdm, vdm.getExpr(), true);
	}

	private void runIgnoreResolution(ViewDataModel vdm, ASTNode node, boolean removeChildren) {
		// 01 - Remove the Markers and the lines from our Security Vulnerability View.
		clearProblem(vdm, removeChildren);
	}

}
