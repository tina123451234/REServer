package property;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PurchaserDAO {

  private final MongoCollection<Document> collection;

  private static final int MAX_RESULTS = 100;

  public PurchaserDAO() {

    MongoClient mongoClient =
            MongoClients.create("mongodb://localhost:27017");

    MongoDatabase database =
            mongoClient.getDatabase("realEstate");

    collection =
            database.getCollection("purchasers");

    System.out.println(
            "Connected to Purchasers Collection! Documents: "
                    + collection.countDocuments()
    );
  }

  public boolean newPurchaser(Purchaser purchaser) {

    try {

      Document doc = new Document()
              .append("purchaser_id", purchaser.purchaserID)
              .append("name", purchaser.name)
              .append("email", purchaser.email)
              .append(
                      "interested_postcodes",
                      purchaser.interestedPostcodes
              );

      collection.insertOne(doc);

      return true;

    } catch (Exception e) {

      return false;
    }
  }

  public Optional<Purchaser> getPurchaserById(
          String purchaserID
  ) {

    Document doc =
            collection.find(
                    Filters.eq(
                            "purchaser_id",
                            purchaserID
                    )
            ).first();

    if (doc == null) {
      return Optional.empty();
    }

    return Optional.of(docToPurchaser(doc));
  }

  public List<Purchaser> getAllPurchasers() {

    List<Purchaser> result =
            new ArrayList<>();

    for (Document doc :
            collection.find().limit(MAX_RESULTS)) {

      result.add(docToPurchaser(doc));
    }

    return result;
  }

  public List<Purchaser> getPurchasersByPostcode(
          String postcode
  ) {

    List<Purchaser> result =
            new ArrayList<>();

    for (
            Document doc :
            collection.find(
                    Filters.eq(
                            "interested_postcodes",
                            postcode
                    )
            ).limit(MAX_RESULTS)
    ) {

      result.add(docToPurchaser(doc));
    }

    return result;
  }

  private Purchaser docToPurchaser(
          Document doc
  ) {

    return new Purchaser(

            doc.getString("purchaser_id"),

            doc.getString("name"),

            doc.getString("email"),

            (List<String>) doc.get(
                    "interested_postcodes"
            )
    );
  }
}