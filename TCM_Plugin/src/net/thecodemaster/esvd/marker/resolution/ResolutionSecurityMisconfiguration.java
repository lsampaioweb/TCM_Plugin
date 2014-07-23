package net.thecodemaster.esvd.marker.resolution;

import org.eclipse.core.resources.IMarker;

/**
 * @author Luciano Sampaio
 */
public class ResolutionSecurityMisconfiguration extends AbstractResolution {

	public ResolutionSecurityMisconfiguration(int position, ResolutionMessage resolutionMessage, IMarker marker) {
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
