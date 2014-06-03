package net.thecodemaster.evd.visitor;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.thecodemaster.evd.graph.BindingResolver;
import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.graph.VariableBindingManager;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.ui.enumeration.EnumStatusVariable;
import net.thecodemaster.evd.verifier.CodeAnalyzer;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * The main responsibility of this class is to find entry-points and vulnerable variables.
 * 
 * @author Luciano Sampaio
 */
public class VisitorPointsToAnalysis extends CodeAnalyzer {

	public VisitorPointsToAnalysis() {
	}

	public void run(List<IResource> resources, CallGraph callGraph) {
		setCallGraph(callGraph);

		// 01 - Iterate over all the resources.
		for (IResource resource : resources) {
			// We need this information when we are going retrieve the variable bindings in the callGraph.
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
		// 01 - Get the list of methods in the current resource and its invocations.
		Map<MethodDeclaration, List<Expression>> methods = getCallGraph().getMethods(resource);

		// 02 - Iterate over all the method declarations of the current resource.
		for (MethodDeclaration methodDeclaration : methods.keySet()) {
			// To avoid unnecessary processing, we only process methods that are
			// not invoked by any other method in the same file. Because if the method
			// is invoked, eventually it will be processed.
			// 03 - Get the list of methods that invokes this method.
			Map<MethodDeclaration, List<Expression>> invokers = getCallGraph().getInvokers(methodDeclaration);
			boolean shouldProcess = false;
			if (invokers.size() > 0) {
				// 04 - Iterate over all the methods that invokes this method.
				for (MethodDeclaration callers : invokers.keySet()) {

					IResource resourceCaller = BindingResolver.getResource(callers);
					// If it is a method invocation from another file.
					if (!resourceCaller.equals(resource)) {
						shouldProcess = true;
						break;
					}
				}
			} else {
				shouldProcess = true;
			}

			if (shouldProcess) {
				run(methodDeclaration);
			}
		}
	}

	/**
	 * Run the vulnerability detection on the current method declaration.
	 * 
	 * @param methodDeclaration
	 */
	protected void run(MethodDeclaration methodDeclaration) {
		// The depth control the investigation mechanism to avoid infinitive loops.
		int depth = 0;
		Block block = methodDeclaration.getBody();
		if (null != block) {
			for (Object object : block.statements()) {
				inspectNode(depth, new DataFlow(), (Statement) object);
			}
		}
	}

	/**
	 * 07
	 */
	@Override
	protected void inspectAssignment(int depth, DataFlow dataFlow, Assignment expression) {
		// 01 - Get the elements from the expression.
		Expression leftHandSide = expression.getLeftHandSide();
		Expression rightHandSide = expression.getRightHandSide();

		// 02 - Add the new variable to the callGraph.
		addVariableToCallGraphAndInspectInitializer(depth, dataFlow, leftHandSide, rightHandSide);
	}

	/**
	 * 42
	 */
	@Override
	protected void inspectSimpleName(int depth, DataFlow dataFlow, SimpleName expression) {
		// 01 - Try to retrieve the variable from the list of variables.
		VariableBindingManager manager = getCallGraph().getLastReference(expression);

		inspectSimpleName(depth, dataFlow, expression, manager);
	}

	@Override
	protected void inspectMethodInvocationWithOrWithOutSourceCode(int depth, DataFlow dataFlow,
			Expression methodInvocation) {
		// Some method invocations can be in a chain call, we have to investigate them all.
		// response.sendRedirect(login);
		// getServletContext().getRequestDispatcher(login).forward(request, response);
		List<Expression> expressions = Creator.newList();

		Expression Optionalexpression = methodInvocation;
		while (null != Optionalexpression) {
			expressions.add(Optionalexpression);

			Optionalexpression = BindingResolver.getExpression(Optionalexpression);
		}

		for (Expression expression : expressions) {
			super.inspectMethodInvocationWithOrWithOutSourceCode(depth, dataFlow, expression);
		}
	}

	@Override
	protected void inspectMethodWithSourceCode(int depth, DataFlow dataFlow, Expression methodInvocation,
			MethodDeclaration methodDeclaration) {
		// If this method declaration has parameters, we have to add the values from
		// the invocation to these parameters.

		// 01 - Get the parameters of this method declaration.
		List<SingleVariableDeclaration> parameters = BindingResolver.getParameters(methodDeclaration);

		if (parameters.size() > 0) {
			int parameterIndex = 0;
			for (SingleVariableDeclaration parameter : parameters) {
				// 02 - The SimpleName of this parameter will be used for the addVariableToCallGraph.
				SimpleName parameterName = parameter.getName();

				// 03 - Retrieve the variable binding of this parameter from the callGraph.
				Expression initializer = BindingResolver.getParameterAtIndex(methodInvocation, parameterIndex++);

				// 04 - We add the content with the one that came from the method invocation.
				getCallGraph().addVariableToCallGraph(parameterName, initializer);

				// 05 - Add a method reference to this variable (if it is a variable).
				addReferenceToInitializer(methodInvocation, initializer);
			}
		}

		// 05 - Now I inspect the body of the method.
		super.inspectMethodWithSourceCode(depth, dataFlow, methodInvocation, methodDeclaration);
	}

	@Override
	protected void inspectMethodWithOutSourceCode(int depth, DataFlow dataFlow, Expression expression) {
		// We have to iterate over its parameters to see if any is vulnerable.
		// If there is a vulnerable parameter and if this is a method from an object
		// we set this object as vulnerable.
		List<Expression> parameters = BindingResolver.getParameters(expression);
		for (Expression parameter : parameters) {
			// 01 - Add a method reference to this variable (if it is a variable).
			addReferenceToInitializer(expression, parameter);

			inspectNode(depth, dataFlow, parameter);
		}
	}

	/**
	 * 60
	 */
	@Override
	protected void inspectVariableDeclarationStatement(int depth, DataFlow dataFlow,
			VariableDeclarationStatement statement) {
		List<?> fragments = statement.fragments();
		for (Iterator<?> iter = fragments.iterator(); iter.hasNext();) {
			// VariableDeclarationFragment: is the plain variable declaration part.
			// Example: "int x=0, y=0;" contains two VariableDeclarationFragments, "x=0" and "y=0"
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) iter.next();

			// 01 - Add the new variable to the callGraph.
			addVariableToCallGraphAndInspectInitializer(depth, dataFlow, fragment.getName(), fragment.getInitializer());
		}
	}

	private void addVariableToCallGraphAndInspectInitializer(int depth, DataFlow dataFlow, Expression variableName,
			Expression initializer) {
		VariableBindingManager manager = getCallGraph().addVariableToCallGraph(variableName, initializer);
		if (null != manager) {
			// 01 - Add a reference to this variable (if it is a variable).
			addReferenceToInitializer(variableName, initializer);

			DataFlow newDataFlow = dataFlow.addNodeToPath(variableName);
			// 01 - Inspect the Initializer to verify if this variable is vulnerable.
			inspectNode(depth, newDataFlow, initializer);

			// 02 - If there a vulnerable path, then this variable is vulnerable.
			EnumStatusVariable status = (newDataFlow.isVulnerable()) ? EnumStatusVariable.VULNERABLE
					: EnumStatusVariable.NOT_VULNERABLE;
			manager.setStatus(newDataFlow, status);
		}
	}

}
