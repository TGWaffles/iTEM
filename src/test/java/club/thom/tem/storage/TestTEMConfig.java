package club.thom.tem.storage;

import club.thom.tem.TEM;
import club.thom.tem.helpers.TestHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertFalse;


@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({TEM.class})
public class TestTEMConfig {
    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        TestHelper.setupTEMConfigAndMainClass();
        TestHelper.startRequestsLoop();
    }

    @Test
    public void testInvalidKey() {
        assertFalse(TEMConfig.isKeyValid("abc123"));
    }
}
