package net.thecodemaster.esvd.verifier;

import java.util.List;

import net.thecodemaster.esvd.constant.Constant;
import net.thecodemaster.esvd.context.Context;
import net.thecodemaster.esvd.graph.BindingResolver;
import net.thecodemaster.esvd.graph.CallGraph;
import net.thecodemaster.esvd.graph.CodeAnalyzer;
import net.thecodemaster.esvd.graph.VariableBinding;
import net.thecodemaster.esvd.graph.flow.DataFlow;
import net.thecodemaster.esvd.graph.flow.Flow;
import net.thecodemaster.esvd.helper.HelperCodeAnalyzer;
import net.thecodemaster.esvd.point.ExitPoint;
import net.thecodemaster.esvd.xmlloader.LoaderExitPoint;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
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
	 * List with all the ExitPoints of this verifier.
	 */
	private List<ExitPoint>	exitPoints;
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

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	protected List<Integer> getRules() {
		return rules;
	}

	private void setRules(List<Integer> rules) {
		this.rules = rules;
	}

	public List<ExitPoint> getExitPoints() {
		if (null == exitPoints) {
			// Loads all the ExitPoints of this verifier.
			exitPoints = (new LoaderExitPoint(this)).load();
		}

		return exitPoints;
	}

	public void run(CallGraph callGraph, IResource resource, CompilationUnit compilationUnit, Flow loopControl,
			Context context, DataFlow dataFlow, Expression expression, List<Integer> rules) {
		if (requiredExtraInspection(dataFlow)) {
			setCallGraph(callGraph);
			setCurrentResource(resource);
			setCurrentCompilationUnit(compilationUnit);
			setRules(rules);
			inspectNode(loopControl, context, dataFlow, expression);
		}
	}

	protected boolean requiredExtraInspection(DataFlow dataFlow) {
		return false;
	}

	/**
	 * 07
	 */
	@Override
	protected void inspectAssignment(Flow loopControl, Context context, DataFlow dataFlow, Assignment expression) {
		Expression rightHandSide = expression.getRightHandSide();
		inspectNode(loopControl, context, dataFlow.addNodeToPath(rightHandSide), rightHandSide);
	}

	@Override
	protected void UpdateIfVulnerable(VariableBinding variableBinding, DataFlow dataFlow) {
		// 02 - If there is a vulnerable path, then this variable is vulnerable.
		HelperCodeAnalyzer.updateVariableBindingDataFlow(variableBinding, dataFlow);
	}

	@Override
	protected void inspectMethodWithSourceCode(Flow loopControl, Context context, DataFlow dataFlow,
			ASTNode methodInvocation, MethodDeclaration methodDeclaration) {
		// 01 - Get the context for this method.
		Context newContext = getContext(loopControl, context, methodDeclaration, methodInvocation);

		// 02 - Now I inspect the body of the method.
		super.inspectMethodWithSourceCode(loopControl, newContext, dataFlow, methodInvocation, methodDeclaration);
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
		// 08 - (new Class(...)).run(..);
		Expression instance = BindingResolver.getInstanceIfItIsAnObject(methodInvocation);

		if (methodDeclaration.isConstructor()) {
			// Cases: 07
			return getCallGraph().getClassContext(context, instance);
		} else if (Modifier.isStatic(methodDeclaration.getModifiers())) {
			// Cases: 06
			return getCallGraph().getStaticContext(context, methodDeclaration, methodInvocation);
		} else {
			if (null != instance) {
				// Cases: 03, 04, 05
				// The instance must exist, if it does not, it is probably an assignment or syntax error.
				// Animal a1 = new Animal() / Animal a2 = a1 / a1.method();
				instance = findRealInstance(loopControl, context, instance);

				return getCallGraph().getInstanceContext(context, methodDeclaration, methodInvocation, instance);
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
	 * @param node
	 * @return
	 */
	protected boolean matchRules(List<Integer> rules, ASTNode node) {
		if ((null == rules) || (null == node)) {
			// There is nothing we can do to verify it.
			return true;
		}

		// -1 Anything is valid.
		// 0 Only sanitized values are valid.
		// 1 LITERAL and sanitized values are valid.
		for (Integer astNodeValue : rules) {
			if (astNodeValue == Constant.LITERAL) {
				switch (node.getNodeType()) {
					case ASTNode.STRING_LITERAL:
					case ASTNode.CHARACTER_LITERAL:
					case ASTNode.NUMBER_LITERAL:
					case ASTNode.NULL_LITERAL:
						return true;
				}
			} else if (astNodeValue == node.getNodeType()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String toString() {
		return getName();
	}

}
