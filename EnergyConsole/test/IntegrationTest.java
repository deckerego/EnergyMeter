import org.junit.Test;
import play.libs.F.Callback;
import play.test.TestBrowser;

import static org.junit.Assert.assertTrue;
import static play.test.Helpers.*;

public class IntegrationTest {

    @Test
    public void testCurrent() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                browser.goTo("http://localhost:3333/current");
                assertTrue(browser.pageSource().contains("amperage"));
            }
        });
    }

}
