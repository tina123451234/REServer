package app;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;

import property.PropertyDAO;
import property.PropertyController;

import property.PurchaserDAO;
import property.PurchaserController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class REServer {

    private static final Logger LOG =
            LoggerFactory.getLogger(REServer.class);

    public static void main(String[] args) {

        // DAO objects
        var properties = new PropertyDAO();
        var purchasers = new PurchaserDAO();

        // Controllers
        PropertyController propertyHandler =
                new PropertyController(properties);

        PurchaserController purchaserHandler =
                new PurchaserController(purchasers);

        // start server
        var app = Javalin.create()
                .get("/", ctx ->
                        ctx.result(
                                "Real Estate server is running"
                        )
                )
                .start(7070);

        // Routes
        JavalinConfig config = new JavalinConfig();

        config.router.apiBuilder(() -> {

            // PROPERTY ROUTES

            app.get("/property/{propertyID}", ctx -> {
                propertyHandler.getPropertyByID(
                        ctx,
                        ctx.pathParam("propertyID")
                );
            });

            app.get("/property", ctx -> {
                propertyHandler.getAllProperties(ctx);
            });

            app.post("/property", ctx -> {
                propertyHandler.createProperty(ctx);
            });

            app.get("/property/postcode/{postcode}", ctx -> {
                propertyHandler.findPropertyByPostCode(
                        ctx,
                        ctx.pathParam("postcode")
                );
            });

            // PURCHASER ROUTES

            app.post("/purchaser", ctx -> {
                purchaserHandler.createPurchaser(ctx);
            });

            app.get("/purchaser", ctx -> {
                purchaserHandler.getAllPurchasers(ctx);
            });
        });
    }
}