package net.thecodemaster.esvd.test;

import java.util.List;

import net.thecodemaster.esvd.Activator;

import org.eclipse.core.resources.IResource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CommandInjection.class, CookiePoisoning.class, CrossSiteScripting.class,
		FalseNegativePositiveOfOthers.class, FromOtherClasses.class, InfinitiveLoop.class, InnerClass.class,
		LDAPInjection.class, LogForging.class, PathTraversal.class, ReflectionInjection.class,
		SanitizationPoints.class, SecurityMisconfiguration.class, SQLInjection.class, TypesOfCode.class,
		UnvalidatedRedirecting.class, VariableDeclaration.class, VulnerabilityPathReporter.class, XPathInjection.class })
public class AllTests extends AbstractTestVerifier {

	@BeforeClass
	public static void startPlugin() {
		// This will make sure our plug-in is running.
		Activator.getDefault().getStateLocation();
	}

	@Override
	protected List<IResource> getResources() {
		return null;
	}

	@AfterClass
	public static void cleanUp() {
		System.out.println("Finished.");
	}

}
