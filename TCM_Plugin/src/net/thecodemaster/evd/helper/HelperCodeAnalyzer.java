package net.thecodemaster.evd.helper;

import java.util.List;

import net.thecodemaster.evd.context.Context;
import net.thecodemaster.evd.graph.BindingResolver;
import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.graph.VariableBinding;
import net.thecodemaster.evd.ui.enumeration.EnumVariableStatus;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
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
	private static Expression getNameIfItIsAnObject(ASTNode node) {
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

		return null;
	}

	public static Expression getInstanceIfItIsAnObject(ASTNode node) {
		Expression expression = getNameIfItIsAnObject(node);

		if (null != expression) {
			return expression;
		}

		while (null != node) {
			switch (node.getNodeType()) {
				case ASTNode.ARRAY_INITIALIZER: // 04
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

	public static IResource getSuperClassResource(CallGraph callGraph, TypeDeclaration typeDeclaration) {
		// 01 - We will need the name of the package where the super class is located.
		String packageName = null;

		if (null != typeDeclaration) {
			// 02 - Get the SuperClass.
			SimpleType superClass = (SimpleType) typeDeclaration.getSuperclassType();

			// A call to a super constructor means we have a super class, however if the superClass
			// object is null, it might be a syntax error in the code. We have to be prepared for that.
			if (null != superClass) {
				// 03 - Get the name of the super class.
				Name superClassName = superClass.getName();

				// 04 - We have two cases.
				// Case 01: The superClassName is a QualifiedName: some.package.Animal
				// Case 02: The superClassName is a SimpleName : Animal
				switch (superClassName.getNodeType()) {
					case ASTNode.QUALIFIED_NAME: // 40
						// The qualified name is the package where the class is located.
						packageName = superClassName.getFullyQualifiedName();
						break;
					case ASTNode.SIMPLE_NAME: // 42
						// If we just have the name of the class, we have two cases.
						// Case 01: The super class is in the same package.
						// Case 02: The super class is in another package.
						String superClassPackageName = superClassName.getFullyQualifiedName();

						// 05 - Get the compilation unit of the current class.
						CompilationUnit cu = BindingResolver.getCompilationUnit(typeDeclaration);

						// 06 - Get the list of imports.
						List<ImportDeclaration> imports = BindingResolver.getImports(cu);

						// 07 - Iterate over the list and try to find the superclass's import.
						for (ImportDeclaration importDeclaration : imports) {
							String currentPackageName = importDeclaration.getName().getFullyQualifiedName();
							if (currentPackageName.endsWith(superClassPackageName)) {
								packageName = currentPackageName;
								break;
							}
						}

						if (null == packageName) {
							packageName = cu.getPackage().getName().getFullyQualifiedName();
						}
						break;
				}
			}
		}
		// 08 - Now that we have the package where the super class is located, we can get
		// the resource file.
		return callGraph.getResourceFromPackageName(packageName);
	}

}