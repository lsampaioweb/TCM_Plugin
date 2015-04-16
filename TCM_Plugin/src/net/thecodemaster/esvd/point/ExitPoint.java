package net.thecodemaster.esvd.point;

import java.util.Map;

import net.thecodemaster.esvd.graph.Parameter;
import net.thecodemaster.esvd.verifier.Verifier;

/**
 * @author Luciano Sampaio Package : java.sql.DriverManager Method : getConnection Parameters: [0] : -1; (String url)
 *         [1] : 0; (String user) [2] : 0; (String password)
 */
public class ExitPoint extends AbstractPoint {

	private Map<Parameter, Integer>	parameters;
	private final Verifier					verifier;

	public ExitPoint(Verifier verifier, String qualifiedName, String methodName) {
		super(qualifiedName, methodName);
		this.verifier = verifier;
	}

	public Map<Parameter, Integer> getParameters() {
		return parameters;
	}

	public void setParameters(Map<Parameter, Integer> parameters) {
		this.parameters = parameters;
	}

	public Verifier getVerifier() {
		return verifier;
	}
}
