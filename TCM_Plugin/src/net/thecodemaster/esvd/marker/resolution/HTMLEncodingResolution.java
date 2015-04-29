package net.thecodemaster.esvd.marker.resolution;


public class HTMLEncodingResolution extends AbstractEncodingResolution {

	public HTMLEncodingResolution(int position) {
		super(position);

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

		// FIXME Improve this description
		description = "Encode data for use in HTML using HTML entity encoding.";

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
