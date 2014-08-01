package net.thecodemaster.esvd.test;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.thecodemaster.esvd.graph.CallGraph;
import net.thecodemaster.esvd.graph.flow.DataFlow;
import net.thecodemaster.esvd.helper.Creator;
import net.thecodemaster.esvd.verifier.Verifier;
import net.thecodemaster.esvd.verifier.security.VerifierCommandInjection;
import net.thecodemaster.esvd.verifier.security.VerifierCookiePoisoning;
import net.thecodemaster.esvd.verifier.security.VerifierCrossSiteScripting;
import net.thecodemaster.esvd.verifier.security.VerifierHTTPResponseSplitting;
import net.thecodemaster.esvd.verifier.security.VerifierLDAPInjection;
import net.thecodemaster.esvd.verifier.security.VerifierLogForging;
import net.thecodemaster.esvd.verifier.security.VerifierPathTraversal;
import net.thecodemaster.esvd.verifier.security.VerifierReflectionInjection;
import net.thecodemaster.esvd.verifier.security.VerifierSQLInjection;
import net.thecodemaster.esvd.verifier.security.VerifierSecurityMisconfiguration;
import net.thecodemaster.esvd.verifier.security.VerifierXPathInjection;
import net.thecodemaster.esvd.visitor.VisitorCallGraph;
import net.thecodemaster.esvd.visitor.VisitorPointsToAnalysis;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
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

	protected static final String	PROJECT				= "WebDemo";
	protected static final String	PROJECT_TEST		= "01 - WebDemoTest";

	protected static final String	PACKAGE_BASE		= "src/base";
	protected static final String	PACKAGE_OTHER_PACK	= "src/other/pack";
	protected static final String	PACKAGE_SERVLET		= "src/servlet";

	@Before
	public void setUp() {
		try {
			allVulnerablePaths = Creator.newList();

			// 01 - Create the callGraph object.
			CallGraph callGraph = new CallGraph();

			// 02 - The visitor that will populate the callGraph.
			VisitorCallGraph visitorCallGraph = new VisitorCallGraph(callGraph, null);

			// 03 - The files that will be processed.
			List<IResource> resources = getResources();
			for (IResource resource : resources) {
				visitorCallGraph.visit(resource);

				// 04 - Get the list of verifiers that will be executed.
				List<Verifier> verifiers = createListVerifiers();

				if (verifiers.size() > 0) {
					// 05 - The class that will set the status(VULNERABLE, NOT_VULNERABLE) of all the variables.
					VisitorPointsToAnalysis pointToAnalysis = new VisitorPointsToAnalysis();

					List<DataFlow> currentList = pointToAnalysis.run(resources, callGraph, verifiers, null);

					if (currentList.size() > 0) {
						allVulnerablePaths.add(currentList);
					}
				}

				// 06 - Each JUnit perform its own tests now.
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected abstract List<IResource> getResources();

	protected List<IResource> getResources(Map<String, List<String>> resourcesPackagesAndNames) {
		List<IResource> resources = Creator.newList();

		// 01 - Get the main project.
		IProject project = getProject(PROJECT);

		// <String, List<String> // <<Package> , <List of resources>>
		for (Entry<String, List<String>> entry : resourcesPackagesAndNames.entrySet()) {
			for (String resourceName : entry.getValue()) {
				// 02 - Get the folder of this resource.
				IFolder folder = project.getFolder(entry.getKey());

				// 03 - Get the resource and add it to the list.
				resources.add(getResource(entry.getKey(), folder, resourceName));
			}

		}

		return resources;
	}

	private IResource getResource(String folderName, IFolder folder, String resourceName) {
		IFile javaSRC = folder.getFile(resourceName);
		if (!javaSRC.exists()) {
			javaSRC = renameFile(folderName, folder, resourceName);
		}

		return JavaCore.createCompilationUnitFrom(javaSRC).getResource();
	}

	protected List<String> newList(String... fileNames) {
		List<String> newList = Creator.newList();

		for (String fileName : fileNames) {
			newList.add(fileName);
		}

		return newList;
	}

	protected static IProject getProject(String projectName) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();

		return root.getProject(projectName);
	}

	private IFile renameFile(String folderName, IFolder folder, String resourceName) {
		IFile javaSRC = null;
		try {
			javaSRC = folder.getFile(resourceName + "2");
			if (javaSRC.exists()) {
				IProject projectTest = getProject(PROJECT_TEST);

				// 02 - Get the folder of this resource.
				IFolder folderTest = projectTest.getFolder(folderName);

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

	protected List<Verifier> createListVerifiers() {
		List<Verifier> verifiers = Creator.newList();

		verifiers.add(new VerifierCommandInjection());
		verifiers.add(new VerifierCookiePoisoning());
		verifiers.add(new VerifierCrossSiteScripting());
		verifiers.add(new VerifierHTTPResponseSplitting());
		verifiers.add(new VerifierLDAPInjection());
		verifiers.add(new VerifierLogForging());
		verifiers.add(new VerifierPathTraversal());
		verifiers.add(new VerifierReflectionInjection());
		verifiers.add(new VerifierSecurityMisconfiguration());
		verifiers.add(new VerifierSQLInjection());
		verifiers.add(new VerifierXPathInjection());

		return verifiers;
	}

}