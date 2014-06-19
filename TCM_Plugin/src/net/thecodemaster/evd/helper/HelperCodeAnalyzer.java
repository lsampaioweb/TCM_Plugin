package net.thecodemaster.evd.helper;

import java.util.List;

import net.thecodemaster.evd.context.Context;
import net.thecodemaster.evd.graph.BindingResolver;
import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.graph.VariableBinding;
import net.thecodemaster.evd.ui.enumeration.EnumVariableStatus;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * @author Luciano Sampaio
 * @Date: 2014-06-17
 * @Version: 01
 */
public abstract class HelperCodeAnalyzer {

	/**
	 * @param variableBinding
	 * @param dataFlow
	 */
	public static void updateVariableBindingStatus(VariableBinding variableBinding, DataFlow dataFlow) {
		EnumVariableStatus status = (dataFlow.hasVulnerablePath()) ? EnumVariableStatus.VULNERABLE
				: EnumVariableStatus.NOT_VULNERABLE;
		variableBinding.setStatus(status).setDataFlow(dataFlow);
	}

	/**
	 * @param variableBinding
	 */
	public static void updateVariableBindingStatusToPrimitive(VariableBinding variableBinding) {
		variableBinding.setStatus(EnumVariableStatus.NOT_VULNERABLE);
	}

	/**
	 * @param expression
	 * @return
	 */
	public static boolean isPrimitive(Expression expression) {
		// 01 - Get the type if it is a variable or the return type if it is a method.
		ITypeBinding typeBinding = BindingResolver.resolveTypeBinding(expression);

		// 02 - If the type is primitive, we return and that's it.
		if ((null != typeBinding) && (typeBinding.isPrimitive())) {
			return true;
		}

		// 03 - If the type is a Wrapper, the method isPrimitive returns false, so we have to check that.
		return BindingResolver.isWrapperOfPrimitive(typeBinding);
	}

	/**
	 * 32
	 * 
	 * @param dataFlow
	 * @param methodInvocation
	 * @return
	 */
	public static DataFlow getDataFlow(DataFlow dataFlow, Expression methodInvocation) {
		// 01 request.getParameter("b");
		// 02 boolean b = Boolean.valueOf(request.getParameter("b"));
		// 03 String a = request.getParameter("a");
		// 04 return request.getParameter("b");
		// If the parent is a VariableDeclarationFragment or a return type.
		if (isPrimitive(methodInvocation)) {
			return new DataFlow(methodInvocation);
		} else {
			return getDataFlowBasedOnTheParent(dataFlow, methodInvocation);
		}
	}

	/**
	 * @param dataFlow
	 * @param expression
	 * @return
	 */
	public static DataFlow getDataFlowBasedOnTheParent(DataFlow dataFlow, Expression expression) {
		ASTNode node = expression.getParent();
		while (null != node) {
			switch (node.getNodeType()) {
				case ASTNode.BLOCK: // 08
					return new DataFlow(expression);

				case ASTNode.ASSIGNMENT: // 07
				case ASTNode.CLASS_INSTANCE_CREATION: // 14
				case ASTNode.METHOD_INVOCATION: // 32
				case ASTNode.RETURN_STATEMENT: // 41
				case ASTNode.VARIABLE_DECLARATION_FRAGMENT: // 59
				case ASTNode.ENHANCED_FOR_STATEMENT: // 70
					return dataFlow.addNodeToPath(expression);
			}

			node = node.getParent();
		}

		return new DataFlow(expression);
	}

	/**
	 * 32
	 * 
	 * @param methodInvocation
	 * @return
	 */
	public static List<Expression> getMethodsFromChainInvocation(Expression methodInvocation) {
		List<Expression> methodsInChain = Creator.newList();

		Expression optionalexpression = methodInvocation;
		while (null != optionalexpression) {
			switch (optionalexpression.getNodeType()) {
				case ASTNode.CLASS_INSTANCE_CREATION: // 14
				case ASTNode.METHOD_INVOCATION: // 32
					// The order should be:
					// getServletContext().getRequestDispatcher(login).forward(request, response);
					// 01 - getServletContext()
					// 02 - getRequestDispatcher(login)
					// 03 - forward(request, response);
					methodsInChain.add(0, optionalexpression);
					break;
			}

			optionalexpression = BindingResolver.getExpression(optionalexpression);
		}
		return methodsInChain;
	}

	/**
	 * 32
	 * 
	 * @param callGraph
	 * @param context
	 * @param method
	 * @return
	 */
	public static VariableBinding getVariableBindingIfItIsAnObject(CallGraph callGraph, Context context, Expression method) {
		Expression expression = getNameIfItIsAnObject(method);

		return (null != expression) ? callGraph.getLastReference(context, expression) : null;
	}

	/**
	 * @param node
	 * @return
	 */
	public static Expression getNameIfItIsAnObject(ASTNode node) {
		Expression expression = BindingResolver.getExpression(node);

		while (null != expression) {
			switch (expression.getNodeType()) {
				case ASTNode.SIMPLE_NAME: // 42 - This is the one we want to find.
					return expression;
				case ASTNode.THIS_EXPRESSION: // 52
					expression = ((ThisExpression) expression).getQualifier();
					break;
				default:
					expression = BindingResolver.getExpression(expression);
					break;
			}
		}

		while (null != node) {
			switch (node.getNodeType()) {
				case ASTNode.BLOCK: // 08
					return null; // Stop conditions.
				case ASTNode.SIMPLE_NAME: // 42 - This is the one we want to find.
					return (Expression) node;
				case ASTNode.VARIABLE_DECLARATION_FRAGMENT: // 59
					return ((VariableDeclarationFragment) node).getName();
			}

			node = node.getParent();
		}

		return null;
	}

}