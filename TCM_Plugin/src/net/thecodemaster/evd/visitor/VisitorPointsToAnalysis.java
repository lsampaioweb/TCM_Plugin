package net.thecodemaster.evd.visitor;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.thecodemaster.evd.graph.BindingResolver;
import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.graph.VariableBindingManager;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.ui.l10n.Message;
import net.thecodemaster.evd.verifier.CodeAnalyzer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * The main responsibility of this class is to find entry-points and vulnerable variables.
 * 
 * @author Luciano Sampaio
 */
public class VisitorPointsToAnalysis extends CodeAnalyzer {

	private IProgressMonitor	progressMonitor;

	private IProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	private void setProgressMonitor(IProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}

	/**
	 * Notifies that a subtask of the main task is beginning.
	 * 
	 * @param taskName
	 *          The text that will be displayed to the user.
	 */
	private void setSubTask(String taskName) {
		if (null != getProgressMonitor()) {
			getProgressMonitor().subTask(taskName);
		}
	}

	private void addParametersToCallGraph(int depth, List<Expression> currentInvocations,
			MethodDeclaration methodDeclaration) {
		// 01 - Care only about the invocations to this method.
		for (Expression invocation : currentInvocations) {
			if (BindingResolver.areMethodsEqual(methodDeclaration, invocation)) {

				addParametersToCallGraph(depth, invocation, methodDeclaration);

				run(depth, methodDeclaration);
			}
		}
	}

	private void addParametersToCallGraph(int depth, Expression methodInvocation, MethodDeclaration methodDeclaration) {
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
				addReferenceToInitializer(depth, methodInvocation, initializer);
			}
		}
	}

	public void run(IProgressMonitor monitor, List<IResource> resources, CallGraph callGraph) {
		setCallGraph(callGraph);
		setProgressMonitor(monitor);

		// 01 - Iterate over all the resources.
		for (IResource resource : resources) {
			// We need this information when we are going retrieve the variable bindings in the callGraph.
			setCurrentResource(resource);
			setSubTask(Message.Plugin.VISITOR_POINTS_TO_ANALYSIS_SUB_TASK + resource.getName());

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
			// 03 - We need the compilation unit to check if there are markers in the current resource.
			setCurrentCompilationUnit(BindingResolver.getParentCompilationUnit(methodDeclaration));

			// 04 - The depth control the investigation mechanism to avoid infinitive loops.
			int depth = 0;

			// To avoid unnecessary processing, we only process methods that are
			// not invoked by any other method in the same file. Because if the method
			// is invoked, eventually it will be processed.
			// 05 - Get the list of methods that invokes this method.
			Map<MethodDeclaration, List<Expression>> invokers = getCallGraph().getInvokers(methodDeclaration);
			if (invokers.size() > 0) {
				// 06 - Iterate over all the methods that invokes this method.
				for (Entry<MethodDeclaration, List<Expression>> caller : invokers.entrySet()) {

					IResource resourceCaller = BindingResolver.getResource(caller.getKey());
					// 07 - If it is a method invocation from another file.
					if (!resourceCaller.equals(resource)) {

						// If this method declaration has parameters, we have to add the values from
						// the invocation to these parameters.
						addParametersToCallGraph(depth, caller.getValue(), methodDeclaration);
					}
				}
			} else {
				run(depth, methodDeclaration);
			}

		}
	}

	/**
	 * Run the vulnerability detection on the current method declaration.
	 * 
	 * @param methodDeclaration
	 */
	protected void run(int depth, MethodDeclaration methodDeclaration) {
		// Block block = methodDeclaration.getBody();
		// if (null != block) {
		// for (Object object : block.statements()) {
		// inspectNode(depth, new DataFlow(), (Statement) object);
		// }
		// }
		inspectNode(depth, new DataFlow(), methodDeclaration.getBody());
	}

	/**
	 * 07
	 */
	@Override
	protected void inspectAssignment(int depth, DataFlow dataFlow, Assignment node) {
		// 01 - Get the elements from the expression.
		Expression leftHandSide = node.getLeftHandSide();
		Expression rightHandSide = node.getRightHandSide();

		// if (node.getOperator().equals(Operator.PLUS_ASSIGN)) {
		// // 02 - Get the AST of this node.
		// AST ast = node.getAST();
		//
		// // 03 - Create a new InfixExpression object.
		// InfixExpression expr = ast.newInfixExpression();
		//
		// // 04 - Copy the old objects.
		// Expression left = (Expression) ASTNode.copySubtree(ast, node.getLeftHandSide());
		// Expression right = (Expression) ASTNode.copySubtree(ast, node.getRightHandSide());
		//
		// // 05 - Add the old bindings to the newly created objects.
		// getCallGraph().addBinding(left, leftHandSide);
		//
		// switch (rightHandSide.getNodeType()) {
		// case ASTNode.SIMPLE_NAME:
		// getCallGraph().addBinding(right, rightHandSide);
		// break;
		// case ASTNode.INFIX_EXPRESSION:
		// InfixExpression infixExpression = (InfixExpression) rightHandSide;
		// Expression leftOperand = infixExpression.getLeftOperand();
		// Expression rightOperand = infixExpression.getRightOperand();
		//
		// List<Expression> extendedOperands = BindingResolver.getParameters(infixExpression);
		// getCallGraph().addBinding(leftOperand, leftOperand);
		// getCallGraph().addBinding(rightOperand, rightOperand);
		//
		// for (Expression current : extendedOperands) {
		// getCallGraph().addBinding(current, current);
		// }
		// break;
		// }
		//
		// // String message = "a";
		// // message += "b" + c + getMessage();
		// // Result: message = message + "b"
		// // 06 - Add the values to the InfixExpression.
		// expr.setLeftOperand(left);
		// expr.setOperator(InfixExpression.Operator.PLUS);
		// expr.setRightOperand(right);
		//
		// rightHandSide = expr;
		// } else {
		// rightHandSide = node.getRightHandSide();
		// }

		// 02 - Add the new variable to the callGraph.
		addVariableToCallGraphAndInspectInitializer(depth, dataFlow, leftHandSide, rightHandSide);
	}

	/**
	 * 32
	 */
	@Override
	protected void inspectMethodInvocationWithOrWithOutSourceCode(int depth, DataFlow dataFlow,
			Expression methodInvocation) {
		// Some method invocations can be in a chain call, we have to investigate them all.
		// response.sendRedirect(login);
		// getServletContext().getRequestDispatcher(login).forward(request, response);
		List<Expression> expressions = Creator.newList();

		Expression optionalexpression = methodInvocation;
		while (null != optionalexpression) {
			switch (optionalexpression.getNodeType()) {
				case ASTNode.CLASS_INSTANCE_CREATION: // 14
				case ASTNode.METHOD_INVOCATION: // 32
					expressions.add(optionalexpression);
					break;
			}

			optionalexpression = BindingResolver.getExpression(optionalexpression);
		}

		for (Expression expression : expressions) {
			super.inspectMethodInvocationWithOrWithOutSourceCode(depth, dataFlow.addNodeToPath(expression), expression);
		}
	}

	@Override
	protected void inspectMethodWithSourceCode(int depth, DataFlow dataFlow, Expression methodInvocation,
			MethodDeclaration methodDeclaration) {
		// If this method declaration has parameters, we have to add the values from
		// the invocation to these parameters.
		addParametersToCallGraph(depth, methodInvocation, methodDeclaration);

		// 01 - Now I inspect the body of the method.
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
			addReferenceToInitializer(depth, expression, parameter);

			inspectNode(depth, dataFlow.addNodeToPath(parameter), parameter);
		}
	}

	/**
	 * 42
	 */
	@Override
	protected void inspectSimpleName(int depth, DataFlow dataFlow, SimpleName expression) {
		// 01 - Try to retrieve the variable from the list of variables.
		VariableBindingManager variableBinding = getCallGraph().getLastReference(expression);

		inspectSimpleName(depth, dataFlow, expression, variableBinding);
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

}
