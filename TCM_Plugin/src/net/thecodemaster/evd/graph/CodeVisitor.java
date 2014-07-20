package net.thecodemaster.evd.graph;

import java.util.Iterator;
import java.util.List;

import net.thecodemaster.evd.context.Context;
import net.thecodemaster.evd.graph.flow.DataFlow;
import net.thecodemaster.evd.graph.flow.Flow;
import net.thecodemaster.evd.logger.PluginLogger;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

public abstract class CodeVisitor {

	/**
	 * This object contains all the methods, variables and their interactions, on the project that is being analyzed.
	 */
	private CallGraph	callGraph;
	/**
	 * The current resource that is being analyzed.
	 */
	private IResource	currentResource;

	protected void setCallGraph(CallGraph callGraph) {
		this.callGraph = callGraph;
	}

	protected CallGraph getCallGraph() {
		return callGraph;
	}

	protected void setCurrentResource(IResource currentResource) {
		this.currentResource = currentResource;
	}

	protected IResource getCurrentResource() {
		return currentResource;
	}

	protected void inspectNode(Flow loopControl, Context context, DataFlow dataFlow, ASTNode node) {
		if (null == node) {
			return;
		}

		// 01 - Add the new element into the loopControl object.
		loopControl = addElementToLoopControl(loopControl, node);

		// 02 - To avoid infinitive loop, this check is necessary.
		if (hasLoop(loopControl)) {
			PluginLogger.logError("A loop was found: " + loopControl, null);
			return;
		}

		switch (node.getNodeType()) {
			case ASTNode.ARRAY_ACCESS: // 02
				inspectArrayAccess(loopControl, context, dataFlow, (ArrayAccess) node);
				break;
			case ASTNode.ARRAY_CREATION: // 03
				inspectArrayCreation(loopControl, context, dataFlow, (ArrayCreation) node);
				break;
			case ASTNode.ARRAY_INITIALIZER: // 04
				inspectArrayInitializer(loopControl, context, dataFlow, (ArrayInitializer) node);
				break;
			case ASTNode.ASSIGNMENT: // 07
				inspectAssignment(loopControl, context, dataFlow, (Assignment) node);
				break;
			case ASTNode.BLOCK: // 08
				inspectBlock(loopControl, context, dataFlow, (Block) node);
				break;
			case ASTNode.BOOLEAN_LITERAL: // 09
				inspectBooleanLiteral(loopControl, context, dataFlow, (BooleanLiteral) node);
				break;
			case ASTNode.BREAK_STATEMENT: // 10
				inspectBreakStatement(loopControl, context, dataFlow, (BreakStatement) node);
				break;
			case ASTNode.CAST_EXPRESSION: // 11
				inspectCastExpression(loopControl, context, dataFlow, (CastExpression) node);
				break;
			case ASTNode.CATCH_CLAUSE: // 12
				inspectCatchClause(loopControl, context, dataFlow, (CatchClause) node);
				break;
			case ASTNode.CHARACTER_LITERAL: // 13
				inspectCharacterLiteral(loopControl, context, dataFlow, (CharacterLiteral) node);
				break;
			case ASTNode.CLASS_INSTANCE_CREATION: // 14
				inspectClassInstanceCreation(loopControl, context, dataFlow, (ClassInstanceCreation) node);
				break;
			case ASTNode.CONDITIONAL_EXPRESSION: // 16
				inspectConditionExpression(loopControl, context, dataFlow, (ConditionalExpression) node);
				break;
			case ASTNode.CONSTRUCTOR_INVOCATION: // 17
				inspectConstructorInvocation(loopControl, context, dataFlow, (ConstructorInvocation) node);
				break;
			case ASTNode.CONTINUE_STATEMENT: // 18
				inspectContinueStatement(loopControl, context, dataFlow, (ContinueStatement) node);
				break;
			case ASTNode.DO_STATEMENT: // 19
				inspectDoStatement(loopControl, context, dataFlow, (DoStatement) node);
				break;
			case ASTNode.EMPTY_STATEMENT: // 20
				inspectEmptyStatement(loopControl, context, dataFlow, (EmptyStatement) node);
				break;
			case ASTNode.EXPRESSION_STATEMENT: // 21
				inspectExpressionStatement(loopControl, context, dataFlow, (ExpressionStatement) node);
				break;
			case ASTNode.FIELD_ACCESS: // 22
				inspectFieldAccess(loopControl, context, dataFlow, (FieldAccess) node);
				break;
			case ASTNode.FOR_STATEMENT: // 24
				inspectForStatement(loopControl, context, dataFlow, (ForStatement) node);
				break;
			case ASTNode.IF_STATEMENT: // 25
				inspectIfStatement(loopControl, context, dataFlow, (IfStatement) node);
				break;
			case ASTNode.INFIX_EXPRESSION: // 27
				inspectInfixExpression(loopControl, context, dataFlow, (InfixExpression) node);
				break;
			case ASTNode.METHOD_INVOCATION: // 32
				inspectMethodInvocation(loopControl, context, dataFlow, (MethodInvocation) node);
				break;
			case ASTNode.NULL_LITERAL: // 33
				inspectNullLiteral(loopControl, context, dataFlow, (NullLiteral) node);
				break;
			case ASTNode.NUMBER_LITERAL: // 34
				inspectNumberLiteral(loopControl, context, dataFlow, (NumberLiteral) node);
				break;
			case ASTNode.PARENTHESIZED_EXPRESSION: // 36
				inspectParenthesizedExpression(loopControl, context, dataFlow, (ParenthesizedExpression) node);
				break;
			case ASTNode.POSTFIX_EXPRESSION: // 37
				inspectPostfixExpression(loopControl, context, dataFlow, (PostfixExpression) node);
				break;
			case ASTNode.PREFIX_EXPRESSION: // 38
				inspectPrefixExpression(loopControl, context, dataFlow, (PrefixExpression) node);
				break;
			case ASTNode.QUALIFIED_NAME: // 40
				inspectQualifiedName(loopControl, context, dataFlow, (QualifiedName) node);
				break;
			case ASTNode.RETURN_STATEMENT: // 41
				inspectReturnStatement(loopControl, context, dataFlow, (ReturnStatement) node);
				break;
			case ASTNode.SIMPLE_NAME: // 42
				inspectSimpleName(loopControl, context, dataFlow, (SimpleName) node);
				break;
			case ASTNode.STRING_LITERAL: // 45
				inspectStringLiteral(loopControl, context, dataFlow, (StringLiteral) node);
				break;
			case ASTNode.SUPER_CONSTRUCTOR_INVOCATION: // 46
				inspectSuperConstructorInvocation(loopControl, context, dataFlow, (SuperConstructorInvocation) node);
				break;
			case ASTNode.SUPER_FIELD_ACCESS: // 47
				inspectSuperFieldAccess(loopControl, context, dataFlow, (SuperFieldAccess) node);
				break;
			case ASTNode.SUPER_METHOD_INVOCATION: // 48
				inspectSuperMethodInvocation(loopControl, context, dataFlow, (SuperMethodInvocation) node);
				break;
			case ASTNode.SWITCH_CASE: // 49
				inspectSwitchCase(loopControl, context, dataFlow, (SwitchCase) node);
				break;
			case ASTNode.SWITCH_STATEMENT: // 50
				inspectSwitchStatement(loopControl, context, dataFlow, (SwitchStatement) node);
				break;
			case ASTNode.SYNCHRONIZED_STATEMENT: // 51
				inspectSynchronizedStatement(loopControl, context, dataFlow, (SynchronizedStatement) node);
				break;
			case ASTNode.THIS_EXPRESSION: // 52
				inspectThisExpression(loopControl, context, dataFlow, (ThisExpression) node);
				break;
			case ASTNode.THROW_STATEMENT: // 53
				inspectThrowStatement(loopControl, context, dataFlow, (ThrowStatement) node);
				break;
			case ASTNode.TRY_STATEMENT: // 54
				inspectTryStatement(loopControl, context, dataFlow, (TryStatement) node);
				break;
			case ASTNode.TYPE_LITERAL: // 57
				inspectTypeLiteral(loopControl, context, dataFlow, (TypeLiteral) node);
				break;
			case ASTNode.VARIABLE_DECLARATION_STATEMENT: // 60
				inspectVariableDeclarationStatement(loopControl, context, dataFlow, (VariableDeclarationStatement) node);
				break;
			case ASTNode.WHILE_STATEMENT: // 61
				inspectWhileStatement(loopControl, context, dataFlow, (WhileStatement) node);
				break;
			case ASTNode.INSTANCEOF_EXPRESSION: // 62
				inspectInstanceofExpression(loopControl, context, dataFlow, (InstanceofExpression) node);
				break;
			case ASTNode.ENHANCED_FOR_STATEMENT: // 70
				inspectEnhancedForStatement(loopControl, context, dataFlow, (EnhancedForStatement) node);
				break;
			default:
				PluginLogger.logError("inspectStatement Default Node Type: " + node.getNodeType() + " - " + node, null);
		}
	}

	/**
	 * 02 int a = <b>array[1]<b/>
	 */
	protected void inspectArrayAccess(Flow loopControl, Context context, DataFlow dataFlow, ArrayAccess expression) {
		inspectNode(loopControl, context, dataFlow, expression.getArray());
	}

	/**
	 * 03 int[] array = new int[] <b>{1, 2};<b/>
	 */
	protected void inspectArrayCreation(Flow loopControl, Context context, DataFlow dataFlow, ArrayCreation expression) {
		iterateOverParameters(loopControl, context, dataFlow, expression.getInitializer());
	}

	/**
	 * 04 int[] array = <b>{1, 2};<b/>
	 */
	protected void inspectArrayInitializer(Flow loopControl, Context context, DataFlow dataFlow,
			ArrayInitializer expression) {
		iterateOverParameters(loopControl, context, dataFlow, expression);
	}

	/**
	 * 07 b = a;
	 */
	protected void inspectAssignment(Flow loopControl, Context context, DataFlow dataFlow, Assignment expression) {
		Expression leftHandSide = expression.getLeftHandSide();
		Expression rightHandSide = expression.getRightHandSide();

		inspectNode(loopControl, context, dataFlow.addNodeToPath(leftHandSide), leftHandSide);
		inspectNode(loopControl, context, dataFlow.addNodeToPath(rightHandSide), rightHandSide);
	}

	/**
	 * 08
	 */
	protected void inspectBlock(Flow loopControl, Context context, DataFlow dataFlow, Block block) {
		if (null != block) {
			for (Object object : block.statements()) {
				inspectNode(loopControl, context, dataFlow.addNodeToPath(null), (Statement) object);
			}
		}
	}

	/**
	 * 09
	 */
	protected void inspectBooleanLiteral(Flow loopControl, Context context, DataFlow dataFlow, BooleanLiteral node) {
		// Nothing to do.
	}

	/**
	 * 10
	 */
	protected void inspectBreakStatement(Flow loopControl, Context context, DataFlow dataFlow, BreakStatement statement) {
		// Nothing to do.
	}

	/**
	 * 11 Person a = <b>(Person) object;<b/>
	 */
	protected void inspectCastExpression(Flow loopControl, Context context, DataFlow dataFlow, CastExpression expression) {
		inspectNode(loopControl, context, dataFlow, expression.getExpression());
	}

	/**
	 * 12
	 */
	protected void inspectCatchClause(Flow loopControl, Context context, DataFlow dataFlow, CatchClause expression) {
		inspectNode(loopControl, context, dataFlow, expression.getBody());
	}

	/**
	 * 13
	 */
	protected void inspectCharacterLiteral(Flow loopControl, Context context, DataFlow dataFlow, CharacterLiteral node) {
		// Nothing to do.
	}

	/**
	 * 14 new Person(...);
	 */
	protected void inspectClassInstanceCreation(Flow loopControl, Context context, DataFlow dataFlow,
			ClassInstanceCreation node) {
		inspectInvocation(loopControl, context, dataFlow, node);
	}

	/**
	 * 16 (null != a) ? a : b;
	 */
	protected void inspectConditionExpression(Flow loopControl, Context context, DataFlow dataFlow,
			ConditionalExpression expression) {
		Expression conditionalExpression = expression.getExpression();
		Expression thenExpression = expression.getThenExpression();
		Expression elseExpression = expression.getElseExpression();

		inspectNode(loopControl, context, dataFlow.addNodeToPath(null), conditionalExpression);
		inspectNode(loopControl, context, dataFlow.addNodeToPath(thenExpression), thenExpression);
		inspectNode(loopControl, context, dataFlow.addNodeToPath(elseExpression), elseExpression);
	}

	/**
	 * 17 <br/>
	 * this(name2,10);
	 */
	protected void inspectConstructorInvocation(Flow loopControl, Context context, DataFlow dataFlow,
			ConstructorInvocation invocation) {
		// 01 - Inspect the method invocation.
		inspectInvocation(loopControl, context, dataFlow, invocation);
	}

	/**
	 * 18
	 */
	protected void inspectContinueStatement(Flow loopControl, Context context, DataFlow dataFlow,
			ContinueStatement statement) {
		// Nothing to do.
	}

	/**
	 * 19
	 */
	protected void inspectDoStatement(Flow loopControl, Context context, DataFlow dataFlow, DoStatement statement) {
		inspectNode(loopControl, context, dataFlow, statement.getBody());
		inspectNode(loopControl, context, dataFlow.addNodeToPath(null), statement.getExpression());
	}

	/**
	 * 20
	 */
	protected void inspectEmptyStatement(Flow loopControl, Context context, DataFlow dataFlow, EmptyStatement expression) {
		// Nothing to do.
	}

	/**
	 * 21
	 */
	protected void inspectExpressionStatement(Flow loopControl, Context context, DataFlow dataFlow,
			ExpressionStatement expression) {
		inspectNode(loopControl, context, dataFlow, expression.getExpression());
	}

	/**
	 * 22
	 */
	protected void inspectFieldAccess(Flow loopControl, Context context, DataFlow dataFlow, FieldAccess expression) {
		inspectNode(loopControl, context, dataFlow, expression.getName());
	}

	/**
	 * 24
	 */
	protected void inspectForStatement(Flow loopControl, Context context, DataFlow dataFlow, ForStatement statement) {
		inspectNode(loopControl, context, dataFlow, statement.getBody());
	}

	/**
	 * 25
	 */
	protected void inspectIfStatement(Flow loopControl, Context context, DataFlow dataFlow, IfStatement statement) {
		inspectNode(loopControl, context, dataFlow.addNodeToPath(null), statement.getExpression());
		inspectNode(loopControl, context, dataFlow.addNodeToPath(null), statement.getThenStatement());
		inspectNode(loopControl, context, dataFlow.addNodeToPath(null), statement.getElseStatement());
	}

	/**
	 * 27
	 */
	protected void inspectInfixExpression(Flow loopControl, Context context, DataFlow dataFlow, InfixExpression expression) {
		Expression leftOperand = expression.getLeftOperand();
		Expression rightOperand = expression.getRightOperand();

		inspectNode(loopControl, context, dataFlow.addNodeToPath(leftOperand), leftOperand);
		inspectNode(loopControl, context, dataFlow.addNodeToPath(rightOperand), rightOperand);

		iterateOverParameters(loopControl, context, dataFlow, expression);
	}

	/**
	 * 32
	 */
	protected void inspectMethodInvocation(Flow loopControl, Context context, DataFlow dataFlow,
			MethodInvocation invocation) {
		// 01 - Inspect the method invocation.
		inspectInvocation(loopControl, context, dataFlow, invocation);
	}

	/**
	 * 33
	 */
	protected void inspectNullLiteral(Flow loopControl, Context context, DataFlow dataFlow, NullLiteral node) {
		// Nothing to do.
	}

	/**
	 * 34
	 */
	protected void inspectNumberLiteral(Flow loopControl, Context context, DataFlow dataFlow, NumberLiteral node) {
		// Nothing to do.
	}

	/**
	 * 36
	 */
	protected void inspectParenthesizedExpression(Flow loopControl, Context context, DataFlow dataFlow,
			ParenthesizedExpression expression) {
		inspectNode(loopControl, context, dataFlow, expression.getExpression());
	}

	/**
	 * 37 i++
	 */
	protected void inspectPostfixExpression(Flow loopControl, Context context, DataFlow dataFlow,
			PostfixExpression expression) {
		Expression operand = expression.getOperand();

		inspectNode(loopControl, context, dataFlow.addNodeToPath(operand), operand);
	}

	/**
	 * 38 ++i !value
	 */
	protected void inspectPrefixExpression(Flow loopControl, Context context, DataFlow dataFlow,
			PrefixExpression expression) {
		Expression operand = expression.getOperand();

		inspectNode(loopControl, context, dataFlow.addNodeToPath(operand), operand);
	}

	/**
	 * 40
	 */
	protected void inspectQualifiedName(Flow loopControl, Context context, DataFlow dataFlow, QualifiedName expression) {
		inspectNode(loopControl, context, dataFlow, expression.getName());
	}

	/**
	 * 41 return ...
	 */
	protected void inspectReturnStatement(Flow loopControl, Context context, DataFlow dataFlow, ReturnStatement statement) {
		inspectNode(loopControl, context, dataFlow, statement.getExpression());
	}

	/**
	 * 42 variable
	 */
	protected void inspectSimpleName(Flow loopControl, Context context, DataFlow dataFlow, SimpleName expression) {
	}

	/**
	 * 45 ""
	 */
	protected void inspectStringLiteral(Flow loopControl, Context context, DataFlow dataFlow, StringLiteral node) {
		// Nothing to do.
	}

	/**
	 * 46 super(...);
	 */
	protected void inspectSuperConstructorInvocation(Flow loopControl, Context context, DataFlow dataFlow,
			SuperConstructorInvocation invocation) {
		// 01 - Inspect the method invocation.
		inspectInvocation(loopControl, context, dataFlow, invocation);
	}

	/**
	 * 47 super.variable;
	 */
	protected void inspectSuperFieldAccess(Flow loopControl, Context context, DataFlow dataFlow,
			SuperFieldAccess expression) {
		inspectNode(loopControl, context, dataFlow, expression.getName());
	}

	/**
	 * 48 super.methodName(...);
	 */
	protected void inspectSuperMethodInvocation(Flow loopControl, Context context, DataFlow dataFlow,
			SuperMethodInvocation invocation) {
		// 01 - Inspect the method invocation.
		inspectInvocation(loopControl, context, dataFlow, invocation);
	}

	/**
	 * 49
	 */
	protected void inspectSwitchCase(Flow loopControl, Context context, DataFlow dataFlow, SwitchCase statement) {
		// Nothing to do.
	}

	/**
	 * 50
	 */
	protected void inspectSwitchStatement(Flow loopControl, Context context, DataFlow dataFlow, SwitchStatement statement) {
		List<?> switchStatements = statement.statements();
		for (Object switchCases : switchStatements) {
			inspectNode(loopControl, context, dataFlow.addNodeToPath(null), (Statement) switchCases);
		}
	}

	/**
	 * 51
	 */
	protected void inspectSynchronizedStatement(Flow loopControl, Context context, DataFlow dataFlow,
			SynchronizedStatement statement) {
		inspectNode(loopControl, context, dataFlow, statement.getBody());
	}

	/**
	 * 52 this.
	 */
	protected void inspectThisExpression(Flow loopControl, Context context, DataFlow dataFlow, ThisExpression expression) {
		inspectNode(loopControl, context, dataFlow, expression.getQualifier());
	}

	/**
	 * 53 throw new ...
	 */
	protected void inspectThrowStatement(Flow loopControl, Context context, DataFlow dataFlow, ThrowStatement statement) {
		inspectNode(loopControl, context, dataFlow, statement.getExpression());
	}

	/**
	 * 54
	 */
	protected void inspectTryStatement(Flow loopControl, Context context, DataFlow dataFlow, TryStatement statement) {
		inspectNode(loopControl, context, dataFlow.addNodeToPath(null), statement.getBody());

		List<?> listCatches = statement.catchClauses();
		for (Object catchClause : listCatches) {
			inspectNode(loopControl, context, dataFlow.addNodeToPath(null), ((CatchClause) catchClause).getBody());
		}

		inspectNode(loopControl, context, dataFlow.addNodeToPath(null), statement.getFinally());
	}

	/**
	 * 57
	 */
	protected void inspectTypeLiteral(Flow loopControl, Context context, DataFlow dataFlow, TypeLiteral expression) {
		// Nothing to do.
	}

	/**
	 * 60
	 */
	protected void inspectVariableDeclarationStatement(Flow loopControl, Context context, DataFlow dataFlow,
			VariableDeclarationStatement statement) {
		for (Iterator<?> iter = statement.fragments().iterator(); iter.hasNext();) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) iter.next();

			// 01 - Inspect the Initializer.
			inspectNode(loopControl, context, new DataFlow(fragment.getName()), fragment.getInitializer());
		}
	}

	/**
	 * 61
	 */
	protected void inspectWhileStatement(Flow loopControl, Context context, DataFlow dataFlow, WhileStatement statement) {
		inspectNode(loopControl, context, dataFlow.addNodeToPath(null), statement.getExpression());
		inspectNode(loopControl, context, dataFlow, statement.getBody());
	}

	/**
	 * 62
	 */
	protected void inspectInstanceofExpression(Flow loopControl, Context context, DataFlow dataFlow,
			InstanceofExpression expression) {
		// Nothing to do.
	}

	/**
	 * 70
	 */
	protected void inspectEnhancedForStatement(Flow loopControl, Context context, DataFlow dataFlow,
			EnhancedForStatement statement) {
		inspectNode(loopControl, context, dataFlow, statement.getBody());
	}

	/**
	 * Helper Methods
	 */

	protected Flow addElementToLoopControl(Flow loopControl, ASTNode node) {
		switch (node.getNodeType()) {
			case ASTNode.CLASS_INSTANCE_CREATION: // 14
			case ASTNode.CONSTRUCTOR_INVOCATION: // 17
			case ASTNode.METHOD_INVOCATION: // 32
			case ASTNode.SUPER_CONSTRUCTOR_INVOCATION: // 46
			case ASTNode.SUPER_METHOD_INVOCATION: // 48
				loopControl = loopControl.addChild(node);
		}

		return loopControl;
	}

	protected boolean hasLoop(Flow loopControl) {
		return loopControl.hasLoop();
	}

	/**
	 * 03, 04, 27, 32
	 */
	protected void iterateOverParameters(Flow loopControl, Context context, DataFlow dataFlow, ASTNode expression) {
		List<Expression> parameters = BindingResolver.getParameters(expression);
		for (Expression parameter : parameters) {
			inspectNode(loopControl, context, dataFlow.addNodeToPath(parameter), parameter);
		}
	}

	/**
	 * 14, 17, 32, 46, 48
	 */
	protected void inspectInvocation(Flow loopControl, Context context, DataFlow dataFlow, ASTNode invocation) {
	}

}
