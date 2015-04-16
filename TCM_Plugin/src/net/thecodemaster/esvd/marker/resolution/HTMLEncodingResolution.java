package net.thecodemaster.esvd.marker.resolution;

import org.eclipse.core.resources.IMarker;

public class HTMLEncodingResolution extends EncodingResolution {

	public HTMLEncodingResolution(int position, IMarker marker) {
		super(position, marker);

		setLabel(generateLabel());
		setDescription(generateDescription());
	}

	private String generateLabel() {
		return "HTML Encoder";
	}

	private String generateDescription() {
		StringBuffer buf = new StringBuffer();
		String instruction = "-- Double click selection to auto-generate encoding method --";
		String description = "";

		description = "Use HTML Encoding when the data you get from an outside source is to be written directly into your HTML body.";

		buf.append(instruction);
		buf.append("<p><p>");
		buf.append(description);

		return buf.toString();
	}

	@Override
	protected String getEsapiEncoderMethodName() {
		return "encodeForHTML";
	}
}
