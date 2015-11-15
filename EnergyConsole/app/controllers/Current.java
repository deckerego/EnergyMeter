package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import net.deckerego.ec.UsbConnection;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import javax.inject.*;

public class Current extends Controller {
    UsbConnection usbConnection;

    @Inject
    public Current(UsbConnection usbConnection) {
        this.usbConnection = usbConnection;
    }

    public Result fetch() {
        ObjectNode result = Json.newObject();
        result.put("amperage", usbConnection.readLine());
        return ok(result);
    }
}
