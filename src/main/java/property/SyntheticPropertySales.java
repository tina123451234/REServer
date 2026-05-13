package property;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Aggregates.sample;

public class SyntheticPropertySales {

  private static final String MONGO_URI =
      "mongodb+srv://hoyta_db_user:realestate123@m0.cqqrymy.mongodb.net/?retryWrites=true&w=majority&appName=M0";

  private static final String DATABASE_NAME = "realEstate";
  private static final String PROPERTY_COLLECTION = "properties";
  private static final String LISTING_COLLECTION = "listings";

  public static void main(String[] args) {

    try (MongoClient client = MongoClients.create(MONGO_URI)) {
      MongoDatabase db = client.getDatabase(DATABASE_NAME);

      MongoCollection<Document> properties =
          db.getCollection(PROPERTY_COLLECTION);

      MongoCollection<Document> listings =
          db.getCollection(LISTING_COLLECTION);

      listings.drop();

      List<Document> randomProperties =
          properties.aggregate(List.of(sample(1000)))
              .into(new ArrayList<>());

      List<Document> listingDocuments = new ArrayList<>();

      for (Document property : randomProperties) {
        String propertyId = property.getObjectId("_id").toString();

        String purchasePriceString = property.getString("purchase_price");

        if (purchasePriceString == null || purchasePriceString.isEmpty()) {
          continue;
        }

        double lastSalePrice = Double.parseDouble(purchasePriceString);
        long newListingPrice = Math.round(lastSalePrice * 1.20);

        String today = LocalDate.now().toString();

        Listing listing = new Listing(
            propertyId,
            today,
            String.valueOf(newListingPrice)
        );

        ListingPrice listingPrice = new ListingPrice(
            propertyId,
            String.valueOf(newListingPrice),
            today
        );

        listing.prices.add(listingPrice);

        Document priceDoc = new Document()
            .append("propertyID", listingPrice.propertyID)
            .append("price", listingPrice.price)
            .append("priceDate", listingPrice.priceDate);

        Document listingDoc = new Document()
            .append("propertyId", listing.propertyId)
            .append("dateListed", listing.dateListed)
            .append("lastSalePrice", purchasePriceString)
            .append("initialPrice", listing.initialPrice)
            .append("prices", List.of(priceDoc));
        listingDocuments.add(listingDoc);
      }

      if (!listingDocuments.isEmpty()) {
        listings.insertMany(listingDocuments);
      }

      System.out.println(
          "Created " + listingDocuments.size()
              + " synthetic property listings at 20% above last sale price."
      );
    }
  }
}