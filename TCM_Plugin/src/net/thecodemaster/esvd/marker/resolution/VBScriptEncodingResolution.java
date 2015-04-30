package net.thecodemaster.esvd.marker.resolution;

public class VBScriptEncodingResolution extends AbstractEncodingResolution {

	public VBScriptEncodingResolution() {
		setDescription(generateDescription());
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
	public String getLabel() {
		return "VBScript Encoder";
	}

	@Override
	protected String getEsapiEncoderMethodName() {
		return "encodeForVBScript";
	}
}
