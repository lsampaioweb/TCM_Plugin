package net.thecodemaster.evd.verifier;

import java.util.List;
import java.util.Map;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.BindingResolver;
import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.graph.VariableBindingManager;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.marker.MarkerManager;
import net.thecodemaster.evd.point.EntryPoint;
import net.thecodemaster.evd.point.SanitizationPoint;
import net.thecodemaster.evd.ui.enumeration.EnumStatusVariable;
import net.thecodemaster.evd.ui.l10n.Message;
import net.thecodemaster.evd.xmlloader.LoaderEntryPoint;
import net.thecodemaster.evd.xmlloader.LoaderSanitizationPoint;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 * @author Luciano Sampaio
 */
public abstract class CodeAnalyzer {

	/**
	 * The current compilation unit that is being analyzed.
	 */
	private CompilationUnit									currentCompilationUnit;
	/**
	 * The current resource that is being analyzed.
	 */
	private IResource												currentResource;
	/**
	 * This object contains all the methods, variables and their interactions, on the project that is being analyzed.
	 */
	private CallGraph												callGraph;
	/**
	 * List with all the EntryPoints (shared among other instances of the verifiers).
	 */
	private static List<EntryPoint>					entryPoints;
	/**
	 * List with all the Sanitizers (shared among other instances of the verifiers).
	 */
	private static List<SanitizationPoint>	sanitizers;

	public CompilationUnit getCurrentCompilationUnit() {
		return currentCompilationUnit;
	}

	public void setCurrentCompilationUnit(CompilationUnit currentCompilationUnit) {
		this.currentCompilationUnit = currentCompilationUnit;
	}

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

	protected static List<SanitizationPoint> getSanitizationPoints() {
		if (null == sanitizers) {
			// Loads all the Sanitizers.
			loadSanitizationPoints();
		}

		return sanitizers;
	}

	/**
	 * Load all the Sanitizers that the plug-in will use.
	 */
	protected static void loadSanitizationPoints() {
		sanitizers = (new LoaderSanitizationPoint()).load();
	}

	protected String getMessageEntryPoint(String value) {
		return String.format(Message.VerifierSecurityVulnerability.ENTRY_POINT_METHOD, value);
	}

	protected boolean hasReachedMaximumDepth(int depth) {
		return Constant.MAXIMUM_VERIFICATION_DEPTH == depth;
	}

	protected boolean hasMarkerAtPosition(Expression expression) {
		return (null != MarkerManager.hasInvisibleMarkerAtPosition(getCurrentCompilationUnit(), getCurrentResource(),
				expression));
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

	protected boolean isMethodASanitizationPoint(Expression method) {
		for (SanitizationPoint sanitizer : getSanitizationPoints()) {
			if (BindingResolver.methodsHaveSameNameAndPackage(sanitizer, method)) {
				// 01 - Get the expected arguments of this method.
				List<String> expectedParameters = sanitizer.getParameters();

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

	protected void updateVariableBindingStatus(VariableBindingManager variableBinding, DataFlow newDataFlow) {
		EnumStatusVariable status = (newDataFlow.isVulnerable()) ? EnumStatusVariable.VULNERABLE
				: EnumStatusVariable.NOT_VULNERABLE;
		variableBinding.setStatus(newDataFlow, status);
	}

	protected void updateVariableBindingStatusToPrimitive(VariableBindingManager variableBinding) {
		variableBinding.setStatus(EnumStatusVariable.NOT_VULNERABLE);
	}

	protected boolean isPrimitive(Expression variableName) {
		ITypeBinding typeBinding = BindingResolver.resolveTypeBinding(variableName);

		return (null != typeBinding) ? typeBinding.isPrimitive() : false;
	}

	protected void inspectNode(int depth, DataFlow dataFlow, Expression node) {
		if (null == node) {
			return;
		}

		// 01 - To avoid infinitive loop, this check is necessary.
		if (hasReachedMaximumDepth(depth++)) {
			PluginLogger.logError("hasReachedMaximumDepth: " + dataFlow + " - " + node + " - " + depth, null);
			return;
		}

		switch (node.getNodeType()) {
			case ASTNode.ARRAY_ACCESS: // 02
				inspectArrayAccess(depth, dataFlow, (ArrayAccess) node);
				break;
			case ASTNode.ARRAY_CREATION: // 03
				inspectArrayCreation(depth, dataFlow, (ArrayCreation) node);
				break;
			case ASTNode.ARRAY_INITIALIZER: // 04
				inspectArrayInitializer(depth, dataFlow, (ArrayInitializer) node);
				break;
			case ASTNode.ASSIGNMENT: // 07
				inspectAssignment(depth, dataFlow, (Assignment) node);
				break;
			case ASTNode.CAST_EXPRESSION: // 11
				inspectCastExpression(depth, dataFlow, (CastExpression) node);
				break;
			case ASTNode.CONDITIONAL_EXPRESSION: // 16
				inspectConditionExpression(depth, dataFlow, (ConditionalExpression) node);
				break;
			case ASTNode.FIELD_ACCESS: // 22
				inspectFieldAccess(depth, dataFlow, (FieldAccess) node);
				break;
			case ASTNode.INFIX_EXPRESSION: // 27
				inspectInfixExpression(depth, dataFlow, (InfixExpression) node);
				break;
			case ASTNode.CLASS_INSTANCE_CREATION: // 14
			case ASTNode.METHOD_INVOCATION: // 32
				Expression method = node;
				inspectMethodInvocation(depth, dataFlow.addNodeToPath(method), method);
				break;
			case ASTNode.PARENTHESIZED_EXPRESSION: // 36
				inspectParenthesizedExpression(depth, dataFlow, (ParenthesizedExpression) node);
				break;
			case ASTNode.POSTFIX_EXPRESSION: // 37
				inspectPostfixExpression(depth, dataFlow, (PostfixExpression) node);
				break;
			case ASTNode.PREFIX_EXPRESSION: // 38
				inspectPrefixExpression(depth, dataFlow, (PrefixExpression) node);
				break;
			case ASTNode.QUALIFIED_NAME: // 40
				inspectQualifiedName(depth, dataFlow, (QualifiedName) node);
				break;
			case ASTNode.SIMPLE_NAME: // 42
				SimpleName simpleName = (SimpleName) node;
				inspectSimpleName(depth, dataFlow.addNodeToPath(simpleName), simpleName);
				break;
			case ASTNode.SUPER_METHOD_INVOCATION: // 48
				inspectSuperMethodInvocation(depth, dataFlow, (SuperMethodInvocation) node);
				break;
			case ASTNode.THIS_EXPRESSION: // 52
				inspectThisExpression(depth, dataFlow, (ThisExpression) node);
				break;
			case ASTNode.BOOLEAN_LITERAL: // 09
			case ASTNode.CHARACTER_LITERAL: // 13
			case ASTNode.NULL_LITERAL: // 33
			case ASTNode.NUMBER_LITERAL: // 34
			case ASTNode.STRING_LITERAL: // 45
				inspectLiteral(depth, dataFlow, node);
				break;
			default:
				PluginLogger.logError("inspectExpression Default Node Type: " + node.getNodeType() + " - " + node, null);
		}
	}

	protected void inspectNode(int depth, DataFlow dataFlow, Statement node) {
		if (null == node) {
			return;
		}

		// 01 - To avoid infinitive loop, this check is necessary.
		if (hasReachedMaximumDepth(depth++)) {
			PluginLogger.logError("hasReachedMaximumDepth: " + dataFlow + " - " + node + " - " + depth, null);
			return;
		}

		switch (node.getNodeType()) {
			case ASTNode.BLOCK: // 08
				inspectBlock(depth, dataFlow, (Block) node);
				break;
			case ASTNode.BREAK_STATEMENT: // 10
				inspectBreakStatement(depth, dataFlow, (BreakStatement) node);
				break;
			case ASTNode.CONTINUE_STATEMENT: // 18
				inspectContinueStatement(depth, dataFlow, (ContinueStatement) node);
				break;
			case ASTNode.DO_STATEMENT: // 19
				inspectDoStatement(depth, dataFlow, (DoStatement) node);
				break;
			case ASTNode.EXPRESSION_STATEMENT: // 21
				inspectExpressionStatement(depth, dataFlow, (ExpressionStatement) node);
				break;
			case ASTNode.FOR_STATEMENT: // 24
				inspectForStatement(depth, dataFlow, (ForStatement) node);
				break;
			case ASTNode.IF_STATEMENT: // 25
				inspectIfStatement(depth, dataFlow, (IfStatement) node);
				break;
			case ASTNode.RETURN_STATEMENT: // 41
				inspectReturnStatement(depth, dataFlow, (ReturnStatement) node);
				break;
			case ASTNode.SUPER_CONSTRUCTOR_INVOCATION: // 46
				inspectSuperConstructorInvocation(depth, dataFlow, (SuperConstructorInvocation) node);
				break;
			case ASTNode.SWITCH_CASE: // 49
				inspectSwitchCase(depth, dataFlow, (SwitchCase) node);
				break;
			case ASTNode.SWITCH_STATEMENT: // 50
				inspectSwitchStatement(depth, dataFlow, (SwitchStatement) node);
				break;
			case ASTNode.SYNCHRONIZED_STATEMENT: // 51
				inspectSynchronizedStatement(depth, dataFlow, (SynchronizedStatement) node);
				break;
			case ASTNode.THROW_STATEMENT: // 53
				inspectThrowStatement(depth, dataFlow, (ThrowStatement) node);
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
			case ASTNode.ENHANCED_FOR_STATEMENT: // 70
				inspectEnhancedForStatement(depth, dataFlow, (EnhancedForStatement) node);
				break;
			default:
				PluginLogger.logError("inspectStatement Default Node Type: " + node.getNodeType() + " - " + node, null);
		}
	}

	/**
	 * 02
	 */
	protected void inspectArrayAccess(int depth, DataFlow dataFlow, ArrayAccess expression) {
		inspectNode(depth, dataFlow, expression.getArray());
	}

	/**
	 * 03
	 */
	protected void inspectArrayCreation(int depth, DataFlow dataFlow, ArrayCreation expression) {
		iterateOverParameters(depth, dataFlow, expression.getInitializer());
	}

	/**
	 * 03, 04, 27, 32, 46, 48
	 */
	protected void iterateOverParameters(int depth, DataFlow dataFlow, ASTNode expression) {
		List<Expression> parameters = BindingResolver.getParameters(expression);
		for (Expression parameter : parameters) {
			inspectNode(depth, dataFlow.addNodeToPath(parameter), parameter);
		}
	}

	/**
	 * 04
	 */
	protected void inspectArrayInitializer(int depth, DataFlow dataFlow, ArrayInitializer expression) {
		iterateOverParameters(depth, dataFlow, expression);
	}

	/**
	 * 07
	 */
	protected void inspectAssignment(int depth, DataFlow dataFlow, Assignment expression) {
		Expression leftHandSide = expression.getLeftHandSide();
		Expression rightHandSide = expression.getRightHandSide();

		inspectNode(depth, dataFlow.addNodeToPath(leftHandSide), leftHandSide);
		inspectNode(depth, dataFlow.addNodeToPath(rightHandSide), rightHandSide);
	}

	/**
	 * 08
	 */
	protected void inspectBlock(int depth, DataFlow dataFlow, Block block) {
		if (null != block) {
			for (Object object : block.statements()) {
				inspectNode(depth, dataFlow, (Statement) object);
			}
		}
	}

	/**
	 * 10
	 */
	protected void inspectBreakStatement(int depth, DataFlow dataFlow, BreakStatement statement) {
		// Nothing to do.
	}

	/**
	 * 11
	 */
	protected void inspectCastExpression(int depth, DataFlow dataFlow, CastExpression expression) {
		inspectNode(depth, dataFlow, expression.getExpression());
	}

	/**
	 * 16
	 */
	protected void inspectConditionExpression(int depth, DataFlow dataFlow, ConditionalExpression expression) {
		Expression thenExpression = expression.getThenExpression();
		Expression elseExpression = expression.getElseExpression();

		inspectNode(depth, dataFlow.addNodeToPath(thenExpression), thenExpression);
		inspectNode(depth, dataFlow.addNodeToPath(elseExpression), elseExpression);
	}

	/**
	 * 18
	 */
	protected void inspectContinueStatement(int depth, DataFlow dataFlow, ContinueStatement statement) {
		// Nothing to do.
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
		inspectNode(depth, dataFlow, expression.getName());
		// inspectNode(depth, dataFlow, expression.getExpression());
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
		Expression leftOperand = expression.getLeftOperand();
		Expression rightOperand = expression.getRightOperand();

		inspectNode(depth, dataFlow.addNodeToPath(leftOperand), leftOperand);
		inspectNode(depth, dataFlow.addNodeToPath(rightOperand), rightOperand);

		iterateOverParameters(depth, dataFlow, expression);
	}

	/**
	 * 32
	 */
	protected void inspectMethodInvocation(int depth, DataFlow dataFlow, Expression methodInvocation) {
		// 01 - Check if this method is a Sanitization-Point.
		if (isMethodASanitizationPoint(methodInvocation)) {
			// If a sanitization method is being invoked, then we do not have a vulnerability.
			return;
		}

		// 02 - Check if there is a marker, in case there is, we should BELIEVE it is not vulnerable.
		if (hasMarkerAtPosition(methodInvocation)) {
			return;
		}

		// 03 - Check if this method is an Entry-Point.
		if (isMethodAnEntryPoint(methodInvocation)) {
			String message = getMessageEntryPoint(BindingResolver.getFullName(methodInvocation));

			// We found a invocation to a entry point method.
			dataFlow.isVulnerable(Constant.Vulnerability.ENTRY_POINT, message);
			return;
		}

		// 04 - There are 2 cases: When we have the source code of this method and when we do not.
		inspectMethodInvocationWithOrWithOutSourceCode(depth, dataFlow, methodInvocation);
	}

	/**
	 * 32
	 */
	protected void inspectMethodInvocationWithOrWithOutSourceCode(int depth, DataFlow dataFlow,
			Expression methodInvocation) {
		// Some method invocations can be in a chain call, we have to investigate them all.
		// response.sendRedirect(login);
		// getServletContext().getRequestDispatcher(login).forward(request, response);
		// 01 - There are 2 cases: When we have the source code of this method and when we do not.
		MethodDeclaration methodDeclaration = getCallGraph().getMethod(getCurrentResource(), methodInvocation);
		if (null != methodDeclaration) {
			// We have the source code.
			inspectMethodWithSourceCode(depth, dataFlow, methodInvocation, methodDeclaration);
		} else {
			inspectMethodWithOutSourceCode(depth, dataFlow, methodInvocation);
		}

		VariableBindingManager variableBinding = getVariableBindingIfItIsAnObject(methodInvocation);
		// We found a vulnerability.
		if (dataFlow.isVulnerable()) {
			// There are 2 sub-cases: When is a method from an object and when is a method from a library.
			// 01 - stringBuilder.append("...");
			// 02 - System.out.println("..."); Nothing else to do.

			// 02 - Check if this method invocation is being call from a vulnerable object.
			if (null != variableBinding) {
				variableBinding.setStatus(dataFlow, EnumStatusVariable.VULNERABLE);
			}
		} else {
			// 01 - Check if this method invocation is being call from a vulnerable object.
			if (null != variableBinding) {
				processIfStatusUnknownOrUpdateIfVulnerable(depth, dataFlow, variableBinding);
			}
		}
	}

	/**
	 * 32
	 */
	protected void inspectMethodWithSourceCode(int depth, DataFlow dataFlow, Expression methodInvocation,
			MethodDeclaration methodDeclaration) {
		inspectNode(depth, dataFlow, methodDeclaration.getBody());
	}

	/**
	 * 32
	 */
	protected void inspectMethodWithOutSourceCode(int depth, DataFlow dataFlow, Expression methodInvocation) {
		iterateOverParameters(depth, dataFlow, methodInvocation);
	}

	/**
	 * 32
	 */
	protected VariableBindingManager getVariableBindingIfItIsAnObject(Expression method) {
		Expression expression = BindingResolver.getNameIfItIsAnObject(method);

		if (null != expression) {
			return getCallGraph().getLastReference((SimpleName) expression);
		}

		return null;
	}

	/**
	 * 32 , 42
	 */
	protected void processIfStatusUnknownOrUpdateIfVulnerable(int depth, DataFlow dataFlow,
			VariableBindingManager variableBinding) {
		if (variableBinding.status().equals(EnumStatusVariable.VULNERABLE)) {
			dataFlow.replace(variableBinding.getDataFlow());
		} else if (variableBinding.status().equals(EnumStatusVariable.UNKNOWN)) {
			// 01 - This is the case where we have to go deeper into the variable's path.
			inspectNode(depth, dataFlow, variableBinding.getInitializer());

			// 02 - If there is a vulnerable path, then this variable is vulnerable.
			updateVariableBindingStatus(variableBinding, dataFlow);
		}
	}

	/**
	 * 36
	 */
	protected void inspectParenthesizedExpression(int depth, DataFlow dataFlow, ParenthesizedExpression expression) {
		inspectNode(depth, dataFlow, expression.getExpression());
	}

	/**
	 * 37
	 */
	protected void inspectPostfixExpression(int depth, DataFlow dataFlow, PostfixExpression expression) {
		inspectNode(depth, dataFlow, expression.getOperand());
	}

	/**
	 * 38
	 */
	protected void inspectPrefixExpression(int depth, DataFlow dataFlow, PrefixExpression expression) {
		inspectNode(depth, dataFlow, expression.getOperand());
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
	}

	/**
	 * 42
	 */
	protected void inspectSimpleName(int depth, DataFlow dataFlow, SimpleName expression,
			VariableBindingManager variableBinding) {
		if (null != variableBinding) {
			processIfStatusUnknownOrUpdateIfVulnerable(depth, dataFlow, variableBinding);
		} else {
			// If a method is scanned after a method invocation, all the parameters are provided, but
			// if a method is scanned from the initial block declarations loop, some parameter might not be known
			// so it is necessary to investigate WHO invoked this method and what were the provided parameters.
			inspectSimpleNameFromInvokers(depth, dataFlow, expression, variableBinding);
		}
	}

	/**
	 * 42
	 */
	protected void inspectSimpleNameFromInvokers(int depth, DataFlow dataFlow, SimpleName expression,
			VariableBindingManager variableBinding) {
		// This is the case where the variable is an argument of the method.
		// 01 - Get the method signature that is using this parameter.
		MethodDeclaration methodDeclaration = BindingResolver.getParentMethodDeclaration(expression);

		// 02 - Get the index position where this parameter appear.
		int parameterIndex = BindingResolver.getParameterIndex(methodDeclaration, expression);
		if (parameterIndex >= 0) {
			// 03 - Get the list of methods that invokes this method.
			Map<MethodDeclaration, List<Expression>> invokers = getCallGraph().getInvokers(methodDeclaration);

			// 04 - Iterate over all the methods that invokes this method.
			for (List<Expression> currentInvocations : invokers.values()) {

				// 05 - Care only about the invocations to this method.
				for (Expression invocations : currentInvocations) {
					if (BindingResolver.areMethodsEqual(methodDeclaration, invocations)) {
						// 06 - Get the parameter at the index position.
						Expression parameter = BindingResolver.getParameterAtIndex(invocations, parameterIndex);

						// 07 - Run detection on this parameter.
						inspectNode(depth, dataFlow.addNodeToPath(parameter), parameter);
					}
				}
			}
		}
	}

	/**
	 * 46
	 */
	protected void inspectSuperConstructorInvocation(int depth, DataFlow dataFlow, SuperConstructorInvocation statement) {
		iterateOverParameters(depth, dataFlow, statement);

		// TODO - Inspect the source code if we have it.
		Expression superInvocation = statement.getExpression();
	}

	/**
	 * 48
	 */
	protected void inspectSuperMethodInvocation(int depth, DataFlow dataFlow, SuperMethodInvocation expression) {
		// iterateOverParameters(depth, dataFlow, expression);

		// TODO - Inspect the source code if we have it.
		// super.methodName(...);
		// We have to get the name of this method, then get the name of the SuperClass.
		// optionalSuperclassType SimpleType

		// // 01 - Get the name of the method being invoked.
		// Expression superInvocation = expression.getName();
		//
		// // 02 - Get the name of the SuperClass.
		// TypeDeclaration typeDeclaration = BindingResolver.getTypeDeclaration(expression);
		// // 02.1
		// SimpleType superClass = (SimpleType) typeDeclaration.getSuperclassType();
		// // 02.1
		// Name superClassName = superClass.getName();
		//
		// // 03 - Get the list of imports.
		//
		// // 03.1 - Iterate over the list and try to find the superclass import.
		// // If it finds, it means the superclass is in another package.
		// // If it does not find, it means the superclass is in the same package.
		//
		// // 01 - .
		// IResource resource = BindingResolver.getResource(typeDeclaration);
		//
		// // 01 - Get the list of methods in the current resource and its invocations.
		// Map<MethodDeclaration, List<Expression>> methods = getCallGraph().getMethods(resource);

		// 03 - Check if we have the source code of this SuperClass.

		// 04 - Now that we have the class, we try to find the implementation of the method.
		// inspectMethodInvocationWithOrWithOutSourceCode(depth, dataFlow, methodInvocation);
	}

	/**
	 * 52
	 */
	protected void inspectThisExpression(int depth, DataFlow dataFlow, ThisExpression expression) {
		// TODO - Get the reference to the class of this THIS.
		// inspectNode(depth, dataFlow, (Expression) expression.getParent());
	}

	/**
	 * 07, 60, 70
	 */
	protected void addVariableToCallGraphAndInspectInitializer(int depth, DataFlow dataFlow, Expression variableName,
			Expression initializer) {
		VariableBindingManager variableBinding = getCallGraph().addVariableToCallGraph(variableName, initializer);
		if (null != variableBinding) {
			// 01 - Add a reference to this variable (if it is a variable).
			addReferenceToInitializer(depth, variableName, initializer);

			// 02 - Inspect the Initializer to verify if this variable is vulnerable.
			DataFlow newDataFlow = new DataFlow(variableName);
			inspectNode(depth, newDataFlow, initializer);

			// 03 - If there is a vulnerable path, then this variable is vulnerable.
			// But if this variable is of primitive type, then there is nothing to do because they can not be vulnerable.
			if (!isPrimitive(variableName)) {
				updateVariableBindingStatus(variableBinding, newDataFlow);
			} else {
				updateVariableBindingStatusToPrimitive(variableBinding);
			}
		}
	}

	/**
	 * 49
	 */
	protected void inspectSwitchCase(int depth, DataFlow dataFlow, SwitchCase statement) {
		// Nothing to do.
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
	 * 51
	 */
	protected void inspectSynchronizedStatement(int depth, DataFlow dataFlow, SynchronizedStatement statement) {
		inspectNode(depth, dataFlow, statement.getBody());
	}

	/**
	 * 53
	 */
	protected void inspectThrowStatement(int depth, DataFlow dataFlow, ThrowStatement statement) {
		inspectNode(depth, dataFlow, statement.getExpression());
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
		PluginLogger.logError("inspectVariableDeclarationStatement not implemented. " + statement, null);
	}

	/**
	 * 61
	 */
	protected void inspectWhileStatement(int depth, DataFlow dataFlow, WhileStatement statement) {
		inspectNode(depth, dataFlow, statement.getBody());
	}

	/**
	 * 70
	 */
	protected void inspectEnhancedForStatement(int depth, DataFlow dataFlow, EnhancedForStatement statement) {
		SingleVariableDeclaration parameter = statement.getParameter();
		Expression expression = statement.getExpression();
		// 01 - Add the new variable to the callGraph.
		addVariableToCallGraphAndInspectInitializer(depth, dataFlow, parameter.getName(), expression);

		inspectNode(depth, dataFlow, statement.getBody());
	}

	/**
	 * 13, 33, 34, 45
	 */
	protected void inspectLiteral(int depth, DataFlow dataFlow, Expression node) {
	}

	protected void addReferenceToInitializer(int depth, Expression expression, Expression initializer) {
		// 01 - To avoid infinitive loop, this check is necessary.
		if (hasReachedMaximumDepth(depth++)) {
			PluginLogger.logError("addReferenceToInitializer: " + expression + " - " + initializer + " - " + depth, null);
			return;
		}

		if (null != initializer) {
			switch (initializer.getNodeType()) {
				case ASTNode.ARRAY_ACCESS: // 02
					addReferenceArrayAccess(depth, expression, (ArrayAccess) initializer);
					break;
				case ASTNode.ARRAY_INITIALIZER: // 04
					addReferenceArrayInitializer(depth, expression, (ArrayInitializer) initializer);
					break;
				case ASTNode.ASSIGNMENT: // 07
					addReferenceAssgnment(depth, expression, (Assignment) initializer);
					break;
				case ASTNode.CONDITIONAL_EXPRESSION: // 16
					addReferenceConditionalExpression(depth, expression, (ConditionalExpression) initializer);
					break;
				case ASTNode.INFIX_EXPRESSION: // 27
					addReferenceInfixExpression(depth, expression, (InfixExpression) initializer);
					break;
				case ASTNode.PARENTHESIZED_EXPRESSION: // 36
					addReferenceParenthesizedExpression(depth, expression, (ParenthesizedExpression) initializer);
					break;
				case ASTNode.QUALIFIED_NAME: // 40
				case ASTNode.SIMPLE_NAME: // 42
					addReferenceName(depth, expression, (Name) initializer);
					break;
			}
		}
	}

	protected void addReference(Expression expression, IBinding binding) {
		VariableBindingManager variableBindingInitializer = getCallGraph().getLastReference(binding);
		if (null != variableBindingInitializer) {
			variableBindingInitializer.addReferences(expression);
		}
	}

	protected void addReferenceName(int depth, Expression expression, Name initializer) {
		addReference(expression, getCallGraph().resolveBinding(initializer));
	}

	protected void addReferenceArrayAccess(int depth, Expression expression, ArrayAccess initializer) {
		addReference(expression, getCallGraph().resolveBinding(initializer));
	}

	protected void addReferenceArrayInitializer(int depth, Expression expression, ArrayInitializer initializer) {
		List<Expression> expressions = BindingResolver.getParameters(initializer);

		for (Expression current : expressions) {
			addReferenceToInitializer(depth, expression, current);
		}
	}

	protected void addReferenceAssgnment(int depth, Expression expression, Assignment initializer) {
		addReferenceToInitializer(depth, expression, initializer.getLeftHandSide());
		addReferenceToInitializer(depth, expression, initializer.getRightHandSide());
	}

	protected void addReferenceConditionalExpression(int depth, Expression expression, ConditionalExpression initializer) {
		addReferenceToInitializer(depth, expression, initializer.getThenExpression());
		addReferenceToInitializer(depth, expression, initializer.getElseExpression());
	}

	protected void addReferenceInfixExpression(int depth, Expression expression, InfixExpression initializer) {
		addReferenceToInitializer(depth, expression, initializer.getLeftOperand());
		addReferenceToInitializer(depth, expression, initializer.getRightOperand());

		List<Expression> extendedOperands = BindingResolver.getParameters(initializer);

		for (Expression current : extendedOperands) {
			addReferenceToInitializer(depth, expression, current);
		}
	}

	protected void addReferenceParenthesizedExpression(int depth, Expression expression,
			ParenthesizedExpression initializer) {
		addReferenceToInitializer(depth, expression, initializer.getExpression());
	}

}