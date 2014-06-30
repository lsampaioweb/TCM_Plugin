package net.thecodemaster.evd.graph;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.context.Context;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.ui.enumeration.EnumTypeDeclaration;

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
import org.eclipse.jdt.core.dom.MethodDeclaration;
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

	protected void inspectNode(int depth, Context context, DataFlow dataFlow, ASTNode node) {
		if (null == node) {
			return;
		}

		// 01 - To avoid infinitive loop, this check is necessary.
		if (hasReachedMaximumDepth(depth++)) {
			PluginLogger.logError("hasReachedMaximumDepth: " + " - " + node + " - " + depth, null);
			return;
		}

		switch (node.getNodeType()) {
			case ASTNode.ARRAY_ACCESS: // 02
				inspectArrayAccess(depth, context, dataFlow, (ArrayAccess) node);
				break;
			case ASTNode.ARRAY_CREATION: // 03
				inspectArrayCreation(depth, context, dataFlow, (ArrayCreation) node);
				break;
			case ASTNode.ARRAY_INITIALIZER: // 04
				inspectArrayInitializer(depth, context, dataFlow, (ArrayInitializer) node);
				break;
			case ASTNode.ASSIGNMENT: // 07
				inspectAssignment(depth, context, dataFlow, (Assignment) node);
				break;
			case ASTNode.BLOCK: // 08
				inspectBlock(depth, context, dataFlow, (Block) node);
				break;
			case ASTNode.BOOLEAN_LITERAL: // 09
				inspectBooleanLiteral(depth, context, dataFlow, (BooleanLiteral) node);
				break;
			case ASTNode.BREAK_STATEMENT: // 10
				inspectBreakStatement(depth, context, dataFlow, (BreakStatement) node);
				break;
			case ASTNode.CAST_EXPRESSION: // 11
				inspectCastExpression(depth, context, dataFlow, (CastExpression) node);
				break;
			case ASTNode.CHARACTER_LITERAL: // 13
				inspectCharacterLiteral(depth, context, dataFlow, (CharacterLiteral) node);
				break;
			case ASTNode.CLASS_INSTANCE_CREATION: // 14
				inspectClassInstanceCreation(depth, context, dataFlow, (ClassInstanceCreation) node);
				break;
			case ASTNode.CONDITIONAL_EXPRESSION: // 16
				inspectConditionExpression(depth, context, dataFlow, (ConditionalExpression) node);
				break;
			case ASTNode.CONSTRUCTOR_INVOCATION: // 17
				inspectConstructorInvocation(depth, context, dataFlow, (ConstructorInvocation) node);
				break;
			case ASTNode.CONTINUE_STATEMENT: // 18
				inspectContinueStatement(depth, context, dataFlow, (ContinueStatement) node);
				break;
			case ASTNode.DO_STATEMENT: // 19
				inspectDoStatement(depth, context, dataFlow, (DoStatement) node);
				break;
			case ASTNode.EMPTY_STATEMENT: // 20
				inspectEmptyStatement(depth, context, dataFlow, (EmptyStatement) node);
				break;
			case ASTNode.EXPRESSION_STATEMENT: // 21
				inspectExpressionStatement(depth, context, dataFlow, (ExpressionStatement) node);
				break;
			case ASTNode.FIELD_ACCESS: // 22
				inspectFieldAccess(depth, context, dataFlow, (FieldAccess) node);
				break;
			case ASTNode.FOR_STATEMENT: // 24
				inspectForStatement(depth, context, dataFlow, (ForStatement) node);
				break;
			case ASTNode.IF_STATEMENT: // 25
				inspectIfStatement(depth, context, dataFlow, (IfStatement) node);
				break;
			case ASTNode.INFIX_EXPRESSION: // 27
				inspectInfixExpression(depth, context, dataFlow, (InfixExpression) node);
				break;
			case ASTNode.METHOD_INVOCATION: // 32
				inspectMethodInvocation(depth, context, dataFlow, (MethodInvocation) node);
				break;
			case ASTNode.NULL_LITERAL: // 33
				inspectNullLiteral(depth, context, dataFlow, (NullLiteral) node);
				break;
			case ASTNode.NUMBER_LITERAL: // 34
				inspectNumberLiteral(depth, context, dataFlow, (NumberLiteral) node);
				break;
			case ASTNode.PARENTHESIZED_EXPRESSION: // 36
				inspectParenthesizedExpression(depth, context, dataFlow, (ParenthesizedExpression) node);
				break;
			case ASTNode.POSTFIX_EXPRESSION: // 37
				inspectPostfixExpression(depth, context, dataFlow, (PostfixExpression) node);
				break;
			case ASTNode.PREFIX_EXPRESSION: // 38
				inspectPrefixExpression(depth, context, dataFlow, (PrefixExpression) node);
				break;
			case ASTNode.QUALIFIED_NAME: // 40
				inspectQualifiedName(depth, context, dataFlow, (QualifiedName) node);
				break;
			case ASTNode.RETURN_STATEMENT: // 41
				inspectReturnStatement(depth, context, dataFlow, (ReturnStatement) node);
				break;
			case ASTNode.SIMPLE_NAME: // 42
				inspectSimpleName(depth, context, dataFlow, (SimpleName) node);
				break;
			case ASTNode.STRING_LITERAL: // 45
				inspectStringLiteral(depth, context, dataFlow, (StringLiteral) node);
				break;
			case ASTNode.SUPER_CONSTRUCTOR_INVOCATION: // 46
				inspectSuperConstructorInvocation(depth, context, dataFlow, (SuperConstructorInvocation) node);
				break;
			case ASTNode.SUPER_FIELD_ACCESS: // 47
				inspectSuperFieldAccess(depth, context, dataFlow, (SuperFieldAccess) node);
				break;
			case ASTNode.SUPER_METHOD_INVOCATION: // 48
				inspectSuperMethodInvocation(depth, context, dataFlow, (SuperMethodInvocation) node);
				break;
			case ASTNode.SWITCH_CASE: // 49
				inspectSwitchCase(depth, context, dataFlow, (SwitchCase) node);
				break;
			case ASTNode.SWITCH_STATEMENT: // 50
				inspectSwitchStatement(depth, context, dataFlow, (SwitchStatement) node);
				break;
			case ASTNode.SYNCHRONIZED_STATEMENT: // 51
				inspectSynchronizedStatement(depth, context, dataFlow, (SynchronizedStatement) node);
				break;
			case ASTNode.THIS_EXPRESSION: // 52
				inspectThisExpression(depth, context, dataFlow, (ThisExpression) node);
				break;
			case ASTNode.THROW_STATEMENT: // 53
				inspectThrowStatement(depth, context, dataFlow, (ThrowStatement) node);
				break;
			case ASTNode.TRY_STATEMENT: // 54
				inspectTryStatement(depth, context, dataFlow, (TryStatement) node);
				break;
			case ASTNode.TYPE_LITERAL: // 57
				inspectTypeLiteral(depth, context, dataFlow, (TypeLiteral) node);
				break;
			case ASTNode.VARIABLE_DECLARATION_STATEMENT: // 60
				inspectVariableDeclarationStatement(depth, context, dataFlow, (VariableDeclarationStatement) node);
				break;
			case ASTNode.WHILE_STATEMENT: // 61
				inspectWhileStatement(depth, context, dataFlow, (WhileStatement) node);
				break;
			case ASTNode.INSTANCEOF_EXPRESSION: // 62
				inspectInstanceofExpression(depth, context, dataFlow, (InstanceofExpression) node);
				break;
			case ASTNode.ENHANCED_FOR_STATEMENT: // 70
				inspectEnhancedForStatement(depth, context, dataFlow, (EnhancedForStatement) node);
				break;
			default:
				PluginLogger.logError("inspectStatement Default Node Type: " + node.getNodeType() + " - " + node, null);
		}
	}

	/**
	 * 02
	 */
	protected void inspectArrayAccess(int depth, Context context, DataFlow dataFlow, ArrayAccess expression) {
		inspectNode(depth, context, dataFlow, expression.getArray());
	}

	/**
	 * 03
	 */
	protected void inspectArrayCreation(int depth, Context context, DataFlow dataFlow, ArrayCreation expression) {
		iterateOverParameters(depth, context, dataFlow, expression.getInitializer());
	}

	/**
	 * 04
	 */
	protected void inspectArrayInitializer(int depth, Context context, DataFlow dataFlow, ArrayInitializer expression) {
		iterateOverParameters(depth, context, dataFlow, expression);
	}

	/**
	 * 07
	 */
	protected void inspectAssignment(int depth, Context context, DataFlow dataFlow, Assignment expression) {
		Expression leftHandSide = expression.getLeftHandSide();
		Expression rightHandSide = expression.getRightHandSide();

		inspectNode(depth, context, dataFlow.addNodeToPath(leftHandSide), leftHandSide);
		inspectNode(depth, context, dataFlow.addNodeToPath(rightHandSide), rightHandSide);
	}

	/**
	 * 08
	 */
	protected void inspectBlock(int depth, Context context, DataFlow dataFlow, Block block) {
		if (null != block) {
			for (Object object : block.statements()) {
				inspectNode(depth, context, dataFlow.addNodeToPath(null), (Statement) object);
			}
		}
	}

	/**
	 * 09
	 */
	protected void inspectBooleanLiteral(int depth, Context context, DataFlow dataFlow, BooleanLiteral node) {
		// Nothing to do.
	}

	/**
	 * 10
	 */
	protected void inspectBreakStatement(int depth, Context context, DataFlow dataFlow, BreakStatement statement) {
		// Nothing to do.
	}

	/**
	 * 11
	 */
	protected void inspectCastExpression(int depth, Context context, DataFlow dataFlow, CastExpression expression) {
		inspectNode(depth, context, dataFlow, expression.getExpression());
	}

	/**
	 * 13
	 */
	protected void inspectCharacterLiteral(int depth, Context context, DataFlow dataFlow, CharacterLiteral node) {
		// Nothing to do.
	}

	/**
	 * 14
	 */
	protected void inspectClassInstanceCreation(int depth, Context context, DataFlow dataFlow, ClassInstanceCreation node) {
		inspectInvocation(depth, context, dataFlow, node);
	}

	/**
	 * 16
	 */
	protected void inspectConditionExpression(int depth, Context context, DataFlow dataFlow,
			ConditionalExpression expression) {
		Expression thenExpression = expression.getThenExpression();
		Expression elseExpression = expression.getElseExpression();

		inspectNode(depth, context, dataFlow.addNodeToPath(thenExpression), thenExpression);
		inspectNode(depth, context, dataFlow.addNodeToPath(elseExpression), elseExpression);
	}

	/**
	 * 17 <br/>
	 * this(name2,10);
	 */
	protected void inspectConstructorInvocation(int depth, Context context, DataFlow dataFlow,
			ConstructorInvocation invocation) {
		// // 01 - Get the resource of this constructor.
		// IResource resource = BindingResolver.getResource(invocation);

		// 02 - Inspect the method invocation.
		inspectInvocation(depth, context, dataFlow, invocation);
	}

	/**
	 * 18
	 */
	protected void inspectContinueStatement(int depth, Context context, DataFlow dataFlow, ContinueStatement statement) {
		// Nothing to do.
	}

	/**
	 * 19
	 */
	protected void inspectDoStatement(int depth, Context context, DataFlow dataFlow, DoStatement statement) {
		inspectNode(depth, context, dataFlow, statement.getBody());
	}

	/**
	 * 20
	 */
	protected void inspectEmptyStatement(int depth, Context context, DataFlow dataFlow, EmptyStatement expression) {
		// Nothing to do.
	}

	/**
	 * 21
	 */
	protected void inspectExpressionStatement(int depth, Context context, DataFlow dataFlow,
			ExpressionStatement expression) {
		inspectNode(depth, context, dataFlow, expression.getExpression());
	}

	/**
	 * 22
	 */
	protected void inspectFieldAccess(int depth, Context context, DataFlow dataFlow, FieldAccess expression) {
		inspectNode(depth, context, dataFlow, expression.getName());
	}

	/**
	 * 24
	 */
	protected void inspectForStatement(int depth, Context context, DataFlow dataFlow, ForStatement statement) {
		inspectNode(depth, context, dataFlow, statement.getBody());
	}

	/**
	 * 25
	 */
	protected void inspectIfStatement(int depth, Context context, DataFlow dataFlow, IfStatement statement) {
		inspectNode(depth, context, dataFlow.addNodeToPath(null), statement.getThenStatement());
		inspectNode(depth, context, dataFlow.addNodeToPath(null), statement.getElseStatement());
	}

	/**
	 * 27
	 */
	protected void inspectInfixExpression(int depth, Context context, DataFlow dataFlow, InfixExpression expression) {
		Expression leftOperand = expression.getLeftOperand();
		Expression rightOperand = expression.getRightOperand();

		inspectNode(depth, context, dataFlow.addNodeToPath(leftOperand), leftOperand);
		inspectNode(depth, context, dataFlow.addNodeToPath(rightOperand), rightOperand);

		iterateOverParameters(depth, context, dataFlow, expression);
	}

	/**
	 * 32
	 */
	protected void inspectMethodInvocation(int depth, Context context, DataFlow dataFlow, MethodInvocation invocation) {
		// MethodDeclaration methodDeclaration = getMethodDeclaration(invocation);
		// if (null != methodDeclaration) {
		// // We have the source code.
		// inspectMethodWithSourceCode(depth, context, dataFlow, invocation, methodDeclaration);
		// } else {
		// // We do not have the source code.
		// inspectMethodWithOutSourceCode(depth, context, dataFlow, invocation);
		// }

		// 01 - Inspect the method invocation.
		inspectInvocation(depth, context, dataFlow, invocation);
	}

	/**
	 * 33
	 */
	protected void inspectNullLiteral(int depth, Context context, DataFlow dataFlow, NullLiteral node) {
		// Nothing to do.
	}

	/**
	 * 34
	 */
	protected void inspectNumberLiteral(int depth, Context context, DataFlow dataFlow, NumberLiteral node) {
		// Nothing to do.
	}

	/**
	 * 36
	 */
	protected void inspectParenthesizedExpression(int depth, Context context, DataFlow dataFlow,
			ParenthesizedExpression expression) {
		inspectNode(depth, context, dataFlow, expression.getExpression());
	}

	/**
	 * 37 i++
	 */
	protected void inspectPostfixExpression(int depth, Context context, DataFlow dataFlow, PostfixExpression expression) {
		// inspectNode(depth, context, dataFlow, expression.getOperand());
	}

	/**
	 * 38 ++i
	 */
	protected void inspectPrefixExpression(int depth, Context context, DataFlow dataFlow, PrefixExpression expression) {
		// inspectNode(depth, context, dataFlow, expression.getOperand());
	}

	/**
	 * 40
	 */
	protected void inspectQualifiedName(int depth, Context context, DataFlow dataFlow, QualifiedName expression) {
		inspectNode(depth, context, dataFlow, expression.getName());
	}

	/**
	 * 41 return ...
	 */
	protected void inspectReturnStatement(int depth, Context context, DataFlow dataFlow, ReturnStatement statement) {
		inspectNode(depth, context, dataFlow, statement.getExpression());
	}

	/**
	 * 42
	 */
	protected void inspectSimpleName(int depth, Context context, DataFlow dataFlow, SimpleName expression) {
	}

	/**
	 * 45 ""
	 */
	protected void inspectStringLiteral(int depth, Context context, DataFlow dataFlow, StringLiteral node) {
		// Nothing to do.
	}

	/**
	 * 46 super(...);
	 */
	protected void inspectSuperConstructorInvocation(int depth, Context context, DataFlow dataFlow,
			SuperConstructorInvocation invocation) {
		// // 01 - Get the type declaration of the current class.
		// TypeDeclaration typeDeclaration = BindingResolver.getTypeDeclaration(superConstructorInvocation);
		//
		// // 02 - Get the resource of the super class.
		// IResource resource = HelperCodeAnalyzer.getSuperClassResource(getCallGraph(), typeDeclaration);
		//
		// // 03 - Inspect the method.
		// constructorInvocation(depth, context, dataFlow, superConstructorInvocation, resource);
		// 01 - Inspect the method invocation.
		inspectInvocation(depth, context, dataFlow, invocation);
	}

	/**
	 * 47 super.variable;
	 */
	protected void inspectSuperFieldAccess(int depth, Context context, DataFlow dataFlow, SuperFieldAccess expression) {
		inspectNode(depth, context, dataFlow, expression.getName());
	}

	/**
	 * 48 super.methodName(...);
	 */
	protected void inspectSuperMethodInvocation(int depth, Context context, DataFlow dataFlow,
			SuperMethodInvocation invocation) {
		// // 01 - Get the type declaration of the current class.
		// TypeDeclaration typeDeclaration = BindingResolver.getTypeDeclaration(invocation);
		//
		// // 02 - Get the resource of the super class.
		// IResource resource = HelperCodeAnalyzer.getSuperClassResource(getCallGraph(), typeDeclaration);
		//
		// MethodDeclaration methodDeclaration = getMethodDeclaration(invocation);
		// if (null != methodDeclaration) {
		// // We have the source code.
		// inspectMethodWithSourceCode(depth, context, dataFlow, invocation, methodDeclaration);
		// } else {
		// // We do not have the source code.
		// inspectMethodWithOutSourceCode(depth, context, dataFlow, invocation);
		// }
		// 01 - Inspect the method invocation.
		inspectInvocation(depth, context, dataFlow, invocation);
	}

	/**
	 * 49
	 */
	protected void inspectSwitchCase(int depth, Context context, DataFlow dataFlow, SwitchCase statement) {
		// Nothing to do.
	}

	/**
	 * 50
	 */
	protected void inspectSwitchStatement(int depth, Context context, DataFlow dataFlow, SwitchStatement statement) {
		List<?> switchStatements = statement.statements();
		for (Object switchCases : switchStatements) {
			inspectNode(depth, context, dataFlow.addNodeToPath(null), (Statement) switchCases);
		}
	}

	/**
	 * 51
	 */
	protected void inspectSynchronizedStatement(int depth, Context context, DataFlow dataFlow,
			SynchronizedStatement statement) {
		inspectNode(depth, context, dataFlow, statement.getBody());
	}

	/**
	 * 52
	 */
	protected void inspectThisExpression(int depth, Context context, DataFlow dataFlow, ThisExpression expression) {
		inspectNode(depth, context, dataFlow, expression.getQualifier());
	}

	/**
	 * 53
	 */
	protected void inspectThrowStatement(int depth, Context context, DataFlow dataFlow, ThrowStatement statement) {
		inspectNode(depth, context, dataFlow, statement.getExpression());
	}

	/**
	 * 54
	 */
	protected void inspectTryStatement(int depth, Context context, DataFlow dataFlow, TryStatement statement) {
		inspectNode(depth, context, dataFlow.addNodeToPath(null), statement.getBody());

		List<?> listCatches = statement.catchClauses();
		for (Object catchClause : listCatches) {
			inspectNode(depth, context, dataFlow.addNodeToPath(null), ((CatchClause) catchClause).getBody());
		}

		inspectNode(depth, context, dataFlow.addNodeToPath(null), statement.getFinally());
	}

	/**
	 * 57
	 */
	protected void inspectTypeLiteral(int depth, Context context, DataFlow dataFlow, TypeLiteral expression) {
		// Nothing to do.
	}

	/**
	 * 60
	 */
	protected void inspectVariableDeclarationStatement(int depth, Context context, DataFlow dataFlow,
			VariableDeclarationStatement statement) {
		for (Iterator<?> iter = statement.fragments().iterator(); iter.hasNext();) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) iter.next();

			// 01 - Inspect the Initializer.
			inspectNode(depth, context, new DataFlow(fragment.getName()), fragment.getInitializer());
		}
	}

	/**
	 * 61
	 */
	protected void inspectWhileStatement(int depth, Context context, DataFlow dataFlow, WhileStatement statement) {
		inspectNode(depth, context, dataFlow, statement.getBody());
	}

	/**
	 * 62
	 */
	protected void inspectInstanceofExpression(int depth, Context context, DataFlow dataFlow,
			InstanceofExpression expression) {
		// Nothing to do.
	}

	/**
	 * 70
	 */
	protected void inspectEnhancedForStatement(int depth, Context context, DataFlow dataFlow,
			EnhancedForStatement statement) {
		inspectNode(depth, context, dataFlow, statement.getBody());
	}

	/**
	 * Helper Methods
	 */

	protected boolean hasReachedMaximumDepth(int depth) {
		return (Constant.MAXIMUM_VERIFICATION_DEPTH == depth);
	}

	/**
	 * 03, 04, 27, 32
	 */
	protected void iterateOverParameters(int depth, Context context, DataFlow dataFlow, ASTNode expression) {
		List<Expression> parameters = BindingResolver.getParameters(expression);
		for (Expression parameter : parameters) {
			inspectNode(depth, context, dataFlow.addNodeToPath(parameter), parameter);
		}
	}

	/**
	 * 14, 17, 32, 46, 48
	 */
	protected void inspectInvocation(int depth, Context context, DataFlow dataFlow, ASTNode invocation) {
		MethodDeclaration methodDeclaration = getMethodDeclaration(invocation);
		if (null != methodDeclaration) {
			// We have the source code.
			inspectMethodWithSourceCode(depth, context, dataFlow, invocation, methodDeclaration);
		} else {
			// We do not have the source code.
			inspectMethodWithOutSourceCode(depth, context, dataFlow, invocation);
		}
	}

	protected void inspectMethodWithSourceCode(int depth, Context context, DataFlow dataFlow, ASTNode methodInvocation,
			MethodDeclaration methodDeclaration) {
		inspectNode(depth, context, dataFlow, methodDeclaration.getBody());
	}

	protected void inspectMethodWithOutSourceCode(int depth, Context context, DataFlow dataFlow, ASTNode methodInvocation) {
		iterateOverParameters(depth, context, dataFlow, methodInvocation);
	}

	protected MethodDeclaration getMethodDeclaration(ASTNode invocation) {
		// The first idea is to search for this method declaration in the current resource.
		// If the implementation is not found, then we search on the super classes (if any)
		// and lastly we search on other files of the project.

		// 01 - Get the resource of this invocation.
		IResource resource = BindingResolver.getResource(invocation);

		// 02 - Search on the current resource.
		MethodDeclaration methodDeclaration = getMethodDeclaration(resource, invocation);
		if (null != methodDeclaration) {
			return methodDeclaration;
		}
		// 03 - Search on the super classes(if any).

		// 04 - Search on the other files of the project.

		// MethodDeclaration methodDeclaration = getCallGraph().getMethod(getCurrentResource(), methodInvocation);
		// 05 - The source code of the method invocation was not found.
		return null;
	}

	private MethodDeclaration getMethodDeclaration(IResource resource, ASTNode invocation) {
		// ConstructorInvocation - this(...);
		// SuperConstructorInvocation - super(...);
		// ClassInstanceCreation - new Class(...);

		// SuperMethodInvocation - super.method(...);
		// MethodInvocation - method(...); / object.method(...); / static.method(...);

		EnumTypeDeclaration typeDeclaration = null;
		switch (invocation.getNodeType()) {
			case ASTNode.CONSTRUCTOR_INVOCATION:
			case ASTNode.SUPER_CONSTRUCTOR_INVOCATION:
			case ASTNode.CLASS_INSTANCE_CREATION:
				typeDeclaration = EnumTypeDeclaration.CONSTRUCTOR;
				break;
			case ASTNode.SUPER_METHOD_INVOCATION:
			case ASTNode.METHOD_INVOCATION:
				typeDeclaration = EnumTypeDeclaration.METHOD;
				break;
		}

		return getMethodDeclaration(resource, invocation, typeDeclaration);
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
			}
		}

		return null;
	}

}
