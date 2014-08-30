package net.thecodemaster.esvd.graph;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.thecodemaster.esvd.constant.Constant;
import net.thecodemaster.esvd.context.Context;
import net.thecodemaster.esvd.finder.InstanceFinder;
import net.thecodemaster.esvd.finder.ReferenceFinder;
import net.thecodemaster.esvd.graph.flow.DataFlow;
import net.thecodemaster.esvd.graph.flow.Flow;
import net.thecodemaster.esvd.helper.Creator;
import net.thecodemaster.esvd.helper.HelperCodeAnalyzer;
import net.thecodemaster.esvd.logger.PluginLogger;
import net.thecodemaster.esvd.ui.enumeration.EnumTypeDeclaration;
import net.thecodemaster.esvd.ui.l10n.Message;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.AssertStatement;
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
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
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
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
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
			// PluginLogger.logIfDebugging("A loop was found: " + loopControl);
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
			case ASTNode.ASSERT_STATEMENT: // 06
				inspectAssertStatement(loopControl, context, dataFlow, (AssertStatement) node);
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
	 * 06 assert(...);
	 */
	protected void inspectAssertStatement(Flow loopControl, Context context, DataFlow dataFlow, AssertStatement expression) {
		// Nothing to do.
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
		for (Object objCatchClause : listCatches) {
			// 01 - Type cast to be able to use the right methods.
			CatchClause catchClause = (CatchClause) objCatchClause;

			// 02 - The exception variable declaration of this catch clause.
			SingleVariableDeclaration variable = catchClause.getException();

			// 03 - Add the variable of the catch statement to the current context.
			SimpleName name = variable.getName();
			VariableBinding variableBinding = getCallGraph().addVariable(context, name, variable.getInitializer());

			// 04 - Make it vulnerable, so if the developer uses it to print to send it back to the browser,
			// the verifier will catch it.
			DataFlow newDataFlow = new DataFlow(name);
			newDataFlow.hasVulnerablePath(Constant.Vulnerability.INFORMATION_LEAKAGE, getInformationLeakageMessage(name));
			HelperCodeAnalyzer.updateVariableBinding(variableBinding, newDataFlow);

			// 05 - Inspect the body of the catch clause.
			inspectNode(loopControl, context, dataFlow.addNodeToPath(null), catchClause.getBody());
		}

		inspectNode(loopControl, context, dataFlow.addNodeToPath(null), statement.getFinally());
	}

	private String getInformationLeakageMessage(SimpleName name) {
		return String.format(Message.VerifierSecurityVulnerability.INFORMATION_LEAKAGE_MESSAGE, name.getIdentifier());
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
		MethodDeclaration methodDeclaration = getMethodDeclaration(loopControl, context, invocation);

		inspectMethodInvocationWithOrWithOutSourceCode(loopControl, context, dataFlow, invocation, methodDeclaration);
	}

	/**
	 * 32
	 */
	protected void inspectMethodInvocationWithOrWithOutSourceCode(Flow loopControl, Context context, DataFlow dataFlow,
			ASTNode methodInvocation, MethodDeclaration methodDeclaration) {
		if (null != methodDeclaration) {
			// We have the source code.
			inspectMethodWithSourceCode(loopControl, context, dataFlow, methodInvocation, methodDeclaration);
		} else {
			inspectMethodWithOutSourceCode(loopControl, context, dataFlow, methodInvocation);
		}
	}

	protected void inspectMethodWithSourceCode(Flow loopControl, Context context, DataFlow dataFlow,
			ASTNode methodInvocation, MethodDeclaration methodDeclaration) {
		inspectNode(loopControl, context, dataFlow, methodDeclaration.getBody());
	}

	protected void inspectMethodWithOutSourceCode(Flow loopControl, Context context, DataFlow dataFlow,
			ASTNode methodInvocation) {
		iterateOverParameters(loopControl, context, dataFlow, methodInvocation);
	}

	protected MethodDeclaration getMethodDeclaration(Flow loopControl, Context context, ASTNode invocation) {
		switch (invocation.getNodeType()) {
			case ASTNode.CLASS_INSTANCE_CREATION: // 14
				return getClassInstanceCreationDeclaration((ClassInstanceCreation) invocation, EnumTypeDeclaration.CONSTRUCTOR);

			case ASTNode.CONSTRUCTOR_INVOCATION: // 17
				return getConstructorInvocationDeclaration(invocation, EnumTypeDeclaration.CONSTRUCTOR);

			case ASTNode.METHOD_INVOCATION: // 32
				return getMethodInvocationDeclaration(loopControl, context, (MethodInvocation) invocation,
						EnumTypeDeclaration.METHOD);

			case ASTNode.SUPER_CONSTRUCTOR_INVOCATION: // 46
				return getSuperInvocationDeclaration(invocation, EnumTypeDeclaration.CONSTRUCTOR);

			case ASTNode.SUPER_METHOD_INVOCATION: // 48
				return getSuperInvocationDeclaration(invocation, EnumTypeDeclaration.METHOD);
			default:
				PluginLogger.logError("getMethodDeclaration Default Node Type: " + invocation.getNodeType() + " - "
						+ invocation, null);
				return null;
		}
	}

	/**
	 * 14
	 */
	private MethodDeclaration getClassInstanceCreationDeclaration(ClassInstanceCreation invocation,
			EnumTypeDeclaration typeDeclaration) {
		// 01 - new Class(...);
		// 01.1 - It can be an InnerClass or a normal class.
		// 01.2 - With or without constructor.
		// 01.3 - The name of the Class is the name of the file + ".java".
		// 01.4 - It can be in the same package or in another one.

		// TODO - It can be an InnerClass or a normal class.
		// Try to find if the current resource has any inner classes.

		// 01 - Get the name/type of the class.
		// 02 - Get the resource of the class.
		IResource resource = getResource(invocation.getType());

		// 03 - Search the source code of the constructor that was invoked.
		return getMethodDeclaration(resource, invocation, typeDeclaration);
	}

	/**
	 * 17
	 */
	private MethodDeclaration getConstructorInvocationDeclaration(ASTNode invocation, EnumTypeDeclaration typeDeclaration) {
		// 01 - this(...);
		// 01.1 - It has to be inside the current resource.

		// 01 - Get the resource of this constructor.
		IResource resource = getResource(invocation);

		// 02 - Search the source code of the constructor that was invoked.
		return getMethodDeclaration(resource, invocation, typeDeclaration);
	}

	/**
	 * 32
	 */
	private MethodDeclaration getMethodInvocationDeclaration(Flow loopControl, Context context,
			MethodInvocation invocation, EnumTypeDeclaration typeDeclaration) {
		// 01.1 - It can be inside the current resource.
		// 01.2 - It can be inside one of the super classes.
		// 01.3 - It can be in the same package or in another one.

		// 01 - Get the binding of this invocation;
		IBinding binding = BindingResolver.resolveBinding(invocation);
		if (null != binding) {
			// 02 - Get the name of the object (instance or static) that is invoking this method.
			Expression invokerName = BindingResolver.getNameIfItIsAnObject(invocation);

			if ((Modifier.isStatic(binding.getModifiers())) && (null != invokerName)) {
				// If this invocation is to a static method and it has an invoker.
				// 03.1 - Static.method(...);
				// 03.2 - package.name.Static.method(...);

				// 03 - Resource that has the source code of this invocation.
				IResource resource = getResource((Name) invokerName);

				// 04 - Search the source code of the method that was invoked.
				return getMethodDeclaration(resource, invocation, typeDeclaration);
			} else {
				IResource resource = null;
				Type className = null;
				if (null != invokerName) {
					// 03.1 - p.method(...); Person p = new Employee();
					// 03.2 - Interface.method(...); List/ArrayList/LinkedList.
					// These are the most complicated case.
					Expression realReference = findRealReference(loopControl, context, invokerName);

					if (null != realReference) {
						className = ((ClassInstanceCreation) realReference).getType();
						// 04 - Get the resource of this real reference.
						resource = getResource(className);

						typeDeclaration = EnumTypeDeclaration.METHOD_INHERITANCE;
					}
				} else {
					// 03.1 - method(...);
					// 03.2 - staticMethod(...);
					// 03.3 - this.method(...);
					// 04 - Get the resource of this method.
					resource = getResource(invocation);
				}
				return getMethodDeclarationFromCurrentClassOrSuperClasses(resource, invocation, typeDeclaration, className);
			}
		}

		return null;
	}

	/**
	 * 46, 48
	 */
	private MethodDeclaration getSuperInvocationDeclaration(ASTNode invocation, EnumTypeDeclaration typeDeclaration) {
		// 01 - super(...);
		// 01.1 - It has to be inside one of the super classes.
		// 02 - super.method(...);
		// 02.1 - It has to be inside one of the super classes.

		return getMethodDeclarationFromSuperClasses(invocation, typeDeclaration, null);
	}

	private IResource getResource(ASTNode invocation) {
		return BindingResolver.getResource(invocation);
	}

	private IResource getResource(Type className) {
		return BindingResolver.getResource(getCallGraph(), className);
	}

	private IResource getResource(Name name) {
		return BindingResolver.getResource(getCallGraph(), name);
	}

	private MethodDeclaration getMethodDeclarationFromCurrentClassOrSuperClasses(IResource resource, ASTNode invocation,
			EnumTypeDeclaration typeDeclaration, Type className) {
		// 02 - Try to find the source code into the current resource.
		MethodDeclaration methodDeclaration = getMethodDeclaration(resource, invocation, typeDeclaration);
		if (null != methodDeclaration) {
			return methodDeclaration;
		}

		// 03 - Try to find the source code into the super classes.
		return getMethodDeclarationFromSuperClasses(invocation, typeDeclaration, className);
	}

	private MethodDeclaration getMethodDeclarationFromSuperClasses(ASTNode invocation,
			EnumTypeDeclaration typeDeclaration, Type className) {
		List<IResource> resources = Creator.newList();

		if (null == className) {
			// 01 - Get the list of resources from the super class of this invocation.
			resources = getListOfResourcesFromSuperClasses(invocation);
		} else {
			resources = getListOfResourcesFromSuperClasses(className);
		}

		return getMethodDeclarationFromResources(invocation, typeDeclaration, resources);
	}

	private List<IResource> getListOfResourcesFromSuperClasses(ASTNode node) {
		// 01 - Get the type declaration based on the invocation node.
		TypeDeclaration typeDeclaration = BindingResolver.getTypeDeclaration(node);

		if (null != typeDeclaration) {
			// 02 - Get the name/type of the super class.
			Type superClassName = typeDeclaration.getSuperclassType();

			return getListOfResourcesFromSuperClasses(superClassName);
		}

		// 03 - If the type Declaration is null, we return an empty list.
		List<IResource> emptyList = Creator.newList();
		return emptyList;
	}

	private MethodDeclaration getMethodDeclarationFromResources(ASTNode invocation, EnumTypeDeclaration typeDeclaration,
			List<IResource> resources) {
		for (IResource resource : resources) {
			// 02 - Search the source code of the constructor that was invoked.
			MethodDeclaration methodDeclaration = getMethodDeclaration(resource, invocation, typeDeclaration);
			if (null != methodDeclaration) {
				return methodDeclaration;
			}
		}

		// The source code was not found.
		return null;

	}

	private List<IResource> getListOfResourcesFromSuperClasses(Type superClassName) {
		// 01 - Create the list that will contain the super classes.
		List<IResource> resources = Creator.newList();

		while (null != superClassName) {
			// 05 - Get the resource of the class.
			IResource resource = getResource(superClassName);

			// 06 - If the resource of the super class was not found,
			// e.g Library that we do not have the source code, there is nothing we can do.
			if (null == resource) {
				break;
			}

			// 07 - If the resource is not null and it is not already in the list. We add it to the list.
			if (!resources.contains(resource)) {
				resources.add(resource);
			}

			// 08 - Get the methods from this resource.
			superClassName = getCallGraph().getSuperClass(resource);
		}

		// 09 - Return the list with the resources of all the super classes.
		return resources;
	}

	private MethodDeclaration getMethodDeclaration(IResource resource, ASTNode invocation,
			EnumTypeDeclaration typeDeclaration) {
		if (null != resource) {
			// 01 - Get the list of methods in the current resource and its invocations.
			Map<MethodDeclaration, List<ASTNode>> methods = getCallGraph().getMethods(resource);

			if (EnumTypeDeclaration.CONSTRUCTOR.equals(typeDeclaration)) {
				// 02 - Iterate through the list to verify if we have the implementation of this method in our list.
				for (MethodDeclaration methodDeclaration : methods.keySet()) {
					// 03 - Verify if these methods have the same parameters.
					if ((methodDeclaration.isConstructor())
							&& (BindingResolver.haveSameParameters(methodDeclaration, invocation))) {
						return methodDeclaration;
					}
				}
			} else if (EnumTypeDeclaration.METHOD.equals(typeDeclaration)) {
				// 02 - Iterate through the list to verify if we have the implementation of this method in our list.
				for (MethodDeclaration methodDeclaration : methods.keySet()) {
					// 03 - Verify if these methods are the same.
					if (BindingResolver.areMethodsEqual(methodDeclaration, invocation)) {
						return methodDeclaration;
					}
				}
			} else if (EnumTypeDeclaration.METHOD_INHERITANCE.equals(typeDeclaration)) {
				// 02 - Iterate through the list to verify if we have the implementation of this method in our list.
				for (MethodDeclaration methodDeclaration : methods.keySet()) {
					// 03 - Verify if these methods have the same name and parameters.
					if (BindingResolver.haveSameNameAndParameters(methodDeclaration, invocation)) {
						return methodDeclaration;
					}
				}
			}
		}

		return null;
	}

	/**
	 * This first implementation will return the first reference that is found.<br/>
	 * FIXME - There are complex cases that more that one type of reference can be returned. <br/>
	 * Case 01: Animal a = new Animal(); <br/>
	 * Case 02: Animal a = new Person();<br/>
	 * Case 03: Animal a = new Employee();<br/>
	 * Case 04: Animal a = getObject(); <br/>
	 * <br/>
	 * getObject() { if (return new Employee()) else (return new Person()); }<br/>
	 * a.methodToInvoke(); <br/>
	 * What is the method to inspect ?
	 */
	private Expression findRealReference(Flow loopControl, Context context, Expression invokerName) {
		// 01 - Get the last reference of this object.
		VariableBinding variableBinding = getCallGraph().getVariableBinding(context, invokerName);

		if (null != variableBinding) {
			// 02 - Try to find where this variable was created.
			ReferenceFinder finder = new ReferenceFinder(getCallGraph(), getCurrentResource());

			// 03 - Return the real reference of this object.
			return finder.getReference(loopControl, context, variableBinding.getInitializer());
		}

		return null;
	}

	/**
	 * This first implementation will return the first reference that is found.<br/>
	 * FIXME - The instance must exist, if it does not, it is probably an assignment or syntax error. <br/>
	 * Case 01: <br/>
	 * Animal a1 = new Animal() <br/>
	 * Animal a2 = a1 <br/>
	 * a2.method(); <br/>
	 * <br/>
	 */
	protected Expression findRealInstance(Flow loopControl, Context context, Expression instance) {
		Expression instanceReturn = null;
		// 01 - Check if this instance has a context.
		Context instanceContext = getCallGraph().getInstanceContext(context, instance);

		// 02 - If the context is equal it means it does not exist.
		if (instanceContext.equals(context)) {
			// 03 - Get the last reference of this object.
			VariableBinding variableBinding = getCallGraph().getVariableBinding(context, instance);

			if (null != variableBinding) {
				// 03 - Try to find where this variable was created.
				InstanceFinder finder = new InstanceFinder(getCallGraph(), getCurrentResource());

				// 04 - Return the real reference of this object.
				instanceReturn = finder.getReference(loopControl, context, variableBinding.getInitializer());
			}
		}

		// 05 - Return the new or the same(old) instance.
		return (null != instanceReturn) ? instanceReturn : instance;
	}

}
