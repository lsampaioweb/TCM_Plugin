package net.thecodemaster.evd.marker.resolution;

import net.thecodemaster.evd.ui.l10n.Message;

import org.eclipse.core.resources.IMarker;

/**
 * @author Luciano Sampaio
 */
public class SecurityMisconfigurationResolution extends AbstractResolution {

	public SecurityMisconfigurationResolution(int position, IMarker marker) {
		super(position, marker);

		setLabel(Message.VerifierSecurityVulnerability.LABEL_RESOLUTION);
		setDescription(Message.VerifierSecurityVulnerability.DESCRIPTION_RESOLUTION);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(IMarker marker) {
	}

}
