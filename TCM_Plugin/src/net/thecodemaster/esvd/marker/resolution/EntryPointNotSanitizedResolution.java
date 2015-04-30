package net.thecodemaster.esvd.marker.resolution;

import java.util.List;

import net.thecodemaster.esvd.logger.PluginLogger;
import net.thecodemaster.esvd.ui.view.ViewDataModel;

import org.eclipse.core.resources.IMarker;

/**
 * @author Luciano Sampaio
 */
public class EntryPointNotSanitizedResolution extends AbstractResolution {

	private final ResolutionMessage	resolutionMessage;

	public EntryPointNotSanitizedResolution(ResolutionMessage resolutionMessage, IMarker marker) {
		this.resolutionMessage = resolutionMessage;

		// 03 - Get the ViewDataModel of this marker.
		List<ViewDataModel> vdms = getViewDataModelsFromMarker(marker);

		ViewDataModel vdm = vdms.get(0);

		String expression = (null != vdm.getExpr()) ? vdm.getExpr().toString() : "";
		String description = (null != resolutionMessage.getDescription()) ? resolutionMessage.getDescription() : "";
		String descriptionFormatted = String.format(description, expression, expression);

		setDescription(descriptionFormatted);
	}

	@Override
	public String getLabel() {
		return resolutionMessage.getLabel();
	}

	@Override
	public void run(IMarker marker) {
		try {
			// 01 - The comment that will be insert into the source code.
			String comment = String.format("// FIXME - %s", marker.getAttribute(IMarker.MESSAGE, ""));

			runInsertComment(marker, comment);
		} catch (Exception e) {
			PluginLogger.logError(e);
		}
	}
}
