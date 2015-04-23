package net.thecodemaster.esvd.marker.resolution;

import org.eclipse.core.resources.IMarker;

public class VBScriptEncodingResolution extends AbstractEncodingResolution {

	public VBScriptEncodingResolution(int position, IMarker marker) {
		super(position, marker);

		setLabel(generateLabel());
		setDescription(generateDescription());
	}

	private String generateLabel() {
		return "VBScript Encoder";
	}

	private String generateDescription() {
		StringBuffer buf = new StringBuffer();
		String instruction = "-- Double click selection to auto-generate encoding method --";
		String description = "";

		// FIXME Improve this description
		description = "Encode data for insertion inside a data value in a Visual Basic script. Putting user data directly inside a script is quite dangerous. Great care must be taken to prevent putting user data directly into script code itself, as no amount of encoding will prevent attacks there. This method is not recommended as VBScript is only supported by Internet Explorer.";

		buf.append(instruction);
		buf.append("<p><p>");
		buf.append(description);

		return buf.toString();
	}

	@Override
	protected String getEsapiEncoderMethodName() {
		return "encodeForVBScript";
	}
}
