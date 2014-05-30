package net.thecodemaster.evd.visitor;

import java.util.List;
import java.util.Map;

import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.verifier.CodeAnalyzer;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * The main responsibility of this class is to find entry-points and vulnerable variables.
 * 
 * @author Luciano Sampaio
 */
public class VisitorPointToAnalysis extends CodeAnalyzer {

	public VisitorPointToAnalysis() {
	}

	public void run(List<IResource> resources, CallGraph callGraph) {
		setCallGraph(callGraph);

		// 01 - Iterate over all the resources.
		for (IResource resource : resources) {
			// We need this information when we are going retrieve the variable bindings in the callGraph.
			setCurrentResource(resource);

			run(resource);
		}
	}

	/**
	 * Iterate over all the method declarations found in the current resource.
	 * 
	 * @param resource
	 */
	protected void run(IResource resource) {

		// 02 - Get the list of methods in the current resource.
		Map<MethodDeclaration, List<Expression>> methods = getCallGraph().getMethods(resource);

		// 03 - Get all the method invocations of each method declaration.
		for (MethodDeclaration methodDeclaration : methods.keySet()) {
			run(methodDeclaration);
		}
	}

	/**
	 * Run the vulnerability detection on the current method declaration.
	 * 
	 * @param methodDeclaration
	 */
	protected void run(MethodDeclaration methodDeclaration) {
		// The depth control the investigation mechanism to avoid infinitive loops.
		int depth = 0;
		inspectBlock(depth, null, methodDeclaration.getBody());
	}

	// private void addMethodReferenceToVariable(Expression node) {
	// List<Expression> parameters = BindingResolver.getParameters(node);
	// for (Expression parameter : parameters) {
	// checkInitializer(node, parameter);
	// }
	// }

	//
	// private void checkInitializer(Expression expression, Expression initializer) {
	// if (null != initializer) {
	// switch (initializer.getNodeType()) {
	// case ASTNode.SIMPLE_NAME:
	// addReferenceSimpleName(expression, (SimpleName) initializer);
	// break;
	// case ASTNode.QUALIFIED_NAME:
	// addReferenceQualifiedName(expression, (QualifiedName) initializer);
	// break;
	// case ASTNode.ASSIGNMENT:
	// addReferenceAssgnment(expression, (Assignment) initializer);
	// break;
	// case ASTNode.INFIX_EXPRESSION:
	// addReferenceInfixExpression(expression, (InfixExpression) initializer);
	// break;
	// case ASTNode.CONDITIONAL_EXPRESSION:
	// addReferenceConditionalExpression(expression, (ConditionalExpression) initializer);
	// break;
	// case ASTNode.ARRAY_INITIALIZER:
	// addReferenceArrayInitializer(expression, (ArrayInitializer) initializer);
	// break;
	// case ASTNode.PARENTHESIZED_EXPRESSION:
	// addReferenceParenthesizedExpression(expression, (ParenthesizedExpression) initializer);
	// break;
	// }
	// }
	// }
	//
	// private void addReference(Expression expression, IBinding binding) {
	// VariableBindingManager variableBindingInitializer = callGraph.getLastReference(binding);
	// if (null != variableBindingInitializer) {
	// variableBindingInitializer.addReferences(expression);
	// }
	// }
	//
	// private void addReferenceSimpleName(Expression expression, SimpleName initializer) {
	// addReference(expression, initializer.resolveBinding());
	// }
	//
	// private void addReferenceQualifiedName(Expression expression, QualifiedName initializer) {
	// addReference(expression, initializer.resolveBinding());
	// }
	//
	// private void addReferenceAssgnment(Expression expression, Assignment initializer) {
	// checkInitializer(expression, initializer.getLeftHandSide());
	// checkInitializer(expression, initializer.getRightHandSide());
	// }
	//
	// private void addReferenceInfixExpression(Expression expression, InfixExpression initializer) {
	// checkInitializer(expression, initializer.getLeftOperand());
	// checkInitializer(expression, initializer.getRightOperand());
	//
	// List<Expression> extendedOperands = BindingResolver.getParameters(initializer);
	//
	// for (Expression current : extendedOperands) {
	// checkInitializer(expression, current);
	// }
	// }
	//
	// private void addReferenceConditionalExpression(Expression expression, ConditionalExpression initializer) {
	// checkInitializer(expression, initializer.getThenExpression());
	// checkInitializer(expression, initializer.getElseExpression());
	// }
	//
	// private void addReferenceArrayInitializer(Expression expression, ArrayInitializer initializer) {
	// List<Expression> expressions = BindingResolver.getParameters(initializer);
	//
	// for (Expression current : expressions) {
	// checkInitializer(expression, current);
	// }
	// }
	//
	// private void addReferenceParenthesizedExpression(Expression expression, ParenthesizedExpression initializer) {
	// checkInitializer(expression, initializer.getExpression());
	// }

}
