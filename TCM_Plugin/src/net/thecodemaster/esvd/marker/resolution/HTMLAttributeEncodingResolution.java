package net.thecodemaster.esvd.marker.resolution;

import org.eclipse.core.resources.IMarker;

public class HTMLAttributeEncodingResolution extends AbstractEncodingResolution {

	public HTMLAttributeEncodingResolution(int position, IMarker marker) {
		super(position, marker);

		setLabel(generateLabel());
		setDescription(generateDescription());
	}

	private String generateLabel() {
		return "HTML Attribute Encoder";
	}

	private String generateDescription() {
		StringBuffer buf = new StringBuffer();
		String instruction = "-- Double click selection to auto-generate encoding method --";
		String description = "";

		description = "Attributes provide information about an element and use a minimal set of characters. Use the HTML Attribute Encoder when attributes you use in your HTML do not come from your HTML page to safeguard from dangerous characters.";

		buf.append(instruction);
		buf.append("<p><p>");
		buf.append(description);

		return buf.toString();
	}

	@Override
	protected String getEsapiEncoderMethodName() {
		return "encodeForHTMLAttribute";
	}
}
