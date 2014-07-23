package net.thecodemaster.esvd.visitor;

import java.util.Iterator;
import java.util.Stack;

import net.thecodemaster.esvd.graph.CallGraph;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * It adds the interactions between methods, field and etc in the current file to the callGraph object.
 * 
 * @author Luciano Sampaio
 * @Date: 2014-05-07
 * @Version: 02
 */
public class VisitorCompilationUnit extends ASTVisitor {

	/**
	 * The current file that is being analyzed.
	 */
	private final IResource									resource;
	private final CallGraph									callGraph;
	private final Stack<MethodDeclaration>	methodStack;

	public VisitorCompilationUnit(IResource resource, CallGraph callGraph) {
		this.resource = resource;
		this.callGraph = callGraph;

		methodStack = new Stack<MethodDeclaration>();
	}

	private IResource getResource() {
		return resource;
	}

	private CallGraph getCallGraph() {
		return callGraph;
	}

	private Stack<MethodDeclaration> getMethodStack() {
		return methodStack;
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		for (Iterator<?> iter = node.fragments().iterator(); iter.hasNext();) {
			// VariableDeclarationFragment: is the plain variable declaration part.
			// Example: "int x=0, y=0;" contains two VariableDeclarationFragments, "x=0" and "y=0"
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) iter.next();

			getCallGraph().addFieldDeclaration(getResource(), fragment.getName(), fragment.getInitializer());
		}

		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		getCallGraph().addMethodDeclaration(getResource(), node);

		// Push the current method into the stack.
		getMethodStack().push(node);

		return super.visit(node);
	}

	/**
	 * Remove the top element from the stack.
	 */
	@Override
	public void endVisit(MethodDeclaration node) {
		if (!getMethodStack().isEmpty()) {
			getMethodStack().pop();
		}
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		addInvocation(node);

		return super.visit(node);
	}

	@Override
	public boolean visit(MethodInvocation node) {
		addInvocation(node);

		return super.visit(node);
	}

	private void addInvocation(Expression method) {
		if (!getMethodStack().isEmpty()) {
			getCallGraph().addMethodInvocation(getResource(), getMethodStack().peek(), method);
		}
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclarationStatement) {
		// 01 - Get the Inner Class (if any).
		if (!typeDeclarationStatement.isPackageMemberTypeDeclaration()) {
			// System.out.println(typeDeclarationStatement.getName());
			// if (!getMethodStack().isEmpty()) {
			// getCallGraph().addInnerClass(getResource(), getMethodStack().peek(), typeDeclarationStatement);
			// }
		} else {
			// 02 - Get the name/type of the super class (if any).
			Type superClassName = typeDeclarationStatement.getSuperclassType();
			if (null != superClassName) {
				getCallGraph().addSuperClass(getResource(), superClassName);
			}
		}

		return super.visit(typeDeclarationStatement);
	}

	// TODO - Anonymous Class.
	// @Override
	// public boolean visit(AnonymousClassDeclaration anonymousClassDeclaration) {
	// System.out.println(anonymousClassDeclaration.toString());
	//
	// return super.visit(anonymousClassDeclaration);
	// }

}
