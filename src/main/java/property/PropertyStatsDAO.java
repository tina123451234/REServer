package property;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;

public class PropertyStatsDAO {

  private final MongoCollection<Document> collection;

  public PropertyStatsDAO() {
    MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    MongoDatabase database = mongoClient.getDatabase("realEstate");
    collection = database.getCollection("property_stats");
    System.out.println("Connected to property_stats! Documents: " + collection.countDocuments());
  }

  // Increment view count for a specific propertyID
  public void incrementPropertyView(String propertyId) {
    collection.updateOne(Filters.eq("_id", propertyId),
            Updates.inc("viewCount", 1),
            new UpdateOptions().upsert(true));
  }

  // Increment search count for a specific postcode
  public void incrementPostcodeSearch(String postcode) {
    collection.updateOne(
            Filters.eq("postcode", postcode),
            Updates.inc("searchCount", 1),
            new UpdateOptions().upsert(true)
    );
  }


  // Get view count for a propertyID
  public int getPropertyViewCount(String propertyId) {
    Document doc = collection.find(Filters.eq("propertyId", propertyId)).first();
    if (doc == null) return 0;
    return doc.getInteger("viewCount", 0);
  }

  // Get search count for a postcode
  public int getPostcodeSearchCount(String postcode) {
    Document doc = collection.find(Filters.eq("postcode", postcode)).first();
    if (doc == null) return 0;
    return doc.getInteger("searchCount", 0);
  }
}