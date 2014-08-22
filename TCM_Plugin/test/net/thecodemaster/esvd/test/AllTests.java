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
@SuiteClasses({ TestCommandInjection.class, TestCookiePoisoning.class, TestCrossSiteScripting.class,
		TestFalseNegativePositiveOfOthers.class, TestFromOtherClasses.class, TestInfinitiveLoop.class, TestInnerClass.class,
		TestLDAPInjection.class, TestLogForging.class, TestPathTraversal.class, TestReflectionInjection.class, TestRulesComposition.class,
		TestSanitizationPoints.class, TestSecurityMisconfiguration.class, TestSQLInjection.class, TestTypesOfCode.class,
		TestUnvalidatedRedirecting.class, TestVariableDeclaration.class, TestVulnerabilityPathReporter.class, TestXPathInjection.class })
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
