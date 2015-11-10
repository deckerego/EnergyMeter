package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import net.deckerego.ec.Trinket;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class Current extends Controller {
    private static Trinket trinket = new Trinket();

    public Result fetch() {
        ObjectNode result = Json.newObject();
        result.put("amperage", trinket.readLine());
        return ok(result);
    }
}
