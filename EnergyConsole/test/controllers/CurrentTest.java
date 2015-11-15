package controllers;

import net.deckerego.ec.UsbConnection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.mvc.Result;
import play.test.WithApplication;

import static org.junit.Assert.assertEquals;
import static play.test.Helpers.*;

@RunWith(MockitoJUnitRunner.class)
public class CurrentTest extends WithApplication {
    private Current controller;
    @Mock
    private UsbConnection usbConnection;

    @Before
    public void setUp() {
        controller = new Current(usbConnection);
    }

    @Test
    public void testCurrent() {
        Result result = invokeWithContext(fakeRequest(), () -> controller.fetch());
        assertEquals(OK, result.status());
    }
}