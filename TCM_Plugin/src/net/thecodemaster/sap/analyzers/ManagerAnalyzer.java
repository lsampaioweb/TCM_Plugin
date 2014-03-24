package net.thecodemaster.sap.analyzers;

import java.util.Collection;

import net.thecodemaster.sap.utils.Creator;
import net.thecodemaster.sap.utils.UtilProjects;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * @author Luciano Sampaio
 */
public class ManagerAnalyzer implements IResourceVisitor, IResourceDeltaVisitor {

  private Collection<Analyzer>      analyzers;
  private IProgressMonitor          monitor;
  private static Collection<String> resourceTypes;

  /**
   * @param monitor
   */
  public void addMonitor(IProgressMonitor monitor) {
    this.monitor = monitor;
  }

  /**
   * @param analyzer
   */
  public void addAnalyzer(Analyzer analyzer) {
    if (null == analyzers) {
      analyzers = Creator.newCollection();
    }

    analyzers.add(analyzer);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean visit(IResource resource) throws CoreException {
    if (isToPerformDetection(resource)) {
      ICompilationUnit cu = JavaCore.createCompilationUnitFrom((IFile) resource);

      if (cu.isStructureKnown()) {
        // Creates the AST for the ICompilationUnits.
        CompilationUnit compilationUnit = parse(cu);

        for (Analyzer analyzer : analyzers) {
          compilationUnit.accept(analyzer);
        }
      }
    }
    // Return true to continue visiting children.
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean visit(IResourceDelta delta) throws CoreException {
    IResource resource = delta.getResource();

    if (isToPerformDetection(resource)) {
      switch (delta.getKind()) {
        case IResourceDelta.REMOVED:
          // TODO
          break;
        case IResourceDelta.ADDED:
        case IResourceDelta.CHANGED:
          visit(resource);
          break;
      }
    }
    // Return true to continue visiting children.
    return true;
  }

  /**
   * Check if the detection should be perform in this resource or not.
   * 
   * @param resource The resource that will be tested.
   * @return True if the detection should be perform in this resource, otherwise false.
   */
  private boolean isToPerformDetection(IResource resource) {
    if (resource instanceof IFile) {
      if (null == resourceTypes) {
        resourceTypes = UtilProjects.getResourceTypesToPerformDetection();
      }

      for (String resourceType : resourceTypes) {
        if (resource.getFileExtension().equalsIgnoreCase(resourceType)) {
          return true;
        }
      }
    }
    // If it reaches this point, it means that the detection should not be performed in this resource.
    return false;
  }

  /**
   * Reads a ICompilationUnit and creates the AST DOM for manipulating the Java source file.
   * 
   * @param unit
   * @return A compilation unit.
   */
  private static CompilationUnit parse(ICompilationUnit unit) {
    ASTParser parser = ASTParser.newParser(AST.JLS4);
    parser.setKind(ASTParser.K_COMPILATION_UNIT);
    parser.setSource(unit);
    parser.setResolveBindings(true);
    return (CompilationUnit) parser.createAST(null); // parse
  }
}
