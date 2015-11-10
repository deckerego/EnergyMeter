package controllers;

import net.deckerego.ec.Trinket;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

public class Application extends Controller {
    private static Trinket trinket = new Trinket();

    public Result index() {
        return ok(index.render(trinket.readLine()));
    }

}
