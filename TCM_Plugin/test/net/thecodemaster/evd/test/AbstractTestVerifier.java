package net.thecodemaster.evd.test;

import java.util.List;

import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.graph.DataFlow;
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.junit.Before;

public abstract class AbstractTestVerifier {

	protected List<List<DataFlow>>	allVulnerablePaths;

	private static final String			PROJECT				= "WebDemo";
	private static final String			PACKAGE				= "src/servlet";
	protected static final String		PROJECT_TEST	= "WebDemoTest";

	protected static IFolder getFolder(String projectName) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();

		return root.getProject(projectName).getFolder(PACKAGE);
	}

	private IResource getResource(IFolder folder, String resourceName) {
		IFile javaSRC = folder.getFile(resourceName);
		if (!javaSRC.exists()) {
			javaSRC = renameFile(folder, resourceName);
		}

		return JavaCore.createCompilationUnitFrom(javaSRC).getResource();
	}

	private IFile renameFile(IFolder folder, String resourceName) {
		IFile javaSRC = null;
		try {
			javaSRC = folder.getFile(resourceName + "2");
			if (javaSRC.exists()) {
				IFolder folderTest = getFolder(PROJECT_TEST);

				String newPath = String.format("%s/%s", folderTest.getFullPath(), resourceName);
				deleteIfExists(folderTest, resourceName);
				javaSRC.copy(new Path(newPath), true, null);

				javaSRC = folderTest.getFile(resourceName);
			}
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}

		return javaSRC;
	}

	public static void deleteIfExists(IFolder folderTest, String resourceName) {
		try {
			IFile javaSRC = folderTest.getFile(resourceName);
			if (javaSRC.exists()) {
				javaSRC.delete(true, null);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	protected abstract List<IResource> getResources();

	protected List<Verifier> createListVerifiers() {
		List<Verifier> verifiers = Creator.newList();

		verifiers.add(new VerifierCommandInjection());
		verifiers.add(new VerifierCookiePoisoning());
		verifiers.add(new VerifierCrossSiteScripting());
		verifiers.add(new VerifierPathTraversal());
		verifiers.add(new VerifierSecurityMisconfiguration());
		verifiers.add(new VerifierSQLInjection());
		verifiers.add(new VerifierUnvalidatedRedirecting());

		return verifiers;
	}

	protected List<IResource> getRersources(List<String> resourceNames) {
		IFolder folder = getFolder(PROJECT);

		List<IResource> resources = Creator.newList();
		for (String resourceName : resourceNames) {
			resources.add(getResource(folder, resourceName));
		}

		return resources;
	}

	@Before
	public void setUp() {
		try {
			// 01 - Create the callGraph object.
			CallGraph callGraph = new CallGraph();

			// 02 - The visitor that will populate the callGraph.
			VisitorCallGraph visitorCallGraph = new VisitorCallGraph(callGraph);

			// 03 - The files that will be processed.
			List<IResource> resources = getResources();
			for (IResource resource : resources) {
				visitorCallGraph.visit(resource);

				// 04 - The class that will set the status(VULNERABLE, NOT_VULNERABLE) of all the variables.
				VisitorPointsToAnalysis pointToAnalysis = new VisitorPointsToAnalysis();
				pointToAnalysis.run(resources, callGraph);

				// 05 - Get the list of verifiers that will be executed.
				List<Verifier> verifiers = createListVerifiers();

				allVulnerablePaths = Creator.newList();
				// 06 - Run the verifications.
				for (Verifier verifier : verifiers) {
					List<DataFlow> currentList = verifier.run(resources, callGraph, null);

					if (currentList.size() > 0) {
						allVulnerablePaths.add(currentList);
					}
				}

				// 07 - Each JUnit perform its own tests now.
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}