package net.thecodemaster.evd.test;

import java.util.List;

import net.thecodemaster.evd.Activator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.junit.AfterClass;
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

	@AfterClass
	public static void tearDown() throws Exception {
		if ((null != renamedResources) && (renamedResources.size() > 0)) {
			for (String resourceName : renamedResources) {
				try {
					// We have to delete the files.
					IFolder folderTest = getFolder(PROJECT_TEST);

					IFile javaSRC = folderTest.getFile(resourceName);

					javaSRC.delete(true, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected List<IResource> getResources() {
		return null;
	}

}
