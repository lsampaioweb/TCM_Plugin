package net.thecodemaster.esvd.marker.resolution;

public class URLEncodingResolution extends AbstractEncodingResolution {

	public URLEncodingResolution() {
		setDescription(generateDescription());
	}

	private String generateDescription() {
		StringBuffer buf = new StringBuffer();
		String instruction = "-- Double click selection to auto-generate encoding method --";
		String description = "";

		// FIXME Improve this description
		description = "Encode for use in a URL. This method performs URL encoding on the entire string.";

		buf.append(instruction);
		buf.append("<p><p>");
		buf.append(description);

		return buf.toString();
	}

	@Override
	public String getLabel() {
		return "URL Encoder";
	}

	@Override
	protected String getEsapiEncoderMethodName() {
		return "encodeForURL";
	}
}
