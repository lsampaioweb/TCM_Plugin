package net.thecodemaster.evd.graph;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.context.Context;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.helper.HelperCodeAnalyzer;
import net.thecodemaster.evd.marker.MarkerManager;
import net.thecodemaster.evd.point.EntryPoint;
import net.thecodemaster.evd.point.SanitizationPoint;
import net.thecodemaster.evd.ui.enumeration.EnumVariableStatus;
import net.thecodemaster.evd.ui.l10n.Message;
import net.thecodemaster.evd.xmlloader.LoaderEntryPoint;
import net.thecodemaster.evd.xmlloader.LoaderSanitizationPoint;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * @author Luciano Sampaio
 */
public abstract class CodeAnalyzer extends CodeVisitor {

	/**
	 * The current compilation unit that is being analyzed.
	 */
	private CompilationUnit									currentCompilationUnit;
	/**
	 * List with all the EntryPoints (shared among other instances of the verifiers).
	 */
	private static List<EntryPoint>					entryPoints;
	/**
	 * List with all the Sanitizers (shared among other instances of the verifiers).
	 */
	private static List<SanitizationPoint>	sanitizers;
	/**
	 * The object is responsible to update the progress bar of the user interface.
	 */
	private IProgressMonitor								progressMonitor;

	protected void setCurrentCompilationUnit(CompilationUnit currentCompilationUnit) {
		this.currentCompilationUnit = currentCompilationUnit;
	}

	protected CompilationUnit getCurrentCompilationUnit() {
		return currentCompilationUnit;
	}

	protected static List<EntryPoint> getEntryPoints() {
		if (null == entryPoints) {
			// Loads all the EntryPoints.
			entryPoints = (new LoaderEntryPoint()).load();
		}

		return entryPoints;
	}

	protected static List<SanitizationPoint> getSanitizationPoints() {
		if (null == sanitizers) {
			// Loads all the Sanitizers.
			sanitizers = (new LoaderSanitizationPoint()).load();
		}

		return sanitizers;
	}

	protected void setProgressMonitor(IProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}

	protected IProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	protected String getSubTaskMessage() {
		return null;
	}

	/**
	 * Notifies that a subtask of the main task is beginning.
	 * 
	 * @param taskName
	 *          The text that will be displayed to the user.
	 */
	protected void setSubTask(String taskName) {
		if (null != getProgressMonitor()) {
			getProgressMonitor().subTask(taskName);
		}
	}

	protected boolean hasMarkerAtPosition(ASTNode node) {
		return (null != MarkerManager.hasInvisibleMarkerAtPosition(getCurrentCompilationUnit(), getCurrentResource(), node));
	}

	protected String getMessageEntryPoint(String value) {
		return String.format(Message.VerifierSecurityVulnerability.ENTRY_POINT_METHOD, value);
	}

	/**
	 * Returns whether cancellation of current operation has been requested
	 * 
	 * @param reporter
	 * @return true if cancellation has been requested, and false otherwise.
	 */
	protected boolean userCanceledProcess(IProgressMonitor monitor) {
		return ((null != getProgressMonitor()) && (getProgressMonitor().isCanceled()));
	}

	protected void run(IProgressMonitor monitor, CallGraph callGraph, List<IResource> resources) {
		setProgressMonitor(monitor);
		setCallGraph(callGraph);

		// 01 - Iterate over all the resources.
		for (IResource resource : resources) {
			if (!userCanceledProcess(getProgressMonitor())) {
				// 02 - Set the current resource.
				setCurrentResource(resource);

				// 03 - Inform the user what is the current process of the plug-in.
				setSubTask(getSubTaskMessage());

				// 04 - Get the list of methods that will be processed from this resource.
				Map<MethodDeclaration, List<ASTNode>> methodsToProcess = getMethodsToProcess(resources, resource);

				// 05 - Process the detection on these methods.
				run(methodsToProcess);
			} else {
				// 02 - The user has stopped the process.
				return;
			}
		}
	}

	protected Map<MethodDeclaration, List<ASTNode>> getMethodsToProcess(List<IResource> resources, IResource resource) {
		// This map contains the method that will be processed and its invokers.
		Map<MethodDeclaration, List<ASTNode>> methodsToProcess = Creator.newMap();

		// 01 - Get the list of methods in the current resource and its invocations.
		Map<MethodDeclaration, List<ASTNode>> methods = getCallGraph().getMethods(resource);

		// 02 - Iterate over all the method declarations of the current resource.
		for (MethodDeclaration methodDeclaration : methods.keySet()) {

			// To avoid unnecessary processing, we only process methods that are
			// not invoked by any other method in the same file. Because if the method
			// is invoked, eventually it will be processed.
			// 03 - Get the list of methods that invokes this method.
			Map<MethodDeclaration, List<ASTNode>> invokers = getCallGraph().getInvokers(methodDeclaration);
			if (invokers.size() > 0) {
				// 04 - Iterate over all the methods that invokes this method.
				for (Entry<MethodDeclaration, List<ASTNode>> caller : invokers.entrySet()) {

					// 05 - Get the resource of this method (caller).
					IResource resourceCaller = BindingResolver.getResource(caller.getKey());

					// 06 - If this method is invoked by a method from another resource
					// and that resource is not in the list of resources that are going to be processed.
					if ((!resourceCaller.equals(resource)) && (!resources.contains(resourceCaller))) {

						// 07 - Care only about the invocations to this method.
						if (!methodsToProcess.containsKey(methodDeclaration)) {
							List<ASTNode> invocations = Creator.newList();

							// Create a empty list of method invocations.
							methodsToProcess.put(methodDeclaration, invocations);
						}

						// 08 - This method should be processed, add it to the list.
						methodsToProcess.get(methodDeclaration).addAll(caller.getValue());
					}
				}
			} else {
				List<ASTNode> emptyInvokers = Creator.newList();

				// 04 - This method should be processed, add it to the list.
				methodsToProcess.put(methodDeclaration, emptyInvokers);
			}

		}

		return methodsToProcess;
	}

	/**
	 * Run the vulnerability detection on the provided method declaration.
	 * 
	 * @param methodDeclaration
	 */
	protected void run(Map<MethodDeclaration, List<ASTNode>> methodsToProcess) {
		for (Entry<MethodDeclaration, List<ASTNode>> methodDeclaration : methodsToProcess.entrySet()) {
			// 01 - The depth controls the investigation mechanism to avoid infinitive loops.
			int depth = 0;

			// 02 - We need the compilation unit to check if there are markers in the current resource.
			setCurrentCompilationUnit(BindingResolver.getCompilationUnit(methodDeclaration.getKey()));

			// 03 - If this method is not invoked by any other, this method can be an entry method.
			// Main(), doGet or just a never used method.
			if (0 == methodDeclaration.getValue().size()) {

				// 03 - Process the detection on the current method.
				run(depth, methodDeclaration.getKey(), null);
			} else {
				for (ASTNode invoker : methodDeclaration.getValue()) {

					// 04 - Process the detection on the current method.
					run(depth, methodDeclaration.getKey(), invoker);
				}
			}
		}
	}

	/**
	 * Run the vulnerability detection on the current method declaration.
	 * 
	 * @param methodDeclaration
	 */
	protected void run(int depth, MethodDeclaration methodDeclaration, ASTNode invoker) {
	}

	/**
	 * 14
	 */
	@Override
	protected void inspectClassInstanceCreation(int depth, Context context, DataFlow dataFlow,
			ClassInstanceCreation invocation) {
		inspectMethodInvocation(depth, context, dataFlow, invocation);
	}

	/**
	 * 32
	 */
	@Override
	protected void inspectMethodInvocation(int depth, Context context, DataFlow dataFlow, MethodInvocation invocation) {
		inspectMethodInvocation(depth, context, dataFlow, invocation);
	}

	/**
	 * 32
	 */
	protected void inspectMethodInvocation(int depth, Context context, DataFlow dataFlow, Expression methodInvocation) {
		// The steps are:
		// 01 - Break the chain (if any) method calls into multiple method invocations.
		// 02 - Check if the method is an Exit-Point (Only verifiers check that).
		// 03 - Check if the method is a Sanitization-Point.
		// 04 - Check if the method is an Entry-Point.
		// 05 - Check if we have the source code of this method.
		// 06 - Get the context based on one of the 08 cases.
		// 07 - Get the instance object (if any) of the method call, this is important
		// because this is the object that hold the context.

		// 01 - Break the chain (if any) method calls into multiple method invocations.
		List<Expression> methodsInChain = HelperCodeAnalyzer.getMethodsFromChainInvocation(methodInvocation);

		if (1 < methodsInChain.size()) {
			inspectNode(depth, context, dataFlow, methodsInChain.get(methodsInChain.size() - 2));
		}

		inspectEachMethodInvocationOfChainInvocations(depth, context, dataFlow.addNodeToPath(methodInvocation),
				methodInvocation);
	}

	/**
	 * 32
	 */
	protected void inspectEachMethodInvocationOfChainInvocations(int depth, Context context, DataFlow dataFlow,
			Expression methodInvocation) {
		// 03 - Check if the method is a Sanitization-Point.
		if (BindingResolver.isMethodASanitizationPoint(getSanitizationPoints(), methodInvocation)) {
			// If a sanitization method is being invoked, then we do not have a vulnerability.
			return;
		}

		// 04 - Get a new data flow or a child from the parent.
		DataFlow newDataFlow = HelperCodeAnalyzer.getDataFlow(dataFlow, methodInvocation);

		// 05 - Check if the method is an Entry-Point.
		if (BindingResolver.isMethodAnEntryPoint(getEntryPoints(), methodInvocation)) {
			// 06 - Check if there is a marker, in case there is, we should BELIEVE it is not vulnerable.
			if (hasMarkerAtPosition(methodInvocation)) {
				return;
			}

			String message = getMessageEntryPoint(BindingResolver.getFullName(methodInvocation));

			// We found a invocation to a entry point method.
			newDataFlow.hasVulnerablePath(Constant.Vulnerability.ENTRY_POINT, message);
			return;
		}

		// 06 - There are 2 cases: When we have the source code of this method and when we do not.
		inspectMethodInvocationWithOrWithOutSourceCode(depth, context, newDataFlow, methodInvocation);
	}

	/**
	 * 32
	 */
	protected void inspectMethodInvocationWithOrWithOutSourceCode(int depth, Context context, DataFlow dataFlow,
			Expression methodInvocation) {
		// Some method invocations can be in a chain call, we have to investigate them all.
		// response.sendRedirect(login);
		// getServletContext().getRequestDispatcher(login).forward(request, response);
		// 01 - There are 2 cases: When we have the source code of this method and when we do not.
		MethodDeclaration methodDeclaration = getCallGraph().getMethod(getCurrentResource(), methodInvocation);
		if (null != methodDeclaration) {
			// We have the source code.
			inspectMethodWithSourceCode(depth, context, dataFlow, methodInvocation, methodDeclaration);
		} else {
			inspectMethodWithOutSourceCode(depth, context, dataFlow, methodInvocation);
		}

		VariableBinding variableBinding = HelperCodeAnalyzer.getVariableBindingIfItIsAnObject(getCallGraph(), context,
				methodInvocation);
		// We found a vulnerability.
		if (dataFlow.hasVulnerablePath()) {
			// There are 2 sub-cases: When is a method from an object and when is a method from a library.
			// 01 - stringBuilder.append("...");
			// 02 - System.out.println("..."); Nothing else to do.

			// 02 - Check if this method invocation is being call from a vulnerable object.
			if (null != variableBinding) {
				variableBinding.setStatus(EnumVariableStatus.VULNERABLE).setDataFlow(dataFlow);
			}
		} else if (null == methodDeclaration) {
			// 01 - Check if this method invocation is being call from a vulnerable object.
			// Only methods that we do not have the implementation should get here.
			if (null != variableBinding) {
				processIfStatusUnknownOrUpdateIfVulnerable(depth, context, dataFlow, variableBinding);
			}
		}
	}

	/**
	 * 32
	 */
	protected Context getContext(Context context, MethodDeclaration methodDeclaration, ASTNode methodInvocation) {
		return null;
	}

	/**
	 * 32 , 42
	 */
	protected void processIfStatusUnknownOrUpdateIfVulnerable(int depth, Context context, DataFlow dataFlow,
			VariableBinding variableBinding) {
		if (variableBinding.getStatus().equals(EnumVariableStatus.VULNERABLE)) {
			dataFlow.replace(variableBinding.getDataFlow());
		} else if (variableBinding.getStatus().equals(EnumVariableStatus.UNKNOWN)) {
			// 01 - This is the case where we have to go deeper into the variable's path.
			inspectNode(depth, context, dataFlow, variableBinding.getInitializer());

			// 02 - If there is a vulnerable path, then this variable is vulnerable.
			HelperCodeAnalyzer.updateVariableBindingStatus(variableBinding, dataFlow);
		}
	}

	/**
	 * 40
	 */
	@Override
	protected void inspectQualifiedName(int depth, Context context, DataFlow dataFlow, QualifiedName expression) {
		super.inspectQualifiedName(depth, getContext(context, expression), dataFlow, expression);
	}

	/**
	 * 40
	 */
	protected Context getContext(Context context, Expression expression) {
		// 02 - Get the binding of this variable;
		IBinding binding = BindingResolver.resolveBinding(expression);
		if (null != binding) {

			// 03 - Get the instance (object or static);
			Expression instance = HelperCodeAnalyzer.getInstanceIfItIsAnObject(expression);

			if (Modifier.isStatic(binding.getModifiers())) {
				// Person.staticPersonVariable

				// 04 - Get the resource of this static variable.
				IResource resource = HelperCodeAnalyzer.getClassResource(getCallGraph(), instance);

				// 05 - Get the context (top level) of this resource.
				context = getCallGraph().getStaticContext(resource);
			} else {
				// person.publicPersonVariable
				context = getCallGraph().getInstanceContext(context, instance);
			}
		}
		return context;
	}

	/**
	 * 42
	 */
	@Override
	protected void inspectSimpleName(int depth, Context context, DataFlow dataFlow, SimpleName expression) {
		// 01 - Try to retrieve the variable from the list of variables.
		VariableBinding variableBinding = getCallGraph().getVariableBinding(context, expression);

		inspectSimpleName(depth, context, dataFlow, expression, variableBinding);
	}

	/**
	 * 42
	 */
	protected void inspectSimpleName(int depth, Context context, DataFlow dataFlow, SimpleName expression,
			VariableBinding variableBinding) {
		DataFlow newDataFlow = dataFlow.addNodeToPath(expression);

		if (null != variableBinding) {
			processIfStatusUnknownOrUpdateIfVulnerable(depth, context, newDataFlow, variableBinding);
		} else {
			// If a method is scanned after a method invocation, all the parameters are provided, but
			// if a method is scanned from the initial block declarations loop, some parameter might not be known
			// so it is necessary to investigate WHO invoked this method and what were the provided parameters.
			inspectSimpleNameFromInvokers(depth, context, newDataFlow, expression, variableBinding);
		}
	}

	/**
	 * 42
	 */
	protected void inspectSimpleNameFromInvokers(int depth, Context context, DataFlow dataFlow, SimpleName expression,
			VariableBinding variableBinding) {
		// This is the case where the variable is an argument of the method.
		// 01 - Get the method signature that is using this parameter.
		// MethodDeclaration methodDeclaration = BindingResolver.getParentMethodDeclaration(expression);
		//
		// // 02 - Get the index position where this parameter appear.
		// int parameterIndex = BindingResolver.getParameterIndex(methodDeclaration, expression);
		// if (parameterIndex >= 0) {
		// // 03 - Get the list of methods that invokes this method.
		// Map<MethodDeclaration, List<Expression>> invokers = getCallGraph().getInvokers(methodDeclaration);
		//
		// // 04 - Iterate over all the methods that invokes this method.
		// for (List<Expression> currentInvocations : invokers.values()) {
		//
		// for (Expression invocations : currentInvocations) {
		// // 05 - Get the parameter at the index position.
		// Expression parameter = BindingResolver.getParameterAtIndex(invocations, parameterIndex);
		//
		// // 06 - Run detection on this parameter.
		// inspectNode(depth, context, dataFlow.addNodeToPath(parameter), parameter);
		// }
		// }
		// }
	}

}