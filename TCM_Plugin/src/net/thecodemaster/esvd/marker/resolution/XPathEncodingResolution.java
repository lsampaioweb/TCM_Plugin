package net.thecodemaster.esvd.marker.resolution;

import org.eclipse.core.resources.IMarker;

public class XPathEncodingResolution extends AbstractEncodingResolution {

	public XPathEncodingResolution(int position, IMarker marker) {
		super(position, marker);

		setLabel(generateLabel());
		setDescription(generateDescription());
	}

	private String generateLabel() {
		return "XPath Encoder";
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
	protected String getEsapiEncoderMethodName() {
		return "encodeForXPath";
	}
}
