package net.thecodemaster.esvd.visitor;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.thecodemaster.esvd.constant.Constant;
import net.thecodemaster.esvd.context.Context;
import net.thecodemaster.esvd.graph.BindingResolver;
import net.thecodemaster.esvd.graph.CallGraph;
import net.thecodemaster.esvd.graph.CodeAnalyzer;
import net.thecodemaster.esvd.graph.Parameter;
import net.thecodemaster.esvd.graph.VariableBinding;
import net.thecodemaster.esvd.graph.flow.DataFlow;
import net.thecodemaster.esvd.graph.flow.Flow;
import net.thecodemaster.esvd.helper.Creator;
import net.thecodemaster.esvd.helper.HelperCodeAnalyzer;
import net.thecodemaster.esvd.logger.PluginLogger;
import net.thecodemaster.esvd.point.ExitPoint;
import net.thecodemaster.esvd.reporter.Reporter;
import net.thecodemaster.esvd.ui.enumeration.EnumRules;
import net.thecodemaster.esvd.verifier.Verifier;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
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

	/**
	 * The list of all verifiers of this analyzer.
	 */
	private List<Verifier>	verifiers;
	/**
	 * List with all the vulnerable paths found by this verifier.
	 */
	private List<DataFlow>	allVulnerablePaths;

	public VisitorPointsToAnalysis() {
		verifiers = Creator.newList();
	}

	private List<Verifier> getVerifiers() {
		return verifiers;
	}

	private void setVerifiers(List<Verifier> verifiers) {
		this.verifiers = verifiers;
	}

	@Override
	protected String getSubTaskMessage(int numberOfResourcesProcessed, int numberOfResources) {
		return String.format("%d/%d - %s", numberOfResourcesProcessed, numberOfResources, getCurrentResource().getName());
	}

	public List<DataFlow> run(List<IResource> resources, CallGraph callGraph, List<Verifier> verifiers, Reporter reporter) {
		setVerifiers(verifiers);
		setReporter(reporter);
		setCallGraph(callGraph);
		allVulnerablePaths = Creator.newList();

		Map<IResource, List<MethodDeclaration>> resourcesAndMethodsToProcess = getMethodsToProcess(resources);

		super.run(resourcesAndMethodsToProcess);

		if (allVulnerablePaths.size() > 0) {
			reportVulnerability(allVulnerablePaths);
		}

		return allVulnerablePaths;
	}

	private void reportVulnerability(List<DataFlow> allVulnerablePaths) {
		if (null != getReporter()) {
			getReporter().addProblem(getCurrentResource(), allVulnerablePaths);
		}
	}

	/**
	 * Run the vulnerability detection on the current method declaration.
	 * 
	 * @param methodDeclaration
	 */
	@Override
	protected void run(MethodDeclaration methodDeclaration) {
		// System.out.println(methodDeclaration);

		// 01 - Create a context for this method.
		Context context = getCallGraph().newContext(getCurrentResource(), methodDeclaration, null);

		// 02 - Get the root/first element that will be processed.
		Expression root = methodDeclaration.getName();

		// 03 - Start the detection on each and every line of this method.
		inspectNode(new Flow(root), context, new DataFlow(root), methodDeclaration.getBody());
	}

	/**
	 * 07 <br/>
	 * this.a = b <br/>
	 * a = b <br/>
	 * super.a = b <br/>
	 * Person.staticPersonVariable = b <br/>
	 * package.name.person.publicPersonVariable = b
	 */
	@Override
	protected void inspectAssignment(Flow loopControl, Context context, DataFlow dataFlow, Assignment node) {
		// 01 - Get the elements from the expression.
		Expression leftHandSide = node.getLeftHandSide();
		Expression rightHandSide = node.getRightHandSide();

		switch (leftHandSide.getNodeType()) {
			case ASTNode.ARRAY_ACCESS: // 02
			case ASTNode.FIELD_ACCESS: // 22
			case ASTNode.SIMPLE_NAME: // 42
			case ASTNode.SUPER_FIELD_ACCESS: // 47
				break; // Use the same current context.
			case ASTNode.QUALIFIED_NAME: // 40
				// * Get the context of the instance. (Object or static).
				context = getContext(context, leftHandSide);
				break;
			default:
				PluginLogger.logError("inspectAssignment Default Node Type: " + leftHandSide.getNodeType() + " - "
						+ leftHandSide, null);
		}

		// 02 - Try to find if this variable already exists into the current context.
		VariableBinding variableBindingOld = getCallGraph().getLastReference(context, leftHandSide);

		// 03 - Add the new variable to the callGraph.
		VariableBinding variableBindingNew = addVariableToCallGraphAndInspectInitializer(loopControl, context, dataFlow,
				leftHandSide, rightHandSide);

		// 04 - If the variable did not exist before and exists now, it means it is a reference to a global
		// (inheritance)
		// variable.
		if ((null == variableBindingOld) && (null != variableBindingNew)) {

			// 05 - Get the class context. (if any).
			Context classContext = context.getInstanceContext();

			if (null != classContext) {
				// 06 - Add the variable to the class context.
				VariableBinding variableBindingGlobal = getCallGraph().addFieldDeclaration(classContext, leftHandSide,
						rightHandSide);

				if (null != variableBindingGlobal) {
					// 07 - Update the status and the data flow of the global variable.
					variableBindingGlobal.setStatus(variableBindingNew.getStatus());
					HelperCodeAnalyzer.updateVariableBindingDataFlow(variableBindingGlobal, variableBindingNew.getDataFlow());

					// 08 - Update from LOCAL to GLOBAL in the new variable.
					variableBindingNew.setType(variableBindingGlobal.getType());
				}
			}
		}
	}

	/**
	 * 32
	 */
	@Override
	protected void inspectEachMethodInvocationOfChainInvocations(Flow loopControl, Context context, DataFlow dataFlow,
			Expression methodInvocation) {
		// 01 - Check if the method is a Sanitization-Point.
		if (isMethodASanitizationPoint(methodInvocation)) {
			// If a sanitization method is being invoked, then we do not have a vulnerability.
			return;
		}

		// 02 - Get a new data flow or a child from the parent.
		DataFlow newDataFlow = HelperCodeAnalyzer.getDataFlowMethodInvocation(dataFlow, methodInvocation);

		// 03 - Check if the method is an Entry-Point.
		if (isMethodAnEntryPoint(methodInvocation)) {
			// 04 - Check if there is a marker, in case there is, we should BELIEVE it is not vulnerable.
			if (hasMarkerAtPosition(methodInvocation)) {
				return;
			}

			String message = getMessageEntryPoint(BindingResolver.getFullName(methodInvocation));

			// We found an invocation to a entry point method.
			newDataFlow.hasVulnerablePath(Constant.Vulnerability.ENTRY_POINT, message);
			return;
		}

		// 05 - Add a method reference to this instance. i.e: a.method(a);
		if (methodInvocation.getNodeType() == ASTNode.METHOD_INVOCATION) {
			// 06 - Check if this method invocation has an instance.
			Expression instance = BindingResolver.getInstanceIfItIsAnObject(methodInvocation);

			if (null != instance) {
				addReferenceToInitializer(loopControl, context, methodInvocation, instance);
			}
		}

		// 07 - Check if the method is an Exit-Point (Only verifiers check that).
		ExitPoint exitPoint = getExitPointIfMethodIsOne(methodInvocation);

		if (null != exitPoint) {
			inspectExitPoint(loopControl, context, newDataFlow, methodInvocation, exitPoint);
			return;
		}

		// 08 - There are 2 cases: When we have the source code of this method and when we do not.
		super.inspectMethodInvocationWithOrWithOutSourceCode(loopControl, context, newDataFlow, methodInvocation);
	}

	private boolean isMethodAnEntryPoint(Expression methodInvocation) {
		return getEntryPointManager().isMethodAnEntryPoint(methodInvocation);
	}

	private boolean isMethodASanitizationPoint(Expression methodInvocation) {
		return getSanitizationManager().isMethodASanitizationPoint(methodInvocation);
	}

	private ExitPoint getExitPointIfMethodIsOne(Expression methodInvocation) {
		return getExitPointManager().getExitPointIfMethodIsOne(getVerifiers(), methodInvocation);
	}

	protected void inspectExitPoint(Flow loopControl, Context context, DataFlow dataFlow, Expression method,
			ExitPoint exitPoint) {
		// 01 - Get the parameters (received) from the current method.
		List<Expression> receivedParameters = BindingResolver.getParameters(method);

		// 02 - Get the expected parameters of the ExitPoint method.
		Map<Parameter, Integer> expectedParameters = exitPoint.getParameters();

		int index = 0;
		for (int currentRules : expectedParameters.values()) {
			// If the rules are -1, it means the expected parameter can be anything. (We do not care for it).
			if (EnumRules.ANYTHING_IS_VALID.value() != currentRules) {
				Expression expression = receivedParameters.get(index);
				DataFlow newDataFlow = new DataFlow(expression);

				// 03 - Check if there is a marker, in case there is, we should BELIEVE it is not vulnerable.
				if (!hasMarkerAtPosition(expression)) {
					// 04 - Add a method reference to this variable (if it is a variable).
					addReferenceToInitializer(loopControl, context, method, expression);

					// 05 -
					inspectNode(loopControl, context, newDataFlow, expression);

					// 06 -
					Verifier verifier = exitPoint.getVerifier();

					if (null != verifier) {
						// 07 - Some verifiers required most tests.
						verifier.run(getCallGraph(), getCurrentResource(), getCurrentCompilationUnit(), loopControl, context,
								newDataFlow, expression, currentRules);

						// 08 - If the data flow has a vulnerable path, we set the verifier who found it.
						if (newDataFlow.hasVulnerablePath()) {
							newDataFlow.setPriority(verifier.getPriority());
							newDataFlow.setTypeProblem(verifier.getId());
							newDataFlow.setFullPath(loopControl);
							allVulnerablePaths.add(newDataFlow);

							dataFlow.addNodeToPath(expression).replace(newDataFlow);
						}
					}
				}
			}
			index++;
		}
	}

	private void updateDataBinding(Expression expression, DataFlow dataFlow, VariableBinding variableBinding) {
		if (HelperCodeAnalyzer.isPrimitive(expression)) {
			HelperCodeAnalyzer.updateVariableBindingStatusToPrimitive(variableBinding);
		} else {
			HelperCodeAnalyzer.updateVariableBinding(variableBinding, dataFlow);
		}
	}

	@Override
	protected void methodHasBeenProcessed(Flow loopControl, Context context, DataFlow dataFlow,
			Expression methodInvocation, MethodDeclaration methodDeclaration) {
		// 01 - Get a variable binding if it is an object.
		VariableBinding variableBinding = HelperCodeAnalyzer.getVariableBindingIfItIsAnObject(getCallGraph(), context,
				methodInvocation);

		// We found a vulnerability.
		if (dataFlow.hasVulnerablePath()) {
			// There are 2 sub-cases: When is a method from an object and when is a method from a library.
			// 01 - stringBuilder.append("...");
			// 02 - System.out.println("..."); Nothing else to do.

			// 02 - Check if this method invocation is being call from a vulnerable object.
			UpdateIfVulnerable(variableBinding, dataFlow);
		} else {
			super
					.methodHasBeenProcessed(loopControl, context, dataFlow, methodInvocation, methodDeclaration, variableBinding);
		}
	}

	@Override
	protected void UpdateIfVulnerable(VariableBinding variableBinding, DataFlow dataFlow) {
		// 02 - If there is a vulnerable path, then this variable is vulnerable.
		HelperCodeAnalyzer.updateVariableBinding(variableBinding, dataFlow);
	}

	@Override
	protected void inspectMethodWithSourceCode(Flow loopControl, Context context, DataFlow dataFlow,
			ASTNode methodInvocation, MethodDeclaration methodDeclaration) {
		// 01 - Get the current method declaration where this invocation is being performed.
		MethodDeclaration currentMethod = BindingResolver.getParentMethodDeclaration(methodInvocation);

		// 02 - Add this method invocation into the current context.
		getCallGraph().addMethodInvocation(context, currentMethod, methodInvocation);

		// 03 - Create a context for this method.
		Context newContext = getContext(loopControl, context, methodDeclaration, methodInvocation);

		// 04 - If this method declaration has parameters, we have to add the values from
		// the invocation to these parameters.
		addParametersToCallGraph(loopControl, newContext, dataFlow, methodInvocation, methodDeclaration);

		// 05 - Now I inspect the body of the method.
		super.inspectMethodWithSourceCode(loopControl, newContext, dataFlow, methodInvocation, methodDeclaration);
	}

	@Override
	protected void inspectMethodWithOutSourceCode(Flow loopControl, Context context, DataFlow dataFlow, ASTNode method) {
		switch (method.getNodeType()) {
			case ASTNode.CLASS_INSTANCE_CREATION:
				Expression instance = BindingResolver.getInstanceIfItIsAnObject(method);
				context = getCallGraph().newClassContext(context, null, method, instance);
				break;
		}

		// We have to iterate over its parameters to see if any is vulnerable.
		// If there is a vulnerable parameter and if this is a method from an object
		// we set this object as vulnerable.
		List<Expression> parameters = BindingResolver.getParameters(method);
		for (Expression parameter : parameters) {
			// 01 - Add a method reference to this variable (if it is a variable).
			addReferenceToInitializer(loopControl, context, method, parameter);

			inspectNode(loopControl, context, dataFlow.addNodeToPath(parameter), parameter);
		}
	}

	@Override
	protected Context getContext(Flow loopControl, Context context, MethodDeclaration methodDeclaration,
			ASTNode methodInvocation) {
		// We have 8 cases:
		// 01 - method(...);
		// 02 - method1(...).method2(...).method3(...);
		// 03 - obj.method(...);
		// 04 - obj.method1(...).method2(...).method3(...);
		// 05 - getObj(...).method(...);
		// 06 - Class.staticMethod(...);
		// 07 - Class obj = new Class(...);
		// 08 - this(...), super(...)
		// 09 - TODO - (new Class(...)).run(..);
		Expression instance = BindingResolver.getInstanceIfItIsAnObject(methodInvocation);

		if (methodDeclaration.isConstructor()) {
			switch (methodInvocation.getNodeType()) {
				case ASTNode.CONSTRUCTOR_INVOCATION: // 17
				case ASTNode.SUPER_CONSTRUCTOR_INVOCATION: // 46
					// Cases: 08
					return getCallGraph().newInstanceContext(context, methodDeclaration, methodInvocation, instance);
				default:
					// Cases: 07
					return getCallGraph().newClassContext(context, methodDeclaration, methodInvocation, instance);
			}
		} else {
			if (null != instance) {
				if (Modifier.isStatic(methodDeclaration.getModifiers())) {
					// Cases: 06
					return getCallGraph().newStaticContext(context, methodDeclaration, methodInvocation);
				} else {
					// Cases: 03, 04, 05
					// The instance must exist, if it does not, it is probably an assignment or syntax error.
					// Animal a1 = new Animal() / Animal a2 = a1 / a1.method();
					instance = findRealInstance(loopControl, context, instance);

					return getCallGraph().newInstanceContext(context, methodDeclaration, methodInvocation, instance);
				}
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
	protected void inspectReturnStatement(Flow loopControl, Context context, DataFlow dataFlow, ReturnStatement statement) {
		// 01 - Add a reference of the variable into the initializer (if the initializer is also a variable).
		addReferenceToInitializer(loopControl, context, statement, statement.getExpression());

		super.inspectReturnStatement(loopControl, context, dataFlow, statement);
	}

	/**
	 * 60
	 */
	@Override
	protected void inspectVariableDeclarationStatement(Flow loopControl, Context context, DataFlow dataFlow,
			VariableDeclarationStatement statement) {
		for (Iterator<?> iter = statement.fragments().iterator(); iter.hasNext();) {
			// VariableDeclarationFragment: is the plain variable declaration part.
			// Example: "int x=0, y=0;" contains two VariableDeclarationFragments, "x=0" and "y=0"
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) iter.next();

			// 01 - Add the new variable to the callGraph.
			addVariableToCallGraphAndInspectInitializer(loopControl, context, dataFlow, fragment.getName(),
					fragment.getInitializer());
		}
	}

	/**
	 * 70
	 */
	@Override
	protected void inspectEnhancedForStatement(Flow loopControl, Context context, DataFlow dataFlow,
			EnhancedForStatement statement) {
		SingleVariableDeclaration parameter = statement.getParameter();
		Expression expression = statement.getExpression();
		// 01 - Add the new variable to the callGraph.
		addVariableToCallGraphAndInspectInitializer(loopControl, context, dataFlow, parameter.getName(), expression);

		super.inspectEnhancedForStatement(loopControl, context, dataFlow, statement);
	}

	/**
	 * 07, 60, 70
	 */
	private VariableBinding addVariableToCallGraphAndInspectInitializer(Flow loopControl, Context context,
			DataFlow dataFlow, Expression name, Expression initializer) {
		// 01 - Add a reference of the variable into the initializer (if the initializer is also a variable).
		addReferenceToInitializer(loopControl, context, name, initializer);

		// 02 - Inspect the Initializer to verify if this variable is vulnerable.
		DataFlow newDataFlow = HelperCodeAnalyzer.getDataFlowVariable(dataFlow, name);

		inspectNode(loopControl, context, newDataFlow, initializer);

		// 03 - Add the variable to the current context.
		VariableBinding variableBinding = getCallGraph().addVariable(context, name, initializer);

		// 04 - Update the data binding of this variable.
		updateDataBinding(name, newDataFlow, variableBinding);

		return variableBinding;
	}

	private void addParametersToCallGraph(Flow loopControl, Context context, DataFlow dataFlow, ASTNode methodInvocation,
			MethodDeclaration methodDeclaration) {
		// 01 - Get the parameters of this method declaration.
		List<SingleVariableDeclaration> parameters = BindingResolver.getParameters(methodDeclaration);

		if (parameters.size() > 0) {
			int parameterIndex = 0;
			for (SingleVariableDeclaration parameter : parameters) {
				// 02 - The SimpleName of this parameter will be used for the addVariableToCallGraph.
				SimpleName name = parameter.getName();

				// 03 - Retrieve the variable binding of this parameter from the callGraph.
				Expression initializer = BindingResolver.getParameterAtIndex(methodInvocation, parameterIndex++);

				// 04 - Add a reference to this variable (if it is a variable).
				addReferenceToInitializer(loopControl, context, name, initializer);

				// 05 - Add a method reference to this variable (if it is a variable).
				addReferenceToInitializer(loopControl, context, methodInvocation, initializer);

				// 06 - Create a new Data flow for this parameter.
				DataFlow newDataFlow = new DataFlow(name);

				// 07 - Inspect this element.
				inspectNode(loopControl, context, newDataFlow.addNodeToPath(initializer), initializer);

				// 08 - Add the content with the one that came from the method invocation.
				VariableBinding variableBinding = getCallGraph().addParameter(context, name, initializer);

				// 09 - If there is a vulnerable path, then this variable is vulnerable.
				// But if this variable is of primitive type, then there is nothing to do because they can not be
				// vulnerable.
				updateDataBinding(name, newDataFlow, variableBinding);
			}
		}
	}

	private void addReferenceToInitializer(Flow loopControl, Context context, ASTNode expression, Expression initializer) {
		// 01 - To avoid infinitive loop, this check is necessary.
		if (hasLoop(loopControl)) {
			PluginLogger.logError("addReferenceToInitializer: " + expression + " - " + initializer + " - " + loopControl,
					null);
			return;
		}

		if (null != initializer) {
			switch (initializer.getNodeType()) {
				case ASTNode.ARRAY_ACCESS: // 02
					addReferenceArrayAccess(loopControl, context, expression, (ArrayAccess) initializer);
					break;
				case ASTNode.ARRAY_CREATION: // 03
					addReferenceArrayCreation(loopControl, context, expression, (ArrayCreation) initializer);
					break;
				case ASTNode.ARRAY_INITIALIZER: // 04
					addReferenceArrayInitializer(loopControl, context, expression, (ArrayInitializer) initializer);
					break;
				case ASTNode.ASSIGNMENT: // 07
					addReferenceAssignment(loopControl, context, expression, (Assignment) initializer);
					break;
				case ASTNode.CAST_EXPRESSION: // 11
					inspectCastExpression(loopControl, context, expression, (CastExpression) initializer);
					break;
				case ASTNode.CONDITIONAL_EXPRESSION: // 16
					addReferenceConditionalExpression(loopControl, context, expression, (ConditionalExpression) initializer);
					break;
				case ASTNode.FIELD_ACCESS: // 22
					addReferenceFieldAccess(loopControl, context, expression, (FieldAccess) initializer);
					break;
				case ASTNode.INFIX_EXPRESSION: // 27
					addReferenceInfixExpression(loopControl, context, expression, (InfixExpression) initializer);
					break;
				case ASTNode.PARENTHESIZED_EXPRESSION: // 36
					addReferenceParenthesizedExpression(loopControl, context, expression, (ParenthesizedExpression) initializer);
					break;
				case ASTNode.SUPER_FIELD_ACCESS: // 47
					addReferenceSuperFieldAccess(loopControl, context, expression, (SuperFieldAccess) initializer);
					break;
				case ASTNode.QUALIFIED_NAME: // 40
				case ASTNode.SIMPLE_NAME: // 42
					addReferenceName(loopControl, context, expression, (Name) initializer);
					break;
			}
		}
	}

	private void addReference(Context context, ASTNode expression, Expression initializer) {
		context = getContext(context, initializer);

		VariableBinding variableBinding = getCallGraph().getLastReference(context, initializer);
		if (null != variableBinding) {
			variableBinding.addReference(expression, context.hashCode());
		} else {
			// PluginLogger.logError("addReference else: " + expression + " - " + initializer, null);
		}
	}

	private void addReferenceName(Flow loopControl, Context context, ASTNode expression, Name initializer) {
		addReference(context, expression, initializer);
	}

	private void addReferenceArrayAccess(Flow loopControl, Context context, ASTNode expression, ArrayAccess initializer) {
		addReference(context, expression, initializer);
	}

	private void addReferenceArrayCreation(Flow loopControl, Context context, ASTNode expression,
			ArrayCreation initializer) {
		List<Expression> expressions = BindingResolver.getParameters(initializer);

		for (Expression current : expressions) {
			addReferenceToInitializer(loopControl, context, expression, current);
		}
	}

	private void addReferenceArrayInitializer(Flow loopControl, Context context, ASTNode expression,
			ArrayInitializer initializer) {
		List<Expression> expressions = BindingResolver.getParameters(initializer);

		for (Expression current : expressions) {
			addReferenceToInitializer(loopControl, context, expression, current);
		}
	}

	private void addReferenceAssignment(Flow loopControl, Context context, ASTNode expression, Assignment initializer) {
		addReferenceToInitializer(loopControl, context, expression, initializer.getLeftHandSide());
		addReferenceToInitializer(loopControl, context, expression, initializer.getRightHandSide());
	}

	private void inspectCastExpression(Flow loopControl, Context context, ASTNode expression, CastExpression initializer) {
		addReferenceToInitializer(loopControl, context, expression, initializer.getExpression());
	}

	private void addReferenceConditionalExpression(Flow loopControl, Context context, ASTNode expression,
			ConditionalExpression initializer) {
		addReferenceToInitializer(loopControl, context, expression, initializer.getThenExpression());
		addReferenceToInitializer(loopControl, context, expression, initializer.getElseExpression());
	}

	private void addReferenceFieldAccess(Flow loopControl, Context context, ASTNode expression, FieldAccess initializer) {
		addReferenceToInitializer(loopControl, context, expression, initializer.getName());
	}

	private void addReferenceInfixExpression(Flow loopControl, Context context, ASTNode expression,
			InfixExpression initializer) {
		addReferenceToInitializer(loopControl, context, expression, initializer.getLeftOperand());
		addReferenceToInitializer(loopControl, context, expression, initializer.getRightOperand());

		List<Expression> extendedOperands = BindingResolver.getParameters(initializer);

		for (Expression current : extendedOperands) {
			addReferenceToInitializer(loopControl, context, expression, current);
		}
	}

	private void addReferenceParenthesizedExpression(Flow loopControl, Context context, ASTNode expression,
			ParenthesizedExpression initializer) {
		addReferenceToInitializer(loopControl, context, expression, initializer.getExpression());
	}

	private void addReferenceSuperFieldAccess(Flow loopControl, Context context, ASTNode expression,
			SuperFieldAccess initializer) {
		addReferenceToInitializer(loopControl, context, expression, initializer.getName());
	}

}