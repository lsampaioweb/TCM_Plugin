package net.thecodemaster.evd.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ SQLInjection.class, CookiePoisoning.class })
public class AllTests {

}
