import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.ethz.ruediste.roofline.test.SerializationTest;

@RunWith(Suite.class)
@SuiteClasses({ SerializationTest.class })
public class AllTests {

}
