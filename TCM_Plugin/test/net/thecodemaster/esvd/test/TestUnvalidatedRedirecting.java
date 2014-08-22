package net.thecodemaster.esvd.test;

import java.util.List;
import java.util.Map;

import net.thecodemaster.esvd.graph.flow.DataFlow;
import net.thecodemaster.esvd.helper.Creator;

import org.eclipse.core.resources.IResource;
import org.junit.Assert;
import org.junit.Test;

public class TestUnvalidatedRedirecting extends AbstractTestVerifier {

	@Override
	protected List<IResource> getResources() {
		Map<String, List<String>> resourceNames = Creator.newMap();

		resourceNames.put(AbstractTestVerifier.PACKAGE_SERVLET, newList("UnvalidatedRedirecting.java"));

		return getResources(resourceNames);
	}

	@Test
	public void test() {
		Assert.assertEquals(1, allVulnerablePaths.size());

		List<DataFlow> vulnerablePaths = allVulnerablePaths.get(0);
		Assert.assertEquals(4, vulnerablePaths.size());
	}

}
