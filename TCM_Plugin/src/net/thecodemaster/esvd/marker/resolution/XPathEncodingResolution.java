package net.thecodemaster.esvd.marker.resolution;

public class XPathEncodingResolution extends AbstractEncodingResolution {

	public XPathEncodingResolution() {
		setDescription(generateDescription());
	}

	private String generateDescription() {
		StringBuffer buf = new StringBuffer();
		String instruction = "-- Double click selection to auto-generate encoding method --";
		String description = "";

		// FIXME Improve this description
		description = "Encode data for use in an XPath query. NB: The reference implementation encodes almost everything and may over-encode. The difficulty with XPath encoding is that XPath has no built in mechanism for escaping characters. It is possible to use XQuery in a parameterized way to prevent injection.";

		buf.append(instruction);
		buf.append("<p><p>");
		buf.append(description);

		return buf.toString();
	}

	@Override
	public String getLabel() {
		return "XPath Encoder";
	}

	@Override
	protected String getEsapiEncoderMethodName() {
		return "encodeForXPath";
	}
}
