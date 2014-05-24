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
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
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

	private final CompilationUnit						cUnit;
	private final Stack<MethodDeclaration>	methodStack;
	private final CallGraph									callGraph;

	public VisitorCompilationUnit(CompilationUnit cUnit, CallGraph callGraph) {
		this.cUnit = cUnit;
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
			checkInitializer(node, parameter);
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

	@Override
	public boolean visit(Assignment node) {
		if (node.getLeftHandSide().getNodeType() == ASTNode.SIMPLE_NAME) {
			SimpleName leftHandSide = (SimpleName) node.getLeftHandSide();
			Expression rightHandSide = null;

			if (node.getOperator().equals(Operator.PLUS_ASSIGN)) {
				// ASTRewrite rewriter = ASTRewrite.create(cUnit.getAST());
				// AST ast = rewriter.getAST();
				//
				// InfixExpression expr = ast.newInfixExpression();
				//
				// Expression left = (Expression) ASTNode.copySubtree(ast, node.getLeftHandSide());
				// Expression right = (Expression) ASTNode.copySubtree(ast, node.getRightHandSide());
				//
				// // String message = "a";
				// // message += "b";
				// // Result: message = message + "b"
				// expr.setLeftOperand(left);
				// expr.setOperator(InfixExpression.Operator.PLUS);
				// expr.setRightOperand(right);
				//
				// rightHandSide = expr;
				rightHandSide = node.getRightHandSide();
			} else {
				rightHandSide = node.getRightHandSide();
			}

			addVariableToCallGraph(leftHandSide, rightHandSide);
		}

		return super.visit(node);
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

	private void addVariableToCallGraph(SimpleName simpleName, Expression initializer) {
		IBinding binding = simpleName.resolveBinding();

		VariableBindingManager variableBinding = new VariableBindingManager(binding);

		variableBinding.setInitializer(initializer);

		checkInitializer(simpleName, initializer);

		callGraph.addVariable(variableBinding);
	}

	private void checkInitializer(Expression expression, Expression initializer) {
		if (null != initializer) {
			switch (initializer.getNodeType()) {
				case ASTNode.SIMPLE_NAME:
					addReferenceSimpleName(expression, (SimpleName) initializer);
					break;
				case ASTNode.ASSIGNMENT:
					addReferenceAssgnment(expression, (Assignment) initializer);
					break;
				case ASTNode.INFIX_EXPRESSION:
					addReferenceInfixExpression(expression, (InfixExpression) initializer);
					break;
				case ASTNode.CONDITIONAL_EXPRESSION:
					addReferenceConditionalExpression(expression, (ConditionalExpression) initializer);
					break;
			}
		}
	}

	private void addReference(Expression expression, IBinding binding) {
		VariableBindingManager variableBindingInitializer = callGraph.getLastReference(binding);
		if (null != variableBindingInitializer) {
			variableBindingInitializer.addReferences(expression);
		}
	}

	private void addReferenceSimpleName(Expression expression, SimpleName initializer) {
		addReference(expression, initializer.resolveBinding());
	}

	private void addReferenceAssgnment(Expression expression, Assignment initializer) {
		checkInitializer(expression, initializer.getLeftHandSide());
		checkInitializer(expression, initializer.getRightHandSide());
	}

	private void addReferenceInfixExpression(Expression expression, InfixExpression initializer) {
		checkInitializer(expression, initializer.getLeftOperand());
		checkInitializer(expression, initializer.getRightOperand());

		List<Expression> extendedOperands = BindingResolver.getParameters(initializer);

		for (Expression current : extendedOperands) {
			checkInitializer(expression, current);
		}
	}

	private void addReferenceConditionalExpression(Expression expression, ConditionalExpression initializer) {
		checkInitializer(expression, initializer.getThenExpression());
		checkInitializer(expression, initializer.getElseExpression());
	}

}
