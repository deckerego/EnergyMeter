import org.junit.Test;
import play.test.WithBrowser;

import static org.junit.Assert.assertTrue;

public class IntegrationTest extends WithBrowser {

    @Test
    public void runInBrowser() {
        browser.goTo("/current");
        assertTrue(browser.pageSource().contains("amperage"));
    }

}
