package net.thecodemaster.evd.verifier;

import java.util.List;
import java.util.Map;

import net.thecodemaster.evd.graph.BindingResolver;
import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.graph.Parameter;
import net.thecodemaster.evd.point.ExitPoint;
import net.thecodemaster.evd.reporter.Reporter;
import net.thecodemaster.evd.xmlloader.LoaderExitPoint;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * The verifier is the class that actually knows how to find the vulnerability and the one that performs this
 * verification. Each verifier can reimplement/override methods of add new behavior to them.
 * 
 * @author Luciano Sampaio
 */
public abstract class Verifier extends CodeAnalyzer {

	/**
	 * The id of the current verifier.
	 */
	private final int				id;
	/**
	 * The name of the current verifier.
	 */
	private final String		name;
	/**
	 * The object that know how and where to report the found vulnerabilities.
	 */
	private Reporter				reporter;
	/**
	 * List with all the ExitPoints of this verifier.
	 */
	private List<ExitPoint>	exitPoints;

	/**
	 * @param name
	 *          The name of the verifier.
	 * @param id
	 *          The id of the verifier.
	 * @param listEntryPoints
	 *          List with all the EntryPoints methods.
	 */
	public Verifier(int id, String name) {
		this.id = id;
		this.name = name;
	}

	protected int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	protected Reporter getReporter() {
		return reporter;
	}

	protected List<ExitPoint> getExitPoints() {
		if (null == exitPoints) {
			// Loads all the ExitPoints of this verifier.
			exitPoints = (new LoaderExitPoint(getId())).load();
		}

		return exitPoints;
	}

	/**
	 * Notifies that a subtask of the main task is beginning.
	 * 
	 * @param taskName
	 *          The text that will be displayed to the user.
	 */
	protected void setSubTask(String taskName) {
		if ((null != getReporter()) && (null != getReporter().getProgressMonitor())) {
			getReporter().getProgressMonitor().subTask(taskName);
		}
	}

	protected void reportVulnerability(DataFlow dataFlow) {
		getReporter().addProblem(getId(), getCurrentResource(), dataFlow);
	}

	protected ExitPoint getExitPointIfMethodIsOne(Expression method) {
		for (ExitPoint currentExitPoint : getExitPoints()) {
			if (BindingResolver.methodsHaveSameNameAndPackage(currentExitPoint, method)) {
				// 01 - Get the expected arguments of this method.
				Map<Parameter, List<Integer>> expectedParameters = currentExitPoint.getParameters();

				// 02 - Get the received parameters of the current method.
				List<Expression> receivedParameters = BindingResolver.getParameters(method);

				// 03 - It is necessary to check the number of parameters and its types
				// because it may exist methods with the same names but different parameters.
				if (expectedParameters.size() == receivedParameters.size()) {
					boolean isMethodAnExitPoint = true;
					int index = 0;
					for (Parameter expectedParameter : expectedParameters.keySet()) {
						ITypeBinding typeBinding = receivedParameters.get(index++).resolveTypeBinding();

						// Verify if all the parameters are the ones expected. However, there is a case
						// where an Object is expected, and any type is accepted.
						if (!BindingResolver.parametersHaveSameType(expectedParameter.getType(), typeBinding)) {
							isMethodAnExitPoint = false;
							break;
						}
					}

					if (isMethodAnExitPoint) {
						return currentExitPoint;
					}
				}
			}
		}

		return null;
	}

	/**
	 * The public run method that will be invoked by the Analyzer.
	 * 
	 * @param resources
	 * @param callGraph
	 * @param reporter
	 */
	public void run(List<IResource> resources, CallGraph callGraph, Reporter reporter) {
		setCallGraph(callGraph);
		this.reporter = reporter;

		setSubTask(getName());

		// 01 - Run the vulnerability detection on all the provided resources.
		for (IResource resource : resources) {
			// We need this information when we are going to display the vulnerabilities.
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

}
