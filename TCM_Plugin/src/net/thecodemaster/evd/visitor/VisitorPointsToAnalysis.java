package net.thecodemaster.evd.visitor;

import java.util.Iterator;
import java.util.List;

import net.thecodemaster.evd.context.Context;
import net.thecodemaster.evd.graph.BindingResolver;
import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.graph.CodeAnalyzer;
import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.graph.VariableBinding;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.ui.l10n.Message;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * The main responsibility of this class is to find entry-points and vulnerable variables.
 * 
 * @author Luciano Sampaio
 * @Date: 2014-05-07
 * @Version: 01
 */
public class VisitorPointsToAnalysis extends CodeAnalyzer {

	@Override
	protected String getSubTaskMessage() {
		return Message.Plugin.VISITOR_POINTS_TO_ANALYSIS_SUB_TASK + getCurrentResource().getName();
	}

	@Override
	public void run(IProgressMonitor monitor, CallGraph callGraph, List<IResource> resources) {
		super.run(monitor, callGraph, resources);
	}

	/**
	 * Run the vulnerability detection on the current method declaration.
	 * 
	 * @param methodDeclaration
	 */
	@Override
	protected void run(int depth, MethodDeclaration methodDeclaration, Expression invoker) {
		PluginLogger.logIfDebugging("Method:" + methodDeclaration.getName());

		// 02 - TODO -
		// If there is a invoker we have to add the parameters and do more stuff.

		// - Create a context for this method.
		Context context = getCallGraph().newContext(getCurrentResource(), methodDeclaration, invoker);

		// 03 - Start the detection on each and every line of this method.
		inspectNode(depth, context, new DataFlow(methodDeclaration.getName()), methodDeclaration.getBody());
	}

	/**
	 * 07
	 */
	@Override
	protected void inspectAssignment(int depth, Context context, DataFlow dataFlow, Assignment node) {
		// 01 - Get the elements from the expression.
		Expression leftHandSide = node.getLeftHandSide();
		Expression rightHandSide = node.getRightHandSide();

		if (leftHandSide.getNodeType() == ASTNode.SIMPLE_NAME) {
			// 02 - Add the new variable to the callGraph.
			addVariableToCallGraphAndInspectInitializer(depth, context, dataFlow, (SimpleName) leftHandSide, rightHandSide);
		}
	}

	/**
	 * 32
	 */
	@Override
	protected void inspectMethodInvocationWithOrWithOutSourceCode(int depth, Context context, DataFlow dataFlow,
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
			super.inspectMethodInvocationWithOrWithOutSourceCode(depth, context, dataFlow.addNodeToPath(expression),
					expression);
		}
	}

	@Override
	protected void inspectMethodWithSourceCode(int depth, Context context, DataFlow dataFlow,
			Expression methodInvocation, MethodDeclaration methodDeclaration) {
		// 01 - Create a context for this method.
		Context newContext = getCallGraph().newContext(context, methodDeclaration, methodInvocation);

		// 02 - If this method declaration has parameters, we have to add the values from
		// the invocation to these parameters.
		addParametersToCallGraph(depth, newContext, methodInvocation, methodDeclaration);

		// 03 - Now I inspect the body of the method.
		super.inspectMethodWithSourceCode(depth, newContext, dataFlow, methodInvocation, methodDeclaration);
	}

	@Override
	protected void inspectMethodWithOutSourceCode(int depth, Context context, DataFlow dataFlow, Expression expression) {
		// We have to iterate over its parameters to see if any is vulnerable.
		// If there is a vulnerable parameter and if this is a method from an object
		// we set this object as vulnerable.
		List<Expression> parameters = BindingResolver.getParameters(expression);
		for (Expression parameter : parameters) {
			// 01 - Add a method reference to this variable (if it is a variable).
			addReferenceToInitializer(depth, context, expression, parameter);

			inspectNode(depth, context, dataFlow.addNodeToPath(parameter), parameter);
		}
	}

	/**
	 * 60
	 */
	@Override
	protected void inspectVariableDeclarationStatement(int depth, Context context, DataFlow dataFlow,
			VariableDeclarationStatement statement) {
		for (Iterator<?> iter = statement.fragments().iterator(); iter.hasNext();) {
			// VariableDeclarationFragment: is the plain variable declaration part.
			// Example: "int x=0, y=0;" contains two VariableDeclarationFragments, "x=0" and "y=0"
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) iter.next();

			// 01 - Add the new variable to the callGraph.
			addVariableToCallGraphAndInspectInitializer(depth, context, dataFlow, fragment.getName(),
					fragment.getInitializer());
		}
	}

	/**
	 * 70
	 */
	@Override
	protected void inspectEnhancedForStatement(int depth, Context context, DataFlow dataFlow,
			EnhancedForStatement statement) {
		SingleVariableDeclaration parameter = statement.getParameter();
		Expression expression = statement.getExpression();
		// 01 - Add the new variable to the callGraph.
		addVariableToCallGraphAndInspectInitializer(depth, context, dataFlow, parameter.getName(), expression);

		super.inspectEnhancedForStatement(depth, context, dataFlow, statement);
	}

	/**
	 * 07, 60, 70
	 */
	private void addVariableToCallGraphAndInspectInitializer(int depth, Context context, DataFlow dataFlow,
			SimpleName variableName, Expression initializer) {
		// 01 - Add a reference of the variable into the initializer (if the initializer is also a variable).
		addReferenceToInitializer(depth, context, variableName, initializer);

		// 02 - Add the variable to the current context.
		VariableBinding variableBinding = getCallGraph().addVariable(context, variableName, initializer);

		// 03 - Inspect the Initializer to verify if this variable is vulnerable.
		DataFlow newDataFlow = new DataFlow(variableName);
		inspectNode(depth, context, newDataFlow, initializer);

		// 04 - If there is a vulnerable path, then this variable is vulnerable.
		// But if this variable is of primitive type, then there is nothing to do because they can not be vulnerable.
		if (isPrimitive(variableName)) {
			updateVariableBindingStatusToPrimitive(variableBinding);
		} else {
			updateVariableBindingStatus(variableBinding, newDataFlow);
		}
	}

	private void addParametersToCallGraph(int depth, Context context, Expression methodInvocation,
			MethodDeclaration methodDeclaration) {
		// 01 - Get the parameters of this method declaration.
		List<SingleVariableDeclaration> parameters = BindingResolver.getParameters(methodDeclaration);

		if (parameters.size() > 0) {
			int parameterIndex = 0;
			for (SingleVariableDeclaration parameter : parameters) {
				// 02 - The SimpleName of this parameter will be used for the addVariableToCallGraph.
				SimpleName parameterName = parameter.getName();

				// 03 - Retrieve the variable binding of this parameter from the callGraph.
				Expression initializer = BindingResolver.getParameterAtIndex(methodInvocation, parameterIndex++);

				// 04 - Add a reference to this variable (if it is a variable).
				addReferenceToInitializer(depth, context, parameterName, initializer);

				// 05 - Add the content with the one that came from the method invocation.
				getCallGraph().addVariable(context, parameterName, initializer);

				// 06 - Add a method reference to this variable (if it is a variable).
				addReferenceToInitializer(depth, context, methodInvocation, initializer);
			}
		}
	}

	private void addReferenceToInitializer(int depth, Context context, Expression expression, Expression initializer) {
		// 01 - To avoid infinitive loop, this check is necessary.
		if (hasReachedMaximumDepth(depth++)) {
			PluginLogger.logError("addReferenceToInitializer: " + expression + " - " + initializer + " - " + depth, null);
			return;
		}

		if (null != initializer) {
			switch (initializer.getNodeType()) {
				case ASTNode.ARRAY_ACCESS: // 02
					addReferenceArrayAccess(depth, context, expression, (ArrayAccess) initializer);
					break;
				case ASTNode.ARRAY_INITIALIZER: // 04
					addReferenceArrayInitializer(depth, context, expression, (ArrayInitializer) initializer);
					break;
				case ASTNode.ASSIGNMENT: // 07
					addReferenceAssignment(depth, context, expression, (Assignment) initializer);
					break;
				case ASTNode.CAST_EXPRESSION: // 11
					inspectCastExpression(depth, context, expression, (CastExpression) initializer);
					break;
				case ASTNode.CONDITIONAL_EXPRESSION: // 16
					addReferenceConditionalExpression(depth, context, expression, (ConditionalExpression) initializer);
					break;
				case ASTNode.INFIX_EXPRESSION: // 27
					addReferenceInfixExpression(depth, context, expression, (InfixExpression) initializer);
					break;
				case ASTNode.PARENTHESIZED_EXPRESSION: // 36
					addReferenceParenthesizedExpression(depth, context, expression, (ParenthesizedExpression) initializer);
					break;
				case ASTNode.QUALIFIED_NAME: // 40
				case ASTNode.SIMPLE_NAME: // 42
					addReferenceName(depth, context, expression, (Name) initializer);
					break;
			}
		}
	}

	private void addReference(Context context, Expression expression, Expression initializer) {
		VariableBinding variableBinding = getCallGraph().getLastReference(context, initializer);
		if (null != variableBinding) {
			variableBinding.addReferences(expression);
		}
	}

	private void addReferenceName(int depth, Context context, Expression expression, Name initializer) {
		addReference(context, expression, initializer);
	}

	private void addReferenceArrayAccess(int depth, Context context, Expression expression, ArrayAccess initializer) {
		addReference(context, expression, initializer);
	}

	private void addReferenceArrayInitializer(int depth, Context context, Expression expression,
			ArrayInitializer initializer) {
		List<Expression> expressions = BindingResolver.getParameters(initializer);

		for (Expression current : expressions) {
			addReferenceToInitializer(depth, context, expression, current);
		}
	}

	private void addReferenceAssignment(int depth, Context context, Expression expression, Assignment initializer) {
		addReferenceToInitializer(depth, context, expression, initializer.getLeftHandSide());
		addReferenceToInitializer(depth, context, expression, initializer.getRightHandSide());
	}

	private void inspectCastExpression(int depth, Context context, Expression expression, CastExpression initializer) {
		addReferenceToInitializer(depth, context, expression, initializer.getExpression());
	}

	private void addReferenceConditionalExpression(int depth, Context context, Expression expression,
			ConditionalExpression initializer) {
		addReferenceToInitializer(depth, context, expression, initializer.getThenExpression());
		addReferenceToInitializer(depth, context, expression, initializer.getElseExpression());
	}

	private void addReferenceInfixExpression(int depth, Context context, Expression expression,
			InfixExpression initializer) {
		addReferenceToInitializer(depth, context, expression, initializer.getLeftOperand());
		addReferenceToInitializer(depth, context, expression, initializer.getRightOperand());

		List<Expression> extendedOperands = BindingResolver.getParameters(initializer);

		for (Expression current : extendedOperands) {
			addReferenceToInitializer(depth, context, expression, current);
		}
	}

	private void addReferenceParenthesizedExpression(int depth, Context context, Expression expression,
			ParenthesizedExpression initializer) {
		addReferenceToInitializer(depth, context, expression, initializer.getExpression());
	}

}
