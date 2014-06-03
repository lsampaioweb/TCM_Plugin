package net.thecodemaster.evd.test;

import java.util.List;

import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.verifier.Verifier;
import net.thecodemaster.evd.verifier.VerifierCommandInjection;
import net.thecodemaster.evd.verifier.VerifierCookiePoisoning;
import net.thecodemaster.evd.verifier.VerifierCrossSiteScripting;
import net.thecodemaster.evd.verifier.VerifierPathTraversal;
import net.thecodemaster.evd.verifier.VerifierSQLInjection;
import net.thecodemaster.evd.verifier.VerifierSecurityMisconfiguration;
import net.thecodemaster.evd.verifier.VerifierUnvalidatedRedirecting;
import net.thecodemaster.evd.visitor.VisitorCallGraph;
import net.thecodemaster.evd.visitor.VisitorPointsToAnalysis;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.junit.Before;

public abstract class TestVerifier {

	private static final String	PROJECT	= "WebDemo";
	private static final String	PACKAGE	= "src/servlet";
	private CallGraph						callGraph;
	private VisitorCallGraph		visitorCallGraph;
	private List<IResource>			resources;
	private List<Verifier>			verifiers;

	@Before
	public void setUp() {
		try {
			// 01 - Create the callGraph object.
			callGraph = new CallGraph();

			// 02 - The visitor that will populate the callGraph.
			visitorCallGraph = new VisitorCallGraph(callGraph);

			// 03 - The files that will be processed.
			resources = getResources();
			for (IResource resource : resources) {
				visitorCallGraph.visit(resource);

				// 04 - The class that will set the status(VULNERABLE, NOT_VULNERABLE) of all the variables.
				VisitorPointsToAnalysis pointToAnalysis = new VisitorPointsToAnalysis();
				pointToAnalysis.run(resources, callGraph);

				// 05 - Get the list of verifiers that will be executed.
				createListVerifiers();

				// 06 - Run the verifications.
				for (Verifier verifier : getVerifiers()) {
					verifier.run(resources, callGraph, null);
				}

				// Each JUnit perform its own tests now.
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected abstract List<IResource> getResources();

	protected List<IResource> getRersources(List<String> resourceNames) {
		IFolder folder = getFolder();

		List<IResource> resources = Creator.newList();
		for (String resourceName : resourceNames) {
			resources.add(getResource(folder, resourceName));
		}

		return resources;
	}

	private IFolder getFolder() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();

		return root.getProject(PROJECT).getFolder(PACKAGE);
	}

	private IResource getResource(IFolder folder, String resourceName) {
		IFile javaSRC = folder.getFile(resourceName);

		ICompilationUnit cu = JavaCore.createCompilationUnitFrom(javaSRC);

		return cu.getResource();
	}

	private void createListVerifiers() {
		verifiers = Creator.newList();

		getVerifiers().add(new VerifierCommandInjection());
		getVerifiers().add(new VerifierCookiePoisoning());
		getVerifiers().add(new VerifierCrossSiteScripting());
		getVerifiers().add(new VerifierPathTraversal());
		getVerifiers().add(new VerifierSecurityMisconfiguration());
		getVerifiers().add(new VerifierSQLInjection());
		getVerifiers().add(new VerifierUnvalidatedRedirecting());
	}

	private List<Verifier> getVerifiers() {
		return verifiers;
	}

}