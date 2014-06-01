package net.thecodemaster.evd.verifier;

import java.util.List;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.BindingResolver;
import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.marker.annotation.AnnotationManager;
import net.thecodemaster.evd.point.EntryPoint;
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
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
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

	protected void inspectNode(int depth, DataFlow dataFlow, ASTNode node) {
		// 01 - To avoid infinitive loop, this check is necessary.
		if (hasReachedMaximumDepth(depth++)) {
			return;
		}

		switch (node.getNodeType()) {
			case ASTNode.ARRAY_INITIALIZER: // 04
				inspectArrayInitializer(depth, dataFlow, (ArrayInitializer) node);
				break;
			case ASTNode.ASSIGNMENT: // 07
				inspectAssignment(depth, dataFlow, (Assignment) node);
				break;
			case ASTNode.BLOCK: // 08
				inspectBlock(depth, dataFlow, (Block) node);
				break;
			case ASTNode.CAST_EXPRESSION: // 11
				inspectCastExpression(depth, dataFlow, (CastExpression) node);
				break;
			case ASTNode.CLASS_INSTANCE_CREATION: // 14
				inspectClassInstanceCreation(depth, dataFlow, (ClassInstanceCreation) node);
				break;
			case ASTNode.CONDITIONAL_EXPRESSION: // 16
				inspectConditionExpression(depth, dataFlow, (ConditionalExpression) node);
				break;
			case ASTNode.DO_STATEMENT: // 19
				inspectDoStatement(depth, dataFlow, (DoStatement) node);
				break;
			case ASTNode.EXPRESSION_STATEMENT: // 21
				inspectExpressionStatement(depth, dataFlow, (ExpressionStatement) node);
				break;
			case ASTNode.FIELD_ACCESS: // 22
				inspectFieldAccess(depth, dataFlow, (FieldAccess) node);
				break;
			case ASTNode.FOR_STATEMENT: // 24
				inspectForStatement(depth, dataFlow, (ForStatement) node);
				break;
			case ASTNode.IF_STATEMENT: // 25
				inspectIfStatement(depth, dataFlow, (IfStatement) node);
				break;
			case ASTNode.INFIX_EXPRESSION: // 27
				inspectInfixExpression(depth, dataFlow, (InfixExpression) node);
				break;
			case ASTNode.METHOD_INVOCATION: // 32
				MethodInvocation method = (MethodInvocation) node;
				inspectMethodInvocation(depth, dataFlow.addNodeToPath(method), method);
				break;
			case ASTNode.PARENTHESIZED_EXPRESSION: // 36
				inspectParenthesizedExpression(depth, dataFlow, (ParenthesizedExpression) node);
				break;
			case ASTNode.PREFIX_EXPRESSION: // 38
				inspectPrefixExpression(depth, dataFlow, (PrefixExpression) node);
				break;
			case ASTNode.QUALIFIED_NAME: // 40
				inspectQualifiedName(depth, dataFlow, (QualifiedName) node);
				break;
			case ASTNode.RETURN_STATEMENT: // 41
				inspectReturnStatement(depth, dataFlow, (ReturnStatement) node);
				break;
			case ASTNode.SIMPLE_NAME: // 42
				SimpleName simpleName = (SimpleName) node;
				inspectSimpleName(depth, dataFlow.addNodeToPath(simpleName), simpleName);
				break;
			case ASTNode.SWITCH_STATEMENT: // 50
				inspectSwitchStatement(depth, dataFlow, (SwitchStatement) node);
				break;
			case ASTNode.TRY_STATEMENT: // 54
				inspectTryStatement(depth, dataFlow, (TryStatement) node);
				break;
			case ASTNode.VARIABLE_DECLARATION_STATEMENT: // 60
				inspectVariableDeclarationStatement(depth, dataFlow, (VariableDeclarationStatement) node);
				break;
			case ASTNode.WHILE_STATEMENT: // 61
				inspectWhileStatement(depth, dataFlow, (WhileStatement) node);
				break;
			case ASTNode.CHARACTER_LITERAL: // 13
			case ASTNode.NULL_LITERAL: // 33
			case ASTNode.NUMBER_LITERAL: // 34
			case ASTNode.STRING_LITERAL: // 45
				Expression expression = (Expression) node;
				inspectLiteral(depth, dataFlow.addNodeToPath(expression), expression);
				break;
			default:
				PluginLogger.logError("inspectStatement Default Node Type: " + node.getNodeType() + " - " + node, null);
		}
	}

	/**
	 * 04
	 */
	protected void inspectArrayInitializer(int depth, DataFlow dataFlow, ArrayInitializer expression) {
		List<Expression> parameters = BindingResolver.getParameters(expression);
		for (Expression parameter : parameters) {
			inspectNode(depth, dataFlow, parameter);
		}
	}

	/**
	 * 07
	 */
	protected void inspectAssignment(int depth, DataFlow dataFlow, Assignment expression) {
		// 01 - Get the elements from the operation.
		Expression leftHandSide = expression.getLeftHandSide();
		Expression rightHandSide = expression.getRightHandSide();

		// 02 - Check each element.
		inspectNode(depth, dataFlow, leftHandSide);
		inspectNode(depth, dataFlow, rightHandSide);
	}

	/**
	 * 08
	 */
	protected void inspectBlock(int depth, DataFlow dataFlow, Block block) {
		if (null != block) {
			List<?> statements = block.statements();
			for (Object object : statements) {
				inspectNode(depth, dataFlow, (Statement) object);
			}
		}
	}

	/**
	 * 11
	 */
	protected void inspectCastExpression(int depth, DataFlow dataFlow, CastExpression expression) {
		inspectNode(depth, dataFlow, expression.getExpression());
	}

	/**
	 * 14
	 */
	protected void inspectClassInstanceCreation(int depth, DataFlow dataFlow, ClassInstanceCreation expression) {
		List<Expression> parameters = BindingResolver.getParameters(expression);
		for (Expression parameter : parameters) {
			inspectNode(depth, dataFlow, parameter);
		}
	}

	/**
	 * 16
	 */
	protected void inspectConditionExpression(int depth, DataFlow dataFlow, ConditionalExpression expression) {
		// 01 - Get the elements from the operation.
		Expression thenExpression = expression.getThenExpression();
		Expression elseExpression = expression.getElseExpression();

		// 02 - Check each element.
		inspectNode(depth, dataFlow, thenExpression);
		inspectNode(depth, dataFlow, elseExpression);
	}

	/**
	 * 19
	 */
	protected void inspectDoStatement(int depth, DataFlow dataFlow, DoStatement statement) {
		inspectNode(depth, dataFlow, statement.getBody());
	}

	/**
	 * 21
	 */
	protected void inspectExpressionStatement(int depth, DataFlow dataFlow, ExpressionStatement expression) {
		inspectNode(depth, dataFlow, expression.getExpression());
	}

	/**
	 * 22
	 */
	protected void inspectFieldAccess(int depth, DataFlow dataFlow, FieldAccess expression) {
		inspectNode(depth, dataFlow, expression.getExpression());
	}

	/**
	 * 24
	 */
	protected void inspectForStatement(int depth, DataFlow dataFlow, ForStatement statement) {
		inspectNode(depth, dataFlow, statement.getBody());
	}

	/**
	 * 25
	 */
	protected void inspectIfStatement(int depth, DataFlow dataFlow, IfStatement statement) {
		inspectNode(depth, dataFlow, statement.getThenStatement());
		inspectNode(depth, dataFlow, statement.getElseStatement());
	}

	/**
	 * 27
	 */
	protected void inspectInfixExpression(int depth, DataFlow dataFlow, InfixExpression expression) {
		// 01 - Get the elements from the operation.
		Expression leftOperand = expression.getLeftOperand();
		Expression rightOperand = expression.getRightOperand();
		List<Expression> extendedOperands = BindingResolver.getParameters(expression);

		// 02 - Check each element.
		inspectNode(depth, dataFlow, leftOperand);
		inspectNode(depth, dataFlow, rightOperand);

		for (Expression extendedOperand : extendedOperands) {
			inspectNode(depth, dataFlow, extendedOperand);
		}
	}

	/**
	 * 32
	 */
	protected void inspectMethodInvocation(int depth, DataFlow dataFlow, MethodInvocation expression) {
		PluginLogger.logIfDebugging("inspectMethodInvocation not implemented.");
	}

	/**
	 * 36
	 */
	protected void inspectParenthesizedExpression(int depth, DataFlow dataFlow, ParenthesizedExpression expression) {
		inspectNode(depth, dataFlow, expression.getExpression());
	}

	/**
	 * 38
	 */
	protected void inspectPrefixExpression(int depth, DataFlow dataFlow, PrefixExpression expression) {
		// 01 - Get the elements from the operation.
		Expression operand = expression.getOperand();

		// 02 - Check each element.
		inspectNode(depth, dataFlow, operand);
	}

	/**
	 * 40
	 */
	protected void inspectQualifiedName(int depth, DataFlow dataFlow, QualifiedName expression) {
		inspectNode(depth, dataFlow, expression.getName());
	}

	/**
	 * 41
	 */
	protected void inspectReturnStatement(int depth, DataFlow dataFlow, ReturnStatement statement) {
		inspectNode(depth, dataFlow, statement.getExpression());
	}

	/**
	 * 42
	 */
	protected void inspectSimpleName(int depth, DataFlow dataFlow, SimpleName expression) {
		PluginLogger.logIfDebugging("inspectSimpleName not implemented.");
	}

	/**
	 * 50
	 */
	protected void inspectSwitchStatement(int depth, DataFlow dataFlow, SwitchStatement statement) {
		List<?> switchStatements = statement.statements();
		for (Object switchCases : switchStatements) {
			inspectNode(depth, dataFlow, (Statement) switchCases);
		}
	}

	/**
	 * 54
	 */
	protected void inspectTryStatement(int depth, DataFlow dataFlow, TryStatement statement) {
		inspectNode(depth, dataFlow, statement.getBody());

		List<?> listCatches = statement.catchClauses();
		for (Object catchClause : listCatches) {
			inspectNode(depth, dataFlow, ((CatchClause) catchClause).getBody());
		}

		inspectNode(depth, dataFlow, statement.getFinally());
	}

	/**
	 * 60
	 */
	protected void inspectVariableDeclarationStatement(int depth, DataFlow dataFlow,
			VariableDeclarationStatement statement) {
		PluginLogger.logIfDebugging("inspectVariableDeclarationStatement not implemented.");
	}

	/**
	 * 61
	 */
	protected void inspectWhileStatement(int depth, DataFlow dataFlow, WhileStatement statement) {
		inspectNode(depth, dataFlow, statement.getBody());
	}

	/**
	 * 13, 33, 34, 45
	 */
	protected void inspectLiteral(int depth, DataFlow dataFlow, Expression node) {
	}

}