package property;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.*;

public class SyntheticPurchaserData {

  private static final String MONGO_URI =
      "mongodb+srv://hoyta_db_user:realestate123@m0.cqqrymy.mongodb.net/?appName=M0";

  private static final String DATABASE_NAME = "realEstate";
  private static final String COLLECTION_NAME = "purchasers";

  private static final String[] POSTCODES = {
      "2000", "2007", "2010", "2020", "2031",
      "2042", "2060", "2077", "2113", "2150",
      "2200", "2250", "2300", "2500", "2650",
      "2750", "2770", "2800"
  };

  public static void main(String[] args) {
    Random random = new Random();

    try (MongoClient client = MongoClients.create(MONGO_URI)) {
      MongoDatabase db = client.getDatabase(DATABASE_NAME);
      MongoCollection<Document> purchasers =
          db.getCollection(COLLECTION_NAME);

      List<Document> batch = new ArrayList<>();

      for (int i = 1; i <= 10_000; i++) {
        int postcodeCount = random.nextInt(6);

        Set<String> selectedPostcodes = new HashSet<>();
        while (selectedPostcodes.size() < postcodeCount) {
          selectedPostcodes.add(
              POSTCODES[random.nextInt(POSTCODES.length)]
          );
        }

        // CHANGE purchaser to match the final purchaser
        Document purchaser = new Document()
            .append("name", "Synthetic Buyer " + i)
            .append("email", "syntheticbuyer" + i + "@example.com")
            .append("interestedPostcodes",
                new ArrayList<>(selectedPostcodes));

        batch.add(purchaser);

        if (batch.size() == 1000) {
          purchasers.insertMany(batch);
          System.out.println("Inserted " + i + " purchasers...");
          batch.clear();
        }
      }

      if (!batch.isEmpty()) {
        purchasers.insertMany(batch);
      }

      System.out.println("Done. Inserted 10,000 synthetic purchasers.");
    }
  }
}
