package net.thecodemaster.esvd.marker.resolution;

public class DNEncodingResolution extends AbstractEncodingResolution {

	public DNEncodingResolution() {
		setDescription(generateDescription());
	}

	private String generateDescription() {
		StringBuffer buf = new StringBuffer();
		String instruction = "-- Double click selection to auto-generate encoding method --";
		String description = "";

		// FIXME Improve this description
		description = "Encode data for use in an LDAP distinguished name.";

		buf.append(instruction);
		buf.append("<p><p>");
		buf.append(description);

		return buf.toString();
	}

	@Override
	public String getLabel() {
		return "DN Encoder";
	}

	@Override
	protected String getEsapiEncoderMethodName() {
		return "encodeForDN";
	}
}
