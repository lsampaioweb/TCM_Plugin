package net.thecodemaster.sap.visitors;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import net.thecodemaster.sap.graph.CallGraph;
import net.thecodemaster.sap.graph.VariableBindingManager;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * @author Luciano Sampaio
 */
public class CompilationUnitVisitor extends ASTVisitor {

	private final Stack<MethodDeclaration>	methodStack;
	private final CallGraph									callGraph;

	public CompilationUnitVisitor(CallGraph callGraph) {
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

		return super.visit(node);
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		addInvokes(node);

		return super.visit(node);
	}

	private void addInvokes(Expression method) {
		if ((null != method) && (!methodStack.isEmpty())) {
			callGraph.addInvokes(methodStack.peek(), method);
		}
	}

	/**
	 * Looks for local variable declarations. For every occurrence of a local variable, a {@link VariableBindingManager}
	 * is created and stored in listVariables map.
	 * 
	 * @param node
	 *          the node to visit
	 * @return static {@code false} to prevent that the simple name in the declaration is understood by
	 *         {@link #visit(SimpleName)} as reference
	 */
	@Override
	public boolean visit(VariableDeclarationStatement node) {
		return addVariableToList(node.fragments());
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		return addVariableToList(node.fragments());
	}

	private boolean addVariableToList(List<?> fragments) {
		for (Iterator<?> iter = fragments.iterator(); iter.hasNext();) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) iter.next();

			// VariableDeclarationFragment: is the plain variable declaration part.
			// Example: "int x=0, y=0;" contains two VariableDeclarationFragments, "x=0" and "y=0"
			IVariableBinding binding = fragment.resolveBinding();

			// Creates the manager of the fragment.
			VariableBindingManager manager = new VariableBindingManager(fragment);

			callGraph.getlistVariables().put(binding, manager);

			// The first assignment is the initializer.
			manager.variableInitialized(fragment.getInitializer());
		}

		return false; // Prevents that SimpleName is interpreted as reference.
	}

	/**
	 * Visits {@link Assignment} AST nodes (e.g. {@code x = 7 + 8} ). Resolves the binding of the left hand side (in the
	 * example: {@code x}). If the binding is found in the listVariables map, we have an assignment of a local variable.
	 * The variable binding manager of this local variable then has to be informed about this assignment.
	 * 
	 * @param node
	 *          the node to visit
	 */
	@Override
	public boolean visit(Assignment node) {
		if (node.getLeftHandSide().getNodeType() == ASTNode.SIMPLE_NAME) {
			IBinding binding = ((SimpleName) node.getLeftHandSide()).resolveBinding();
			if (callGraph.getlistVariables().containsKey(binding)) {
				// It contains the key -> it is an assignment of a local variable.

				VariableBindingManager manager = callGraph.getlistVariables().get(binding);
				manager.variableInitialized(node.getRightHandSide());
			}
		}

		return false; // Prevents that SimpleName is interpreted as reference.
	}

	/**
	 * Visits {@link SimpleName} AST nodes. Resolves the binding of the simple name and looks for it in the listVariables
	 * map. If the binding is found, this is a reference to a local variable. The variable binding manager of this local
	 * variable then has to be informed about that reference.
	 * 
	 * @param node
	 *          the node to visit
	 */
	@Override
	public boolean visit(SimpleName node) {
		IBinding binding = node.resolveBinding();
		if (callGraph.getlistVariables().containsKey(binding)) {
			VariableBindingManager manager = callGraph.getlistVariables().get(binding);
			manager.variableRefereneced(node);
		}

		return true;
	}

}
