package net.thecodemaster.esvd.point;

import java.util.List;
import java.util.Map;

import net.thecodemaster.esvd.graph.Parameter;
import net.thecodemaster.esvd.helper.Creator;

import org.eclipse.jdt.core.dom.Expression;

/**
 * @author Luciano Sampaio
 */
public class EntryAndSanitizationPointManager extends AbstractPointManager {

	// Method Name + List of Packages + List of Parameters
	// package.send(a);
	// package.send(a, b);
	// package2.save();
	private final Map<String, Map<String, List<Map<Parameter, Integer>>>>	dataPoints;

	private Map<String, Map<String, List<Map<Parameter, Integer>>>> getDataPoints() {
		return dataPoints;
	}

	public EntryAndSanitizationPointManager() {
		dataPoints = Creator.newMap();
	}

	public void add(String methodName, String packageName, Map<Parameter, Integer> params) {
		Map<String, List<Map<Parameter, Integer>>> packages = getDataPoints().get(methodName);

		// 01 - Add the method.
		if (null == packages) {
			packages = Creator.newMap();

			getDataPoints().put(methodName, packages);
		}

		List<Map<Parameter, Integer>> parameters = packages.get(packageName);

		// 02 - Add the package.
		if (null == parameters) {
			parameters = Creator.newList();

			packages.put(packageName, parameters);
		}

		// 03 - Add the parameters.
		parameters.add(params);
	}

	protected boolean hasMethod(Expression method) {
		// 01 - Try to find if this method is in the list of points.
		List<Map<Parameter, Integer>> listExpectedParameters = getExpectedParameters(getDataPoints(), method);

		if (null == listExpectedParameters) {
			return false;
		}

		// 02 - Now, we try to find if this method has the same parameters as expected.
		return haveSameParameters(listExpectedParameters, method);
	}

}
