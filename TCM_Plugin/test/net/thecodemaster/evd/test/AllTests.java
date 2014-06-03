package net.thecodemaster.evd.test;

import net.thecodemaster.evd.Activator;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CookiePoisoning.class, SQLInjection.class })
public class AllTests {

	@BeforeClass
	public static void startPlugin() {
		// This will make sure our plugin is running.
		Activator.getDefault().getStateLocation();
	}
}
