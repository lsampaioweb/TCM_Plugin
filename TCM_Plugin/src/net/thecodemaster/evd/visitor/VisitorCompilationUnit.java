package net.thecodemaster.evd.visitor;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import net.thecodemaster.evd.graph.BindingResolver;
import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.graph.VariableBindingManager;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * @author Luciano Sampaio
 */
public class VisitorCompilationUnit extends ASTVisitor {

	private final Stack<MethodDeclaration>	methodStack;
	private final CallGraph									callGraph;

	public VisitorCompilationUnit(CallGraph callGraph) {
		methodStack = new Stack<MethodDeclaration>();

		this.callGraph = callGraph;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		callGraph.addMethod(node);

		// Push the current method into the stack.
		methodStack.push(node);

		return super.visit(node);
	}

	/**
	 * Remove the top element from the stack.
	 */
	@Override
	public void endVisit(MethodDeclaration node) {
		if (!methodStack.isEmpty()) {
			methodStack.pop();
		}
	}

	@Override
	public boolean visit(MethodInvocation node) {
		addInvokes(node);

		addMethodReferenceToVariable(node);

		return super.visit(node);
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		addInvokes(node);

		addMethodReferenceToVariable(node);

		return super.visit(node);
	}

	private void addInvokes(Expression method) {
		if ((null != method) && (!methodStack.isEmpty())) {
			callGraph.addMethodInvocation(methodStack.peek(), method);
		}
	}

	private void addMethodReferenceToVariable(Expression node) {
		List<Expression> parameters = BindingResolver.getParameters(node);
		for (Expression parameter : parameters) {
			IBinding binding = null;

			switch (parameter.getNodeType()) {
				case ASTNode.SIMPLE_NAME:
					binding = ((SimpleName) parameter).resolveBinding();
					break;
				case ASTNode.ASSIGNMENT:
					Assignment assignment = (Assignment) parameter;

					if (assignment.getLeftHandSide().getNodeType() == ASTNode.SIMPLE_NAME) {
						binding = ((SimpleName) assignment.getLeftHandSide()).resolveBinding();

						visit(assignment);
					}
					break;
			}

			if (null != binding) {
				addReference(node, binding);
			}
		}
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		return addVariableToList(node.fragments());
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		return addVariableToList(node.fragments());
	}

	private boolean addVariableToList(List<?> fragments) {
		for (Iterator<?> iter = fragments.iterator(); iter.hasNext();) {
			// VariableDeclarationFragment: is the plain variable declaration part.
			// Example: "int x=0, y=0;" contains two VariableDeclarationFragments, "x=0" and "y=0"
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) iter.next();

			addVariableToCallGraph(fragment.getName(), fragment.getInitializer());
		}

		return true;
	}

	@Override
	public boolean visit(Assignment node) {
		if (node.getLeftHandSide().getNodeType() == ASTNode.SIMPLE_NAME) {
			addVariableToCallGraph((SimpleName) node.getLeftHandSide(), node.getRightHandSide());
		}

		return super.visit(node);
	}

	private void addVariableToCallGraph(SimpleName simpleName, Expression initializer) {
		IBinding binding = simpleName.resolveBinding();

		VariableBindingManager variableBinding = new VariableBindingManager(binding);

		variableBinding.setInitializer(initializer);

		if (null != initializer) {
			switch (initializer.getNodeType()) {
				case ASTNode.SIMPLE_NAME:
					addReferenceSimpleName(simpleName, initializer);
					break;
				case ASTNode.INFIX_EXPRESSION:
					addReferenceInfixExpression(simpleName, (InfixExpression) initializer);
					break;
			}
		}

		callGraph.addVariable(variableBinding);
	}

	private void addReference(Expression expression, IBinding binding) {
		VariableBindingManager variableBindingInitializer = callGraph.getLastReference(binding);
		if (null != variableBindingInitializer) {
			variableBindingInitializer.addReferences(expression);
		}
	}

	private void addReferenceSimpleName(SimpleName simpleName, Expression expression) {
		if (expression.getNodeType() == ASTNode.SIMPLE_NAME) {
			SimpleName initializer = (SimpleName) expression;
			addReference(simpleName, initializer.resolveBinding());
		}
	}

	private void addReferenceInfixExpression(SimpleName simpleName, InfixExpression initializer) {
		addReferenceSimpleName(simpleName, initializer.getLeftOperand());
		addReferenceSimpleName(simpleName, initializer.getRightOperand());

		List<Expression> extendedOperands = BindingResolver.getParameters(initializer);

		for (Expression expression : extendedOperands) {
			addReferenceSimpleName(simpleName, expression);
		}
	}

}
