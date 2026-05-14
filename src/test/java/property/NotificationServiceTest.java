package property;

import org.junit.Test;

import java.util.*;

import static com.mongodb.internal.connection.tlschannel.util.Util.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NotificationServiceTest {

  // --- Stub DAOs ---
  // These replace real MongoDB-backed DAOs with in-memory fakes,
  // so tests run without a database connection.

  // Returns a fixed list of listings instead of querying MongoDB
  static class StubListingDAO extends ListingDAO {
    private final List<Listing> data;
    public StubListingDAO(List<Listing> data) { this.data = data; }
    @Override public List<Listing> getAllListings() { return data; }
  }

  // Returns a fixed map of properties keyed by ID instead of querying MongoDB
  static class StubPropertyDAO extends PropertyDAO {
    private final Map<String, Property> data;
    public StubPropertyDAO(Map<String, Property> data) { this.data = data; }
    @Override public Optional<Property> getPropertyById(String id) {
      return Optional.ofNullable(data.get(id));
    }
  }

  // Returns a fixed list of purchasers instead of querying MongoDB
  static class StubPurchaserDAO extends PurchaserDAO {
    private final List<Purchaser> data;
    public StubPurchaserDAO(List<Purchaser> data) { this.data = data; }
    @Override public List<Purchaser> getAllPurchasers() { return data; }
  }

  // --- Test 1 ---
  // Verifies that a purchaser IS notified when a listing exists
  // in one of their interested postcodes.
  @Test
  public void testPurchaserMatchesListingInInterestedPostcode() {

    // Set up a property located in postcode 2000
    Property prop = new Property("P1", "2000", "500000",
            "1 Main St", "Sydney", "House", "2024-01-01", "2024-02-01");

    // Set up a listing for that property with a known price
    Listing listing = new Listing("P1", "2024-01-01", "500000");
    ListingPrice price = new ListingPrice("P1", "500000", "2024-01-01");
    listing.prices = List.of(price);

    // Set up a purchaser who is interested in postcodes 2000 and 3000
    Purchaser purchaser = new Purchaser("PU1", "Alice",
            "alice@example.com", List.of("2000", "3000"));

    // Wire up the service with stub data
    NotificationService service = new NotificationService(
            new StubListingDAO(List.of(listing)),
            new StubPropertyDAO(Map.of("P1", prop)),
            new StubPurchaserDAO(List.of(purchaser))
    );

    Map<Purchaser, List<String[]>> result = service.getNotifications();

    // Expect exactly one purchaser in the result
    assertEquals(1, result.size());

    // Expect that purchaser to have exactly one matching listing
    List<String[]> matches = result.get(purchaser);
    assertNotNull(matches);
    assertEquals(1, matches.size());

    // Expect the match to contain the correct property ID and price
    assertEquals("P1", matches.get(0)[0]);
    assertEquals("500000", matches.get(0)[1]);
  }

  // --- Test 2 ---
  // Verifies that a purchaser is NOT notified when no listings exist
  // in any of their interested postcodes.
  @Test
  public void testPurchaserNoMatchWhenPostcodeDiffers() {

    // Set up a property in postcode 9999 (not in the purchaser's interests)
    Property prop = new Property("P2", "9999", "800000",
            "2 Other St", "Melbourne", "Apartment", "2024-01-01", "2024-02-01");

    // Set up a listing for that property
    Listing listing = new Listing("P2", "2024-01-01", "800000");
    listing.prices = List.of(new ListingPrice("P2", "800000", "2024-01-01"));

    // Set up a purchaser who is only interested in postcode 2000
    Purchaser purchaser = new Purchaser("PU2", "Bob",
            "bob@example.com", List.of("2000"));

    // Wire up the service with stub data
    NotificationService service = new NotificationService(
            new StubListingDAO(List.of(listing)),
            new StubPropertyDAO(Map.of("P2", prop)),
            new StubPurchaserDAO(List.of(purchaser))
    );

    Map<Purchaser, List<String[]>> result = service.getNotifications();

    // Expect the purchaser to have zero matches since postcode 9999 != 2000
    List<String[]> matches = result.get(purchaser);
    assertTrue(matches.isEmpty());
  }
}