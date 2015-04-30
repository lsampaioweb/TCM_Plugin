package net.thecodemaster.esvd.marker.resolution;

public class HTMLAttributeEncodingResolution extends AbstractEncodingResolution {

	public HTMLAttributeEncodingResolution() {
		setDescription(generateDescription());
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
	public String getLabel() {
		return "HTML Attribute Encoder";
	}

	@Override
	protected String getEsapiEncoderMethodName() {
		return "encodeForHTMLAttribute";
	}
}
