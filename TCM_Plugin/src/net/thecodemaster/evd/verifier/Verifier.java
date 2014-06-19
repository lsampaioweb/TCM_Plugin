package net.thecodemaster.evd.verifier;

import java.util.List;
import java.util.Map;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.context.Context;
import net.thecodemaster.evd.graph.BindingResolver;
import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.graph.CodeAnalyzer;
import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.graph.Parameter;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.helper.HelperCodeAnalyzer;
import net.thecodemaster.evd.point.ExitPoint;
import net.thecodemaster.evd.reporter.Reporter;
import net.thecodemaster.evd.xmlloader.LoaderExitPoint;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;

/**
 * The verifier is the class that actually knows how to find the vulnerability and the one that performs this
 * verification. Each verifier can reimplement/override methods of add new behavior to them.
 * 
 * @author Luciano Sampaio
 */
public abstract class Verifier extends CodeAnalyzer {

	/**
	 * The id of the current verifier.
	 */
	private final int				id;
	/**
	 * The name of the current verifier.
	 */
	private final String		name;
	/**
	 * The object that know how and where to report the found vulnerabilities.
	 */
	private Reporter				reporter;
	/**
	 * List with all the ExitPoints of this verifier.
	 */
	private List<ExitPoint>	exitPoints;
	/**
	 * List with all the vulnerable paths found by this verifier.
	 */
	private List<DataFlow>	allVulnerablePaths;
	/**
	 * The rules that the current parameter must obey.
	 */
	private List<Integer>		rules;

	/**
	 * @param name
	 *          The name of the verifier.
	 * @param id
	 *          The id of the verifier.
	 * @param listEntryPoints
	 *          List with all the EntryPoints methods.
	 */
	public Verifier(int id, String name) {
		this.id = id;
		this.name = name;
	}

	protected int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	protected void setReporter(Reporter reporter) {
		this.reporter = reporter;
	}

	protected Reporter getReporter() {
		return reporter;
	}

	@Override
	protected IProgressMonitor getProgressMonitor() {
		if ((null != getReporter()) && (null != getReporter().getProgressMonitor())) {
			return getReporter().getProgressMonitor();
		}
		return null;
	}

	@Override
	protected String getSubTaskMessage() {
		return String.format("%s: %s", getName(), getCurrentResource().getName());
	}

	protected List<Integer> getRules() {
		return rules;
	}

	protected void setRules(List<Integer> rules) {
		this.rules = rules;
	}

	protected List<ExitPoint> getExitPoints() {
		if (null == exitPoints) {
			// Loads all the ExitPoints of this verifier.
			exitPoints = (new LoaderExitPoint(getId())).load();
		}

		return exitPoints;
	}

	/**
	 * The public run method that will be invoked by the Analyzer.
	 * 
	 * @param resources
	 * @param callGraph
	 * @param reporter
	 * @return
	 */
	public List<DataFlow> run(Reporter reporter, CallGraph callGraph, List<IResource> resources) {
		setReporter(reporter);
		allVulnerablePaths = Creator.newList();

		super.run(getProgressMonitor(), callGraph, resources);

		return allVulnerablePaths;
	}

	/**
	 * Run the vulnerability detection on the current method declaration.
	 * 
	 * @param methodDeclaration
	 */
	@Override
	protected void run(int depth, MethodDeclaration methodDeclaration, Expression invoker) {
		// 01 - Get the context for this method.
		Context context = getCallGraph().getContext(getCurrentResource(), methodDeclaration, invoker);

		// 02 - Start the detection on each and every line of this method.
		inspectNode(depth, context, new DataFlow(methodDeclaration.getName()), methodDeclaration.getBody());
	}

	/**
	 * 07
	 */
	@Override
	protected void inspectAssignment(int depth, Context context, DataFlow dataFlow, Assignment expression) {
		Expression rightHandSide = expression.getRightHandSide();

		inspectNode(depth, context, dataFlow.addNodeToPath(rightHandSide), rightHandSide);
	}

	/**
	 * 32
	 */
	@Override
	protected void inspectEachMethodInvocationOfChainInvocations(int depth, Context context, DataFlow dataFlow,
			Expression methodInvocation) {
		// 02 - Check if the method is an Exit-Point (Only verifiers check that).
		ExitPoint exitPoint = BindingResolver.getExitPointIfMethodIsOne(getExitPoints(), methodInvocation);

		if (null != exitPoint) {
			inspectExitPoint(depth, context, methodInvocation, exitPoint);
		} else {
			super.inspectEachMethodInvocationOfChainInvocations(depth, context, dataFlow, methodInvocation);
		}
	}

	protected void inspectExitPoint(int depth, Context context, Expression method, ExitPoint exitPoint) {
		// 01 - Get the parameters (received) from the current method.
		List<Expression> receivedParameters = BindingResolver.getParameters(method);

		// 02 - Get the expected parameters of the ExitPoint method.
		Map<Parameter, List<Integer>> expectedParameters = exitPoint.getParameters();

		int index = 0;
		for (List<Integer> currentRules : expectedParameters.values()) {
			// If the rules are null, it means the expected parameter can be anything. (We do not care for it).
			if (null != currentRules) {
				setRules(currentRules);
				Expression expression = receivedParameters.get(index);
				DataFlow dataFlow = new DataFlow(expression);

				// 03 - Check if there is a marker, in case there is, we should BELIEVE it is not vulnerable.
				if (!hasMarkerAtPosition(expression)) {
					inspectNode(depth, context, dataFlow, expression);

					if (dataFlow.hasVulnerablePath()) {
						allVulnerablePaths.add(dataFlow);
						reportVulnerability(dataFlow);
					}
				}
			}
			index++;
		}
	}

	@Override
	protected void inspectMethodWithSourceCode(int depth, Context context, DataFlow dataFlow,
			Expression methodInvocation, MethodDeclaration methodDeclaration) {
		// 01 - Get the context for this method.
		Context newContext = getContext(context, methodDeclaration, methodInvocation);

		// 02 - Now I inspect the body of the method.
		super.inspectMethodWithSourceCode(depth, newContext, dataFlow, methodInvocation, methodDeclaration);
	}

	@Override
	protected Context getContext(Context context, MethodDeclaration methodDeclaration, Expression methodInvocation) {
		// We have 8 cases:
		// 01 - method(...);
		// 02 - method1(...).method2(...).method3(...);
		// 03 - obj.method(...);
		// 04 - obj.method1(...).method2(...).method3(...);
		// 05 - getObj(...).method(...);
		// 06 - Class.staticMethod(...);
		// 07 - Class obj = new Class(...);
		// 08 - (new Class(...)).run(..);
		Expression instance = HelperCodeAnalyzer.getNameIfItIsAnObject(methodInvocation);

		if (methodDeclaration.isConstructor()) {
			// Cases: 07
			return getCallGraph().getClassContext(context, methodDeclaration, methodInvocation, instance);
		} else if (Modifier.isStatic(methodDeclaration.getModifiers())) {
			// Cases: 06
			return getCallGraph().getStaticMethodContext(context, methodDeclaration, methodInvocation);
		} else {
			if (null != instance) {
				// Cases: 03, 04, 05
				return getCallGraph().getInstanceMethodContext(context, methodDeclaration, methodInvocation, instance);
			} else {
				// Cases: 01, 02
				return getCallGraph().getContext(context, methodDeclaration, methodInvocation);
			}
		}
	}

	/**
	 * An exit point might have more that one parameter and each of these parameter might have different rules (acceptable
	 * values). That is why we need to check.
	 * 
	 * @param rules
	 * @param parameter
	 * @return
	 */
	protected boolean matchRules(List<Integer> rules, Expression parameter) {
		if (null == parameter) {
			// There is nothing we can do to verify it.
			return true;
		}

		// -1 Anything is valid.
		// 0 Only sanitized values are valid.
		// 1 LITERAL and sanitized values are valid.
		for (Integer astNodeValue : rules) {
			if (astNodeValue == Constant.LITERAL) {
				switch (parameter.getNodeType()) {
					case ASTNode.STRING_LITERAL:
					case ASTNode.CHARACTER_LITERAL:
					case ASTNode.NUMBER_LITERAL:
					case ASTNode.NULL_LITERAL:
						return true;
				}
			} else if (astNodeValue == parameter.getNodeType()) {
				return true;
			}
		}

		return false;
	}

	protected void reportVulnerability(DataFlow dataFlow) {
		if (null != getReporter()) {
			getReporter().addProblem(getCurrentResource(), getId(), dataFlow);
		}
	}

}
