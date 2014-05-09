package net.thecodemaster.evd.graph;

/**
 * @author Luciano Sampaio
 */
public class Parameter {

	private final String	type;

	public Parameter(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return String.format("%s ", getType());
	}

}
