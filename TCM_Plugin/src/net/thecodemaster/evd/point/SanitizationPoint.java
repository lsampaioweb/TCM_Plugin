package net.thecodemaster.evd.point;

import java.util.List;

/**
 * @author Luciano Sampaio Package : <br/>
 *         org.owasp.encoder.Encode <br/>
 *         Method : forHtml <br/>
 *         Parameters: [0] : java.lang.String
 */
public class SanitizationPoint extends AbstractPoint {

	private List<String>	parameters;

	public SanitizationPoint(String qualifiedName, String methodName) {
		super(qualifiedName, methodName);
	}

	public List<String> getParameters() {
		return parameters;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}
}
