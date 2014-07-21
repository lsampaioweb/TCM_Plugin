package net.thecodemaster.evd.point;

import java.util.List;
import java.util.Map;

import net.thecodemaster.evd.graph.BindingResolver;
import net.thecodemaster.evd.graph.Parameter;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class AbstractPointManager {

	protected List<Map<Parameter, List<Integer>>> getExpectedParameters(
			Map<String, Map<String, List<Map<Parameter, List<Integer>>>>> map, Expression method) {
		// 01 - Get the method name.
		// 02 - Verify if this method is in the list of ExitPoints.
		Map<String, List<Map<Parameter, List<Integer>>>> foundPackages = map.get(getName(method));

		if (null == foundPackages) {
			return null;
		}

		// 03 - Get the qualified name (Package + Class) of this method.
		// 04 - Verify if this is really the method we were looking for.
		// Method names can repeat in other classes.
		return foundPackages.get(getQualifiedName(method));
	}

	protected boolean haveSameParameters(List<Map<Parameter, List<Integer>>> listExpectedParameters, Expression method) {
		// 01 - Get the received parameters of the current method.
		List<Expression> receivedParameters = getParameters(method);

		for (Map<Parameter, List<Integer>> expectedParameters : listExpectedParameters) {
			if (haveSameParameters(expectedParameters, receivedParameters)) {
				return true;
			}
		}

		return false;
	}

	protected boolean haveSameParameters(Map<Parameter, List<Integer>> expectedParameters,
			List<Expression> receivedParameters) {
		// 02 - It is necessary to check the number of parameters and its types
		// because it may exist methods with the same names but different parameters.
		if (expectedParameters.size() == receivedParameters.size()) {
			boolean hasFoundMethod = true;
			int index = 0;
			for (Parameter expectedParameter : expectedParameters.keySet()) {
				ITypeBinding typeBinding = receivedParameters.get(index++).resolveTypeBinding();

				// Verify if all the parameters are the ones expected. However, there is a case
				// where an Object is expected, and any type is accepted.
				if (!parametersHaveSameType(expectedParameter.getType(), typeBinding)) {
					hasFoundMethod = false;
					break;
				}
			}

			if (hasFoundMethod) {
				return true;
			}
		}

		return false;
	}

	protected boolean methodsHaveSameNameAndPackage(AbstractPoint abstractPoint, Expression method) {
		// 01 - Get the method name.
		String methodName = getName(method);

		// 02 - Verify if this method is in the list of ExitPoints.
		if (abstractPoint.getMethodName().equals(methodName)) {

			// 03 - Get the qualified name (Package + Class) of this method.
			String qualifiedName = getQualifiedName(method);

			// 04 - Verify if this is really the method we were looking for.
			// Method names can repeat in other classes.
			if (null != qualifiedName) {
				return qualifiedName.matches(abstractPoint.getQualifiedName());
			}
		}

		return false;
	}

	private String getName(ASTNode node) {
		return BindingResolver.getName(node);
	}

	private String getQualifiedName(ASTNode node) {
		return BindingResolver.getQualifiedName(node);
	}

	protected List<Expression> getParameters(ASTNode node) {
		return BindingResolver.getParameters(node);
	}

	protected boolean parametersHaveSameType(String parameter, ITypeBinding other) {
		return BindingResolver.parametersHaveSameType(parameter, other);
	}
}
