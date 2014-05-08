package net.thecodemaster.evd.graph;

/**
 * @author Luciano Sampaio
 */
public class Parameter {

	private final String	type;
	private final String	name;

	public Parameter(String type, String name) {
		this.type = type;
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return String.format("%s %s ", getType(), getName());
	}

}
