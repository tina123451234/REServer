package property;

import io.javalin.http.Context;

import java.util.List;
import java.util.Optional;

public class ListingController {

  private final ListingDAO listings;

  public ListingController(ListingDAO listings) {
    this.listings = listings;
  }

  // implements POST /listing
  public void createListing(Context ctx) {
    Listing listing = ctx.bodyValidator(Listing.class)
            .get();

    if (listings.newListing(listing)) {
      ctx.result("Listing Created");
      ctx.status(201);
    } else {
      ctx.result("Failed to add listing");
      ctx.status(400);
    }
  }

  // implements GET /listing/{listingId}
  public void getListingById(Context ctx, String id) {
    Optional<Listing> listing = listings.getListingById(id);
    if (listing.isPresent()) {
      ctx.html(listingDetailHtml("Listing " + id, listing.get()));
      ctx.status(200);
    } else {
      ctx.html(errorHtml("Listing not found"));
      ctx.status(404);
    }
  }

  // implements GET /listing/property/{propertyId}
  public void getListingsByProperty(Context ctx, String propertyId) {
    List<Listing> result = listings.getListingsByPropertyId(propertyId);
    if (result.isEmpty()) {
      ctx.html(errorHtml("No listings found for property " + propertyId));
      ctx.status(404);
    } else {
      ctx.html(listingListHtml("Listings for Property " + propertyId, result));
      ctx.status(200);
    }
  }

  // implements POST /listing/{listingId}/price
  public void addPrice(Context ctx, String listingId) {
    ListingPrice price = ctx.bodyValidator(ListingPrice.class)
            .get();

    if (listings.addPrice(listingId, price)) {
      ctx.result("Price Added");
      ctx.status(201);
    } else {
      ctx.result("Failed to add price - listing may not exist");
      ctx.status(400);
    }
  }

  // implements GET /listing
  public void getAllListings(Context ctx) {
    List<Listing> allListings = listings.getAllListings();
    if (allListings.isEmpty()) {
      ctx.html(errorHtml("No Listings Found"));
      ctx.status(404);
    } else {
      ctx.html(listingListHtml("All Listings", allListings));
      ctx.status(200);
    }
  }

  // --- HTML helpers ---

  private String listingListHtml(String title, List<Listing> listingList) {
    StringBuilder sb = new StringBuilder();
    sb.append("<!DOCTYPE html><html><head><title>").append(title).append("</title></head><body>");
    sb.append("<h1>").append(title).append("</h1>");
    sb.append("<table border=\"1\" cellpadding=\"6\" cellspacing=\"0\">");
    sb.append("<tr><th>Listing ID</th><th>Property ID</th><th>Date Listed</th><th>Latest Price</th></tr>");
    for (Listing l : listingList) {
      String latestPrice = l.prices.isEmpty()
              ? "N/A"
              : l.prices.get(l.prices.size() - 1).price;
      sb.append("<tr>")
              .append("<td>").append(l.propertyId).append("</td>")
              .append("<td>").append(l.dateListed).append("</td>")
              .append("<td>").append(latestPrice).append("</td>")
              .append("</tr>");
    }
    sb.append("</table></body></html>");
    return sb.toString();
  }

  private String listingDetailHtml(String title, Listing l) {
    StringBuilder sb = new StringBuilder();
    sb.append("<!DOCTYPE html><html><head><title>").append(title).append("</title></head><body>");
    sb.append("<h1>").append(title).append("</h1>");

    // Listing summary
    sb.append("<table border=\"1\" cellpadding=\"6\" cellspacing=\"0\">");
    sb.append("<tr><th>Listing ID</th><th>Property ID</th><th>Date Listed</th></tr>");
    sb.append("<tr>")
            .append("<td>").append(l.propertyId).append("</td>")
            .append("<td>").append(l.dateListed).append("</td>")
            .append("</tr>");
    sb.append("</table>");

    // Price history
    sb.append("<h2>Price History</h2>");
    if (l.prices.isEmpty()) {
      sb.append("<p>No price history available.</p>");
    } else {
      sb.append("<table border=\"1\" cellpadding=\"6\" cellspacing=\"0\">");
      sb.append("<tr><th>Date</th><th>Price</th></tr>");
      for (ListingPrice p : l.prices) {
        sb.append("<tr>")
                .append("<td>").append(p.priceDate).append("</td>")
                .append("<td>$").append(p.price).append("</td>")
                .append("</tr>");
      }
      sb.append("</table>");
    }

    sb.append("</body></html>");
    return sb.toString();
  }

  private String errorHtml(String message) {
    return "<!DOCTYPE html><html><head><title>Error</title></head><body>"
            + "<h1>Error</h1><p>" + message + "</p></body></html>";
  }
}