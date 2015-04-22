package net.thecodemaster.esvd.marker.resolution;

import org.eclipse.core.resources.IMarker;

public class JavaScriptEncodingResolution extends AbstractEncodingResolution {

	public JavaScriptEncodingResolution(int position, IMarker marker) {
		super(position, marker);

		setLabel(generateLabel());
		setDescription(generateDescription());
	}

	private String generateLabel() {
		return "JavaScript Encoder";
	}

	private String generateDescription() {
		StringBuffer buf = new StringBuffer();
		String instruction = "-- Double click selection to auto-generate encoding method --";
		String description = "";

		description = "Use JavaScript encoder when you are using a script that comes from some source outside of your page or when you obtain input from the user that will be put into the script. The encoder changes potentially dangerous characters into usable safe ones.";

		buf.append(instruction);
		buf.append("<p><p>");
		buf.append(description);

		return buf.toString();
	}

	@Override
	protected String getEsapiEncoderMethodName() {
		return "encodeForJavaScript";
	}
}
