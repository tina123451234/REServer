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

      // Optional: clears previous synthetic listings so you do not duplicate them
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

        double oldPrice = Double.parseDouble(purchasePriceString);

        long initialListingPrice = Math.round(oldPrice * 1.20);

        String today = LocalDate.now().toString();

        Listing listing = new Listing(
            propertyId,
            today,
            String.valueOf(initialListingPrice)
        );

        // Create 3 price records:
        // initial price, then two reductions one week apart
        double currentPrice = initialListingPrice;

        for (int j = 0; j < 3; j++) {
          String priceDate =
              LocalDate.now().plusDays(j * 7L).toString();

          ListingPrice listingPrice = new ListingPrice(
              propertyId,
              String.valueOf(Math.round(currentPrice)),
              priceDate
          );

          listing.prices.add(listingPrice);

          // reduce by 3% each time after storing current price
          currentPrice *= 0.97;
        }

        List<Document> priceDocs = new ArrayList<>();

        for (ListingPrice p : listing.prices) {
          priceDocs.add(
              new Document()
                  .append("propertyID", p.propertyID)
                  .append("price", p.price)
                  .append("priceDate", p.priceDate)
          );
        }

        Document listingDoc = new Document()
            .append("propertyId", listing.propertyId)
            .append("dateListed", listing.dateListed)
            .append("initialPrice", listing.initialPrice)
            .append("prices", priceDocs);

        listingDocuments.add(listingDoc);
      }

      if (!listingDocuments.isEmpty()) {
        listings.insertMany(listingDocuments);
      }

      System.out.println(
          "Created " + listingDocuments.size()
              + " synthetic property listings with price histories."
      );
    }
  }
}