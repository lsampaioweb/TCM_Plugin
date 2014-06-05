package net.thecodemaster.evd.test;

import java.util.List;

import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.helper.Creator;

import org.eclipse.core.resources.IResource;
import org.junit.Assert;
import org.junit.Test;

public class SQLInjection extends AbstractTestVerifier {

	@Override
	protected List<IResource> getResources() {
		List<String> resourceNames = Creator.newList();

		resourceNames.add("SQLInjection.java");

		return getRersources(resourceNames);
	}

	@Test
	public void test() {
		Assert.assertEquals(2, allVulnerablePaths.size());

		List<DataFlow> vulnerablePaths01 = allVulnerablePaths.get(0);
		Assert.assertEquals(1, vulnerablePaths01.size());

		List<DataFlow> vulnerablePaths02 = allVulnerablePaths.get(1);
		Assert.assertEquals(19, vulnerablePaths02.size());
	}

}
