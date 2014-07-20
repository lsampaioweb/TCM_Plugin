package net.thecodemaster.evd.graph;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import net.thecodemaster.evd.context.Context;
import net.thecodemaster.evd.finder.InstanceFinder;
import net.thecodemaster.evd.finder.ReferenceFinder;
import net.thecodemaster.evd.graph.flow.DataFlow;
import net.thecodemaster.evd.graph.flow.Flow;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.helper.HelperCodeAnalyzer;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.marker.MarkerManager;
import net.thecodemaster.evd.point.EntryPoint;
import net.thecodemaster.evd.point.SanitizationPoint;
import net.thecodemaster.evd.reporter.Reporter;
import net.thecodemaster.evd.ui.enumeration.EnumTypeDeclaration;
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
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * @author Luciano Sampaio
 */
public abstract class CodeAnalyzer extends CodeVisitor {

	/**
	 * This object contains all the methods, variables and their interactions, on the project that is being analyzed.
	 */
	private CallGraph												callGraph;
	/**
	 * The current resource that is being analyzed.
	 */
	private IResource												currentResource;
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
	 * The object that know how and where to report the found vulnerabilities.
	 */
	private Reporter												reporter;

	protected void setCallGraph(CallGraph callGraph) {
		this.callGraph = callGraph;
	}

	protected CallGraph getCallGraph() {
		return callGraph;
	}

	protected void setCurrentResource(IResource currentResource) {
		this.currentResource = currentResource;
	}

	protected IResource getCurrentResource() {
		return currentResource;
	}

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

	protected void run(Map<IResource, List<MethodDeclaration>> resourcesAndMethodsToProcess, CallGraph callGraph) {
		setCallGraph(callGraph);

		int numberOfResources = resourcesAndMethodsToProcess.size();
		int numberOfResourcesProcessed = 0;

		// 01 - Iterate over all the resources.
		for (Entry<IResource, List<MethodDeclaration>> entry : resourcesAndMethodsToProcess.entrySet()) {
			if (!userCanceledProcess(getProgressMonitor())) {
				// 02 - Set the current resource.
				setCurrentResource(entry.getKey());

				// 03 - Inform the user what is the current process of the plug-in.
				setSubTask(getSubTaskMessage(++numberOfResourcesProcessed, numberOfResources));

				// 04 - Get the list of methods that will be processed from this resource.
				List<MethodDeclaration> methodsToProcess = entry.getValue();

				// 05 - We need the compilation unit to check if there are markers in the current resource.
				setCurrentCompilationUnit(methodsToProcess);

				// 06 - Process the detection on these methods.
				run(methodsToProcess);
			} else {
				// 02 - The user has stopped the process.
				return;
			}
		}
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

	protected Map<IResource, List<MethodDeclaration>> getMethodsToProcess(List<IResource> resources, CallGraph callGraph) {
		Map<IResource, List<MethodDeclaration>> resourcesAndMethodsToProcess = Creator.newMap();

		ListIterator<IResource> iterator = resources.listIterator();

		while (iterator.hasNext()) {
			IResource resource = iterator.next();

			List<MethodDeclaration> methodsToProcess = Creator.newList();

			// 02 - Get the list of methods in the current resource and its invocations.
			Map<MethodDeclaration, List<ASTNode>> methods = callGraph.getMethods(resource);

			// 03 - Iterate over all the method declarations of the current resource.
			for (MethodDeclaration methodDeclaration : methods.keySet()) {

				// To avoid unnecessary processing, we only process methods that are
				// not invoked by any other method in the same file. Because if the method
				// is invoked, eventually it will be processed.
				// 04 - Get the list of methods that invokes this method.
				Map<MethodDeclaration, List<ASTNode>> invokers = callGraph.getInvokers(methodDeclaration);
				if (invokers.size() > 0) {
					// 05 - Iterate over all the methods that invokes this method.
					for (Entry<MethodDeclaration, List<ASTNode>> caller : invokers.entrySet()) {

						// 06 - Get the resource of this method (caller).
						IResource resourceCaller = BindingResolver.getResource(caller.getKey());

						// 07 - If this method is invoked by a method from another resource
						// and that resource is not in the list of resources that are going to be processed.
						if ((null != resourceCaller) && (!resourceCaller.equals(resource)) && (!resources.contains(resourceCaller))) {

							// 08 - Add the resource caller to the list of resources that should be processed.
							iterator.add(resourceCaller);
							// 09 - Make the iterator "see" this new element.
							iterator.previous();
							// 10 - Clear any old warnings.
							getReporter().clearOldProblems(resourceCaller);
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
		DataFlow newDataFlow = HelperCodeAnalyzer.getDataFlow(dataFlow, methodInvocation);

		// 02 - There are 2 cases: When we have the source code of this method and when we do not.
		inspectMethodInvocationWithOrWithOutSourceCode(loopControl, context, newDataFlow, methodInvocation);
	}

	/**
	 * 32
	 */
	protected void inspectMethodInvocationWithOrWithOutSourceCode(Flow loopControl, Context context, DataFlow dataFlow,
			Expression methodInvocation) {
		// Some method invocations can be in a chain call, we have to investigate them all.
		// response.sendRedirect(login);
		// getServletContext().getRequestDispatcher(login).forward(request, response);
		// 01 - There are 2 cases: When we have the source code of this method and when we do not.
		MethodDeclaration methodDeclaration = getMethodDeclaration(loopControl, context, methodInvocation);

		inspectMethodInvocationWithOrWithOutSourceCode(loopControl, context, dataFlow, methodInvocation, methodDeclaration);

		VariableBinding variableBinding = HelperCodeAnalyzer.getVariableBindingIfItIsAnObject(getCallGraph(), context,
				methodInvocation);

		methodHasBeenProcessed(loopControl, context, dataFlow, methodInvocation, methodDeclaration, variableBinding);
	}

	/**
	 * 32
	 */
	private void inspectMethodInvocationWithOrWithOutSourceCode(Flow loopControl, Context context, DataFlow dataFlow,
			ASTNode methodInvocation, MethodDeclaration methodDeclaration) {
		if (null != methodDeclaration) {
			// We have the source code.
			inspectMethodWithSourceCode(loopControl, context, dataFlow, methodInvocation, methodDeclaration);
		} else {
			inspectMethodWithOutSourceCode(loopControl, context, dataFlow, methodInvocation);
		}
	}

	protected void methodHasBeenProcessed(Flow loopControl, Context context, DataFlow dataFlow,
			Expression methodInvocation, MethodDeclaration methodDeclaration, VariableBinding variableBinding) {
		if (null == methodDeclaration) {
			// 01 - Check if this method invocation is being call from a vulnerable object.
			// Only methods that we do not have the implementation should get here.
			if (null != variableBinding) {
				processIfStatusUnknownOrUpdateIfVulnerable(loopControl, context, dataFlow, variableBinding);
			}
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
			UpdateIfVulnerable(loopControl, context, dataFlow, variableBinding);
		}
	}

	protected void UpdateIfVulnerable(Flow loopControl, Context context, DataFlow dataFlow,
			VariableBinding variableBinding) {
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
			} else if (null != instance) {
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
		DataFlow newDataFlow = dataFlow.addNodeToPath(expression);

		if (null != variableBinding) {
			processIfStatusUnknownOrUpdateIfVulnerable(loopControl, context, newDataFlow, variableBinding);
		} else {
			// If a method is scanned after a method invocation, all the parameters are provided, but
			// if a method is scanned from the initial block declarations loop, some parameter might not be known
			// so it is necessary to investigate WHO invoked this method and what were the provided parameters.
			inspectSimpleNameFromInvokers(loopControl, context, newDataFlow, expression, variableBinding);
		}
	}

	/**
	 * 42
	 */
	protected void inspectSimpleNameFromInvokers(Flow loopControl, Context context, DataFlow dataFlow,
			SimpleName expression, VariableBinding variableBinding) {
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
		// inspectNode(loopControl, context, dataFlow.addNodeToPath(parameter), parameter);
		// }
		// }
		// }
	}

	/**
	 * 14, 17, 32, 46, 48
	 */
	@Override
	protected void inspectInvocation(Flow loopControl, Context context, DataFlow dataFlow, ASTNode invocation) {
		MethodDeclaration methodDeclaration = getMethodDeclaration(loopControl, context, invocation);

		inspectMethodInvocationWithOrWithOutSourceCode(loopControl, context, dataFlow, invocation, methodDeclaration);
	}

	protected void inspectMethodWithSourceCode(Flow loopControl, Context context, DataFlow dataFlow,
			ASTNode methodInvocation, MethodDeclaration methodDeclaration) {
		inspectNode(loopControl, context, dataFlow, methodDeclaration.getBody());
	}

	protected void inspectMethodWithOutSourceCode(Flow loopControl, Context context, DataFlow dataFlow,
			ASTNode methodInvocation) {
		iterateOverParameters(loopControl, context, dataFlow, methodInvocation);
	}

	protected MethodDeclaration getMethodDeclaration(Flow loopControl, Context context, ASTNode invocation) {
		switch (invocation.getNodeType()) {
			case ASTNode.CLASS_INSTANCE_CREATION: // 14
				return getClassInstanceCreationDeclaration((ClassInstanceCreation) invocation, EnumTypeDeclaration.CONSTRUCTOR);

			case ASTNode.CONSTRUCTOR_INVOCATION: // 17
				return getConstructorInvocationDeclaration(invocation, EnumTypeDeclaration.CONSTRUCTOR);

			case ASTNode.METHOD_INVOCATION: // 32
				return getMethodInvocationDeclaration(loopControl, context, (MethodInvocation) invocation,
						EnumTypeDeclaration.METHOD);

			case ASTNode.SUPER_CONSTRUCTOR_INVOCATION: // 46
				return getSuperInvocationDeclaration(invocation, EnumTypeDeclaration.CONSTRUCTOR);

			case ASTNode.SUPER_METHOD_INVOCATION: // 48
				return getSuperInvocationDeclaration(invocation, EnumTypeDeclaration.METHOD);
			default:
				PluginLogger.logError("getMethodDeclaration Default Node Type: " + invocation.getNodeType() + " - "
						+ invocation, null);
				return null;
		}
	}

	/**
	 * 14
	 */
	private MethodDeclaration getClassInstanceCreationDeclaration(ClassInstanceCreation invocation,
			EnumTypeDeclaration typeDeclaration) {
		// 01 - new Class(...);
		// 01.1 - It can be an InnerClass or a normal class.
		// 01.2 - With or without constructor.
		// 01.3 - The name of the Class is the name of the file + ".java".
		// 01.4 - It can be in the same package or in another one.

		// TODO - It can be an InnerClass or a normal class.
		// Try to find if the current resource has any inner classes.

		// 01 - Get the name/type of the class.
		// 02 - Get the resource of the class.
		IResource resource = getResource(invocation.getType());

		// 03 - Search the source code of the constructor that was invoked.
		return getMethodDeclaration(resource, invocation, typeDeclaration);
	}

	/**
	 * 17
	 */
	private MethodDeclaration getConstructorInvocationDeclaration(ASTNode invocation, EnumTypeDeclaration typeDeclaration) {
		// 01 - this(...);
		// 01.1 - It has to be inside the current resource.

		// 01 - Get the resource of this constructor.
		IResource resource = getResource(invocation);

		// 02 - Search the source code of the constructor that was invoked.
		return getMethodDeclaration(resource, invocation, typeDeclaration);
	}

	/**
	 * 32
	 */
	private MethodDeclaration getMethodInvocationDeclaration(Flow loopControl, Context context,
			MethodInvocation invocation, EnumTypeDeclaration typeDeclaration) {
		// 01.1 - It can be inside the current resource.
		// 01.2 - It can be inside one of the super classes.
		// 01.3 - It can be in the same package or in another one.

		// 01 - Get the binding of this invocation;
		IBinding binding = BindingResolver.resolveBinding(invocation);
		if (null != binding) {
			// 02 - Get the name of the object (instance or static) that is invoking this method.
			Expression invokerName = BindingResolver.getNameIfItIsAnObject(invocation);

			if ((Modifier.isStatic(binding.getModifiers())) && (null != invokerName)) {
				// If this invocation is to a static method and it has an invoker.
				// 03.1 - Static.method(...);
				// 03.2 - package.name.Static.method(...);

				// 03 - Resource that has the source code of this invocation.
				IResource resource = getResource((Name) invokerName);

				// 04 - Search the source code of the method that was invoked.
				return getMethodDeclaration(resource, invocation, typeDeclaration);
			} else {
				IResource resource = null;
				Type className = null;
				if (null != invokerName) {
					// 03.1 - p.method(...); Person p = new Employee();
					// 03.2 - Interface.method(...); List/ArrayList/LinkedList.
					// These are the most complicated case.
					Expression realReference = findRealReference(loopControl, context, invokerName);

					if (null != realReference) {
						className = ((ClassInstanceCreation) realReference).getType();
						// 04 - Get the resource of this real reference.
						resource = getResource(className);

						typeDeclaration = EnumTypeDeclaration.METHOD_INHERITANCE;
					}
				} else {
					// 03.1 - method(...);
					// 03.2 - staticMethod(...);
					// 03.3 - this.method(...);
					// 04 - Get the resource of this method.
					resource = getResource(invocation);
				}
				return getMethodDeclarationFromCurrentClassOrSuperClasses(resource, invocation, typeDeclaration, className);
			}
		}

		return null;
	}

	/**
	 * 46, 48
	 */
	private MethodDeclaration getSuperInvocationDeclaration(ASTNode invocation, EnumTypeDeclaration typeDeclaration) {
		// 01 - super(...);
		// 01.1 - It has to be inside one of the super classes.
		// 02 - super.method(...);
		// 02.1 - It has to be inside one of the super classes.

		return getMethodDeclarationFromSuperClasses(invocation, typeDeclaration, null);
	}

	private IResource getResource(ASTNode invocation) {
		return BindingResolver.getResource(invocation);
	}

	private IResource getResource(Type className) {
		return BindingResolver.getResource(getCallGraph(), className);
	}

	private IResource getResource(Name name) {
		return BindingResolver.getResource(getCallGraph(), name);
	}

	private MethodDeclaration getMethodDeclarationFromCurrentClassOrSuperClasses(IResource resource, ASTNode invocation,
			EnumTypeDeclaration typeDeclaration, Type className) {
		// 02 - Try to find the source code into the current resource.
		MethodDeclaration methodDeclaration = getMethodDeclaration(resource, invocation, typeDeclaration);
		if (null != methodDeclaration) {
			return methodDeclaration;
		}

		// 03 - Try to find the source code into the super classes.
		return getMethodDeclarationFromSuperClasses(invocation, typeDeclaration, className);
	}

	private MethodDeclaration getMethodDeclarationFromSuperClasses(ASTNode invocation,
			EnumTypeDeclaration typeDeclaration, Type className) {
		List<IResource> resources = Creator.newList();

		if (null == className) {
			// 01 - Get the list of resources from the super class of this invocation.
			resources = getListOfResourcesFromSuperClasses(invocation);
		} else {
			resources = getListOfResourcesFromSuperClasses(className);
		}

		return getMethodDeclarationFromResources(invocation, typeDeclaration, resources);
	}

	private List<IResource> getListOfResourcesFromSuperClasses(ASTNode node) {
		// 01 - Get the type declaration based on the invocation node.
		TypeDeclaration typeDeclaration = BindingResolver.getTypeDeclaration(node);

		if (null != typeDeclaration) {
			// 02 - Get the name/type of the super class.
			Type superClassName = typeDeclaration.getSuperclassType();

			return getListOfResourcesFromSuperClasses(superClassName);
		}

		// 03 - If the type Declaration is null, we return an empty list.
		List<IResource> emptyList = Creator.newList();
		return emptyList;
	}

	private MethodDeclaration getMethodDeclarationFromResources(ASTNode invocation, EnumTypeDeclaration typeDeclaration,
			List<IResource> resources) {
		for (IResource resource : resources) {
			// 02 - Search the source code of the constructor that was invoked.
			MethodDeclaration methodDeclaration = getMethodDeclaration(resource, invocation, typeDeclaration);
			if (null != methodDeclaration) {
				return methodDeclaration;
			}
		}

		// The source code was not found.
		return null;

	}

	private List<IResource> getListOfResourcesFromSuperClasses(Type superClassName) {
		// 01 - Create the list that will contain the super classes.
		List<IResource> resources = Creator.newList();

		while (null != superClassName) {
			// 05 - Get the resource of the class.
			IResource resource = getResource(superClassName);

			// 06 - If the resource of the super class was not found,
			// e.g Library that we do not have the source code, there is nothing we can do.
			if (null == resource) {
				break;
			}

			// 07 - If the resource is not null and it is not already in the list. We add it to the list.
			if (!resources.contains(resource)) {
				resources.add(resource);
			}

			// 08 - Get the methods from this resource.
			superClassName = getCallGraph().getSuperClass(resource);
		}

		// 09 - Return the list with the resources of all the super classes.
		return resources;
	}

	private MethodDeclaration getMethodDeclaration(IResource resource, ASTNode invocation,
			EnumTypeDeclaration typeDeclaration) {
		if (null != resource) {
			// 01 - Get the list of methods in the current resource and its invocations.
			Map<MethodDeclaration, List<ASTNode>> methods = getCallGraph().getMethods(resource);

			if (EnumTypeDeclaration.CONSTRUCTOR.equals(typeDeclaration)) {
				// 02 - Iterate through the list to verify if we have the implementation of this method in our list.
				for (MethodDeclaration methodDeclaration : methods.keySet()) {
					// 03 - Verify if these methods have the same parameters.
					if ((methodDeclaration.isConstructor())
							&& (BindingResolver.haveSameParameters(methodDeclaration, invocation))) {
						return methodDeclaration;
					}
				}
			} else if (EnumTypeDeclaration.METHOD.equals(typeDeclaration)) {
				// 02 - Iterate through the list to verify if we have the implementation of this method in our list.
				for (MethodDeclaration methodDeclaration : methods.keySet()) {
					// 03 - Verify if these methods are the same.
					if (BindingResolver.areMethodsEqual(methodDeclaration, invocation)) {
						return methodDeclaration;
					}
				}
			} else if (EnumTypeDeclaration.METHOD_INHERITANCE.equals(typeDeclaration)) {
				// 02 - Iterate through the list to verify if we have the implementation of this method in our list.
				for (MethodDeclaration methodDeclaration : methods.keySet()) {
					// 03 - Verify if these methods have the same name and parameters.
					if (BindingResolver.haveSameNameAndParameters(methodDeclaration, invocation)) {
						return methodDeclaration;
					}
				}
			}
		}

		return null;
	}

	/**
	 * This first implementation will return the first reference that is found.<br/>
	 * FIXME - There are complex cases that more that one type of reference can be returned. <br/>
	 * Case 01: Animal a = new Animal(); <br/>
	 * Case 02: Animal a = new Person();<br/>
	 * Case 03: Animal a = new Employee();<br/>
	 * Case 04: Animal a = getObject(); <br/>
	 * <br/>
	 * getObject() { if (return new Employee()) else (return new Person()); }<br/>
	 * a.methodToInvoke(); <br/>
	 * What is the method to inspect ?
	 */
	private Expression findRealReference(Flow loopControl, Context context, Expression invokerName) {
		// 01 - Get the last reference of this object.
		VariableBinding variableBinding = getCallGraph().getVariableBinding(context, invokerName);

		if (null != variableBinding) {
			// 02 - Try to find where this variable was created.
			ReferenceFinder finder = new ReferenceFinder(getCallGraph(), getCurrentResource());

			// 03 - Return the real reference of this object.
			return finder.getReference(loopControl, context, variableBinding.getInitializer());
		}

		return null;
	}

	/**
	 * This first implementation will return the first reference that is found.<br/>
	 * FIXME - The instance must exist, if it does not, it is probably an assignment or syntax error. <br/>
	 * Case 01: <br/>
	 * Animal a1 = new Animal() <br/>
	 * Animal a2 = a1 <br/>
	 * a2.method(); <br/>
	 * <br/>
	 */
	protected Expression findRealInstance(Flow loopControl, Context context, Expression instance) {
		Expression instanceReturn = null;
		// 01 - Check if this instance has a context.
		Context instanceContext = getCallGraph().getInstanceContext(context, instance);

		// 02 - If the context is equal it means it does not exist.
		if (instanceContext.equals(context)) {
			// 03 - Get the last reference of this object.
			VariableBinding variableBinding = getCallGraph().getVariableBinding(context, instance);

			if (null != variableBinding) {
				// 03 - Try to find where this variable was created.
				InstanceFinder finder = new InstanceFinder(getCallGraph(), getCurrentResource());

				// 04 - Return the real reference of this object.
				instanceReturn = finder.getReference(loopControl, context, variableBinding.getInitializer());
			}
		}

		// 05 - Return the new or the same(old) instance.
		return (null != instanceReturn) ? instanceReturn : instance;
	}

}