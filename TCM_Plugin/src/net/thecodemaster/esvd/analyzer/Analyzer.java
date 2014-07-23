package net.thecodemaster.esvd.analyzer;

import java.util.List;

import net.thecodemaster.esvd.graph.CallGraph;
import net.thecodemaster.esvd.helper.Creator;
import net.thecodemaster.esvd.reporter.Reporter;
import net.thecodemaster.esvd.verifier.Verifier;
import net.thecodemaster.esvd.visitor.VisitorPointsToAnalysis;

import org.eclipse.core.resources.IResource;

/**
 * The Analyzer is like a category of one or several verifiers. On this first version we only have a
 * SecurityVulnerability Analyzer but in the future we might add others.
 * 
 * @author Luciano Sampaio
 */
public abstract class Analyzer {

	/**
	 * The list of all verifiers of this analyzer.
	 */
	private final List<Verifier>	verifiers;

	public Analyzer() {
		verifiers = Creator.newList();
	}

	protected List<Verifier> getVerifiers() {
		return verifiers;
	}

	/**
	 * This method will iterate over all the Verifiers of this Analyzer and start invoking each of them.
	 * 
	 * @param reporter
	 *          The object that know where and how to displayed the found vulnerabilities. {@link Reporter}
	 * @param callGraph
	 *          The object that contains the callGraph of all methods and variables of the analyzed source code.
	 *          {@link CallGraph}
	 * @param resources
	 *          The list of modified resources that needs to be verified.
	 */
	public void run(List<IResource> resources, CallGraph callGraph, Reporter reporter) {
		if (getVerifiers().size() > 0) {

			VisitorPointsToAnalysis pointToAnalysis = new VisitorPointsToAnalysis();

			pointToAnalysis.run(resources, callGraph, getVerifiers(), reporter);
		}
	}

}
