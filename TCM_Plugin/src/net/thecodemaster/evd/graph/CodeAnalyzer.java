package net.thecodemaster.evd.graph;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.context.Context;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.marker.MarkerManager;
import net.thecodemaster.evd.point.EntryPoint;
import net.thecodemaster.evd.point.SanitizationPoint;
import net.thecodemaster.evd.ui.enumeration.EnumVariableStatus;
import net.thecodemaster.evd.ui.l10n.Message;
import net.thecodemaster.evd.xmlloader.LoaderEntryPoint;
import net.thecodemaster.evd.xmlloader.LoaderSanitizationPoint;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
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
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
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
	/**
	 * The object is responsible to update the progress bar of the user interface.
	 */
	private IProgressMonitor								progressMonitor;

	protected void setCurrentCompilationUnit(CompilationUnit currentCompilationUnit) {
		this.currentCompilationUnit = currentCompilationUnit;
	}

	protected CompilationUnit getCurrentCompilationUnit() {
		return currentCompilationUnit;
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
			entryPoints = (new LoaderEntryPoint()).load();
		}

		return entryPoints;
	}

	protected static List<SanitizationPoint> getSanitizationPoints() {
		if (null == sanitizers) {
			// Loads all the Sanitizers.
			sanitizers = (new LoaderSanitizationPoint()).load();
		}

		return sanitizers;
	}

	protected void setProgressMonitor(IProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}

	protected IProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	protected String getSubTaskMessage() {
		return null;
	}

	/**
	 * Notifies that a subtask of the main task is beginning.
	 * 
	 * @param taskName
	 *          The text that will be displayed to the user.
	 */
	protected void setSubTask(String taskName) {
		if (null != getProgressMonitor()) {
			getProgressMonitor().subTask(taskName);
		}
	}

	protected boolean hasReachedMaximumDepth(int depth) {
		return Constant.MAXIMUM_VERIFICATION_DEPTH == depth;
	}

	protected boolean hasMarkerAtPosition(Expression expression) {
		return (null != MarkerManager.hasInvisibleMarkerAtPosition(getCurrentCompilationUnit(), getCurrentResource(),
				expression));
	}

	protected String getMessageEntryPoint(String value) {
		return String.format(Message.VerifierSecurityVulnerability.ENTRY_POINT_METHOD, value);
	}

	/**
	 * Returns whether cancellation of current operation has been requested
	 * 
	 * @param reporter
	 * @return true if cancellation has been requested, and false otherwise.
	 */
	protected boolean userCanceledProcess(IProgressMonitor monitor) {
		return ((null != getProgressMonitor()) && (getProgressMonitor().isCanceled()));
	}

	protected void updateVariableBindingStatus(VariableBinding variableBinding, DataFlow dataFlow) {
		EnumVariableStatus status = (dataFlow.isVulnerable()) ? EnumVariableStatus.VULNERABLE
				: EnumVariableStatus.NOT_VULNERABLE;
		variableBinding.setStatus(status).setDataFlow(dataFlow);
	}

	protected void updateVariableBindingStatusToPrimitive(VariableBinding variableBinding) {
		variableBinding.setStatus(EnumVariableStatus.NOT_VULNERABLE);
	}

	protected boolean isPrimitive(Expression expression) {
		// 01 - Get the type if it is a variable or the return type if it is a method.
		ITypeBinding typeBinding = BindingResolver.resolveTypeBinding(expression);

		// 02 - If the type is primitive, we return and that's it.
		if (typeBinding.isPrimitive()) {
			return true;
		}

		// 03 - If the type is a Wrapper, the method isPrimitive returns false, so we have to check that.
		return BindingResolver.isWrapperOfPrimitive(typeBinding);
	}

	protected void run(IProgressMonitor monitor, CallGraph callGraph, List<IResource> resources) {
		setProgressMonitor(monitor);
		setCallGraph(callGraph);

		// 01 - Iterate over all the resources.
		for (IResource resource : resources) {
			if (!userCanceledProcess(getProgressMonitor())) {
				// 02 - Set the current resource.
				setCurrentResource(resource);

				// 03 - Inform the user what is the current process of the plug-in.
				setSubTask(getSubTaskMessage());

				// 04 - Get the list of methods that will be processed from this resource.
				Map<MethodDeclaration, List<Expression>> methodsToProcess = getMethodsToProcess(resources, resource);

				// 05 - Process the detection on these methods.
				run(methodsToProcess);
			} else {
				// 02 - The user has stopped the process.
				return;
			}
		}
	}

	protected Map<MethodDeclaration, List<Expression>> getMethodsToProcess(List<IResource> resources, IResource resource) {
		// This map contains the method that will be processed and its invokers.
		Map<MethodDeclaration, List<Expression>> methodsToProcess = Creator.newMap();

		// 01 - Get the list of methods in the current resource and its invocations.
		Map<MethodDeclaration, List<Expression>> methods = getCallGraph().getMethods(resource);

		// 02 - Iterate over all the method declarations of the current resource.
		for (MethodDeclaration methodDeclaration : methods.keySet()) {

			// To avoid unnecessary processing, we only process methods that are
			// not invoked by any other method in the same file. Because if the method
			// is invoked, eventually it will be processed.
			// 03 - Get the list of methods that invokes this method.
			Map<MethodDeclaration, List<Expression>> invokers = getCallGraph().getInvokers(methodDeclaration);
			if (invokers.size() > 0) {
				// 04 - Iterate over all the methods that invokes this method.
				for (Entry<MethodDeclaration, List<Expression>> caller : invokers.entrySet()) {

					// 05 - Get the resource of this method (caller).
					IResource resourceCaller = BindingResolver.getResource(caller.getKey());

					// 06 - If this method is invoked by a method from another resource
					// and that resource is not in the list of resources that are going to be processed.
					if ((!resourceCaller.equals(resource)) && (!resources.contains(resourceCaller))) {

						// 07 - Care only about the invocations to this method.
						if (!methodsToProcess.containsKey(methodDeclaration)) {
							List<Expression> invocations = Creator.newList();

							// Create a empty list of method invocations.
							methodsToProcess.put(methodDeclaration, invocations);
						}

						// 08 - This method should be processed, add it to the list.
						methodsToProcess.get(methodDeclaration).addAll(caller.getValue());
					}
				}
			} else {
				List<Expression> emptyInvokers = Creator.newList();

				// 04 - This method should be processed, add it to the list.
				methodsToProcess.put(methodDeclaration, emptyInvokers);
			}

		}

		return methodsToProcess;
	}

	/**
	 * Run the vulnerability detection on the provided method declaration.
	 * 
	 * @param methodDeclaration
	 */
	protected void run(Map<MethodDeclaration, List<Expression>> methodsToProcess) {
		for (Entry<MethodDeclaration, List<Expression>> methodDeclaration : methodsToProcess.entrySet()) {
			// 01 - The depth controls the investigation mechanism to avoid infinitive loops.
			int depth = 0;

			// 02 - We need the compilation unit to check if there are markers in the current resource.
			setCurrentCompilationUnit(BindingResolver.getCompilationUnit(methodDeclaration.getKey()));

			// 03 - If this method is not invoked by any other, this method can be an entry method.
			// Main(), doGet or just a never used method.
			if (0 == methodDeclaration.getValue().size()) {

				// 03 - Process the detection on the current method.
				run(depth, methodDeclaration.getKey(), null);
			} else {
				for (Expression invoker : methodDeclaration.getValue()) {

					// 04 - Process the detection on the current method.
					run(depth, methodDeclaration.getKey(), invoker);
				}
			}
		}
	}

	/**
	 * Run the vulnerability detection on the current method declaration.
	 * 
	 * @param methodDeclaration
	 */
	protected void run(int depth, MethodDeclaration methodDeclaration, Expression invoker) {
	}

	protected void inspectNode(int depth, Context context, DataFlow dataFlow, Expression node) {
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
			case ASTNode.CAST_EXPRESSION: // 11
				inspectCastExpression(depth, context, dataFlow, (CastExpression) node);
				break;
			case ASTNode.CONDITIONAL_EXPRESSION: // 16
				inspectConditionExpression(depth, context, dataFlow, (ConditionalExpression) node);
				break;
			case ASTNode.FIELD_ACCESS: // 22
				inspectFieldAccess(depth, context, dataFlow, (FieldAccess) node);
				break;
			case ASTNode.INFIX_EXPRESSION: // 27
				inspectInfixExpression(depth, context, dataFlow, (InfixExpression) node);
				break;
			case ASTNode.CLASS_INSTANCE_CREATION: // 14
			case ASTNode.METHOD_INVOCATION: // 32
				inspectMethodInvocation(depth, context, dataFlow, node);
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
			case ASTNode.SIMPLE_NAME: // 42
				inspectSimpleName(depth, context, dataFlow, (SimpleName) node);
				break;
			case ASTNode.SUPER_METHOD_INVOCATION: // 48
				inspectSuperMethodInvocation(depth, context, dataFlow, (SuperMethodInvocation) node);
				break;
			case ASTNode.THIS_EXPRESSION: // 52
				inspectThisExpression(depth, context, dataFlow, (ThisExpression) node);
				break;
			case ASTNode.TYPE_LITERAL: // 57
				inspectTypeLiteral(depth, context, dataFlow, (TypeLiteral) node);
				break;
			case ASTNode.INSTANCEOF_EXPRESSION: // 62
				inspectInstanceofExpression(depth, context, dataFlow, (InstanceofExpression) node);
				break;
			case ASTNode.BOOLEAN_LITERAL: // 09
			case ASTNode.CHARACTER_LITERAL: // 13
			case ASTNode.NULL_LITERAL: // 33
			case ASTNode.NUMBER_LITERAL: // 34
			case ASTNode.STRING_LITERAL: // 45
				inspectLiteral(depth, context, dataFlow, node);
				break;
			default:
				PluginLogger.logError("inspectExpression Default Node Type: " + node.getNodeType() + " - " + node, null);
		}
	}

	protected void inspectNode(int depth, Context context, DataFlow dataFlow, Statement node) {
		if (null == node) {
			return;
		}

		// 01 - To avoid infinitive loop, this check is necessary.
		if (hasReachedMaximumDepth(depth++)) {
			PluginLogger.logError("hasReachedMaximumDepth: " + " - " + node + " - " + depth, null);
			return;
		}

		switch (node.getNodeType()) {
			case ASTNode.BLOCK: // 08
				inspectBlock(depth, context, dataFlow, (Block) node);
				break;
			case ASTNode.BREAK_STATEMENT: // 10
				inspectBreakStatement(depth, context, dataFlow, (BreakStatement) node);
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
			case ASTNode.FOR_STATEMENT: // 24
				inspectForStatement(depth, context, dataFlow, (ForStatement) node);
				break;
			case ASTNode.IF_STATEMENT: // 25
				inspectIfStatement(depth, context, dataFlow, (IfStatement) node);
				break;
			case ASTNode.RETURN_STATEMENT: // 41
				inspectReturnStatement(depth, context, dataFlow, (ReturnStatement) node);
				break;
			case ASTNode.SUPER_CONSTRUCTOR_INVOCATION: // 46
				inspectSuperConstructorInvocation(depth, context, dataFlow, (SuperConstructorInvocation) node);
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
			case ASTNode.THROW_STATEMENT: // 53
				inspectThrowStatement(depth, context, dataFlow, (ThrowStatement) node);
				break;
			case ASTNode.TRY_STATEMENT: // 54
				inspectTryStatement(depth, context, dataFlow, (TryStatement) node);
				break;
			case ASTNode.VARIABLE_DECLARATION_STATEMENT: // 60
				inspectVariableDeclarationStatement(depth, context, dataFlow, (VariableDeclarationStatement) node);
				break;
			case ASTNode.WHILE_STATEMENT: // 61
				inspectWhileStatement(depth, context, dataFlow, (WhileStatement) node);
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
	 * 03, 04, 17, 27, 32, 46, 48
	 */
	protected void iterateOverParameters(int depth, Context context, DataFlow dataFlow, ASTNode expression) {
		List<Expression> parameters = BindingResolver.getParameters(expression);
		for (Expression parameter : parameters) {
			inspectNode(depth, context, dataFlow.addNodeToPath(parameter), parameter);
		}
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
	 * 17
	 */
	protected void inspectConstructorInvocation(int depth, Context context, DataFlow dataFlow,
			ConstructorInvocation statement) {
		iterateOverParameters(depth, context, dataFlow, statement);
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
	protected void inspectMethodInvocation(int depth, Context context, DataFlow dataFlow, Expression methodInvocation) {
		// 01 - Check if this method is a Sanitization-Point.
		if (BindingResolver.isMethodASanitizationPoint(getSanitizationPoints(), methodInvocation)) {
			// If a sanitization method is being invoked, then we do not have a vulnerability.
			return;
		}

		// 03 - Get a new data flow or a child from the parent.
		DataFlow newDataFlow = getDataFlow(dataFlow, methodInvocation);

		// 03 - Check if this method is an Entry-Point.
		if (BindingResolver.isMethodAnEntryPoint(getEntryPoints(), methodInvocation)) {
			// 04 - Check if there is a marker, in case there is, we should BELIEVE it is not vulnerable.
			if (hasMarkerAtPosition(methodInvocation)) {
				return;
			}

			String message = getMessageEntryPoint(BindingResolver.getFullName(methodInvocation));

			// We found a invocation to a entry point method.
			newDataFlow.isVulnerable(Constant.Vulnerability.ENTRY_POINT, message);
			return;
		}

		// 04 - There are 2 cases: When we have the source code of this method and when we do not.
		inspectMethodInvocationWithOrWithOutSourceCode(depth, context, newDataFlow, methodInvocation);
	}

	/**
	 * 32
	 */
	protected DataFlow getDataFlow(DataFlow dataFlow, Expression methodInvocation) {
		// 01 request.getParameter("b");
		// 02 boolean b = Boolean.valueOf(request.getParameter("b"));
		// 03 String a = request.getParameter("a");
		// 04 return request.getParameter("b");
		// If the parent is a VariableDeclarationFragment or a return type.
		if (isPrimitive(methodInvocation)) {
			return new DataFlow(methodInvocation);
		} else {
			return BindingResolver.getDataFlowBasedOnTheParent(dataFlow, methodInvocation);
		}
	}

	/**
	 * 32
	 */
	protected void inspectMethodInvocationWithOrWithOutSourceCode(int depth, Context context, DataFlow dataFlow,
			Expression methodInvocation) {
		// Some method invocations can be in a chain call, we have to investigate them all.
		// response.sendRedirect(login);
		// getServletContext().getRequestDispatcher(login).forward(request, response);
		// 01 - There are 2 cases: When we have the source code of this method and when we do not.
		MethodDeclaration methodDeclaration = getCallGraph().getMethod(getCurrentResource(), methodInvocation);
		if (null != methodDeclaration) {
			// We have the source code.
			inspectMethodWithSourceCode(depth, context, dataFlow, methodInvocation, methodDeclaration);
		} else {
			inspectMethodWithOutSourceCode(depth, context, dataFlow, methodInvocation);
		}

		VariableBinding variableBinding = getVariableBindingIfItIsAnObject(context, methodInvocation);
		// We found a vulnerability.
		if (dataFlow.isVulnerable()) {
			// There are 2 sub-cases: When is a method from an object and when is a method from a library.
			// 01 - stringBuilder.append("...");
			// 02 - System.out.println("..."); Nothing else to do.

			// 02 - Check if this method invocation is being call from a vulnerable object.
			if (null != variableBinding) {
				variableBinding.setStatus(EnumVariableStatus.VULNERABLE).setDataFlow(dataFlow);
			}
		} else {
			// 01 - Check if this method invocation is being call from a vulnerable object.
			if (null != variableBinding) {
				processIfStatusUnknownOrUpdateIfVulnerable(depth, context, dataFlow, variableBinding);
			}
		}
	}

	/**
	 * 32
	 */
	protected void inspectMethodWithSourceCode(int depth, Context context, DataFlow dataFlow,
			Expression methodInvocation, MethodDeclaration methodDeclaration) {
		inspectNode(depth, context, dataFlow, methodDeclaration.getBody());
	}

	/**
	 * 32
	 */
	protected void inspectMethodWithOutSourceCode(int depth, Context context, DataFlow dataFlow,
			Expression methodInvocation) {
		iterateOverParameters(depth, context, dataFlow, methodInvocation);
	}

	/**
	 * 32
	 */
	protected VariableBinding getVariableBindingIfItIsAnObject(Context context, Expression method) {
		Expression expression = BindingResolver.getNameIfItIsAnObject(method);

		if (null != expression) {
			return getCallGraph().getLastReference(context, expression);
		}

		return null;
	}

	/**
	 * 32 , 42
	 */
	protected void processIfStatusUnknownOrUpdateIfVulnerable(int depth, Context context, DataFlow dataFlow,
			VariableBinding variableBinding) {
		if (variableBinding.getStatus().equals(EnumVariableStatus.VULNERABLE)) {
			dataFlow.replace(variableBinding.getDataFlow());
		} else if (variableBinding.getStatus().equals(EnumVariableStatus.UNKNOWN)) {
			// 01 - This is the case where we have to go deeper into the variable's path.
			inspectNode(depth, context, dataFlow, variableBinding.getInitializer());

			// 02 - If there is a vulnerable path, then this variable is vulnerable.
			updateVariableBindingStatus(variableBinding, dataFlow);
		}
	}

	/**
	 * 36
	 */
	protected void inspectParenthesizedExpression(int depth, Context context, DataFlow dataFlow,
			ParenthesizedExpression expression) {
		inspectNode(depth, context, dataFlow, expression.getExpression());
	}

	/**
	 * 37
	 */
	protected void inspectPostfixExpression(int depth, Context context, DataFlow dataFlow, PostfixExpression expression) {
		inspectNode(depth, context, dataFlow, expression.getOperand());
	}

	/**
	 * 38
	 */
	protected void inspectPrefixExpression(int depth, Context context, DataFlow dataFlow, PrefixExpression expression) {
		inspectNode(depth, context, dataFlow, expression.getOperand());
	}

	/**
	 * 40
	 */
	protected void inspectQualifiedName(int depth, Context context, DataFlow dataFlow, QualifiedName expression) {
		inspectNode(depth, context, dataFlow, expression.getName());
	}

	/**
	 * 41
	 */
	protected void inspectReturnStatement(int depth, Context context, DataFlow dataFlow, ReturnStatement statement) {
		inspectNode(depth, context, dataFlow, statement.getExpression());
	}

	/**
	 * 42
	 */
	protected void inspectSimpleName(int depth, Context context, DataFlow dataFlow, SimpleName expression) {
		// 01 - Try to retrieve the variable from the list of variables.
		VariableBinding variableBinding = getCallGraph().getVariableBinding(context, expression);

		inspectSimpleName(depth, context, dataFlow, expression, variableBinding);
	}

	/**
	 * 42
	 */
	protected void inspectSimpleName(int depth, Context context, DataFlow dataFlow, SimpleName expression,
			VariableBinding variableBinding) {
		DataFlow newDataFlow = dataFlow.addNodeToPath(expression);

		if (null != variableBinding) {
			processIfStatusUnknownOrUpdateIfVulnerable(depth, context, newDataFlow, variableBinding);
		} else {
			// If a method is scanned after a method invocation, all the parameters are provided, but
			// if a method is scanned from the initial block declarations loop, some parameter might not be known
			// so it is necessary to investigate WHO invoked this method and what were the provided parameters.
			inspectSimpleNameFromInvokers(depth, context, newDataFlow, expression, variableBinding);
		}
	}

	/**
	 * 42
	 */
	protected void inspectSimpleNameFromInvokers(int depth, Context context, DataFlow dataFlow, SimpleName expression,
			VariableBinding variableBinding) {
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

				for (Expression invocations : currentInvocations) {
					// 05 - Get the parameter at the index position.
					Expression parameter = BindingResolver.getParameterAtIndex(invocations, parameterIndex);

					// 06 - Run detection on this parameter.
					inspectNode(depth, context, dataFlow.addNodeToPath(parameter), parameter);
				}
			}
		}
	}

	/**
	 * 46
	 */
	protected void inspectSuperConstructorInvocation(int depth, Context context, DataFlow dataFlow,
			SuperConstructorInvocation statement) {
		iterateOverParameters(depth, context, dataFlow, statement);

		// TODO - Inspect the source code if we have it.
		Expression superInvocation = statement.getExpression();
	}

	/**
	 * 48
	 */
	protected void inspectSuperMethodInvocation(int depth, Context context, DataFlow dataFlow,
			SuperMethodInvocation expression) {
		// iterateOverParameters(depth, context, dataFlow, expression);

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
		// inspectMethodInvocationWithOrWithOutSourceCode(depth, context, dataFlow, methodInvocation);
	}

	/**
	 * 52
	 */
	protected void inspectThisExpression(int depth, Context context, DataFlow dataFlow, ThisExpression expression) {
		// TODO - Get the reference to the class of this THIS.
		// inspectNode(depth, context, dataFlow, (Expression) expression.getParent());
	}

	/**
	 * 57
	 */
	protected void inspectTypeLiteral(int depth, Context context, DataFlow dataFlow, TypeLiteral expression) {
		// Nothing to do.
	}

	/**
	 * 62
	 */
	protected void inspectInstanceofExpression(int depth, Context context, DataFlow dataFlow,
			InstanceofExpression expression) {
		PluginLogger.logIfDebugging(expression.toString());
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
	 * 60
	 */
	protected void inspectVariableDeclarationStatement(int depth, Context context, DataFlow dataFlow,
			VariableDeclarationStatement statement) {
	}

	/**
	 * 61
	 */
	protected void inspectWhileStatement(int depth, Context context, DataFlow dataFlow, WhileStatement statement) {
		inspectNode(depth, context, dataFlow, statement.getBody());
	}

	/**
	 * 70
	 */
	protected void inspectEnhancedForStatement(int depth, Context context, DataFlow dataFlow,
			EnhancedForStatement statement) {
		inspectNode(depth, context, dataFlow, statement.getBody());
	}

	/**
	 * 13, 33, 34, 45
	 */
	protected void inspectLiteral(int depth, Context context, DataFlow dataFlow, Expression node) {
	}

}