package net.thecodemaster.esvd.marker.resolution;

public class XMLEncodingResolution extends AbstractEncodingResolution {

	public XMLEncodingResolution() {
		setDescription(generateDescription());
	}

	private String generateDescription() {
		StringBuffer buf = new StringBuffer();
		String instruction = "-- Double click selection to auto-generate encoding method --";
		String description = "";

		// FIXME Improve this description
		description = "Encode data for use in an XML element. The implementation should follow the XML Encoding Standard from the W3C. The use of a real XML parser is strongly encouraged. However, in the hopefully rare case that you need to make sure that data is safe for inclusion in an XML document and cannot use a parse, this method provides a safe mechanism to do so.";

		buf.append(instruction);
		buf.append("<p><p>");
		buf.append(description);

		return buf.toString();
	}

	@Override
	public String getLabel() {
		return "XML Encoder";
	}

	@Override
	protected String getEsapiEncoderMethodName() {
		return "encodeForXML";
	}
}
