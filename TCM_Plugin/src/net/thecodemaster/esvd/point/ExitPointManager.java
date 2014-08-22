package net.thecodemaster.esvd.point;

import java.util.List;
import java.util.Map;

import net.thecodemaster.esvd.graph.Parameter;
import net.thecodemaster.esvd.verifier.Verifier;

import org.eclipse.jdt.core.dom.Expression;

/**
 * @author Luciano Sampaio
 */
public class ExitPointManager extends AbstractPointManager {

	public ExitPoint getExitPointIfMethodIsOne(List<Verifier> verifiers, Expression method) {
		// 01 - Get the received parameters of the current method.
		List<Expression> receivedParameters = getParameters(method);

		// 02 - Iterate all the verifiers.
		for (Verifier verifier : verifiers) {

			// 03 - Get the list of exit-points.
			List<ExitPoint> exitPoints = verifier.getExitPoints();

			// 04 - Iterate over all the exit-points of each verifier.
			for (ExitPoint currentExitPoint : exitPoints) {

				// 05 - Check if the method have the same name and package.
				if (methodsHaveSameNameAndPackage(currentExitPoint, method)) {

					// 06 - Get the expected arguments of this method.
					Map<Parameter, Integer> expectedParameters = currentExitPoint.getParameters();

					// 07 - Verify if they have the same parameters.
					if (haveSameParameters(expectedParameters, receivedParameters)) {
						return currentExitPoint;
					}
				}
			}
		}

		return null;
	}
}
