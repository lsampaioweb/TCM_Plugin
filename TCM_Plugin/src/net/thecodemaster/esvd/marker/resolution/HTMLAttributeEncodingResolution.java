package net.thecodemaster.esvd.marker.resolution;


public class HTMLAttributeEncodingResolution extends AbstractEncodingResolution {

	public HTMLAttributeEncodingResolution(int position) {
		super(position);

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

		// FIXME Improve this description
		description = "Encode data for use in HTML attributes.";

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
