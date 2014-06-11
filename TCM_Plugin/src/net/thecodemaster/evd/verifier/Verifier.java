package net.thecodemaster.evd.verifier;

import java.util.List;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.graph.CodeAnalyzer;
import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.point.ExitPoint;
import net.thecodemaster.evd.reporter.Reporter;
import net.thecodemaster.evd.xmlloader.LoaderExitPoint;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
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
	 * List with all the vulnerable paths found by this verifier.
	 */
	private List<DataFlow>	allVulnerablePaths;

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

	protected void setReporter(Reporter reporter) {
		this.reporter = reporter;
	}

	protected Reporter getReporter() {
		return reporter;
	}

	@Override
	protected IProgressMonitor getProgressMonitor() {
		if ((null != getReporter()) && (null != getReporter().getProgressMonitor())) {
			return getReporter().getProgressMonitor();
		}
		return null;
	}

	@Override
	protected String getSubTaskMessage() {
		return String.format("%s: %s", getName(), getCurrentResource().getName());
	}

	protected List<ExitPoint> getExitPoints() {
		if (null == exitPoints) {
			// Loads all the ExitPoints of this verifier.
			exitPoints = (new LoaderExitPoint(getId())).load();
		}

		return exitPoints;
	}

	/**
	 * The public run method that will be invoked by the Analyzer.
	 * 
	 * @param resources
	 * @param callGraph
	 * @param reporter
	 * @return
	 */
	public List<DataFlow> run(Reporter reporter, CallGraph callGraph, List<IResource> resources) {
		setReporter(reporter);
		allVulnerablePaths = Creator.newList();

		super.run(getProgressMonitor(), callGraph, resources);

		return allVulnerablePaths;
	}

	/**
	 * Run the vulnerability detection on the current method declaration.
	 * 
	 * @param methodDeclaration
	 */
	@Override
	protected void run(int depth, MethodDeclaration methodDeclaration, Expression invoker) {
		PluginLogger.logIfDebugging("Method:" + methodDeclaration.getName());

		// 02 - TODO -
		// If there is a invoker we have to add the parameters and do more stuff.

		// 03 - Start the detection on each and every line of this method.
		// inspectNode(depth, context, new DataFlow(methodDeclaration.getName()), methodDeclaration.getBody());
	}

	/**
	 * An exit point might have more that one parameter and each of these parameter might have different rules (acceptable
	 * values). That is why we need to check.
	 * 
	 * @param rules
	 * @param parameter
	 * @return
	 */
	protected boolean matchRules(List<Integer> rules, Expression parameter) {
		if (null == parameter) {
			// There is nothing we can do to verify it.
			return true;
		}

		// -1 Anything is valid.
		// 0 Only sanitized values are valid.
		// 1 LITERAL and sanitized values are valid.
		for (Integer astNodeValue : rules) {
			if (astNodeValue == Constant.LITERAL) {
				switch (parameter.getNodeType()) {
					case ASTNode.STRING_LITERAL:
					case ASTNode.CHARACTER_LITERAL:
					case ASTNode.NUMBER_LITERAL:
					case ASTNode.NULL_LITERAL:
						return true;
				}
			} else if (astNodeValue == parameter.getNodeType()) {
				return true;
			}
		}

		return false;
	}

	protected void reportVulnerability(DataFlow dataFlow) {
		if (null != getReporter()) {
			getReporter().addProblem(getCurrentResource(), getId(), dataFlow);
		}
	}

}
