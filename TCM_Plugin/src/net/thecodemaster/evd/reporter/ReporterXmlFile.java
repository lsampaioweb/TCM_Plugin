package net.thecodemaster.evd.reporter;

import java.util.List;

import net.thecodemaster.evd.graph.flow.DataFlow;

import org.eclipse.core.resources.IResource;

/**
 * This class knows where and how to report the vulnerabilities into XML files.
 * 
 * @author Luciano Sampaio
 */
public class ReporterXmlFile implements IReporter {

	@Override
	public int getType() {
		return IReporter.XML_FILE_VIEW;
	}

	@Override
	public void clearOldProblems(List<IResource> resources) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearOldProblems(IResource resource) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addProblem(IResource resource, List<DataFlow> allVulnerablePaths) {
		// TODO Auto-generated method stub

	}

}
