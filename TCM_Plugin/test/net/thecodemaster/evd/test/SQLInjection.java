package net.thecodemaster.evd.test;

import static org.junit.Assert.fail;

import java.util.List;

import net.thecodemaster.evd.helper.Creator;

import org.eclipse.core.resources.IResource;
import org.junit.Test;

public class SQLInjection extends TestVerifier {

	@Override
	protected List<IResource> getResources() {
		List<String> resourceNames = Creator.newList();

		resourceNames.add("SQLInjection.java");

		return getRersources(resourceNames);
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
