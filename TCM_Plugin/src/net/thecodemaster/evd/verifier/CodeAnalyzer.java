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
import net.thecodemaster.evd.marker.annotation.AnnotationManager;
import net.thecodemaster.evd.point.EntryPoint;
import net.thecodemaster.evd.point.ExitPoint;
import net.thecodemaster.evd.ui.l10n.Message;
import net.thecodemaster.evd.xmlloader.LoaderEntryPoint;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 * @author Luciano Sampaio
 */
public abstract class CodeAnalyzer {

	/**
	 * The current resource that is being analyzed.
	 */
	private IResource								currentResource;
	/**
	 * This object contains all the methods, variables and their interactions, on the project that is being analyzed.
	 */
	private CallGraph								callGraph;
	/**
	 * List with all the EntryPoints (shared among other instances of the verifiers).
	 */
	private static List<EntryPoint>	entryPoints;

	protected void setCurrentResource(IResource currentResource) {
		this.currentResource = currentResource;
	}

	protected IResource getCurrentResource() {
		return currentResource;
	}

	protected void setCallGraph(CallGraph callGraph) {
		this.callGraph = callGraph;
	}

	protected CallGraph getCallGraph() {
		return callGraph;
	}

	protected static List<EntryPoint> getEntryPoints() {
		if (null == entryPoints) {
			// Loads all the EntryPoints.
			loadEntryPoints();
		}

		return entryPoints;
	}

	/**
	 * Load all the EntryPoints that the plug-in will use.
	 */
	protected static void loadEntryPoints() {
		entryPoints = (new LoaderEntryPoint()).load();
	}

	protected String getMessageEntryPoint(String value) {
		return String.format(Message.VerifierSecurityVulnerability.ENTRY_POINT_METHOD, value);
	}

	protected boolean isVulnerable(Expression expression) {
		return false;
	}

	protected boolean hasReachedMaximumDepth(int depth) {
		return Constant.MAXIMUM_VERIFICATION_DEPTH == depth;
	}

	protected boolean hasAnnotationAtPosition(Expression expression) {
		return AnnotationManager.hasAnnotationAtPosition(expression);
	}

	protected boolean isMethodASanitizationPoint(Expression method) {
		return false;
	}

	protected boolean isMethodAnEntryPoint(Expression method) {
		for (EntryPoint currentEntryPoint : getEntryPoints()) {
			if (BindingResolver.methodsHaveSameNameAndPackage(currentEntryPoint, method)) {
				// 01 - Get the expected arguments of this method.
				List<String> expectedParameters = currentEntryPoint.getParameters();

				// 02 - Get the received parameters of the current method.
				List<Expression> receivedParameters = BindingResolver.getParameters(method);

				// 03 - It is necessary to check the number of parameters and its types
				// because it may exist methods with the same names but different parameters.
				if (expectedParameters.size() == receivedParameters.size()) {
					boolean isMethodAnEntryPoint = true;
					int index = 0;
					for (String expectedParameter : expectedParameters) {
						ITypeBinding typeBinding = receivedParameters.get(index++).resolveTypeBinding();

						// Verify if all the parameters are the ones expected.
						if (!BindingResolver.parametersHaveSameType(expectedParameter, typeBinding)) {
							isMethodAnEntryPoint = false;
							break;
						}
					}

					if (isMethodAnEntryPoint) {
						return true;
					}
				}
			}
		}

		return false;
	}

	protected void inspectNode(int depth, DataFlow df, ASTNode node) {
		// 01 - To avoid infinitive loop, this check is necessary.
		if (hasReachedMaximumDepth(depth)) {
			return;
		}

		switch (node.getNodeType()) {
			case ASTNode.ARRAY_INITIALIZER: // 04
				inspectArrayInitializer(depth, df, (ArrayInitializer) node);
				break;
			case ASTNode.ASSIGNMENT: // 07
				inspectAssignment(depth, df, (Assignment) node);
				break;
			case ASTNode.BLOCK: // 08
				inspectBlock(depth, df, (Block) node);
				break;
			case ASTNode.CAST_EXPRESSION: // 11
				inspectCastExpression(depth, df, (CastExpression) node);
				break;
			case ASTNode.CLASS_INSTANCE_CREATION: // 14
				inspectClassInstanceCreation(depth, df, (ClassInstanceCreation) node);
				break;
			case ASTNode.CONDITIONAL_EXPRESSION: // 16
				inspectConditionExpression(depth, df, (ConditionalExpression) node);
				break;
			case ASTNode.DO_STATEMENT: // 19
				inspectDoStatement(depth, df, (DoStatement) node);
				break;
			case ASTNode.EXPRESSION_STATEMENT: // 21
				inspectExpressionStatement(depth, df, (ExpressionStatement) node);
				break;
			case ASTNode.FOR_STATEMENT: // 24
				inspectForStatement(depth, df, (ForStatement) node);
				break;
			case ASTNode.IF_STATEMENT: // 25
				inspectIfStatement(depth, df, (IfStatement) node);
				break;
			case ASTNode.INFIX_EXPRESSION: // 27
				inspectInfixExpression(depth, df, (InfixExpression) node);
				break;
			case ASTNode.METHOD_INVOCATION: // 32
				inspectMethodInvocation(depth, df, (MethodInvocation) node);
				break;
			case ASTNode.PARENTHESIZED_EXPRESSION: // 36
				inspectParenthesizedExpression(depth, df, (ParenthesizedExpression) node);
				break;
			case ASTNode.PREFIX_EXPRESSION: // 38
				inspectPrefixExpression(depth, df, (PrefixExpression) node);
				break;
			case ASTNode.QUALIFIED_NAME: // 40
				inspectQualifiedName(depth, df, (QualifiedName) node);
				break;
			case ASTNode.RETURN_STATEMENT: // 41
				inspectReturnStatement(depth, df, (ReturnStatement) node);
				break;
			case ASTNode.SIMPLE_NAME: // 42
				inspectSimpleName(depth, df, (SimpleName) node);
				break;
			case ASTNode.SWITCH_STATEMENT: // 50
				inspectSwitchStatement(depth, df, (SwitchStatement) node);
				break;
			case ASTNode.TRY_STATEMENT: // 54
				inspectTryStatement(depth, df, (TryStatement) node);
				break;
			case ASTNode.VARIABLE_DECLARATION_STATEMENT: // 60
				inspectVariableDeclarationStatement(depth, df, (VariableDeclarationStatement) node);
				break;
			case ASTNode.WHILE_STATEMENT: // 61
				inspectWhileStatement(depth, df, (WhileStatement) node);
				break;
			case ASTNode.CHARACTER_LITERAL: // 13
			case ASTNode.NULL_LITERAL: // 33
			case ASTNode.NUMBER_LITERAL: // 34
			case ASTNode.STRING_LITERAL: // 45
				inspectLiteral(depth, df, (Expression) node);
				break;
			default:
				PluginLogger.logError("inspectStatement Default Node Type: " + node.getNodeType() + " - " + node, null);
		}
	}

	/**
	 * 04
	 */
	protected void inspectArrayInitializer(int depth, DataFlow df, ArrayInitializer expression) {
		List<Expression> parameters = BindingResolver.getParameters(expression);
		for (Expression parameter : parameters) {
			inspectNode(depth, df, parameter);
		}
	}

	/**
	 * 07
	 */
	protected void inspectAssignment(int depth, DataFlow df, Assignment expression) {
		// 01 - Get the elements from the operation.
		Expression leftHandSide = expression.getLeftHandSide();
		Expression rightHandSide = expression.getRightHandSide();

		// 02 - Check each element.
		inspectNode(depth, df, leftHandSide);
		inspectNode(depth, df, rightHandSide);
	}

	/**
	 * 08
	 */
	protected void inspectBlock(int depth, DataFlow df, Block block) {
		if (null != block) {
			List<?> statements = block.statements();
			for (Object object : statements) {
				inspectNode(depth, df, (Statement) object);
			}
		}
	}

	/**
	 * 11
	 */
	protected void inspectCastExpression(int depth, DataFlow df, CastExpression expression) {
		inspectNode(depth, df, expression.getExpression());
	}

	/**
	 * 14
	 */
	protected void inspectClassInstanceCreation(int depth, DataFlow df, ClassInstanceCreation expression) {
		List<Expression> parameters = BindingResolver.getParameters(expression);
		for (Expression parameter : parameters) {
			inspectNode(depth, df, parameter);
		}
	}

	/**
	 * 16
	 */
	protected void inspectConditionExpression(int depth, DataFlow df, ConditionalExpression expression) {
		// 01 - Get the elements from the operation.
		Expression thenExpression = expression.getThenExpression();
		Expression elseExpression = expression.getElseExpression();

		// 02 - Check each element.
		inspectNode(depth, df, thenExpression);
		inspectNode(depth, df, elseExpression);
	}

	/**
	 * 19
	 */
	protected void inspectDoStatement(int depth, DataFlow df, DoStatement statement) {
		inspectNode(depth, df, statement.getBody());
	}

	/**
	 * 21
	 */
	protected void inspectExpressionStatement(int depth, DataFlow df, ExpressionStatement expression) {
		inspectNode(depth, df, expression.getExpression());
	}

	/**
	 * 24
	 */
	protected void inspectForStatement(int depth, DataFlow df, ForStatement statement) {
		inspectNode(depth, df, statement.getBody());
	}

	/**
	 * 25
	 */
	protected void inspectIfStatement(int depth, DataFlow df, IfStatement statement) {
		inspectNode(depth, df, statement.getThenStatement());
		inspectNode(depth, df, statement.getElseStatement());
	}

	/**
	 * 27
	 */
	protected void inspectInfixExpression(int depth, DataFlow df, InfixExpression expression) {
		// 01 - Get the elements from the operation.
		Expression leftOperand = expression.getLeftOperand();
		Expression rightOperand = expression.getRightOperand();
		List<Expression> extendedOperands = BindingResolver.getParameters(expression);

		// 02 - Check each element.
		inspectNode(depth, df, leftOperand);
		inspectNode(depth, df, rightOperand);

		for (Expression extendedOperand : extendedOperands) {
			inspectNode(depth, df, extendedOperand);
		}
	}

	/**
	 * 32 TODO Verify if we have to do something with the dfParent.
	 */
	protected void inspectMethodInvocation(int depth, DataFlow dfParent, MethodInvocation expression) {
		PluginLogger.logIfDebugging("inspectMethodInvocation not implemented.");
	}

	private void inspectParameterOfExitPoint(int depth, DataFlow df, MethodInvocation method, ExitPoint exitPoint) {
		// 01 - Get the parameters (received) from the current method.
		List<Expression> receivedParameters = BindingResolver.getParameters(method);

		// 02 - Get the expected parameters of the ExitPoint method.
		Map<Parameter, List<Integer>> expectedParameters = exitPoint.getParameters();

		int index = 0;
		for (List<Integer> rules : expectedParameters.values()) {
			// If the rules are null, it means the expected parameter can be anything. (We do not care for it).
			if (null != rules) {
				Expression expr = receivedParameters.get(index);

				// checkExpression(depth, df, rules, expr);
			}
			index++;
		}
	}

	/**
	 * 36
	 */
	protected void inspectParenthesizedExpression(int depth, DataFlow df, ParenthesizedExpression expression) {
		inspectNode(depth, df, expression.getExpression());
	}

	/**
	 * 38
	 */
	protected void inspectPrefixExpression(int depth, DataFlow df, PrefixExpression expression) {
		// 01 - Get the elements from the operation.
		Expression operand = expression.getOperand();

		// 02 - Check each element.
		inspectNode(depth, df, operand);
	}

	/**
	 * 40
	 */
	protected void inspectQualifiedName(int depth, DataFlow df, QualifiedName expression) {
		inspectNode(depth, df, expression.getName());
	}

	/**
	 * 41
	 */
	protected void inspectReturnStatement(int depth, DataFlow df, ReturnStatement statement) {
		inspectNode(depth, df, statement.getExpression());
	}

	/**
	 * 42
	 */
	protected void inspectSimpleName(int depth, DataFlow df, SimpleName expression) {
		// 01 - Try to retrieve the variable from the list of variables.
		VariableBindingManager manager = getCallGraph().getVariableBinding(expression);
		if (null != manager) {

			// 02 - This is the case where we have to go deeper into the variable's path.
			Expression initializer = manager.getInitializer();
			inspectNode(depth, df, initializer);
		} else {
			// This is the case where the variable is an argument of the method.
			// 04 - Get the method signature that is using this parameter.
			MethodDeclaration methodDeclaration = BindingResolver.getParentMethodDeclaration(expression);

			// 05 - Get the index position where this parameter appears.
			int parameterIndex = BindingResolver.getParameterIndex(methodDeclaration, expression);
			if (parameterIndex >= 0) {
				// 06 - Get the list of methods that invokes this method.
				Map<MethodDeclaration, List<Expression>> invokers = getCallGraph().getInvokers(methodDeclaration);

				if (null != invokers) {
					// 07 - Iterate over all the methods that invokes this method.
					for (List<Expression> currentInvocations : invokers.values()) {

						// 08 - Care only about the invocations to this method.
						for (Expression invocation : currentInvocations) {
							if (BindingResolver.areMethodsEqual(methodDeclaration, invocation)) {
								// 09 - Get the parameter at the index position.
								Expression parameter = BindingResolver.getParameterAtIndex(invocation, parameterIndex);

								// 10 - Run detection on this parameter.
								inspectNode(depth, df, parameter);
							}
						}

					}
				}
			}

		}
	}

	/**
	 * 50
	 */
	protected void inspectSwitchStatement(int depth, DataFlow df, SwitchStatement statement) {
		List<?> switchStatements = statement.statements();
		for (Object switchCases : switchStatements) {
			inspectNode(depth, df, (Statement) switchCases);
		}
	}

	/**
	 * 54
	 */
	protected void inspectTryStatement(int depth, DataFlow df, TryStatement statement) {
		inspectNode(depth, df, statement.getBody());

		List<?> listCatches = statement.catchClauses();
		for (Object catchClause : listCatches) {
			inspectNode(depth, df, ((CatchClause) catchClause).getBody());
		}

		inspectNode(depth, df, statement.getFinally());
	}

	/**
	 * 60
	 */
	protected void inspectVariableDeclarationStatement(int depth, DataFlow df, VariableDeclarationStatement statement) {
		PluginLogger.logIfDebugging("inspectVariableDeclarationStatement not implemented.");
	}

	/**
	 * 61
	 */
	protected void inspectWhileStatement(int depth, DataFlow df, WhileStatement statement) {
		inspectNode(depth, df, statement.getBody());
	}

	/**
	 * 13, 33, 34, 45
	 */
	protected void inspectLiteral(int depth, DataFlow df, Expression node) {
	}

}