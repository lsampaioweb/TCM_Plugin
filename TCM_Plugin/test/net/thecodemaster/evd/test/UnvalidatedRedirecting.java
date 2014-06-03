package net.thecodemaster.evd.test;

import java.util.List;

import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.helper.Creator;

import org.eclipse.core.resources.IResource;
import org.junit.Assert;
import org.junit.Test;

public class UnvalidatedRedirecting extends AbstractTestVerifier {

	@Override
	protected List<IResource> getResources() {
		List<String> resourceNames = Creator.newList();

		resourceNames.add("UnvalidatedRedirecting.java");

		return getRersources(resourceNames);
	}

	@Test
	public void test() {
		Assert.assertEquals(1, allVulnerablePaths.size());

		List<DataFlow> vulnerablePaths = allVulnerablePaths.get(0);
		Assert.assertEquals(4, vulnerablePaths.size());
	}

}
