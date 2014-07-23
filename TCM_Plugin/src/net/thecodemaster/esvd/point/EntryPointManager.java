package net.thecodemaster.esvd.point;

import org.eclipse.jdt.core.dom.Expression;

/**
 * @author Luciano Sampaio
 */
public class EntryPointManager extends EntryAndSanitizationPointManager {

	public boolean isMethodAnEntryPoint(Expression method) {
		return hasMethod(method);
	}

}
