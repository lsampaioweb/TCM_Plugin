package net.thecodemaster.evd.graph;

import java.util.Arrays;
import java.util.List;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.helper.Convert;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.point.AbstractPoint;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;

/**
 * @author Luciano Sampaio
 */
public class BindingResolver {

	private BindingResolver() {
	}

	private static ASTNode findAncestor(ASTNode node, int nodeType) {
		while ((node != null) && (node.getNodeType() != nodeType)) {
			node = node.getParent();
		}
		return node;
	}

	public static CompilationUnit getParentCompilationUnit(ASTNode node) {
		return (CompilationUnit) findAncestor(node, ASTNode.COMPILATION_UNIT);
	}

	public static MethodDeclaration getParentMethodDeclaration(ASTNode node) {
		return (MethodDeclaration) findAncestor(node, ASTNode.METHOD_DECLARATION);
	}

	public static MethodInvocation getParentMethodInvocation(ASTNode node) {
		return (MethodInvocation) findAncestor(node, ASTNode.METHOD_INVOCATION);
	}

	public static Block getParentBlock(ASTNode node) {
		return (Block) findAncestor(node, ASTNode.BLOCK);
	}

	public static Statement getParentStatement(ASTNode node) {
		while ((node != null) && (!(node instanceof Statement))) {
			node = node.getParent();
		}
		return (Statement) node;
	}

	private static String getName(IBinding binding) {
		return (null != binding) ? binding.getName() : null;
	}

	public static String getName(ASTNode node) {
		return getName(resolveBinding(node));
	}

	public static IMethodBinding resolveBinding(ASTNode node) {
		if (node.getNodeType() == ASTNode.METHOD_DECLARATION) {
			return ((MethodDeclaration) node).resolveBinding();
		} else if (node.getNodeType() == ASTNode.METHOD_INVOCATION) {
			return ((MethodInvocation) node).resolveMethodBinding();
		} else if (node.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
			return ((ClassInstanceCreation) node).resolveConstructorBinding();
		}

		return null;
	}

	/**
	 * Returns the type binding representing the class or interface that declares this method or constructor.
	 * 
	 * @param methodBinding
	 * @return the binding of the class or interface that declares this method or constructor
	 */
	private static ITypeBinding getDeclaringClass(IMethodBinding methodBinding) {
		return (null != methodBinding) ? methodBinding.getDeclaringClass() : null;
	}

	/**
	 * Returns the binding for the package in which this type is declared. The package of a recovered type reference
	 * binding is either the package of the enclosing type, or, if the type name is the name of a well-known type, the
	 * package of the matching well-known type.
	 * 
	 * @param typeBinding
	 * @return the binding for the package in which this class, interface, enum, or annotation type is declared, or null
	 *         if this type binding represents a primitive type, an array type, the null type, a type variable, a wild
	 *         card type, a capture binding.
	 */
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

	public static String getQualifiedName(ASTNode node) {
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

	public static List<Expression> getParameters(Expression expr) {
		if (expr.getNodeType() == ASTNode.METHOD_INVOCATION) {
			return getParameters(((MethodInvocation) expr).arguments());
		} else if (expr.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
			return getParameters(((ClassInstanceCreation) expr).arguments());
		}

		return Creator.newList();
	}

	public static List<ITypeBinding> getParameterTypes(MethodDeclaration node) {
		IMethodBinding methodBinding = resolveBinding(node);

		return (null != methodBinding) ? Arrays.asList(methodBinding.getParameterTypes()) : null;
	}

	@SuppressWarnings("unchecked")
	public static List<SingleVariableDeclaration> getParameters(MethodDeclaration node) {

		return (null != node) ? node.parameters() : null;
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

		return parameterIndex;
	}

	public static Expression getParameterAtIndex(Expression expr, int parameterIndex) {
		// TODO - add the case for class creation.
		if (expr.getNodeType() == ASTNode.METHOD_INVOCATION) {
			List<Expression> parameters = getParameters(((MethodInvocation) expr).arguments());

			if (parameterIndex < parameters.size()) {
				return parameters.get(parameterIndex);
			}
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
	public static List<Expression> getParameters(List<?> arguments) {
		List<Expression> expressions = Creator.newList();

		if (null != arguments) {
			expressions = (List<Expression>) arguments;
		}

		return expressions;
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
