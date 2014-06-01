package net.thecodemaster.evd.verifier;

import java.util.List;
import java.util.Map;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.BindingResolver;
import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.graph.Parameter;
import net.thecodemaster.evd.graph.VariableBindingManager;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.point.ExitPoint;
import net.thecodemaster.evd.reporter.Reporter;
import net.thecodemaster.evd.ui.enumeration.EnumStatusVariable;
import net.thecodemaster.evd.xmlloader.LoaderExitPoint;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * The verifier is the class that actually knows how to find the vulnerability and the one that performs this
 * verification. Each verifier can reimplement/override methods of add new behavior to them.
 * 
 * @author Luciano Sampaio
 */
public abstract class Verifier extends CodeAnalyzer {

	/**
	 * The id of the current verifier.
	 */
	private final int				id;
	/**
	 * The name of the current verifier.
	 */
	private final String		name;
	/**
	 * The object that know how and where to report the found vulnerabilities.
	 */
	private Reporter				reporter;
	/**
	 * List with all the ExitPoints of this verifier.
	 */
	private List<ExitPoint>	exitPoints;

	/**
	 * @param name
	 *          The name of the verifier.
	 * @param id
	 *          The id of the verifier.
	 * @param listEntryPoints
	 *          List with all the EntryPoints methods.
	 */
	public Verifier(int id, String name) {
		this.id = id;
		this.name = name;
	}

	protected int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	protected Reporter getReporter() {
		return reporter;
	}

	protected List<ExitPoint> getExitPoints() {
		if (null == exitPoints) {
			// Loads all the ExitPoints of this verifier.
			exitPoints = (new LoaderExitPoint(getId())).load();
		}

		return exitPoints;
	}

	/**
	 * Notifies that a subtask of the main task is beginning.
	 * 
	 * @param taskName
	 *          The text that will be displayed to the user.
	 */
	protected void setSubTask(String taskName) {
		if ((null != getReporter()) && (null != getReporter().getProgressMonitor())) {
			getReporter().getProgressMonitor().subTask(taskName);
		}
	}

	protected void reportVulnerability(DataFlow dataFlow) {
		getReporter().addProblem(getId(), getCurrentResource(), dataFlow);
	}

	protected ExitPoint getExitPointIfMethodIsOne(Expression method) {
		for (ExitPoint currentExitPoint : getExitPoints()) {
			if (BindingResolver.methodsHaveSameNameAndPackage(currentExitPoint, method)) {
				// 01 - Get the expected arguments of this method.
				Map<Parameter, List<Integer>> expectedParameters = currentExitPoint.getParameters();

				// 02 - Get the received parameters of the current method.
				List<Expression> receivedParameters = BindingResolver.getParameters(method);

				// 03 - It is necessary to check the number of parameters and its types
				// because it may exist methods with the same names but different parameters.
				if (expectedParameters.size() == receivedParameters.size()) {
					boolean isMethodAnExitPoint = true;
					int index = 0;
					for (Parameter expectedParameter : expectedParameters.keySet()) {
						ITypeBinding typeBinding = receivedParameters.get(index++).resolveTypeBinding();

						// Verify if all the parameters are the ones expected. However, there is a case
						// where an Object is expected, and any type is accepted.
						if (!BindingResolver.parametersHaveSameType(expectedParameter.getType(), typeBinding)) {
							isMethodAnExitPoint = false;
							break;
						}
					}

					if (isMethodAnExitPoint) {
						return currentExitPoint;
					}
				}
			}
		}

		return null;
	}

	/**
	 * The public run method that will be invoked by the Analyzer.
	 * 
	 * @param resources
	 * @param callGraph
	 * @param reporter
	 */
	public void run(List<IResource> resources, CallGraph callGraph, Reporter reporter) {
		setCallGraph(callGraph);
		this.reporter = reporter;

		setSubTask(getName());

		// 01 - Run the vulnerability detection on all the provided resources.
		for (IResource resource : resources) {
			// We need this information when we are going to display the vulnerabilities.
			setCurrentResource(resource);

			run(resource);
		}
	}

	/**
	 * Iterate over all the method declarations found in the current resource.
	 * 
	 * @param resource
	 */
	protected void run(IResource resource) {
		// 01 - Get the list of methods in the current resource.
		Map<MethodDeclaration, List<Expression>> methods = getCallGraph().getMethods(resource);

		// 02 - Get all the method invocations of each method declaration.
		for (List<Expression> invocations : methods.values()) {

			// 03 - Iterate over all method invocations to verify if it is a ExitPoint.
			for (Expression method : invocations) {
				ExitPoint exitPoint = getExitPointIfMethodIsOne(method);

				if (null != exitPoint) {
					// 04 - Some methods will need to have access to the resource that is currently being analyzed.
					// but we do not want to pass it to all these methods as a parameter.
					setCurrentResource(resource);

					// 05 - This is an ExitPoint method and it needs to be verified.
					run(method, exitPoint);
				}
			}
		}
	}

	protected void run(Expression method, ExitPoint exitPoint) {
		// 01 - Get the parameters (received) from the current method.
		List<Expression> receivedParameters = BindingResolver.getParameters(method);

		// 02 - Get the expected parameters of the ExitPoint method.
		Map<Parameter, List<Integer>> expectedParameters = exitPoint.getParameters();

		int index = 0;
		int depth = 0;
		for (List<Integer> rules : expectedParameters.values()) {
			// If the rules are null, it means the expected parameter can be anything. (We do not care for it).
			if (null != rules) {
				Expression expression = receivedParameters.get(index);
				DataFlow dataFlow = new DataFlow();

				inspectNode(depth, dataFlow, expression);
				if (dataFlow.isVulnerable()) {
					reportVulnerability(dataFlow);
				}
			}
			index++;
		}
	}

	/**
	 * 32 TODO Verify if we have to do something with the dfParent.
	 */
	@Override
	protected void inspectMethodInvocation(int depth, DataFlow dfParent, MethodInvocation methodInvocation) {
		// 01 - Check if this method is a Sanitization-Point.
		if (isMethodASanitizationPoint(methodInvocation)) {
			// If a sanitization method is being invoked, then we do not have a vulnerability.
			return;
		}

		// 02 - Check if this method is an Entry-Point.
		if (isMethodAnEntryPoint(methodInvocation)) {
			String message = getMessageEntryPoint(BindingResolver.getFullName(methodInvocation));

			// We found a invocation to a entry point method.
			dfParent.isVulnerable(Constant.Vulnerability.ENTRY_POINT, message);
			return;
		}

		// 03 - Check if there is an annotation, in case there is, we should BELIEVE it is not vulnerable.
		if (hasAnnotationAtPosition(methodInvocation)) {
			return;
		}

		// 04 - There are 2 cases: When we have the source code of this method and when we do not.
		MethodDeclaration methodDeclaration = getCallGraph().getMethod(getCurrentResource(), methodInvocation);

		// We have to iterate over its parameters to see if any is vulnerable.
		// If there is a vulnerable parameter and if this is a method from an object
		// we set this object as vulnerable.
		List<Expression> parameters = BindingResolver.getParameters(methodInvocation);
		for (Expression parameter : parameters) {

			// 02 - A new dataFlow for this variable.
			DataFlow dataFlow = new DataFlow(parameter);

			inspectNode(depth, dataFlow, parameter);
			// We found a vulnerability.
			if (dataFlow.isVulnerable()) {
				// There are 2 sub-cases: When is a method from an object and when is a method from a library.
				// 01 - stringBuilder.append("...");
				// 02 - System.out.println("..."); Nothing else to do.

			}
		}
		if (null != methodDeclaration) {
			// We have the source code.
			// inspectMethodDeclaration(depth, dfParent, methodInvocation, methodDeclaration);
		}
	}

	/**
	 * 42
	 */
	@Override
	protected void inspectSimpleName(int depth, DataFlow dataFlow, SimpleName expression) {
		// 01 - Try to retrieve the variable from the list of variables.
		VariableBindingManager manager = getCallGraph().getVariableBinding(expression);
		if (null != manager) {
			if (manager.status().equals(EnumStatusVariable.VULNERABLE)) {
				dataFlow.replace(manager.getDataFlow());
			} else if (manager.status().equals(EnumStatusVariable.UNKNOWN)) {
				// 02 - This is the case where we have to go deeper into the variable's path.
				inspectNode(depth, dataFlow, manager.getInitializer());

				// 03 - If there a vulnerable path, then this variable is vulnerable.
				EnumStatusVariable status = (dataFlow.isVulnerable()) ? EnumStatusVariable.VULNERABLE
						: EnumStatusVariable.NOT_VULNERABLE;
				manager.setStatus(dataFlow, status);
			}
		} else {
			// If I don't know this variable, it is a parameter.
			PluginLogger.logIfDebugging("inspectSimpleName");
			// TODO do what here ?
		}
	}

}
