package net.thecodemaster.evd.graph;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.helper.Convert;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.point.AbstractPoint;
import net.thecodemaster.evd.point.EntryPoint;
import net.thecodemaster.evd.point.ExitPoint;
import net.thecodemaster.evd.point.SanitizationPoint;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
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

	public static CompilationUnit getCompilationUnit(ASTNode node) {
		return (CompilationUnit) findAncestor(node, ASTNode.COMPILATION_UNIT);
	}

	public static TypeDeclaration getTypeDeclaration(ASTNode node) {
		return (TypeDeclaration) findAncestor(node, ASTNode.TYPE_DECLARATION);
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

	public static IResource getResource(ASTNode node) {
		try {
			CompilationUnit cUnit = getCompilationUnit(node);
			return cUnit.getJavaElement().getCorrespondingResource();
		} catch (JavaModelException e) {
			PluginLogger.logError(e);
		}
		return null;
	}

	public static IBinding resolveBinding(ASTNode node) {
		while (null != node) {
			switch (node.getNodeType()) {
				case ASTNode.ARRAY_ACCESS: // 02
					node = ((ArrayAccess) node).getArray();
					break;
				case ASTNode.CLASS_INSTANCE_CREATION: // 14
					return ((ClassInstanceCreation) node).resolveConstructorBinding();
				case ASTNode.FIELD_ACCESS: // 22
					return ((FieldAccess) node).resolveFieldBinding();
				case ASTNode.METHOD_DECLARATION: // 31
					return ((MethodDeclaration) node).resolveBinding();
				case ASTNode.METHOD_INVOCATION: // 32
					return ((MethodInvocation) node).resolveMethodBinding();
				case ASTNode.QUALIFIED_NAME: // 40
				case ASTNode.SIMPLE_NAME: // 42
					return ((Name) node).resolveBinding();
				default:
					PluginLogger.logError("resolveBinding default:" + node.getNodeType() + " - " + node, null);
					node = null;
			}
		}
		return null;
	}

	/**
	 * @param node
	 * @return
	 */
	public static ASTNode getParentWhoHasAReference(ASTNode node) {
		while (null != node) {
			switch (node.getNodeType()) {
				case ASTNode.BLOCK: // 08
					return null;
				case ASTNode.ASSIGNMENT: // 07
					return ((Assignment) node).getLeftHandSide();
				case ASTNode.CLASS_INSTANCE_CREATION: // 14
				case ASTNode.METHOD_INVOCATION: // 32
				case ASTNode.RETURN_STATEMENT: // 41
					return node;
				case ASTNode.VARIABLE_DECLARATION_FRAGMENT: // 59
					return ((VariableDeclarationFragment) node).getName();
			}

			node = node.getParent();
		}

		return null;
	}

	public static Expression getExpression(ASTNode node) {
		if (null != node) {
			switch (node.getNodeType()) {
				case ASTNode.ARRAY_ACCESS: // 02
					return ((ArrayAccess) node).getArray();
				case ASTNode.CAST_EXPRESSION: // 11
					return ((CastExpression) node).getExpression();
				case ASTNode.CLASS_INSTANCE_CREATION: // 14
					return ((ClassInstanceCreation) node).getExpression();
				case ASTNode.FIELD_ACCESS: // 22
					return ((FieldAccess) node).getExpression();
				case ASTNode.INFIX_EXPRESSION: // 27
					return null;
				case ASTNode.METHOD_INVOCATION: // 32
					return ((MethodInvocation) node).getExpression();
				case ASTNode.PARENTHESIZED_EXPRESSION: // 36
					return ((ParenthesizedExpression) node).getExpression();
				case ASTNode.SUPER_CONSTRUCTOR_INVOCATION: // 46
					return ((SuperConstructorInvocation) node).getExpression();
				case ASTNode.QUALIFIED_NAME: // 40
				case ASTNode.SIMPLE_NAME: // 42
				case ASTNode.STRING_LITERAL: // 45
				case ASTNode.THIS_EXPRESSION: // 52
					return null;
				default:
					PluginLogger.logError("getExpression default:" + node.getNodeType() + " - " + node, null);
					return null;
			}
		}
		return null;
	}

	private static String getName(IBinding binding) {
		return (null != binding) ? binding.getName() : "";
	}

	private static String getName(ASTNode node) {
		return getName(resolveBinding(node));
	}

	public static ITypeBinding resolveTypeBinding(Expression expression) {
		return expression.resolveTypeBinding();
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
		return getQualifiedName((IMethodBinding) resolveBinding(node));
	}

	public static String getFullName(Expression node) {
		// Cases:
		// 01 - getPassword();
		// 02 - request.getParameter("password");
		switch (node.getNodeType()) {
			case ASTNode.METHOD_INVOCATION: // 32
				return ((MethodInvocation) node).toString();
			default:
				PluginLogger.logError("getFullName default:" + node.getNodeType() + " - " + node, null);
				return null;
		}
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

	public static List<Expression> getParameters(ASTNode node) {
		List<Expression> parameters = Creator.newList();

		if (null != node) {
			switch (node.getNodeType()) {
				case ASTNode.ARRAY_INITIALIZER: // 04
					return getParameters(((ArrayInitializer) node).expressions());
				case ASTNode.CLASS_INSTANCE_CREATION: // 14
					return getParameters(((ClassInstanceCreation) node).arguments());
				case ASTNode.CONSTRUCTOR_INVOCATION: // 17
					return getParameters(((ConstructorInvocation) node).arguments());
				case ASTNode.INFIX_EXPRESSION: // 27
					return getParameters(((InfixExpression) node).extendedOperands());
				case ASTNode.METHOD_INVOCATION: // 32
					return getParameters(((MethodInvocation) node).arguments());
				case ASTNode.SUPER_CONSTRUCTOR_INVOCATION: // 46
					return getParameters(((SuperConstructorInvocation) node).arguments());
				case ASTNode.SUPER_METHOD_INVOCATION: // 48
					return getParameters(((SuperMethodInvocation) node).arguments());
				default:
					PluginLogger.logError("getParameters default:" + node.getNodeType() + " - " + node, null);
			}
		}
		return parameters;
	}

	@SuppressWarnings("unchecked")
	public static List<SingleVariableDeclaration> getParameters(MethodDeclaration node) {
		List<SingleVariableDeclaration> parameters = Creator.newList();

		if ((null != node) && (null != node.parameters())) {
			parameters = node.parameters();
		}

		return parameters;
	}

	@SuppressWarnings("unchecked")
	public static List<ImportDeclaration> getImports(CompilationUnit cu) {
		List<ImportDeclaration> imports = Creator.newList();

		if ((null != cu) && (null != cu.imports())) {
			imports = cu.imports();
		}

		return imports;
	}

	private static List<ITypeBinding> getParameterTypes(MethodDeclaration node) {
		IMethodBinding methodBinding = (IMethodBinding) resolveBinding(node);

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

	public static Expression getParameterAtIndex(ASTNode expr, int parameterIndex) {
		List<Expression> parameters = getParameters(expr);

		if (parameterIndex < parameters.size()) {
			return parameters.get(parameterIndex);
		}

		return null;
	}

	public static boolean areMethodsEqual(MethodDeclaration method, ASTNode otherMethod) {
		String methodName = getName(method);
		String otherName = getName(otherMethod);

		// 02 - Verify if they have the same name.
		if (methodName.equals(otherName)) {

			// 03 - Get the qualified name (Package + Class) of these methods.
			String qualifiedName = getQualifiedName(method);
			String otherQualifiedName = getQualifiedName(otherMethod);

			// 04 - Verify if they are from the same package and class.
			// Method names can repeat in other classes.
			if (qualifiedName.equals(otherQualifiedName)) {
				return haveSameParameters(method, otherMethod);
			}
		}

		return false;
	}

	public static boolean haveSameParameters(MethodDeclaration method, ASTNode otherMethod) {
		// 05 - Get their parameters.
		List<ITypeBinding> methodParameters = getParameterTypes(method);
		List<Expression> otherParameters = getParameters(otherMethod);

		// 06 - It is necessary to check the number of parameters and its types
		// because it may exist methods with the same names but different parameters.
		if (methodParameters.size() == otherParameters.size()) {
			int index = 0;
			for (ITypeBinding currentParameter : methodParameters) {
				ITypeBinding otherTypeBinding = otherParameters.get(index++).resolveTypeBinding();

				// 07 - Verify if all the parameters are the ones expected. However, there is a case
				// where an Object is expected, and any type is accepted.
				if (!parametersHaveSameType(currentParameter.getQualifiedName(), otherTypeBinding)) {
					return false;
				}
			}

			return true;
		}

		return false;
	}

	public static boolean methodsHaveSameNameAndPackage(AbstractPoint abstractPoint, Expression method) {
		// 01 - Get the method name.
		String methodName = getName(method);

		// 02 - Verify if this method is in the list of ExitPoints.
		if (abstractPoint.getMethodName().equals(methodName)) {

			// 03 - Get the qualified name (Package + Class) of this method.
			String qualifiedName = getQualifiedName(method);

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

	public static boolean isWrapperOfPrimitive(ITypeBinding typeBinding) {
		if (null == typeBinding) {
			return false;
		}

		String name = typeBinding.getQualifiedName();
		if (null != name) {
			// boolean, byte, char, short, int, long, float, and double,
			if (name.equals("java.lang.Boolean")) {
				return true;
			} else if (name.equals("java.lang.Byte")) {
				return true;
			} else if (name.equals("java.lang.Character")) {
				return true;
			} else if (name.equals("java.lang.Short")) {
				return true;
			} else if (name.equals("java.lang.Integer")) {
				return true;
			} else if (name.equals("java.lang.Long")) {
				return true;
			} else if (name.equals("java.lang.Float")) {
				return true;
			} else if (name.equals("java.lang.Double")) {
				return true;
			}
		}
		return false;
	}

	public static boolean isMethodAnEntryPoint(List<EntryPoint> entryPoints, Expression method) {
		for (EntryPoint currentEntryPoint : entryPoints) {
			if (methodsHaveSameNameAndPackage(currentEntryPoint, method)) {
				// 01 - Get the expected arguments of this method.
				List<String> expectedParameters = currentEntryPoint.getParameters();

				// 02 - Get the received parameters of the current method.
				List<Expression> receivedParameters = getParameters(method);

				// 03 - It is necessary to check the number of parameters and its types
				// because it may exist methods with the same names but different parameters.
				if (expectedParameters.size() == receivedParameters.size()) {
					boolean isMethodAnEntryPoint = true;
					int index = 0;
					for (String expectedParameter : expectedParameters) {
						ITypeBinding typeBinding = receivedParameters.get(index++).resolveTypeBinding();

						// Verify if all the parameters are the ones expected.
						if (!parametersHaveSameType(expectedParameter, typeBinding)) {
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

	public static boolean isMethodASanitizationPoint(List<SanitizationPoint> sanitizationPoints, Expression method) {
		for (SanitizationPoint sanitizer : sanitizationPoints) {
			if (methodsHaveSameNameAndPackage(sanitizer, method)) {
				// 01 - Get the expected arguments of this method.
				List<String> expectedParameters = sanitizer.getParameters();

				// 02 - Get the received parameters of the current method.
				List<Expression> receivedParameters = getParameters(method);

				// 03 - It is necessary to check the number of parameters and its types
				// because it may exist methods with the same names but different parameters.
				if (expectedParameters.size() == receivedParameters.size()) {
					boolean isMethodAnEntryPoint = true;
					int index = 0;
					for (String expectedParameter : expectedParameters) {
						ITypeBinding typeBinding = receivedParameters.get(index++).resolveTypeBinding();

						// Verify if all the parameters are the ones expected.
						if (!parametersHaveSameType(expectedParameter, typeBinding)) {
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

	public static ExitPoint getExitPointIfMethodIsOne(List<ExitPoint> exitPoints, Expression method) {
		for (ExitPoint currentExitPoint : exitPoints) {
			if (methodsHaveSameNameAndPackage(currentExitPoint, method)) {
				// 01 - Get the expected arguments of this method.
				Map<Parameter, List<Integer>> expectedParameters = currentExitPoint.getParameters();

				// 02 - Get the received parameters of the current method.
				List<Expression> receivedParameters = getParameters(method);

				// 03 - It is necessary to check the number of parameters and its types
				// because it may exist methods with the same names but different parameters.
				if (expectedParameters.size() == receivedParameters.size()) {
					boolean isMethodAnExitPoint = true;
					int index = 0;
					for (Parameter expectedParameter : expectedParameters.keySet()) {
						ITypeBinding typeBinding = receivedParameters.get(index++).resolveTypeBinding();

						// Verify if all the parameters are the ones expected. However, there is a case
						// where an Object is expected, and any type is accepted.
						if (!parametersHaveSameType(expectedParameter.getType(), typeBinding)) {
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

}
