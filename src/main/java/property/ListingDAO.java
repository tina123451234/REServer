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

  // POST /listing — create a new listing for a property
  public boolean newListing(Listing listing) {
    try {
      Document doc = new Document()
              .append("listing_id",   listing.propertyId)
              .append("property_id",  listing.propertyId)
              .append("date_listed",  listing.dateListed);
      listingCollection.insertOne(doc);
      // store the initial price in listing_prices
      if (listing.initialPrice != null) {
        Document priceDoc = new Document()
                .append("listing_id", listing.propertyId)
                .append("price",      listing.initialPrice)
                .append("price_date", listing.dateListed);
        priceCollection.insertOne(priceDoc);
      }
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  // GET /listing/{listingId}
  public Optional<Listing> getListingById(String listingId) {
    Document doc = listingCollection.find(Filters.eq("listing_id", listingId)).first();
    if (doc == null) return Optional.empty();
    Listing listing = docToListing(doc);
    listing.prices = getPricesForListing(listingId);
    return Optional.of(listing);
  }

  // GET /listing/property/{propertyId} — all listings for a property
  public List<Listing> getListingsByPropertyId(String propertyId) {
    List<Listing> result = new ArrayList<>();
    for (Document doc : listingCollection.find(Filters.eq("property_id", propertyId)).limit(MAX_RESULTS)) {
      Listing listing = docToListing(doc);
      listing.prices = getPricesForListing(listing.propertyId);
      result.add(listing);
    }
    return result;
  }

  // POST /listing/{listingId}/price — add a new price to an existing listing
  public boolean addPrice(String listingId, ListingPrice price) {
    // check listing exists first
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
      return false;
    }
  }

  // Get all prices for a listing (price history)
  public List<ListingPrice> getPricesForListing(String listingId) {
    List<ListingPrice> result = new ArrayList<>();
    for (Document doc : priceCollection.find(Filters.eq("listing_id", listingId))) {
      result.add(docToListingPrice(doc));
    }
    return result;
  }

  // Get all listings
  public List<Listing> getAllListings() {
    List<Listing> result = new ArrayList<>();
    for (Document doc : listingCollection.find().limit(MAX_RESULTS)) {
      Listing listing = docToListing(doc);
      listing.prices = getPricesForListing(listing.propertyId);
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
    price.price     = doc.getString("price");
    price.priceDate = doc.getString("price_date");
    return price;
  }
}