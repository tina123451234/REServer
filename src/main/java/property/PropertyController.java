package property;

import io.javalin.http.Context;
import io.javalin.openapi.*;

import java.util.List;
import java.util.Optional;

public class PropertyController {

    private final PropertyDAO properties;
    private final PropertyStatsDAO stats;

    public PropertyController(PropertyDAO properties, PropertyStatsDAO stats) {
        this.properties = properties;
        this.stats = stats;
    }

    // implements POST /property
    @OpenApi(
            summary = "Create a new property",
            operationId = "createProperty",
            path = "/property",
            methods = HttpMethod.POST,
            tags = {"Property"},
            requestBody = @OpenApiRequestBody(
                    content = {@OpenApiContent(from = Property.class)}
            ),
            responses = {
                    @OpenApiResponse(status = "201", description = "Property Created"),
                    @OpenApiResponse(status = "400", description = "Failed to add property")
            }
    )
    public void createProperty(Context ctx) {
        Property property = ctx.bodyValidator(Property.class).get();
        if (properties.newProperty(property)) {
            ctx.result("Property Created");
            ctx.status(201);
        } else {
            ctx.result("Failed to add property");
            ctx.status(400);
        }
    }

    // implements GET /property
    // Optional query params: minPrice, maxPrice
    @OpenApi(
            summary = "Get all properties",
            operationId = "getAllProperties",
            path = "/property",
            methods = HttpMethod.GET,
            tags = {"Property"},
            queryParams = {
                    @OpenApiParam(name = "minPrice", type = Long.class, description = "Minimum property price", required = false),
                    @OpenApiParam(name = "maxPrice", type = Long.class, description = "Maximum property price", required = false)
            },
            responses = {
                    @OpenApiResponse(status = "200", description = "List of properties returned"),
                    @OpenApiResponse(status = "404", description = "No Properties Found")
            }
    )
    public void getAllProperties(Context ctx) {
        String minParam = ctx.queryParam("minPrice");
        String maxParam = ctx.queryParam("maxPrice");

        List<Property> allProperties;
        if (minParam != null || maxParam != null) {
            long min = minParam != null ? Long.parseLong(minParam) : Long.MIN_VALUE;
            long max = maxParam != null ? Long.parseLong(maxParam) : Long.MAX_VALUE;
            allProperties = properties.getPropertiesByPriceRange(min, max);
        } else {
            allProperties = properties.getAllProperties();
        }

        if (allProperties.isEmpty()) {
            ctx.html(errorHtml("No Properties Found"));
            ctx.status(404);
        } else {
            ctx.html(propertyListHtml("All Properties", allProperties));
            ctx.status(200);
        }
    }

    // implements GET /property/{propertyID}
    @OpenApi(
            summary = "Get property by ID",
            operationId = "getPropertyByID",
            path = "/property/{propertyID}",
            methods = HttpMethod.GET,
            tags = {"Property"},
            pathParams = {
                    @OpenApiParam(name = "propertyID", description = "The property ID", required = true)
            },
            responses = {
                    @OpenApiResponse(status = "200", description = "Property found"),
                    @OpenApiResponse(status = "404", description = "Property not found")
            }
    )
    public void getPropertyByID(Context ctx, String id) {
        stats.incrementPropertyView(id);
        Optional<Property> property = properties.getPropertyById(id);
        if (property.isPresent()) {
            ctx.html(propertyListHtml("Property " + id, List.of(property.get())));
            ctx.status(200);
        } else {
            ctx.html(errorHtml("Property not found"));
            ctx.status(404);
        }
    }

    // implements GET /property/postcode/{postcodeID}
    @OpenApi(
            summary = "Find properties by postcode",
            operationId = "findPropertyByPostCode",
            path = "/property/postcode/{postcodeID}",
            methods = HttpMethod.GET,
            tags = {"Property"},
            pathParams = {
                    @OpenApiParam(name = "postcodeID", description = "The postcode to search", required = true)
            },
            responses = {
                    @OpenApiResponse(status = "200", description = "Properties found for postcode"),
                    @OpenApiResponse(status = "404", description = "No properties for postcode found")
            }
    )
    public void findPropertyByPostCode(Context ctx, String postCode) {
        stats.incrementPostcodeSearch(postCode);
        List<Property> result = properties.getPropertiesByPostCode(postCode);
        if (result.isEmpty()) {
            ctx.html(errorHtml("No properties for postcode found"));
            ctx.status(404);
        } else {
            ctx.html(propertyListHtml("Properties in Postcode " + postCode, result));
            ctx.status(200);
        }
    }

    // --- HTML helpers (unchanged) ---

    private String propertyListHtml(String title, List<Property> props) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><title>").append(title).append("</title></head><body>");
        sb.append("<h1>").append(title).append("</h1>");
        sb.append("<table border=\"1\" cellpadding=\"6\" cellspacing=\"0\">");
        sb.append("<tr><th>Property ID</th><th>Postcode</th><th>Price</th><th>For Sale</th></tr>");
        for (Property p : props) {
            sb.append("<tr>")
                    .append("<td>").append(p.propertyID).append("</td>")
                    .append("<td>").append(p.postcode).append("</td>")
                    .append("<td>").append(p.propertyPrice).append("</td>")
                    .append("<td>").append(p.forSale).append("</td>")
                    .append("</tr>");
        }
        sb.append("</table></body></html>");
        return sb.toString();
    }

    private String errorHtml(String message) {
        return "<!DOCTYPE html><html><head><title>Error</title></head><body>"
                + "<h1>Error</h1><p>" + message + "</p></body></html>";
    }
}