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
			callGraph.addInvokes(methodStack.peek(), method);
		}
	}

	private void addMethodReferenceToVariable(Expression node) {
		List<Expression> parameters = BindingResolver.getParameters(node);
		for (Expression parameter : parameters) {
			IBinding binding = null;

			if (parameter.getNodeType() == ASTNode.SIMPLE_NAME) {
				binding = ((SimpleName) parameter).resolveBinding();
			} else if (parameter.getNodeType() == ASTNode.ASSIGNMENT) {
				Assignment assignment = (Assignment) parameter;

				if (assignment.getLeftHandSide().getNodeType() == ASTNode.SIMPLE_NAME) {
					binding = ((SimpleName) assignment.getLeftHandSide()).resolveBinding();

					addVariableToCallGraph(binding, assignment.getRightHandSide());
				}
			}

			if (null != binding) {
				VariableBindingManager variableBinding = callGraph.getLastReference(binding);
				if (null != variableBinding) {
					variableBinding.addMethod(node);
				}
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

			addVariableToCallGraph(fragment.resolveBinding(), fragment.getInitializer());
		}

		return true;
	}

	@Override
	public boolean visit(Assignment node) {
		if (node.getLeftHandSide().getNodeType() == ASTNode.SIMPLE_NAME) {
			IBinding binding = ((SimpleName) node.getLeftHandSide()).resolveBinding();

			addVariableToCallGraph(binding, node.getRightHandSide());
		}

		return super.visit(node);
	}

	private void addVariableToCallGraph(IBinding binding, Expression initializer) {
		VariableBindingManager variableBinding = new VariableBindingManager(binding);

		if ((null != initializer) && (initializer.getNodeType() == ASTNode.SIMPLE_NAME)) {
			IBinding bindingInitializer = ((SimpleName) initializer).resolveBinding();
			variableBinding.setInitializer(callGraph.getLastReference(bindingInitializer));
		} else {
			variableBinding.setInitializer(initializer);
		}

		callGraph.addVariable(variableBinding);
	}

}
