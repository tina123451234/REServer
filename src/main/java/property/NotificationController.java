package property;

import io.javalin.http.Context;
import java.util.*;

public class NotificationController {

  private final NotificationService service;

  public NotificationController(NotificationService service) {
    this.service = service;
  }

  public void notify(Context ctx) {
    Map<Purchaser, List<String[]>> notifications = service.getNotifications();

    StringBuilder sb = new StringBuilder();
    sb.append("<!DOCTYPE html><html><head><title>Notifications</title></head><body>");
    sb.append("<h1>Property Notifications</h1>");

    for (Map.Entry<Purchaser, List<String[]>> entry : notifications.entrySet()) {
      Purchaser p = entry.getKey();
      List<String[]> matches = entry.getValue();

      sb.append("<h2>").append(p.name)
              .append(" (").append(p.email).append(")</h2>");
      sb.append("<p>Interested postcodes: ")
              .append(p.interestedPostcodes).append("</p>");

      if (matches.isEmpty()) {
        sb.append("<p>No matching listings.</p>");
      } else {
        sb.append("<table border=\"1\" cellpadding=\"6\" cellspacing=\"0\">");
        sb.append("<tr><th>Property ID</th><th>Latest Price</th></tr>");
        for (String[] match : matches) {
          sb.append("<tr><td>").append(match[0])
                  .append("</td><td>$").append(match[1])
                  .append("</td></tr>");
        }
        sb.append("</table>");
      }
    }

    sb.append("</body></html>");
    ctx.html(sb.toString());
    ctx.status(200);
  }
}