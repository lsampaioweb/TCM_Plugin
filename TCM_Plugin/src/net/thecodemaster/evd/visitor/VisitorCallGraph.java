package net.thecodemaster.evd.visitor;

import java.util.List;

import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.helper.HelperProjects;
import net.thecodemaster.evd.helper.Timer;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.reporter.Reporter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * This class creates a CompilationUnit from the current resource and uses the VISITOR pattern to create the call graph.
 * 
 * @Author: Luciano Sampaio
 * @Date: 2014-05-07
 * @Version: 01
 */
public class VisitorCallGraph implements IResourceVisitor, IResourceDeltaVisitor {

	/**
	 * The resource files that were updated since the last build.
	 */
	private final List<IResource>	resourcesUpdated;

	/**
	 * This object contains all the methods, variables and their interactions, on the project that is being analyzed. At
	 * any given time, we should only have on call graph of the code.
	 */
	private final CallGraph				callGraph;

	public VisitorCallGraph(CallGraph callGraph) {
		this.callGraph = callGraph;
		resourcesUpdated = Creator.newList();
	}

	/**
	 * Reads a ICompilationUnit and creates the AST DOM for manipulating the Java source file.
	 * 
	 * @param unit
	 * @return A compilation unit.
	 */
	private CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null); // Parse.
	}

	public List<IResource> run(IProject project) throws CoreException {
		project.accept(this);

		return resourcesUpdated;
	}

	public List<IResource> run(IResourceDelta delta) throws CoreException {
		delta.accept(this);

		return resourcesUpdated;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		IResource resource = delta.getResource();

		switch (delta.getKind()) {
			case IResourceDelta.REMOVED:
				// Delete old files, markers and etc related to this project.
				Reporter.getInstance().clearOldProblems(resource);
				break;
			case IResourceDelta.ADDED:
			case IResourceDelta.CHANGED:
				return visit(resource);
		}
		// Return true to continue visiting children.
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean visit(IResource resource) throws CoreException {
		if (HelperProjects.isToPerformDetection(resource)) {
			ICompilationUnit cu = JavaCore.createCompilationUnitFrom((IFile) resource);

			if (cu.isStructureKnown()) {
				// Creates the AST for the ICompilationUnits.
				Timer timer = (new Timer("01.1.1 - Parsing: " + resource.getName())).start();
				CompilationUnit cUnit = parse(cu);
				PluginLogger.logIfDebugging(timer.stop().toString());

				// Visit the compilation unit.
				timer = (new Timer("01.1.2 - Visiting: " + resource.getName())).start();

				// Remove old interactions of this resource.
				callGraph.remove(resource);

				// Add a new empty branch.
				callGraph.setCurrentResource(resource);
				cUnit.accept(new VisitorCompilationUnit(cUnit, callGraph));
				PluginLogger.logIfDebugging(timer.stop().toString());

				// Add this resource to the list of updated resources.
				resourcesUpdated.add(resource);
			}
		}
		// Return true to continue visiting children.
		return true;
	}

}
