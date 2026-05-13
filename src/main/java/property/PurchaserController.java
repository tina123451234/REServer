package property;

import io.javalin.http.Context;

import java.util.List;

public class PurchaserController {

  private final PurchaserDAO purchasers;

  public PurchaserController(PurchaserDAO purchasers) {
    this.purchasers = purchasers;
  }

  // POST /purchaser
  public void createPurchaser(Context ctx) {

    Purchaser purchaser =
            ctx.bodyValidator(Purchaser.class).get();

    // validate max 5 postcodes
    if (purchaser.interestedPostcodes.size() > 5) {
      ctx.result("Maximum 5 postcodes allowed");
      ctx.status(400);
      return;
    }

    if (purchasers.newPurchaser(purchaser)) {
      ctx.result("Purchaser Created");
      ctx.status(201);
    } else {
      ctx.result("Failed to create purchaser");
      ctx.status(400);
    }
  }

  // GET /purchaser
  public void getAllPurchasers(Context ctx) {

    List<Purchaser> allPurchasers =
            purchasers.getAllPurchasers();

    if (allPurchasers.isEmpty()) {
      ctx.html(errorHtml("No purchasers found"));
      ctx.status(404);
    } else {
      ctx.html(
              purchaserListHtml(
                      "All Purchasers",
                      allPurchasers
              )
      );
      ctx.status(200);
    }
  }

  private String purchaserListHtml(
          String title,
          List<Purchaser> purchasers
  ) {

    StringBuilder sb = new StringBuilder();

    sb.append("<!DOCTYPE html><html><head><title>")
            .append(title)
            .append("</title></head><body>");

    sb.append("<h1>").append(title).append("</h1>");

    sb.append("<table border=\"1\" cellpadding=\"6\" cellspacing=\"0\">");

    sb.append(
            "<tr><th>ID</th><th>Name</th><th>Email</th><th>Interested Postcodes</th></tr>"
    );

    for (Purchaser p : purchasers) {

      sb.append("<tr>")
              .append("<td>").append(p.purchaserID).append("</td>")
              .append("<td>").append(p.name).append("</td>")
              .append("<td>").append(p.email).append("</td>")
              .append("<td>").append(p.interestedPostcodes).append("</td>")
              .append("</tr>");
    }

    sb.append("</table></body></html>");

    return sb.toString();
  }

  private String errorHtml(String message) {

    return "<!DOCTYPE html><html><head><title>Error</title></head><body>"
            + "<h1>Error</h1><p>"
            + message
            + "</p></body></html>";
  }
}