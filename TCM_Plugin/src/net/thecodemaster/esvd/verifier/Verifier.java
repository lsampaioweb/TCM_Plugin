package net.thecodemaster.esvd.verifier;

import java.util.List;

import net.thecodemaster.esvd.context.Context;
import net.thecodemaster.esvd.graph.BindingResolver;
import net.thecodemaster.esvd.graph.CallGraph;
import net.thecodemaster.esvd.graph.CodeAnalyzer;
import net.thecodemaster.esvd.graph.VariableBinding;
import net.thecodemaster.esvd.graph.flow.DataFlow;
import net.thecodemaster.esvd.graph.flow.Flow;
import net.thecodemaster.esvd.helper.HelperCodeAnalyzer;
import net.thecodemaster.esvd.helper.HelperVerifiers;
import net.thecodemaster.esvd.point.ExitPoint;
import net.thecodemaster.esvd.ui.enumeration.EnumRules;
import net.thecodemaster.esvd.xmlloader.LoaderExitPoint;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
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
	 * The priority of the current verifier.
	 */
	private final int				priority;
	/**
	 * List with all the ExitPoints of this verifier.
	 */
	private List<ExitPoint>	exitPoints;
	/**
	 * The rules that the current parameter must obey.
	 */
	private int							rules;

	/**
	 * @param name
	 *          The name of the verifier.
	 * @param id
	 *          The id of the verifier.
	 * @param listEntryPoints
	 *          List with all the EntryPoints methods.
	 */
	public Verifier(int id, String name, int priority) {
		this.id = id;
		this.name = name;
		this.priority = priority;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getPriority() {
		return priority;
	}

	protected int getRules() {
		return rules;
	}

	private void setRules(int rules) {
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
			Context context, DataFlow dataFlow, Expression expression, int rules) {
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
	protected boolean matchRules(int rules, ASTNode node) {
		if ((EnumRules.ANYTHING_IS_VALID.value() == rules) || (null == node)) {
			// There is nothing we can do to verify it.
			return true;
		}

		// ANYTHING_IS_VALID(1),
		// SANITIZED(2),
		// LITERAL(4),
		// STRING_CONCATENATION(8);
		List<EnumRules> listRules = HelperVerifiers.getRulesFromValue(rules);

		for (EnumRules currentRule : listRules) {

			if (EnumRules.LITERAL == currentRule) {
				switch (node.getNodeType()) {
					case ASTNode.STRING_LITERAL:
					case ASTNode.CHARACTER_LITERAL:
					case ASTNode.NUMBER_LITERAL:
					case ASTNode.NULL_LITERAL:
						return true;
				}
			} else if (EnumRules.STRING_CONCATENATION == currentRule) {
				// Queries should not be concatenated, but "a" + "b" + "c" is not
				// a security vulnerability.
				// TODO - It is not fully implemented.
				if (ASTNode.INFIX_EXPRESSION == node.getNodeType()) {
					InfixExpression expression = (InfixExpression) node;

					Expression leftOperand = expression.getLeftOperand();
					Expression rightOperand = expression.getRightOperand();
					List<Expression> extendedOperands = BindingResolver.getParameters(expression);

					if (ASTNode.STRING_LITERAL == leftOperand.getNodeType()) {
						if (ASTNode.STRING_LITERAL == rightOperand.getNodeType()) {
							if (extendedOperands.size() > 0) {
								for (Expression extendedOperand : extendedOperands) {
									if (ASTNode.STRING_LITERAL != extendedOperand.getNodeType()) {
										return false;
									}
								}
							}
							return true;
						}
					}
				}

			}
		}

		return false;
	}

	@Override
	public String toString() {
		return getName();
	}

}
