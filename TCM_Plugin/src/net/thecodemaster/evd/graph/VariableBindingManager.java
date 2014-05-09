package net.thecodemaster.evd.graph;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * @author Luciano Sampaio
 */
public class VariableBindingManager {
	private final VariableDeclarationFragment	variableDeclarationFragment;

	private Statement													firstReference;

	private Expression												initializer;

	/**
	 * Constructor.
	 * 
	 * @param variableDeclarationFragment
	 *          the variable declaration fragment of the variable this manager handles.
	 */
	public VariableBindingManager(VariableDeclarationFragment variableDeclarationFragment) {
		this.variableDeclarationFragment = variableDeclarationFragment;
	}

	/**
	 * Has to be called for every reference to the variable handled by this instance.
	 * 
	 * @param reference
	 *          the AST node that references the local variable
	 */
	public void variableRefereneced(SimpleName reference) {
		if (!isAlreadyReferenced()) {
			// First reference -> store the statement that contains the reference directly.
			firstReference = BindingResolver.getParentStatement(reference);
		} else {
			// It's not the first declaration.
			while (!isReferenceWhithinScope(reference)) {
				// But it's referenced from a different block.
				moveFirstReferenceOneBlockLevelUp();
				// move first reference statement up in the block hierarchy
				// until the new first reference and the currently focused
				// reference appear within the same block.
			}
		}
	}

	/**
	 * Call this method to report an assignment of the variable
	 * 
	 * @param initializer
	 *          the AST node that represents the value of the assignment
	 */
	public void variableInitialized(Expression initializer) {
		if (!isAlreadyReferenced()) {
			// Only save if variable has not been referenced yet.
			this.initializer = initializer;
		}
	}

	/**
	 * Sets the {@link #firstReference} one block level up. For example, suppose {@code System.out.println(x);} is the {
	 * {@link #firstReference} statement.
	 */
	private void moveFirstReferenceOneBlockLevelUp() {
		ASTNode node = BindingResolver.getParentBlock(firstReference);
		while ((node != null) && (!(node.getParent() instanceof Block))) {
			node = node.getParent();
		}

		firstReference = (Statement) node;
	}

	/**
	 * Checks whether reference is within the same or within an underlying block like {@link #firstReference}.
	 * 
	 * @param reference
	 *          the reference to be tested
	 * @return {@code true}, if reference is within the same or underlying block. {@code false} otherwise.
	 */
	private boolean isReferenceWhithinScope(SimpleName reference) {
		Block firstReferenceBlock = BindingResolver.getParentBlock(firstReference);
		// get the block that contains the first reference statement

		ASTNode node = reference;

		// step down in the ast parent-child-node hierarchy. If
		// reference figures within the same block or an underlying
		// block of firstReferenceBlock, firstReferenceBlock has to
		// appear some where while stepping down.
		while (node != null) {
			if (node == firstReferenceBlock) {
				return true;
			}

			node = node.getParent();
		}

		// If it doesn't, reference neither is element of firstReferenceBlock
		// nor of a sub-block of firstReferenceBlock.
		return false;
	}

	/**
	 * Tells us whether already a reference to the local variable has been recorded.
	 * 
	 * @return {@code true} if a reference has been recorded, {@code false} otherwise
	 */
	private boolean isAlreadyReferenced() {
		return firstReference != null;
	}

	/**
	 * Getter for the first reference {@link Statement}.
	 * 
	 * @return first reference {@link Statement}
	 */
	public Statement getFirstReference() {
		return firstReference;
	}

	/**
	 * Getter for the last initializer value, before the variable is referenced for the first time.
	 * 
	 * @return the value the variable has to be initialized with
	 */
	public Expression getInitializer() {
		return initializer;
	}

	/**
	 * Getter for the {@link VariableDeclarationFragment} provided as constructor parameter.
	 * 
	 * @return the {@link VariableDeclarationFragment} of the variable handled by this manager
	 */
	public VariableDeclarationFragment getVariableDeclarationFragment() {
		return variableDeclarationFragment;
	}
}
