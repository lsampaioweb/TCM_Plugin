package net.thecodemaster.esvd.point;

/**
 * @author Luciano Sampaio
 */
public abstract class AbstractPoint {

	private final String	qualifiedName;
	private final String	methodName;

	public AbstractPoint(String qualifiedName, String methodName) {
		this.qualifiedName = qualifiedName;
		this.methodName = methodName;
	}

	public String getQualifiedName() {
		return qualifiedName;
	}

	public String getMethodName() {
		return methodName;
	}

	@Override
	public String toString() {
		return String.format("%s.%s", getQualifiedName(), getMethodName());
	}

}