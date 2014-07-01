package net.thecodemaster.evd.test;

import java.util.List;

import net.thecodemaster.evd.Activator;

import org.eclipse.core.resources.IResource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CookiePoisoning.class, CrossSiteScripting.class, FromOtherClasses.class,
		SecurityMisconfiguration.class, SQLInjection.class, UnvalidatedRedirecting.class, VariableDeclaration.class,
		VulnerabilityPathReporter.class })
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
