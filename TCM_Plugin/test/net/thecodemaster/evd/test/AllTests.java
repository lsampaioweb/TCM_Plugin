package net.thecodemaster.evd.test;

import java.util.List;

import net.thecodemaster.evd.Activator;

import org.eclipse.core.resources.IResource;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CookiePoisoning.class, CrossSiteScripting.class, SecurityMisconfiguration.class, SQLInjection.class,
		UnvalidatedRedirecting.class, VariableDeclaration.class, VulnerabilityPathReporter.class })
public class AllTests extends AbstractTestVerifier {

	@BeforeClass
	public static void startPlugin() {
		// This will make sure our plug-in is running.
		Activator.getDefault().getStateLocation();
	}

	// public static Test suite() {
	// TestSuite suite = new TestSuite("jUnit for the TCM_EVD Plug-in");
	// // $JUnit-BEGIN$
	// suite.addTestSuite(CookiePoisoning.class);
	// suite.addTestSuite(CrossSiteScripting.class);
	// suite.addTestSuite(SecurityMisconfiguration.class);
	// suite.addTestSuite(SQLInjection.class);
	// suite.addTestSuite(UnvalidatedRedirecting.class);
	// suite.addTestSuite(VariableDeclaration.class);
	// suite.addTestSuite(VulnerabilityPathReporter.class);
	// // $JUnit-END$
	// return suite;
	// }

	@Override
	protected List<IResource> getResources() {
		return null;
	}

}
