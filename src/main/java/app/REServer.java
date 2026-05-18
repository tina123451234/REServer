package app;

import io.javalin.Javalin;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;

import property.*;

public class REServer {

  public static void main(String[] args) {

    // DAOs
    var properties = new PropertyDAO();
    var purchasers = new PurchaserDAO();
    var listings = new ListingDAO();
    var stats = new PropertyStatsDAO();

    // Controllers
    PropertyController propertyHandler = new PropertyController(properties, stats);
    PurchaserController purchaserHandler = new PurchaserController(purchasers);
    ListingController listingHandler = new ListingController(listings);

    NotificationService notificationService =
            new NotificationService(listings, properties, purchasers, stats);
    NotificationController notificationHandler =
            new NotificationController(notificationService);

    // Everything inside create() — config, plugins, routes
    Javalin.create(config -> {

      // OpenAPI + Swagger UI
      config.registerPlugin(new OpenApiPlugin(pluginConfig ->
              pluginConfig.withDefinitionConfiguration((version, def) ->
                      def.withInfo(info -> {
                        info.setTitle("Real Estate API");
                        info.setVersion("1.0");
                      })
              )
      ));
      config.registerPlugin(new SwaggerPlugin());

      // Routes
      config.router.apiBuilder(() -> {

        ApiBuilder.get("/", ctx ->
                ctx.result("Real Estate server is running")
        );

        // PROPERTY
        ApiBuilder.get("/property", ctx ->
                propertyHandler.getAllProperties(ctx)
        );
        ApiBuilder.post("/property", ctx ->
                propertyHandler.createProperty(ctx)
        );
        ApiBuilder.get("/property/{propertyID}", ctx ->
                propertyHandler.getPropertyByID(ctx, ctx.pathParam("propertyID"))
        );
        ApiBuilder.get("/property/postcode/{postcode}", ctx ->
                propertyHandler.findPropertyByPostCode(ctx, ctx.pathParam("postcode"))
        );

        // PURCHASER
        ApiBuilder.post("/purchaser", ctx ->
                purchaserHandler.createPurchaser(ctx)
        );
        ApiBuilder.get("/purchaser", ctx ->
                purchaserHandler.getAllPurchasers(ctx)
        );

        // LISTING
        ApiBuilder.post("/listing", ctx ->
                listingHandler.createListing(ctx)
        );
        ApiBuilder.get("/listing", ctx ->
                listingHandler.getAllListings(ctx)
        );
        ApiBuilder.get("/listing/{listingId}", ctx ->
                listingHandler.getListingById(ctx, ctx.pathParam("listingId"))
        );
        ApiBuilder.get("/listing/property/{propertyId}", ctx ->
                listingHandler.getListingsByProperty(ctx, ctx.pathParam("propertyId"))
        );
        ApiBuilder.post("/listing/{listingId}/price", ctx ->
                listingHandler.addPrice(ctx, ctx.pathParam("listingId"))
        );

        // NOTIFICATION
        ApiBuilder.get("/notify", ctx ->
                notificationHandler.notify(ctx)
        );
      });

    }).start(7070);
  }
}