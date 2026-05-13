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

public class ListingDAO {

  private final MongoCollection<Document> listingCollection;
  private final MongoCollection<Document> priceCollection;
  private static final int MAX_RESULTS = 100;

  public ListingDAO() {
    MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    MongoDatabase database = mongoClient.getDatabase("realEstate");
    listingCollection = database.getCollection("listings");
    priceCollection   = database.getCollection("listing_prices");
    System.out.println("Connected to MongoDB listings! Documents: " + listingCollection.countDocuments());
  }

  // POST /listing
  public boolean newListing(Listing listing) {
    try {
      Document doc = new Document()
              .append("listing_id",  listing.propertyId)  // use propertyId as listing_id
              .append("property_id", listing.propertyId)
              .append("date_listed", listing.dateListed);
      listingCollection.insertOne(doc);

      if (listing.initialPrice != null) {
        Document priceDoc = new Document()
                .append("listing_id", listing.propertyId)  // consistent key
                .append("price",      listing.initialPrice)
                .append("price_date", listing.dateListed);
        priceCollection.insertOne(priceDoc);
      }
      return true;
    } catch (Exception e) {
      e.printStackTrace();  // actually log the error
      return false;
    }
  }

  // GET /listing/{listingId}
  public Optional<Listing> getListingById(String listingId) {
    Document doc = listingCollection.find(Filters.eq("listing_id", listingId)).first();
    if (doc == null) return Optional.empty();
    Listing listing = docToListing(doc);
    listing.prices = getPricesForListing(listingId);  // use the param, not listing.propertyId
    return Optional.of(listing);
  }

  // GET /listing/property/{propertyId}
  public List<Listing> getListingsByPropertyId(String propertyId) {
    List<Listing> result = new ArrayList<>();
    for (Document doc : listingCollection.find(Filters.eq("property_id", propertyId)).limit(MAX_RESULTS)) {
      Listing listing = docToListing(doc);
      listing.prices = getPricesForListing(doc.getString("listing_id"));  // fix: use listing_id from doc
      result.add(listing);
    }
    return result;
  }

  // POST /listing/{listingId}/price
  public boolean addPrice(String listingId, ListingPrice price) {
    Document listing = listingCollection.find(Filters.eq("listing_id", listingId)).first();
    if (listing == null) return false;
    try {
      Document priceDoc = new Document()
              .append("listing_id", listingId)
              .append("price",      price.price)
              .append("price_date", price.priceDate);
      priceCollection.insertOne(priceDoc);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  // Get all prices for a listing
  public List<ListingPrice> getPricesForListing(String listingId) {
    List<ListingPrice> result = new ArrayList<>();
    for (Document doc : priceCollection.find(Filters.eq("listing_id", listingId))) {
      result.add(docToListingPrice(doc));
    }
    return result;
  }

  // GET /listing
  public List<Listing> getAllListings() {
    List<Listing> result = new ArrayList<>();
    for (Document doc : listingCollection.find().limit(MAX_RESULTS)) {
      Listing listing = docToListing(doc);
      listing.prices = getPricesForListing(doc.getString("listing_id"));  // fix: use listing_id from doc
      result.add(listing);
    }
    return result;
  }

  private Listing docToListing(Document doc) {
    Listing listing = new Listing();
    listing.propertyId = doc.getString("property_id");
    listing.dateListed = doc.getString("date_listed");
    return listing;
  }

  private ListingPrice docToListingPrice(Document doc) {
    ListingPrice price = new ListingPrice(
            doc.getString("price"),
            doc.getString("price_date"),
            doc.getString("listing_id")
    );
    price.propertyID = doc.getString("listing_id");
    price.price      = doc.getString("price");
    price.priceDate  = doc.getString("price_date");
    return price;
  }
}