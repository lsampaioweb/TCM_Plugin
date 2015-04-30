package net.thecodemaster.esvd.marker.resolution;

public class HTMLEncodingResolution extends AbstractEncodingResolution {

	public HTMLEncodingResolution() {
		setDescription(generateDescription());
	}

	private String generateDescription() {
		StringBuffer buf = new StringBuffer();
		String instruction = "-- Double click selection to auto-generate encoding method --";
		String description = "";

		// FIXME Improve this description
		description = "Encode data for use in HTML using HTML entity encoding.";

		buf.append(instruction);
		buf.append("<p><p>");
		buf.append(description);

		return buf.toString();
	}

	@Override
	public String getLabel() {
		return "HTML Encoder";
	}

	@Override
	protected String getEsapiEncoderMethodName() {
		return "encodeForHTML";
	}
}
