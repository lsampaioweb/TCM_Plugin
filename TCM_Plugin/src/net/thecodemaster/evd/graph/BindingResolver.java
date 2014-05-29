package net.thecodemaster.evd.graph;

import java.util.Arrays;
import java.util.List;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.helper.Convert;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.point.AbstractPoint;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * @author Luciano Sampaio
 */
public class BindingResolver {

	private BindingResolver() {
	}

	private static ASTNode findAncestor(ASTNode node, int nodeType) {
		while ((null != node) && (node.getNodeType() != nodeType)) {
			node = node.getParent();
		}
		return node;
	}

	public static CompilationUnit getParentCompilationUnit(ASTNode node) {
		return (CompilationUnit) findAncestor(node, ASTNode.COMPILATION_UNIT);
	}

	public static Block getParentBlock(ASTNode node) {
		return (Block) findAncestor(node, ASTNode.BLOCK);
	}

	public static MethodDeclaration getParentMethodDeclaration(ASTNode node) {
		return (MethodDeclaration) findAncestor(node, ASTNode.METHOD_DECLARATION);
	}

	public static ASTNode getFirstParentBeforeBlock(ASTNode node) {
		while ((null != node) && (null != node.getParent()) && (node.getParent().getNodeType() != ASTNode.BLOCK)) {
			node = node.getParent();
		}
		return node;
	}

	public static Expression getParentWhoHasAReference(ASTNode node) {
		while (null != node) {
			switch (node.getNodeType()) {
				case ASTNode.METHOD_INVOCATION:
				case ASTNode.CLASS_INSTANCE_CREATION:
					return (Expression) node;
				case ASTNode.VARIABLE_DECLARATION_FRAGMENT:
					VariableDeclarationFragment vdf = (VariableDeclarationFragment) node;
					return vdf.getName();
				case ASTNode.ASSIGNMENT:
					Assignment assignment = (Assignment) node;
					return assignment.getLeftHandSide();
			}

			node = node.getParent();
		}

		return null;
	}

	private static String getName(IBinding binding) {
		return (null != binding) ? binding.getName() : null;
	}

	private static String getName(ASTNode node) {
		return getName(resolveBinding(node));
	}

	private static IMethodBinding resolveBinding(ASTNode node) {
		if (node.getNodeType() == ASTNode.METHOD_DECLARATION) {
			return ((MethodDeclaration) node).resolveBinding();
		} else if (node.getNodeType() == ASTNode.METHOD_INVOCATION) {
			return ((MethodInvocation) node).resolveMethodBinding();
		} else if (node.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
			return ((ClassInstanceCreation) node).resolveConstructorBinding();
		}

		return null;
	}

	private static ITypeBinding getDeclaringClass(IMethodBinding methodBinding) {
		return (null != methodBinding) ? methodBinding.getDeclaringClass() : null;
	}

	private static IPackageBinding getPackage(ITypeBinding typeBinding) {
		return (null != typeBinding) ? typeBinding.getPackage() : null;
	}

	private static String getQualifiedName(IMethodBinding node) {
		String qualifiedName = null;

		ITypeBinding clazz = getDeclaringClass(node);
		if (null != clazz) {

			IPackageBinding pkg = getPackage(clazz);
			if (null != pkg) {
				qualifiedName = String.format("%s.%s", getName(pkg), getName(clazz));
			}
		}

		return qualifiedName;
	}

	private static String getQualifiedName(ASTNode node) {
		return getQualifiedName(resolveBinding(node));
	}

	public static String getFullName(Expression expr) {
		// Cases:
		// 01 - getPassword();
		// 02 - request.getParameter("password");
		if (expr.getNodeType() == ASTNode.METHOD_INVOCATION) {
			return ((MethodInvocation) expr).toString();
		}

		return null;
	}

	/**
	 * This method was created because the list returned from the arguments is not generic.
	 * 
	 * @param arguments
	 *          The live ordered list of argument expressions in this method invocation expression.
	 * @return List<Expression>
	 */
	@SuppressWarnings("unchecked")
	private static List<Expression> getParameters(List<?> arguments) {
		List<Expression> expressions = Creator.newList();

		if (null != arguments) {
			expressions = (List<Expression>) arguments;
		}

		return expressions;
	}

	public static List<Expression> getParameters(Expression expr) {
		List<Expression> parameters = Creator.newList();

		if (null != expr) {
			if (expr.getNodeType() == ASTNode.METHOD_INVOCATION) {
				parameters = getParameters(((MethodInvocation) expr).arguments());
			} else if (expr.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
				parameters = getParameters(((ClassInstanceCreation) expr).arguments());
			} else if (expr.getNodeType() == ASTNode.INFIX_EXPRESSION) {
				parameters = getParameters(((InfixExpression) expr).extendedOperands());
			} else if (expr.getNodeType() == ASTNode.ARRAY_INITIALIZER) {
				parameters = getParameters(((ArrayInitializer) expr).expressions());
			}

		}
		return parameters;
	}

	@SuppressWarnings("unchecked")
	private static List<SingleVariableDeclaration> getParameters(MethodDeclaration node) {
		List<SingleVariableDeclaration> parameters = Creator.newList();

		if ((null != node) && (null != node.parameters())) {
			parameters = node.parameters();
		}

		return parameters;
	}

	private static List<ITypeBinding> getParameterTypes(MethodDeclaration node) {
		IMethodBinding methodBinding = resolveBinding(node);

		return (null != methodBinding) ? Arrays.asList(methodBinding.getParameterTypes()) : null;
	}

	public static int getParameterIndex(MethodDeclaration node, SimpleName parameterToSearch) {
		List<SingleVariableDeclaration> parameters = getParameters(node);

		int parameterIndex = -1;
		String currentName;
		String otherName = parameterToSearch.getIdentifier();
		for (SingleVariableDeclaration currentParameter : parameters) {
			parameterIndex++;
			currentName = currentParameter.getName().getIdentifier();

			if (currentName.equals(otherName)) {
				return parameterIndex;
			}
		}

		return -1;
	}

	public static Expression getParameterAtIndex(Expression expr, int parameterIndex) {
		List<Expression> parameters = getParameters(expr);

		if (parameterIndex < parameters.size()) {
			return parameters.get(parameterIndex);
		}

		return null;
	}

	public static boolean areMethodsEqual(MethodDeclaration method, Expression other) {
		String methodName = BindingResolver.getName(method);
		String otherName = BindingResolver.getName(other);

		// 02 - Verify if they have the same name.
		if (methodName.equals(otherName)) {

			// 03 - Get the qualified name (Package + Class) of these methods.
			String qualifiedName = BindingResolver.getQualifiedName(method);
			String otherQualifiedName = BindingResolver.getQualifiedName(other);

			// 04 - Verify if they are from the same package and class.
			// Method names can repeat in other classes.
			if (qualifiedName.equals(otherQualifiedName)) {

				// 05 - Get their parameters.
				List<ITypeBinding> methodParameters = BindingResolver.getParameterTypes(method);
				List<Expression> otherParameters = BindingResolver.getParameters(other);

				// 06 - It is necessary to check the number of parameters and its types
				// because it may exist methods with the same names but different parameters.
				if (methodParameters.size() == otherParameters.size()) {
					int index = 0;
					for (ITypeBinding currentParameter : methodParameters) {
						ITypeBinding otherTypeBinding = otherParameters.get(index++).resolveTypeBinding();

						// 07 - Verify if all the parameters are the ones expected. However, there is a case
						// where an Object is expected, and any type is accepted.
						if (!BindingResolver.parametersHaveSameType(currentParameter.getQualifiedName(), otherTypeBinding)) {
							return false;
						}
					}

					return true;
				}
			}
		}

		return false;
	}

	public static boolean methodsHaveSameNameAndPackage(AbstractPoint abstractPoint, Expression method) {
		// 01 - Get the method name.
		String methodName = BindingResolver.getName(method);

		// 02 - Verify if this method is in the list of ExitPoints.
		if (abstractPoint.getMethodName().equals(methodName)) {

			// 03 - Get the qualified name (Package + Class) of this method.
			String qualifiedName = BindingResolver.getQualifiedName(method);

			// 04 - Verify if this is really the method we were looking for.
			// Method names can repeat in other classes.
			if (null != qualifiedName) {
				return qualifiedName.matches(abstractPoint.getQualifiedName());
			}
		}

		return false;
	}

	public static boolean parametersHaveSameType(String parameter, ITypeBinding other) {
		if (parameter.equals(Constant.OBJECT)) {
			return true;
		}

		if (other == null) {
			return false;
		}

		if (parameter.equals(other.getQualifiedName())) {
			return true;
		}

		// These are the special (WRAPPER) cases.
		// boolean, byte, char, short, int, long, float, and double
		String newName = Convert.fromPrimitiveNameToWrapperClass(other.getQualifiedName());
		return (parameter.equals(newName));
	}

}
