package net.thecodemaster.esvd.reporter;

import java.util.List;

import net.thecodemaster.esvd.graph.flow.DataFlow;

import org.eclipse.core.resources.IResource;

public interface IReporter {

	int	SECURITY_VIEW		= 1;
	int	TEXT_FILE_VIEW	= 2;
	int	XML_FILE_VIEW		= 3;

	/**
	 * @return One of the type of reports we have. <br/ >
	 *         SECURITY_VIEW, TEXT_FILE_VIEW, XML_FILE_VIEW
	 */
	int getType();

	/**
	 * Delete the old problems of the provided list.
	 * 
	 * @param resources
	 *          The list of resources that will have all of its old problems deleted.
	 */
	void clearOldProblems(List<IResource> resources);

	/**
	 * Delete the old problems of the provided resource.
	 * 
	 * @param resource
	 *          The resources that will have all of its old problems deleted.
	 */
	void clearOldProblems(IResource resource);

	/**
	 * Add the problem to one or more of the options selected by the user.
	 * 
	 * @param resource
	 *          The resource where the vulnerability was found.
	 * @param allVulnerablePaths
	 *          The data flow of the vulnerability, from where it started to where it finished. {@link DataFlow}.
	 */
	void addProblem(IResource resource, List<DataFlow> allVulnerablePaths);

}
