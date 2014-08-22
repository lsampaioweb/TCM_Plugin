package net.thecodemaster.esvd.ui.enumeration;

/**
 * @author Luciano Sampaio
 */
public enum EnumRules {
	ANYTHING_IS_VALID(1), SANITIZED(2), LITERAL(4), STRING_CONCATENATION(8);

	private int	id;

	EnumRules(int id) {
		this.id = id;
	}

	public int value() {
		return id;
	}

}
