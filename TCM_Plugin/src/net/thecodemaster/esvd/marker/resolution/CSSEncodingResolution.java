package net.thecodemaster.esvd.marker.resolution;

import org.eclipse.core.resources.IMarker;

public class CSSEncodingResolution extends AbstractEncodingResolution {

	public CSSEncodingResolution(int position, IMarker marker) {
		super(position, marker);

		setLabel(generateLabel());
		setDescription(generateDescription());
	}

	private String generateLabel() {
		return "CSS Encoder";
	}

	private String generateDescription() {
		StringBuffer buf = new StringBuffer();
		String instruction = "-- Double click selection to auto-generate encoding method --";
		String description = "";

		description = "Use CSS Encoding when the data you get from an outside source is to be written directly into your Cascading Style Sheet or your style tag. Doing so will encode malicious characters so an attacker can not easily exploit CSS interpreter vulnerabilities.";

		buf.append(instruction);
		buf.append("<p><p>");
		buf.append(description);

		return buf.toString();
	}

	@Override
	protected String getEsapiEncoderMethodName() {
		return "encodeForCSS";
	}
}
