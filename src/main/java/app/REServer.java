package app;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;

import property.PropertyDAO;
import property.PropertyController;

import property.PurchaserDAO;
import property.PurchaserController;

import property.ListingDAO;
import property.ListingController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class REServer {

    private static final Logger LOG =
            LoggerFactory.getLogger(REServer.class);

    public static void main(String[] args) {

        // DAO objects
        var properties = new PropertyDAO();
        var purchasers = new PurchaserDAO();
        var listings = new ListingDAO();

        // Controllers
        PropertyController propertyHandler =
                new PropertyController(properties);

        PurchaserController purchaserHandler =
                new PurchaserController(purchasers);

        ListingController listingHandler =
                new ListingController(listings);

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


            //LISTING ROUTES
            app.post("/listing", ctx -> {
                listingHandler.createListing(ctx);
            });

            app.get("/listing", ctx -> {
                listingHandler.getAllListings(ctx);
            });

            app.get("/listing/{listingId}", ctx -> {
                listingHandler.getListingById(
                        ctx,
                        ctx.pathParam("listingId")
                );
            });

            app.get("/listing/property/{propertyId}", ctx -> {
                listingHandler.getListingsByProperty(
                        ctx,
                        ctx.pathParam("propertyId")
                );
            });

            app.post("/listing/{listingId}/price", ctx -> {
                listingHandler.addPrice(
                        ctx,
                        ctx.pathParam("listingId")
                );
            });


        });
    }
}