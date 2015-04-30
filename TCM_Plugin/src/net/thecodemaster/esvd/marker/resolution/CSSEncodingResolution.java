package net.thecodemaster.esvd.marker.resolution;

public class CSSEncodingResolution extends AbstractEncodingResolution {

	public CSSEncodingResolution() {
		setDescription(generateDescription());
	}

	private String generateDescription() {
		StringBuffer buf = new StringBuffer();
		String instruction = "-- Double click selection to auto-generate encoding method --";
		String description = "";

		// FIXME Improve this description
		description = "Encode data for use in Cascading Style Sheets (CSS) content.";

		buf.append(instruction);
		buf.append("<p><p>");
		buf.append(description);

		return buf.toString();
	}

	@Override
	public String getLabel() {
		return "CSS Encoder";
	}

	@Override
	protected String getEsapiEncoderMethodName() {
		return "encodeForCSS";
	}
}
