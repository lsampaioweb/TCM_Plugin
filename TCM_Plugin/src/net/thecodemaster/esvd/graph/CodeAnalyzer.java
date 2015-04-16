package net.thecodemaster.esvd.graph;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import net.thecodemaster.esvd.context.Context;
import net.thecodemaster.esvd.graph.flow.DataFlow;
import net.thecodemaster.esvd.graph.flow.Flow;
import net.thecodemaster.esvd.helper.Creator;
import net.thecodemaster.esvd.helper.HelperCodeAnalyzer;
import net.thecodemaster.esvd.logger.PluginLogger;
import net.thecodemaster.esvd.marker.MarkerManager;
import net.thecodemaster.esvd.point.EntryPointManager;
import net.thecodemaster.esvd.point.ExitPointManager;
import net.thecodemaster.esvd.point.SanitizationPointManager;
import net.thecodemaster.esvd.reporter.Reporter;
import net.thecodemaster.esvd.ui.enumeration.EnumVariableStatus;
import net.thecodemaster.esvd.ui.l10n.Message;
import net.thecodemaster.esvd.xmlloader.LoaderEntryPoint;
import net.thecodemaster.esvd.xmlloader.LoaderSanitizationPoint;

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
	private CompilationUnit					currentCompilationUnit;
	/**
	 * List with all the EntryPoints (shared among other instances of the verifiers).
	 */
	private static EntryPointManager		entryPointManager;
	/**
	 * List with all the Sanitizers (shared among other instances of the verifiers).
	 */
	private static SanitizationPointManager	sanitizationPointManager;
	/**
	 * List with all the EntryPoints (shared among other instances of the verifiers).
	 */
	private static ExitPointManager			exitPointManager;
	/**
	 * The object that know how and where to report the found vulnerabilities.
	 */
	private Reporter						reporter;

	protected void setCurrentCompilationUnit(CompilationUnit currentCompilationUnit) {
		this.currentCompilationUnit = currentCompilationUnit;
	}

	protected void setCurrentCompilationUnit(List<MethodDeclaration> methodsToProcess) {
		if (0 < methodsToProcess.size()) {
			MethodDeclaration methodDeclaration = methodsToProcess.get(0);
			if (null != methodDeclaration) {
				setCurrentCompilationUnit(BindingResolver.getCompilationUnit(methodDeclaration));
			}
		}
	}

	protected CompilationUnit getCurrentCompilationUnit() {
		return currentCompilationUnit;
	}

	protected static EntryPointManager getEntryPointManager() {
		if (null == entryPointManager) {
			// Loads all the EntryPoints.
			entryPointManager = (new LoaderEntryPoint()).load();
		}

		return entryPointManager;
	}

	protected static SanitizationPointManager getSanitizationManager() {
		if (null == sanitizationPointManager) {
			// Loads all the Sanitizers.
			sanitizationPointManager = (new LoaderSanitizationPoint()).load();
		}

		return sanitizationPointManager;
	}

	protected static ExitPointManager getExitPointManager() {
		if (null == exitPointManager) {
			// Loads all the ExitPoints.
			exitPointManager = new ExitPointManager();
		}

		return exitPointManager;
	}

	protected void setReporter(Reporter reporter) {
		this.reporter = reporter;
	}

	protected Reporter getReporter() {
		return reporter;
	}

	protected IProgressMonitor getProgressMonitor() {
		if (null != getReporter()) {
			return getReporter().getProgressMonitor();
		}
		return null;
	}

	protected String getSubTaskMessage(int numberOfResourcesProcessed, int numberOfResources) {
		return null;
	}

	/**
	 * Notifies that a subtask of the main task is beginning.
	 * 
	 * @param taskName
	 *            The text that will be displayed to the user.
	 */
	protected void setSubTask(String taskName) {
		if (null != getProgressMonitor()) {
			getProgressMonitor().subTask(taskName);
		}
	}

	protected boolean hasMarkerAtPosition(ASTNode node) {
		return (null != MarkerManager.hasInvisibleMarkerAtPosition(getCurrentCompilationUnit(), getCurrentResource(),
				node));
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

	protected void run(Map<IResource, List<MethodDeclaration>> resourcesAndMethodsToProcess) {
		int numberOfResources = resourcesAndMethodsToProcess.size();
		int numberOfResourcesProcessed = 0;
		int numberOfMethodsProcessed = 0;

		// 01 - Iterate over all the resources.
		for (Entry<IResource, List<MethodDeclaration>> entry : resourcesAndMethodsToProcess.entrySet()) {
			if (!userCanceledProcess(getProgressMonitor())) {
				// 02 - Set the current resource.
				setCurrentResource(entry.getKey());
				// System.out.println(entry.getKey());

				// 03 - Inform the user what is the current process of the plug-in.
				setSubTask(getSubTaskMessage(++numberOfResourcesProcessed, numberOfResources));

				// 04 - Get the list of methods that will be processed from this resource.
				List<MethodDeclaration> methodsToProcess = entry.getValue();

				// 05 - We need the compilation unit to check if there are markers in the current resource.
				setCurrentCompilationUnit(methodsToProcess);

				// 06 - Process the detection on these methods.
				run(methodsToProcess);

				// 07 - Get the number of method processed on the current resource.
				numberOfMethodsProcessed += methodsToProcess.size();
			} else {
				// 02 - The user has stopped the process.
				return;
			}
		}

		// 08 - Print some statistical information.
		PluginLogger.logIfDebugging(String.format("01.2 - Processed resources: %d - Processed methods: %d",
				numberOfResourcesProcessed, numberOfMethodsProcessed));
	}

	/**
	 * Run the vulnerability detection on the provided method declaration.
	 * 
	 * @param methodDeclaration
	 */
	protected void run(List<MethodDeclaration> methodsToProcess) {
		for (MethodDeclaration methodDeclaration : methodsToProcess) {
			// 01 - If this method is not invoked by any other, this method can be an entry method.
			// Main(), doGet or just a never used method.
			// 02 - Process the detection on the current method.
			run(methodDeclaration);
		}
	}

	/**
	 * Run the vulnerability detection on the current method declaration.
	 * 
	 * @param methodDeclaration
	 */
	protected void run(MethodDeclaration methodDeclaration) {
	}

	protected Map<IResource, List<MethodDeclaration>> getMethodsToProcess(List<IResource> resources) {
		Map<IResource, List<MethodDeclaration>> resourcesAndMethodsToProcess = Creator.newMap();

		ListIterator<IResource> iterator = resources.listIterator();

		// 01 - The list will contain the list of resource callers that were added to also be processed.
		List<IResource> addedResourceCallers = Creator.newList();

		while (iterator.hasNext()) {
			IResource resource = iterator.next();

			List<MethodDeclaration> methodsToProcess = Creator.newList();

			// 02 - Get the list of methods in the current resource and its invocations.
			Map<MethodDeclaration, List<ASTNode>> methods = getCallGraph().getMethods(resource);

			// 03 - Iterate over all the method declarations of the current resource.
			for (MethodDeclaration methodDeclaration : methods.keySet()) {

				// To avoid unnecessary processing, we only process methods that are
				// not invoked by any other method in the same file. Because if the method
				// is invoked, eventually it will be processed.
				// 04 - Get the list of methods that invokes this method.
				Map<MethodDeclaration, List<ASTNode>> invokers = getCallGraph().getInvokers(methodDeclaration);
				if (invokers.size() > 0) {
					// 05 - Iterate over all the methods that invokes this method.
					for (Entry<MethodDeclaration, List<ASTNode>> caller : invokers.entrySet()) {

						// 06 - Get the resource of this method (caller).
						IResource resourceCaller = BindingResolver.getResource(caller.getKey());

						// 07 - If this method is invoked by a method from another resource
						// and that resource is not in the list of resources that are going to be processed.
						if ((null != resourceCaller) && (!resourceCaller.equals(resource))
								&& (!resources.contains(resourceCaller))) {

							// 08 - Add the resource caller to the list of resources that should be processed.
							iterator.add(resourceCaller);
							// 09 - Make the iterator "see" this new element.
							iterator.previous();
							// 10 - Remove old contexts of this resource.
							getCallGraph().removeChildContexts(resourceCaller);
							// 11 - Add the resourceCaller to the list.
							addedResourceCallers.add(resourceCaller);
						}
					}
				} else {
					// 04 - This method should be processed, add it to the list.
					methodsToProcess.add(methodDeclaration);
				}

			}

			// 03 - Add it to the list.
			resourcesAndMethodsToProcess.put(resource, methodsToProcess);
		}

		// 11 - If any resourceCaller was inserted to be processed. We have to clear any old problems (if any).
		if (null != getReporter()) {
			getReporter().clearOldProblems(addedResourceCallers);
		}

		return resourcesAndMethodsToProcess;
	}

	/**
	 * 14
	 */
	@Override
	protected void inspectClassInstanceCreation(Flow loopControl, Context context, DataFlow dataFlow,
			ClassInstanceCreation invocation) {
		inspectMethodInvocation(loopControl, context, dataFlow, invocation);
	}

	/**
	 * 32
	 */
	@Override
	protected void inspectMethodInvocation(Flow loopControl, Context context, DataFlow dataFlow,
			MethodInvocation invocation) {
		inspectMethodInvocation(loopControl, context, dataFlow, (Expression) invocation);
	}

	/**
	 * 32
	 */
	protected void inspectMethodInvocation(Flow loopControl, Context context, DataFlow dataFlow,
			Expression methodInvocation) {
		// The steps are:
		// 01 - Break the chain (if any) method calls into multiple method invocations.
		// 02 - Check if the method is an Exit-Point (Only verifiers check that).
		// 03 - Check if the method is a Sanitization-Point.
		// 04 - Check if the method is an Entry-Point.
		// 05 - Check if we have the source code of this method.
		// 06 - Get the context based on one of the 08 cases.
		// 07 - Get the instance object (if any) of the method call, this is important
		// because this is the object that holds the context.

		// 01 - Break the chain (if any) method calls into multiple method invocations.
		List<Expression> methodsInChain = HelperCodeAnalyzer.getMethodsFromChainInvocation(methodInvocation);

		// getServletContext().getRequestDispatcher(login).forward(request, response);
		if (1 < methodsInChain.size()) {
			inspectNode(loopControl, context, dataFlow, methodsInChain.get(methodsInChain.size() - 2));
		}

		inspectEachMethodInvocationOfChainInvocations(loopControl, context, dataFlow.addNodeToPath(methodInvocation),
				methodInvocation);
	}

	/**
	 * 32
	 */
	protected void inspectEachMethodInvocationOfChainInvocations(Flow loopControl, Context context, DataFlow dataFlow,
			Expression methodInvocation) {
		// 01 - Get a new data flow or a child from the parent.
		DataFlow newDataFlow = HelperCodeAnalyzer.getDataFlowMethodInvocation(dataFlow, methodInvocation);

		// 02 - There are 2 cases: When we have the source code of this method and when we do not.
		inspectMethodInvocationWithOrWithOutSourceCode(loopControl, context, newDataFlow, methodInvocation);
	}

	/**
	 * 32
	 */
	protected void inspectMethodInvocationWithOrWithOutSourceCode(Flow loopControl, Context context, DataFlow dataFlow,
			Expression methodInvocation) {
		// 01 - There are 2 cases: When we have the source code of this method and when we do not.
		MethodDeclaration methodDeclaration = getMethodDeclaration(loopControl, context, methodInvocation);

		// 02 -
		inspectMethodInvocationWithOrWithOutSourceCode(loopControl, context, dataFlow, methodInvocation,
				methodDeclaration);

		// 03 -
		methodHasBeenProcessed(loopControl, context, dataFlow, methodInvocation, methodDeclaration);
	}

	protected void methodHasBeenProcessed(Flow loopControl, Context context, DataFlow dataFlow,
			Expression methodInvocation, MethodDeclaration methodDeclaration) {
		if (null == methodDeclaration) {
			// 01 - Get a variable binding if it is an object.
			VariableBinding variableBinding = HelperCodeAnalyzer.getVariableBindingIfItIsAnObject(getCallGraph(),
					context, methodInvocation);

			methodHasBeenProcessed(loopControl, context, dataFlow, methodInvocation, methodDeclaration, variableBinding);
		}
	}

	protected void methodHasBeenProcessed(Flow loopControl, Context context, DataFlow dataFlow,
			Expression methodInvocation, MethodDeclaration methodDeclaration, VariableBinding variableBinding) {
		// 01 - Only methods that we do not have the implementation should get here.
		if ((null == methodDeclaration) && (null != variableBinding)) {
			// 02 - Check if this method invocation is being call from a vulnerable object.
			processIfStatusUnknownOrUpdateIfVulnerable(loopControl, context, dataFlow, variableBinding);
		}
	}

	/**
	 * 32
	 */
	protected Context getContext(Flow loopControl, Context context, MethodDeclaration methodDeclaration,
			ASTNode methodInvocation) {
		return null;
	}

	/**
	 * 32 , 42
	 */
	protected void processIfStatusUnknownOrUpdateIfVulnerable(Flow loopControl, Context context, DataFlow dataFlow,
			VariableBinding variableBinding) {
		if (variableBinding.getStatus().equals(EnumVariableStatus.VULNERABLE)) {
			dataFlow.replace(variableBinding.getDataFlow());
		} else if (variableBinding.getStatus().equals(EnumVariableStatus.UNKNOWN)) {
			// 01 - This is the case where we have to go deeper into the variable's path.
			inspectNode(loopControl, context, dataFlow, variableBinding.getInitializer());

			// 02 - If there is a vulnerable path, then this variable is vulnerable.
			UpdateIfVulnerable(variableBinding, dataFlow);
		}
	}

	protected void UpdateIfVulnerable(VariableBinding variableBinding, DataFlow dataFlow) {
	}

	/**
	 * 40
	 */
	@Override
	protected void inspectQualifiedName(Flow loopControl, Context context, DataFlow dataFlow, QualifiedName expression) {
		super.inspectQualifiedName(loopControl, getContext(context, expression), dataFlow, expression);
	}

	/**
	 * 40
	 */
	protected Context getContext(Context context, Expression expression) {
		// 02 - Get the binding of this variable;
		IBinding binding = BindingResolver.resolveBinding(expression);
		if (null != binding) {

			// 03 - Get the instance (object or static);
			Expression instance = BindingResolver.getInstanceIfItIsAnObject(expression);

			if (Modifier.isStatic(binding.getModifiers())) {
				// Case: 01 - Person.staticPersonVariable
				// Case: 02 - staticPersonVariable

				// 04 - Get the resource of this static variable.
				IResource resource = HelperCodeAnalyzer.getClassResource(getCallGraph(), instance);

				if (null != resource) {
					// 05 - Get the context (top level) of this resource.
					context = getCallGraph().getStaticContext(resource);
				}
			} else if ((null != instance) && (!instance.equals(expression))) {
				// Case: 03 - person.publicPersonVariable
				context = getCallGraph().getInstanceContext(context, instance);
			}
		}
		return context;
	}

	/**
	 * 42
	 */
	@Override
	protected void inspectSimpleName(Flow loopControl, Context context, DataFlow dataFlow, SimpleName expression) {
		// 01 - Try to retrieve the variable from the list of variables.
		VariableBinding variableBinding = getCallGraph().getVariableBinding(context, expression);

		inspectSimpleName(loopControl, context, dataFlow, expression, variableBinding);
	}

	/**
	 * 42
	 */
	protected void inspectSimpleName(Flow loopControl, Context context, DataFlow dataFlow, SimpleName expression,
			VariableBinding variableBinding) {
		if (null != variableBinding) {
			processIfStatusUnknownOrUpdateIfVulnerable(loopControl, context, dataFlow.addNodeToPath(expression),
					variableBinding);
		}
	}

}