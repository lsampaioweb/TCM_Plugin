package net.thecodemaster.sap.verifiers;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.thecodemaster.sap.constants.Constants;
import net.thecodemaster.sap.graph.BindingResolver;
import net.thecodemaster.sap.graph.CallGraph;
import net.thecodemaster.sap.graph.Parameter;
import net.thecodemaster.sap.graph.VariableBindingManager;
import net.thecodemaster.sap.graph.VulnerabilityPath;
import net.thecodemaster.sap.loggers.PluginLogger;
import net.thecodemaster.sap.points.EntryPoint;
import net.thecodemaster.sap.points.ExitPoint;
import net.thecodemaster.sap.reporters.Reporter;
import net.thecodemaster.sap.utils.Creator;
import net.thecodemaster.sap.xmlloaders.ExitPointLoader;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 * @author Luciano Sampaio
 */
public abstract class Verifier {

	/**
	 * The name of the current verifier.
	 */
	private final String						verifierName;
	/**
	 * The id of the current verifier.
	 */
	private final int								verifierId;
	/**
	 * The current resource that is being analyzed.
	 */
	private IResource								currentResource;
	/**
	 * This object contains all the methods, variables and their interactions, on the project that is being analyzed.
	 */
	private CallGraph								callGraph;
	/**
	 * The report object
	 */
	private Reporter								reporter;
	/**
	 * List with all the ExitPoints of this verifier (shared among other instances of this verifier).
	 */
	private static List<ExitPoint>	exitPoints;
	/**
	 * List with all the EntryPoints (shared among other instances of the verifiers).
	 */
	private static List<EntryPoint>	entryPoints;

	/**
	 * @param name
	 *          The name of the verifier.
	 * @param id
	 *          The id of the verifier.
	 */
	public Verifier(String name, int id) {
		this.verifierName = name;
		this.verifierId = id;
	}

	/**
	 * @param name
	 *          The name of the verifier.
	 * @param id
	 *          The id of the verifier.
	 * @param listEntryPoints
	 *          List with all the EntryPoints methods.
	 */
	public Verifier(String name, int id, List<EntryPoint> listEntryPoints) {
		this(name, id);
		entryPoints = listEntryPoints;
	}

	public String getName() {
		return verifierName;
	}

	private int getVerifierId() {
		return verifierId;
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
			loadExitPoints();
		}

		return exitPoints;
	}

	protected void loadExitPoints() {
		exitPoints = (new ExitPointLoader(getVerifierId())).load();
	}

	protected static List<EntryPoint> getEntryPoints() {
		if (null == entryPoints) {
			entryPoints = Creator.newList();
		}

		return entryPoints;
	}

	public void run(List<IResource> resources, CallGraph callGraph, Reporter reporter) {
		this.callGraph = callGraph;
		this.reporter = reporter;

		setSubTask(getName());

		// Perform the verifications on the resources.
		run(resources);
	}

	protected void run(List<IResource> resources) {
		// 01 - Run the vulnerability detection on all the provided resources.
		for (IResource resource : resources) {
			if (getCallGraph().containsFile(resource)) {
				// 02 - Delete any old markers of this resource.
				getReporter().clearProblems(resource);

				// 03 - Get the list of methods in the current resource.
				Map<MethodDeclaration, List<Expression>> methods = getCallGraph().getMethods(resource);

				// 04 - Get all the method invocations of each method declaration.
				for (List<Expression> invocations : methods.values()) {

					// 05 - Iterate over all method invocations to verify if it is a ExitPoint.
					for (Expression method : invocations) {
						ExitPoint exitPoint = getExitPointIfMethodIsOne(method);

						if (null != exitPoint) {
							// 06 - Some methods will need to have access to the resource that is currently being analyzed.
							// but we do not want to pass it to all these methods as a parameter.
							setCurrentResource(resource);

							// 07 - This is an ExitPoint method and it needs to be verified.
							run(method, exitPoint);
						}
					}

				}
			}
		}
	}

	protected void run(Expression method, ExitPoint exitPoint) {
		// 01 - Get the expected parameters of the ExitPoint method.
		Map<Parameter, List<Integer>> expectedParameters = exitPoint.getParameters();

		// 02 - Get the parameters (received) from the current method.
		List<Expression> receivedParameters = BindingResolver.getParameters(method);

		int index = 0;
		int depth = 0;
		Expression expr;
		VulnerabilityPath vp;
		for (List<Integer> rules : expectedParameters.values()) {
			// If the rules are null, it means the expected parameter can be anything. (We do not care for it).
			if (null != rules) {
				expr = receivedParameters.get(index);
				vp = new VulnerabilityPath(expr);

				checkExpression(vp, rules, expr, depth);
				if (!vp.isEmpty()) {
					reportVulnerability(vp);
				}
			}
			index++;
		}
	}

	/**
	 * @param method
	 * @return An ExitPoint object if this node belongs to the list, otherwise null.
	 */
	protected ExitPoint getExitPointIfMethodIsOne(Expression method) {
		for (ExitPoint currentExitPoint : getExitPoints()) {
			if (BindingResolver.methodsHaveSameNameAndPackage(currentExitPoint, method)) {
				// 05 - Get the expected arguments of this method.
				Map<Parameter, List<Integer>> expectedParameters = currentExitPoint.getParameters();

				// 06 - Get the received parameters of the current method.
				List<Expression> receivedParameters = BindingResolver.getParameters(method);

				// 07 - It is necessary to check the number of parameters and its types
				// because it may exist methods with the same names but different parameters.
				if (expectedParameters.size() == receivedParameters.size()) {
					boolean isMethodAnExitPoint = true;
					int index = 0;
					for (Parameter expectedParameter : expectedParameters.keySet()) {
						ITypeBinding typeBinding = receivedParameters.get(index++).resolveTypeBinding();

						// Verify if all the parameters are the ones expected. However, there is a case
						// where an Object is expected, and any type is accepted.
						if (!BindingResolver.parametersHaveSameType(expectedParameter.getQualifiedName(), typeBinding)) {
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

	protected boolean isMethodAnEntryPoint(Expression method) {
		for (EntryPoint currentEntryPoint : getEntryPoints()) {
			if (BindingResolver.methodsHaveSameNameAndPackage(currentEntryPoint, method)) {
				// 05 - Get the expected arguments of this method.
				List<String> expectedParameters = currentEntryPoint.getParameters();

				// 06 - Get the received parameters of the current method.
				List<Expression> receivedParameters = BindingResolver.getParameters(method);

				// 07 - It is necessary to check the number of parameters and its types
				// because it may exist methods with the same names but different parameters.
				if (expectedParameters.size() == receivedParameters.size()) {
					int index = 0;
					for (String expectedParameter : expectedParameters) {
						ITypeBinding typeBinding = receivedParameters.get(index++).resolveTypeBinding();

						// Verify if all the parameters are the ones expected.
						if (!BindingResolver.parametersHaveSameType(expectedParameter, typeBinding)) {
							return false;
						}
					}

					return true;
				}
			}
		}

		return false;
	}

	protected boolean isMethodASanitizationPoint(Expression method) {
		return false;
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

	protected String getMessageLiteral(String value) {
		return null;
	}

	protected String getMessageEntryPoint(String value) {
		return null;
	}

	protected String getMessageNullLiteral() {
		return null;
	}

	protected void checkExpression(VulnerabilityPath vp, List<Integer> rules, Expression expr, int depth) {
		// 01 - If the parameter matches the rules (Easy case), the parameter is okay, otherwise we need to check for more
		// things.
		if (!matchRules(rules, expr)) {

			// To avoid infinitive loop, this check is necessary.
			if (Constants.MAXIMUM_DEPTH == depth) {
				// Informs that we can no longer investigate because it looks like we are in an infinitive loop.
				vp.foundInfinitiveLoop(expr);

				return;
			}

			// 02 - We need to check the type of the parameter and deal with it accordingly to its type.
			switch (expr.getNodeType()) {
				case ASTNode.STRING_LITERAL:
				case ASTNode.NUMBER_LITERAL:
				case ASTNode.NULL_LITERAL:
					checkLiteral(vp, expr);
					break;
				case ASTNode.INFIX_EXPRESSION:
					checkInfixExpression(vp, rules, expr, ++depth);
					break;
				// case ASTNode.PREFIX_EXPRESSION:
				// TODO.
				// break;
				case ASTNode.CONDITIONAL_EXPRESSION:
					checkConditionExpression(vp, rules, expr, ++depth);
					break;
				case ASTNode.SIMPLE_NAME:
					checkSimpleName(vp, rules, expr, ++depth);
					break;
				case ASTNode.METHOD_INVOCATION:
					checkMethodInvocation(vp, rules, expr, ++depth);
					break;
				default:
					PluginLogger.logError("Default Node Type: " + expr.getNodeType() + " - " + expr, null);
			}
		}
	}

	protected boolean matchRules(List<Integer> rules, Expression parameter) {
		if (null == parameter) {
			// There is nothing we can do to verify it.
			return true;
		}

		for (Integer astNodeValue : rules) {
			if (astNodeValue == parameter.getNodeType()) {
				return true;
			}
		}

		return false;
	}

	protected void checkLiteral(VulnerabilityPath vp, Expression expr) {
		String message = null;
		switch (expr.getNodeType()) {
			case ASTNode.STRING_LITERAL:
				message = getMessageLiteral(((StringLiteral) expr).getLiteralValue());
				break;
			case ASTNode.NUMBER_LITERAL:
				message = getMessageLiteral(((NumberLiteral) expr).getToken());
				break;
			case ASTNode.NULL_LITERAL:
				message = getMessageNullLiteral();
				break;
		}

		// 01 - Informs that this node is a vulnerability.
		vp.foundVulnerability(expr, message);
	}

	protected void checkInfixExpression(VulnerabilityPath vp, List<Integer> rules, Expression expr, int depth) {
		InfixExpression parameter = (InfixExpression) expr;

		// 01 - Get the elements from the operation.
		Expression leftOperand = parameter.getLeftOperand();
		Expression rightOperand = parameter.getRightOperand();
		List<Expression> extendedOperands = BindingResolver.getParameters(parameter.extendedOperands());

		// 02 - Check each element.
		checkExpression(vp.addNodeToPath(leftOperand), rules, leftOperand, depth);
		checkExpression(vp.addNodeToPath(rightOperand), rules, rightOperand, depth);

		for (Expression expression : extendedOperands) {
			checkExpression(vp.addNodeToPath(expression), rules, expression, depth);
		}
	}

	protected void checkConditionExpression(VulnerabilityPath vp, List<Integer> rules, Expression expr, int depth) {
		ConditionalExpression parameter = (ConditionalExpression) expr;

		// 01 - Get the elements from the operation.
		Expression thenExpression = parameter.getThenExpression();
		Expression elseExpression = parameter.getElseExpression();

		// 02 - Check each element.
		checkExpression(vp.addNodeToPath(thenExpression), rules, thenExpression, depth);
		checkExpression(vp.addNodeToPath(elseExpression), rules, elseExpression, depth);
	}

	protected void checkSimpleName(VulnerabilityPath vp, List<Integer> rules, Expression expr, int depth) {
		SimpleName simpleName = (SimpleName) expr;
		IBinding binding = simpleName.resolveBinding();

		// 01 - Try to retrieve the variable from the list of variables.
		VariableBindingManager manager = getCallGraph().getlistVariables().get(binding);
		if (null != manager) {

			// 02 - This is the case where we have to go deeper into the variable's path.
			Expression initializer = manager.getInitializer();
			checkExpression(vp.addNodeToPath(initializer), rules, initializer, depth);
		} else {
			// This is the case where the variable is an argument of the method.
			// 04 - Get the method signature that is using this parameter.
			MethodDeclaration methodDeclaration = BindingResolver.getParentMethodDeclaration(simpleName);

			// 05 - Get the index position where this parameter appear.
			int parameterIndex = BindingResolver.getParameterIndex(methodDeclaration, simpleName);
			if (parameterIndex >= 0) {
				// 06 - Get the list of methods that invokes this method.
				Map<MethodDeclaration, List<Expression>> invokers = getCallGraph().getInvokers(methodDeclaration);

				if (null != invokers) {
					// 07 - Iterate over all the methods that invokes this method.
					for (Entry<MethodDeclaration, List<Expression>> current : invokers.entrySet()) {
						List<Expression> currentInvocations = current.getValue();

						// 08 - Care only about the invocations to this method.
						for (Expression expression : currentInvocations) {
							if (BindingResolver.areMethodsEqual(methodDeclaration, expression)) {
								// 09 - Get the parameter at the index position.
								Expression parameter = BindingResolver.getParameterAtIndex(expression, parameterIndex);

								// 10 - Run detection on this parameter.
								checkExpression(vp.addNodeToPath(parameter), rules, parameter, depth);
							}
						}

					}
				}
			}

		}
	}

	protected void checkMethodInvocation(VulnerabilityPath vp, List<Integer> rules, Expression expr, int depth) {
		// 01 - Check if this method is a Sanitization-Point.
		if (isMethodASanitizationPoint(expr)) {
			// If a sanitization method is being invoked, then we do not have a vulnerability.
			return;
		}

		// 02 - Check if this method is a Entry-Point.
		if (isMethodAnEntryPoint(expr)) {
			String message = getMessageEntryPoint(BindingResolver.getFullName(expr));

			// If a entry point method is being invoked, then we DO have a vulnerability.
			vp.foundVulnerability(expr, message);
			return;
		}

		// 03 - Follow the data flow of this method and try to identify what is the return from it.

		// Get the implementation of this method. If the return is NULL it means this is a library that the developer
		// does not own the source code.
		MethodDeclaration methodDeclaration = getCallGraph().getMethod(getCurrentResource(), expr);

		if (null != methodDeclaration) {
			checkBlock(vp, rules, methodDeclaration.getBody(), depth);
		} else {
			System.out.println("Method:" + expr);
		}
	}

	protected void checkBlock(VulnerabilityPath vp, List<Integer> rules, Block block, int depth) {
		List<?> statements = block.statements();
		for (Object object : statements) {
			checkStatement(vp, rules, (Statement) object, depth);
		}
	}

	protected void checkStatement(VulnerabilityPath vp, List<Integer> rules, Statement statement, int depth) {
		if (statement.getNodeType() == ASTNode.RETURN_STATEMENT) {
			Expression expr = ((ReturnStatement) statement).getExpression();
			checkExpression(vp.addNodeToPath(expr), rules, expr, depth);
		} else if (Constants.MAXIMUM_DEPTH == depth) {
			// To avoid infinitive loop, this check is necessary.
			// Informs that we can no longer investigate because it looks like we are in an infinitive loop.
			vp.foundInfinitiveLoop(statement);

			return;
		} else {
			switch (statement.getNodeType()) {
				case ASTNode.FOR_STATEMENT:
					checkIfBlockOrStatement(vp, rules, ((ForStatement) statement).getBody(), depth);
					break;
				case ASTNode.WHILE_STATEMENT:
					checkIfBlockOrStatement(vp, rules, ((WhileStatement) statement).getBody(), depth);
					break;
				case ASTNode.DO_STATEMENT:
					checkIfBlockOrStatement(vp, rules, ((DoStatement) statement).getBody(), depth);
					break;
				case ASTNode.IF_STATEMENT:
					IfStatement is = (IfStatement) statement;

					checkIfBlockOrStatement(vp, rules, is.getThenStatement(), depth);
					checkIfBlockOrStatement(vp, rules, is.getElseStatement(), depth);
					break;
				case ASTNode.TRY_STATEMENT:
					TryStatement tryStatement = (TryStatement) statement;

					checkIfBlockOrStatement(vp, rules, tryStatement.getBody(), depth);

					List<?> listCatches = tryStatement.catchClauses();
					for (Object catchClause : listCatches) {
						checkIfBlockOrStatement(vp, rules, ((CatchClause) catchClause).getBody(), depth);
					}

					checkIfBlockOrStatement(vp, rules, tryStatement.getFinally(), depth);
					break;
				case ASTNode.SWITCH_STATEMENT:
					SwitchStatement switchStatement = (SwitchStatement) statement;

					List<?> switchStatements = switchStatement.statements();
					for (Object switchCases : switchStatements) {
						checkIfBlockOrStatement(vp, rules, (Statement) switchCases, depth);
					}
					break;
			}
		}
	}

	protected void checkIfBlockOrStatement(VulnerabilityPath vp, List<Integer> rules, Statement statement, int depth) {
		if (null == statement) {
			return;
		}

		switch (statement.getNodeType()) {
			case ASTNode.BLOCK:
				checkBlock(vp, rules, (Block) statement, ++depth);
				break;
			default:
				checkStatement(vp, rules, statement, ++depth);
				break;
		}
	}

	protected void reportVulnerability(VulnerabilityPath vp) {
		getReporter().addProblem(getVerifierId(), getCurrentResource(), vp);
	}

}
