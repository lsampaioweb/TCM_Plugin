package net.thecodemaster.esvd.marker.resolution;

public class JavaScriptEncodingResolution extends AbstractEncodingResolution {

	public JavaScriptEncodingResolution() {
		setDescription(generateDescription());
	}

	private String generateDescription() {
		StringBuffer buf = new StringBuffer();
		String instruction = "-- Double click selection to auto-generate encoding method --";
		String description = "";

		// FIXME Improve this description
		description = "Encode data for insertion inside a data value or function argument in JavaScript. Including user data directly inside a script is quite dangerous. Great care must be taken to prevent including user data directly into script code itself, as no amount of encoding will prevent attacks there. Please note there are some JavaScript functions that can never safely receive untrusted data as input – even if the user input is encoded.";

		buf.append(instruction);
		buf.append("<p><p>");
		buf.append(description);

		return buf.toString();
	}

	@Override
	public String getLabel() {
		return "JavaScript Encoder";
	}

	@Override
	protected String getEsapiEncoderMethodName() {
		return "encodeForJavaScript";
	}
}
