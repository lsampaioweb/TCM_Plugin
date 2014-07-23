package net.thecodemaster.esvd.point;

import org.eclipse.jdt.core.dom.Expression;

/**
 * @author Luciano Sampaio
 */
public class SanitizationPointManager extends EntryAndSanitizationPointManager {

	public boolean isMethodASanitizationPoint(Expression method) {
		return hasMethod(method);
	}

}
