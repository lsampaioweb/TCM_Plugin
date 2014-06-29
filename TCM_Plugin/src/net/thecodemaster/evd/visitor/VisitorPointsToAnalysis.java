package net.thecodemaster.evd.visitor;

import java.util.Iterator;
import java.util.List;

import net.thecodemaster.evd.context.Context;
import net.thecodemaster.evd.graph.BindingResolver;
import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.graph.CodeAnalyzer;
import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.graph.VariableBinding;
import net.thecodemaster.evd.helper.HelperCodeAnalyzer;
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
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
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
	protected void run(int depth, MethodDeclaration methodDeclaration, ASTNode invoker) {
		// 02 - TODO -
		// If there is a invoker we have to add the parameters and do more stuff.

		// - Create a context for this method.
		Context context = getCallGraph().newContext(getCurrentResource(), methodDeclaration, invoker);

		// 03 - Start the detection on each and every line of this method.
		inspectNode(depth, context, new DataFlow(methodDeclaration.getName()), methodDeclaration.getBody());
	}

	/**
	 * 07 <br/>
	 * a = b <br/>
	 * Person.staticPersonVariable <br/>
	 * person.publicPersonVariable
	 */
	@Override
	protected void inspectAssignment(int depth, Context context, DataFlow dataFlow, Assignment node) {
		// 01 - Get the elements from the expression.
		Expression leftHandSide = node.getLeftHandSide();
		Expression rightHandSide = node.getRightHandSide();

		switch (leftHandSide.getNodeType()) {
			case ASTNode.FIELD_ACCESS: // 22
			case ASTNode.SIMPLE_NAME: // 42
			case ASTNode.SUPER_FIELD_ACCESS: // 47
				break; // Use the same current context.
			case ASTNode.QUALIFIED_NAME: // 40
				// * Get the context of the instance. (Object or static).
				context = getContext(context, leftHandSide);
				break;
			default:
				PluginLogger.logError("inspectAssignment Default Node Type: " + node.getNodeType() + " - " + node, null);
		}

		// 04 - Add the new variable to the callGraph.
		addVariableToCallGraphAndInspectInitializer(depth, context, dataFlow, leftHandSide, rightHandSide);
	}

	@Override
	protected void inspectMethodWithSourceCode(int depth, Context context, DataFlow dataFlow, ASTNode methodInvocation,
			MethodDeclaration methodDeclaration) {
		// 01 - Get the current method declaration where this invocation is being performed.
		MethodDeclaration currentMethod = BindingResolver.getParentMethodDeclaration(methodInvocation);

		// 02 - Add this method invocation into the current context.
		getCallGraph().addMethodInvocation(context, currentMethod, methodInvocation);

		// 03 - Create a context for this method.
		Context newContext = getContext(context, methodDeclaration, methodInvocation);

		// 04 - If this method declaration has parameters, we have to add the values from
		// the invocation to these parameters.
		addParametersToCallGraph(depth, newContext, methodInvocation, methodDeclaration);

		// 05 - Now I inspect the body of the method.
		super.inspectMethodWithSourceCode(depth, newContext, dataFlow, methodInvocation, methodDeclaration);
	}

	@Override
	protected void inspectMethodWithOutSourceCode(int depth, Context context, DataFlow dataFlow, ASTNode method) {
		switch (method.getNodeType()) {
			case ASTNode.CLASS_INSTANCE_CREATION:
				Expression instance = HelperCodeAnalyzer.getInstanceIfItIsAnObject(method);
				context = getCallGraph().newClassContext(context, null, method, instance);
				break;
		}

		// We have to iterate over its parameters to see if any is vulnerable.
		// If there is a vulnerable parameter and if this is a method from an object
		// we set this object as vulnerable.
		List<Expression> parameters = BindingResolver.getParameters(method);
		for (Expression parameter : parameters) {
			// 01 - Add a method reference to this variable (if it is a variable).
			addReferenceToInitializer(depth, context, method, parameter);

			inspectNode(depth, context, dataFlow.addNodeToPath(parameter), parameter);
		}
	}

	@Override
	protected Context getContext(Context context, MethodDeclaration methodDeclaration, ASTNode methodInvocation) {
		// We have 8 cases:
		// 01 - method(...);
		// 02 - method1(...).method2(...).method3(...);
		// 03 - obj.method(...);
		// 04 - obj.method1(...).method2(...).method3(...);
		// 05 - getObj(...).method(...);
		// 06 - Class.staticMethod(...);
		// 07 - Class obj = new Class(...);
		// 08 - (new Class(...)).run(..);
		Expression instance = HelperCodeAnalyzer.getInstanceIfItIsAnObject(methodInvocation);

		if (methodDeclaration.isConstructor()) {
			// Cases: 07
			return getCallGraph().newClassContext(context, methodDeclaration, methodInvocation, instance);
		} else if (Modifier.isStatic(methodDeclaration.getModifiers())) {
			// Cases: 06
			return getCallGraph().newStaticContext(context, methodDeclaration, methodInvocation);
		} else {
			if (null != instance) {
				// Cases: 03, 04, 05
				return getCallGraph().newInstanceContext(context, methodDeclaration, methodInvocation, instance);
			} else {
				// Cases: 01, 02
				return getCallGraph().newContext(context, methodDeclaration, methodInvocation);
			}
		}
	}

	/**
	 * 41
	 */
	@Override
	protected void inspectReturnStatement(int depth, Context context, DataFlow dataFlow, ReturnStatement statement) {
		// 01 - Add a reference of the variable into the initializer (if the initializer is also a variable).
		addReferenceToInitializer(depth, context, statement, statement.getExpression());

		super.inspectReturnStatement(depth, context, dataFlow, statement);
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
			Expression variableName, Expression initializer) {
		// 01 - Add a reference of the variable into the initializer (if the initializer is also a variable).
		addReferenceToInitializer(depth, context, variableName, initializer);

		// 02 - Add the variable to the current context.
		VariableBinding variableBinding = getCallGraph().addVariable(context, variableName, initializer);

		// 03 - Inspect the Initializer to verify if this variable is vulnerable.
		DataFlow newDataFlow = new DataFlow(variableName);
		inspectNode(depth, context, newDataFlow, initializer);

		// 04 - If there is a vulnerable path, then this variable is vulnerable.
		// But if this variable is of primitive type, then there is nothing to do because they can not be vulnerable.
		if (HelperCodeAnalyzer.isPrimitive(variableName)) {
			HelperCodeAnalyzer.updateVariableBindingStatusToPrimitive(variableBinding);
		} else {
			HelperCodeAnalyzer.updateVariableBindingStatus(variableBinding, newDataFlow);
		}
	}

	private void addParametersToCallGraph(int depth, Context context, ASTNode methodInvocation,
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

				// 05 - Add a method reference to this variable (if it is a variable).
				addReferenceToInitializer(depth, context, methodInvocation, initializer);

				// 06 - Add the content with the one that came from the method invocation.
				getCallGraph().addParameter(context, parameterName, initializer);
			}
		}
	}

	private void addReferenceToInitializer(int depth, Context context, ASTNode expression, Expression initializer) {
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
				case ASTNode.FIELD_ACCESS: // 22
					addReferenceFieldAccess(depth, context, expression, (FieldAccess) initializer);
					break;
				case ASTNode.INFIX_EXPRESSION: // 27
					addReferenceInfixExpression(depth, context, expression, (InfixExpression) initializer);
					break;
				case ASTNode.PARENTHESIZED_EXPRESSION: // 36
					addReferenceParenthesizedExpression(depth, context, expression, (ParenthesizedExpression) initializer);
					break;
				case ASTNode.SUPER_FIELD_ACCESS: // 47
					addReferenceSuperFieldAccess(depth, context, expression, (SuperFieldAccess) initializer);
					break;
				case ASTNode.QUALIFIED_NAME: // 40
				case ASTNode.SIMPLE_NAME: // 42
					addReferenceName(depth, context, expression, (Name) initializer);
					break;
			}
		}
	}

	private void addReference(Context context, ASTNode expression, Expression initializer) {
		context = getContext(context, initializer);

		VariableBinding variableBinding = getCallGraph().getLastReference(context, initializer);
		if (null != variableBinding) {
			variableBinding.addReferences(expression);
		} else {
			PluginLogger.logError("addReference else" + initializer, null);
		}
	}

	private void addReferenceName(int depth, Context context, ASTNode expression, Name initializer) {
		addReference(context, expression, initializer);
	}

	private void addReferenceArrayAccess(int depth, Context context, ASTNode expression, ArrayAccess initializer) {
		addReference(context, expression, initializer);
	}

	private void addReferenceArrayInitializer(int depth, Context context, ASTNode expression, ArrayInitializer initializer) {
		List<Expression> expressions = BindingResolver.getParameters(initializer);

		for (Expression current : expressions) {
			addReferenceToInitializer(depth, context, expression, current);
		}
	}

	private void addReferenceAssignment(int depth, Context context, ASTNode expression, Assignment initializer) {
		addReferenceToInitializer(depth, context, expression, initializer.getLeftHandSide());
		addReferenceToInitializer(depth, context, expression, initializer.getRightHandSide());
	}

	private void inspectCastExpression(int depth, Context context, ASTNode expression, CastExpression initializer) {
		addReferenceToInitializer(depth, context, expression, initializer.getExpression());
	}

	private void addReferenceConditionalExpression(int depth, Context context, ASTNode expression,
			ConditionalExpression initializer) {
		addReferenceToInitializer(depth, context, expression, initializer.getThenExpression());
		addReferenceToInitializer(depth, context, expression, initializer.getElseExpression());
	}

	private void addReferenceFieldAccess(int depth, Context context, ASTNode expression, FieldAccess initializer) {
		addReferenceToInitializer(depth, context, expression, initializer.getName());
	}

	private void addReferenceInfixExpression(int depth, Context context, ASTNode expression, InfixExpression initializer) {
		addReferenceToInitializer(depth, context, expression, initializer.getLeftOperand());
		addReferenceToInitializer(depth, context, expression, initializer.getRightOperand());

		List<Expression> extendedOperands = BindingResolver.getParameters(initializer);

		for (Expression current : extendedOperands) {
			addReferenceToInitializer(depth, context, expression, current);
		}
	}

	private void addReferenceParenthesizedExpression(int depth, Context context, ASTNode expression,
			ParenthesizedExpression initializer) {
		addReferenceToInitializer(depth, context, expression, initializer.getExpression());
	}

	private void addReferenceSuperFieldAccess(int depth, Context context, ASTNode expression, SuperFieldAccess initializer) {
		addReferenceToInitializer(depth, context, expression, initializer.getName());
	}

}
