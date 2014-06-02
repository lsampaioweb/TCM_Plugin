package net.thecodemaster.evd.test;

import static org.junit.Assert.fail;

import java.util.List;

import net.thecodemaster.evd.Activator;
import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.visitor.VisitorCallGraph;
import net.thecodemaster.evd.visitor.VisitorPointsToAnalysis;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CookiePoisoning {

	private CallGraph					callGraph;
	private VisitorCallGraph	visitorCallGraph;
	private List<IResource>		resourcesUpdated;
	private IProgressMonitor	monitor;

	@Before
	public void setUp() throws Exception {
		try {
			// 01 -
			callGraph = new CallGraph();

			// 02 -
			visitorCallGraph = new VisitorCallGraph(callGraph);

			// 03 -
			resourcesUpdated = Creator.newList();
			IResource resource = getResource();
			resourcesUpdated.add(resource);
			visitorCallGraph.visit(resource);

			// 04 -
			VisitorPointsToAnalysis pointToAnalysis = new VisitorPointsToAnalysis();
			pointToAnalysis.run(resourcesUpdated, callGraph);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private IResource getResource() {
		Activator.getDefault().getStateLocation();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IFolder folder = root.getProject("WebDemo").getFolder("src/servlet");
		IFile javaSRC = folder.getFile("CookiePoisoning.java");

		ICompilationUnit cu = JavaCore.createCompilationUnitFrom(javaSRC);

		return cu.getResource();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
