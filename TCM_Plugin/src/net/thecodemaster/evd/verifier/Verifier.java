package net.thecodemaster.evd.verifier;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.BindingResolver;
import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.graph.Parameter;
import net.thecodemaster.evd.graph.VariableBindingManager;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.marker.annotation.AnnotationManager;
import net.thecodemaster.evd.point.EntryPoint;
import net.thecodemaster.evd.point.ExitPoint;
import net.thecodemaster.evd.reporter.Reporter;
import net.thecodemaster.evd.ui.l10n.Message;
import net.thecodemaster.evd.xmlloader.LoaderExitPoint;

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
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 * The verifier is the class that actually knows how to find the vulnerability and the one that performs this
 * verification. Each verifier can reimplement/override methods of add new behavior to them.
 * 
 * @author Luciano Sampaio
 */
public abstract class Verifier {

	/**
	 * The name of the current verifier.
	 */
	private final String						name;
	/**
	 * The id of the current verifier.
	 */
	private final int								id;
	/**
	 * The current resource that is being analyzed.
	 */
	private IResource								currentResource;
	/**
	 * This object contains all the methods, variables and their interactions, on the project that is being analyzed.
	 */
	private CallGraph								callGraph;
	/**
	 * The object that know how and where to report the found vulnerabilities.
	 */
	private Reporter								reporter;
	/**
	 * List with all the ExitPoints of this verifier.
	 */
	private List<ExitPoint>					exitPoints;
	/**
	 * List with all the EntryPoints (shared among other instances of the verifiers).
	 */
	private static List<EntryPoint>	entryPoints;

	/**
	 * @param name
	 *          The name of the verifier.
	 * @param id
	 *          The id of the verifier.
	 * @param listEntryPoints
	 *          List with all the EntryPoints methods.
	 */
	public Verifier(String name, int id, List<EntryPoint> listEntryPoints) {
		this.name = name;
		this.id = id;

		entryPoints = listEntryPoints;
	}

	public String getName() {
		return name;
	}

	private int getId() {
		return id;
	}

	private void setCurrentResource(IResource currentResource) {
		this.currentResource = currentResource;
	}

	private IResource getCurrentResource() {
		return currentResource;
	}

	protected CallGraph getCallGraph() {
		return callGraph;
	}

	protected Reporter getReporter() {
		return reporter;
	}

	protected List<ExitPoint> getExitPoints() {
		if (null == exitPoints) {
			// Loads all the ExitPoints of this verifier.
			exitPoints = (new LoaderExitPoint(getId())).load();
		}

		return exitPoints;
	}

	protected List<EntryPoint> getEntryPoints() {
		if (null == entryPoints) {
			entryPoints = Creator.newList();
		}

		return entryPoints;
	}

	/**
	 * Notifies that a subtask of the main task is beginning.
	 * 
	 * @param taskName
	 *          The text that will be displayed to the user.
	 */
	protected void setSubTask(String taskName) {
		if ((null != getReporter()) && (null != getReporter().getProgressMonitor())) {
			getReporter().getProgressMonitor().subTask(taskName);
		}
	}

	protected String getMessageEntryPoint(String value) {
		return String.format(Message.VerifierSecurityVulnerability.ENTRY_POINT_METHOD, value);
	}

	protected void reportVulnerability(DataFlow dataFlow) {
		getReporter().addProblem(getId(), getCurrentResource(), dataFlow);
	}

	protected boolean hasReachedMaximumDepth(int depth) {
		return Constant.MAXIMUM_VERIFICATION_DEPTH == depth;
	}

	protected boolean isVulnerable(Expression expression) {
		return false;
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

	protected ExitPoint getExitPointIfMethodIsOne(Expression method) {
		for (ExitPoint currentExitPoint : getExitPoints()) {
			if (BindingResolver.methodsHaveSameNameAndPackage(currentExitPoint, method)) {
				// 01 - Get the expected arguments of this method.
				Map<Parameter, List<Integer>> expectedParameters = currentExitPoint.getParameters();

				// 02 - Get the received parameters of the current method.
				List<Expression> receivedParameters = BindingResolver.getParameters(method);

				// 03 - It is necessary to check the number of parameters and its types
				// because it may exist methods with the same names but different parameters.
				if (expectedParameters.size() == receivedParameters.size()) {
					boolean isMethodAnExitPoint = true;
					int index = 0;
					for (Parameter expectedParameter : expectedParameters.keySet()) {
						ITypeBinding typeBinding = receivedParameters.get(index++).resolveTypeBinding();

						// Verify if all the parameters are the ones expected. However, there is a case
						// where an Object is expected, and any type is accepted.
						if (!BindingResolver.parametersHaveSameType(expectedParameter.getType(), typeBinding)) {
							isMethodAnExitPoint = false;
							break;
						}
					}

					if (isMethodAnExitPoint) {
						return currentExitPoint;
					}
				}
			}
		}

		return null;
	}

	/**
	 * The public run method that will be invoked by the Analyzer.
	 * 
	 * @param resources
	 * @param callGraph
	 * @param reporter
	 */
	public void run(List<IResource> resources, CallGraph callGraph, Reporter reporter) {
		this.callGraph = callGraph;
		this.reporter = reporter;

		setSubTask(getName());

		// Perform the verifications on the resources.
		// 01 - Run the vulnerability detection on all the provided resources.
		for (IResource resource : resources) {
			run(resource);
		}
	}

	/**
	 * Iterate over all the method declarations found in the current resource.
	 * 
	 * @param resource
	 */
	protected void run(IResource resource) {
		// We need this information when we are going to display the vulnerabilities.
		setCurrentResource(resource);

		// 02 - Get the list of methods in the current resource.
		Map<MethodDeclaration, List<Expression>> methods = getCallGraph().getMethods(resource);

		// 03 - Get all the method invocations of each method declaration.
		for (MethodDeclaration methodDeclaration : methods.keySet()) {
			run(methodDeclaration);
		}
	}

	/**
	 * Run the vulnerability detection on the current method declaration.
	 * 
	 * @param methodDeclaration
	 */
	protected void run(MethodDeclaration methodDeclaration) {
		// The depth control the investigation mechanism to avoid infinitive loops.
		int depth = 0;
		inspectBlock(depth, null, methodDeclaration.getBody());
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
		// 01 - Check if this method is a Sanitization-Point.
		if (isMethodASanitizationPoint(expression)) {
			// If a sanitization method is being invoked, then we do not have a vulnerability.
			return;
		}

		// 02 - Check if this method is an Entry-Point.
		if (isMethodAnEntryPoint(expression)) {
			// String message = getMessageEntryPoint(BindingResolver.getFullName(expression));

			// We found a invocation to a entry point method.
			// df.isVulnerable(Constant.Vulnerability.ENTRY_POINT, message);
			return;
		}

		// 03 - Check if this method is an Exit-Point.
		ExitPoint exitPoint = getExitPointIfMethodIsOne(expression);
		if (null != exitPoint) {
			// 03.1 - A new dataFlow for this variable.
			DataFlow df = new DataFlow(expression);

			// 03.2 - Inspect the Initializer to verify if this variable is vulnerable.
			inspectParameterOfExitPoint(depth, df, expression, exitPoint);

			// 03.3 - If there a vulnerable path, then this variable is vulnerable.
			if (df.isVulnerable()) {
				// 03.4 - We found a vulnerability and have to report it.
				reportVulnerability(df);
			}

			return;
		}

		// 04 - There are 2 cases: When we have the source code of this method and when we do not.
		MethodDeclaration methodDeclaration = getCallGraph().getMethod(getCurrentResource(), expression);

		if (null != methodDeclaration) {
			// 04.1 - We have the source code.

		} else {
			// 04.2 - We do not have the source code.
			// Now we have to investigate if the element who is invoking this method is vulnerable or not.
			Expression objectName = expression.getExpression();
			if (isVulnerable(objectName)) {
				// We found a vulnerability.
			}
		}
	}

	protected void inspectParameterOfExitPoint(int depth, DataFlow df, MethodInvocation method, ExitPoint exitPoint) {
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

			// 05 - Get the index position where this parameter appear.
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
	 * 60 TODO Verify if we have to do something with the dfParent.
	 */
	protected void inspectVariableDeclarationStatement(int depth, DataFlow dfParent,
			VariableDeclarationStatement statement) {
		List<?> fragments = statement.fragments();
		for (Iterator<?> iter = fragments.iterator(); iter.hasNext();) {
			// VariableDeclarationFragment: is the plain variable declaration part.
			// Example: "int x=0, y=0;" contains two VariableDeclarationFragments, "x=0" and "y=0"
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) iter.next();

			SimpleName simpleName = fragment.getName();
			// 01 - Try to retrieve the variable from the list of variables.
			VariableBindingManager manager = getCallGraph().getLastReference(simpleName);
			if (null != manager) {
				// 02 - A new dataFlow for this variable.
				DataFlow df = new DataFlow(simpleName);

				// 03 - Inspect the Initializer to verify if this variable is vulnerable.
				inspectNode(depth, df, fragment.getInitializer());

				// 04 - If there a vulnerable path, then this variable is vulnerable.
				if (df.isVulnerable()) {
					manager.setVulnerable(df);
				}
			}
		}
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
