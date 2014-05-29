package net.thecodemaster.evd.marker.resolution;

import net.thecodemaster.evd.logger.PluginLogger;

import org.eclipse.core.resources.IMarker;

/**
 * @author Luciano Sampaio
 */
public class EntryPointNotSanitizedResolution extends AbstractResolution {

	public EntryPointNotSanitizedResolution(int position, ResolutionMessage resolutionMessage, IMarker marker) {
		super(position, marker);

		setLabel(resolutionMessage.getLabel());
		setDescription(resolutionMessage.getDescription());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(IMarker marker) {
		try {
			// 01 - The comment that will be insert into the source code.
			String comment = "// FIXME - Sanitize this content before use it.";

			runInsertComment(marker, comment);
		} catch (Exception e) {
			PluginLogger.logError(e);
		}
	}
}
