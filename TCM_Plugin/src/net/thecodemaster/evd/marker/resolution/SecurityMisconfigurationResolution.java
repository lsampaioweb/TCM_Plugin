package net.thecodemaster.evd.marker.resolution;

import org.eclipse.core.resources.IMarker;

/**
 * @author Luciano Sampaio
 */
public class SecurityMisconfigurationResolution extends AbstractResolution {

	public SecurityMisconfigurationResolution(int position, ResolutionMessage resolutionMessage, IMarker marker) {
		super(position, marker);

		// 01 - Get the ViewDataModel of this marker.
		// ViewDataModel vdm = getViewDataModelFromMarker(marker);

		setLabel(resolutionMessage.getLabel());
		setDescription(resolutionMessage.getDescription());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(IMarker marker) {
	}

}
