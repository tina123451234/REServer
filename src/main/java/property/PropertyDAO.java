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

public class PropertyDAO {

    private final MongoCollection<Document> collection;
    private static final int MAX_RESULTS = 100; // hack to avoid returning millions of records

    public PropertyDAO() {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("realEstate");
        collection = database.getCollection("properties");
        System.out.println("Connected to MongoDB! Documents: " + collection.countDocuments());
    }

    public boolean newProperty(Property property) {
        try {
            Document doc = new Document()
                    .append("property_id", property.propertyID)
                    .append("post_code", property.postcode)
                    .append("purchase_price", property.propertyPrice)
                    .append("address", property.address)
                    .append("council_name", property.councilName)
                    .append("property_type", property.propertyType)
                    .append("contract_date", property.contractDate)
                    .append("settlement_date", property.settlementDate);
            collection.insertOne(doc);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Optional<Property> getPropertyById(String propertyID) {
        Document doc = collection.find(Filters.eq("property_id", propertyID)).first();
        if (doc == null) return Optional.empty();
        return Optional.of(docToProperty(doc));
    }

    public List<Property> getPropertiesByPostCode(String postCode) {
        List<Property> result = new ArrayList<>();
        for (Document doc : collection.find(Filters.eq("post_code", postCode)).limit(MAX_RESULTS)) {
            result.add(docToProperty(doc));
        }
        return result;
    }

    public List<Property> getAllProperties() {
        List<Property> result = new ArrayList<>();
        for (Document doc : collection.find().limit(MAX_RESULTS)) {
            result.add(docToProperty(doc));
        }
        return result;
    }

    public List<Property> getPropertiesByPriceRange(long minPrice, long maxPrice) {
        List<Property> result = new ArrayList<>();
        for (Document doc : collection.find(
                Filters.and(
                        Filters.gte("purchase_price", String.valueOf(minPrice)),
                        Filters.lte("purchase_price", String.valueOf(maxPrice))
                )).limit(MAX_RESULTS)) {
            result.add(docToProperty(doc));
        }
        return result;
    }

    private Property docToProperty(Document doc) {
        return new Property(
                doc.getString("property_id"),
                doc.getString("post_code"),
                doc.getString("purchase_price"),
                doc.getString("address"),
                doc.getString("council_name"),
                doc.getString("property_type"),
                doc.getString("contract_date"),
                doc.getString("settlement_date")
        );
    }
}