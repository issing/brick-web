package net.isger.brick;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.isger.brick.core.ConsoleManager;
import net.isger.util.Helpers;
import net.isger.util.Https;

public class BrickWebTest extends TestCase {

    private ConsoleManager manager;

    public BrickWebTest(String testName) {
        super(testName);
        manager = new ConsoleManager();
        manager.load();
        while (!manager.getConsole().hasReady()) {
            Helpers.sleep(200l);
        }
        Helpers.sleep(2);
    }

    public static Test suite() {
        return new TestSuite(BrickWebTest.class);
    }

    public void testWeb() {
        System.out.println(Https.get("http://localhost:8080"));
        assertTrue(true);
    }
}
