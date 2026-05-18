package property;

import org.junit.Test;

import java.util.*;

import static com.mongodb.internal.connection.tlschannel.util.Util.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NotificationServiceTest {

  // --- Stub DAOs ---

  static class StubListingDAO extends ListingDAO {
    private final List<Listing> data;
    public StubListingDAO(List<Listing> data) { this.data = data; }
    @Override public List<Listing> getAllListings() { return data; }
  }

  static class StubPropertyDAO extends PropertyDAO {
    private final Map<String, Property> data;
    public StubPropertyDAO(Map<String, Property> data) { this.data = data; }
    @Override public Optional<Property> getPropertyById(String id) {
      return Optional.ofNullable(data.get(id));
    }
  }

  static class StubPurchaserDAO extends PurchaserDAO {
    private final List<Purchaser> data;
    public StubPurchaserDAO(List<Purchaser> data) { this.data = data; }
    @Override public List<Purchaser> getAllPurchasers() { return data; }
  }

  // ✅ NEW — in-memory stub, no MongoDB connection needed
  static class StubPropertyStatsDAO extends PropertyStatsDAO {
    private final Map<String, Integer> viewCounts = new HashMap<>();
    private final Map<String, Integer> searchCounts = new HashMap<>();

    @Override
    public void incrementPropertyView(String propertyId) {
      viewCounts.merge(propertyId, 1, Integer::sum);
    }
    @Override
    public void incrementPostcodeSearch(String postcode) {
      searchCounts.merge(postcode, 1, Integer::sum);
    }
    @Override
    public int getPropertyViewCount(String propertyId) {
      return viewCounts.getOrDefault(propertyId, 0);
    }
    @Override
    public int getPostcodeSearchCount(String postcode) {
      return searchCounts.getOrDefault(postcode, 0);
    }
  }

  // --- Test 1 ---
  @Test
  public void testPurchaserMatchesListingInInterestedPostcode() {

    Property prop = new Property("P1", "2000", "500000",
            "1 Main St", "Sydney", "House", "2024-01-01", "2024-02-01");

    Listing listing = new Listing("P1", "2024-01-01", "500000");
    ListingPrice price = new ListingPrice("P1", "500000", "2024-01-01");
    listing.prices = List.of(price);

    Purchaser purchaser = new Purchaser("PU1", "Alice",
            "alice@example.com", List.of("2000", "3000"));

    NotificationService service = new NotificationService(
            new StubListingDAO(List.of(listing)),
            new StubPropertyDAO(Map.of("P1", prop)),
            new StubPurchaserDAO(List.of(purchaser)),
            new StubPropertyStatsDAO()  // ✅ added
    );

    Map<Purchaser, List<String[]>> result = service.getNotifications();

    assertEquals(1, result.size());
    List<String[]> matches = result.get(purchaser);
    assertNotNull(matches);
    assertEquals(1, matches.size());
    assertEquals("P1", matches.get(0)[0]);
    assertEquals("500000", matches.get(0)[1]);
  }

  // --- Test 2 ---
  @Test
  public void testPurchaserNoMatchWhenPostcodeDiffers() {

    Property prop = new Property("P2", "9999", "800000",
            "2 Other St", "Melbourne", "Apartment", "2024-01-01", "2024-02-01");

    Listing listing = new Listing("P2", "2024-01-01", "800000");
    listing.prices = List.of(new ListingPrice("P2", "800000", "2024-01-01"));

    Purchaser purchaser = new Purchaser("PU2", "Bob",
            "bob@example.com", List.of("2000"));

    NotificationService service = new NotificationService(
            new StubListingDAO(List.of(listing)),
            new StubPropertyDAO(Map.of("P2", prop)),
            new StubPurchaserDAO(List.of(purchaser)),
            new StubPropertyStatsDAO()  // ✅ added
    );

    Map<Purchaser, List<String[]>> result = service.getNotifications();

    List<String[]> matches = result.get(purchaser);
    assertTrue(matches.isEmpty());
  }

  // --- Test 3
  // Verifies hot property alerts fire when view count crosses threshold
  @Test
  public void testHotPropertyAlertFiresAboveThreshold() {

    Property prop = new Property("P3", "2000", "600000",
            "3 Hot St", "Sydney", "House", "2024-01-01", "2024-02-01");

    Listing listing = new Listing("P3", "2024-01-01", "600000");
    listing.prices = List.of(new ListingPrice("P3", "600000", "2024-01-01"));

    Purchaser purchaser = new Purchaser("PU3", "Carol",
            "carol@example.com", List.of("2000"));

    StubPropertyStatsDAO stubStats = new StubPropertyStatsDAO();

    NotificationService service = new NotificationService(
            new StubListingDAO(List.of(listing)),
            new StubPropertyDAO(Map.of("P3", prop)),
            new StubPurchaserDAO(List.of(purchaser)),
            stubStats
    );

    // Simulate 5 views — crosses the threshold of 5
    for (int i = 0; i < 5; i++) {
      stubStats.incrementPropertyView("P3");
    }

    Map<Purchaser, List<String>> alerts = service.getHotPropertyAlerts();

    assertNotNull(alerts.get(purchaser));
    assertEquals(1, alerts.get(purchaser).size());
    assertTrue(alerts.get(purchaser).get(0).contains("P3"));
  }

  // --- Test 4
  // Verifies trending postcode alerts fire when search count crosses threshold
  @Test
  public void testTrendingPostcodeAlertFiresAboveThreshold() {

    Purchaser purchaser = new Purchaser("PU4", "Dave",
            "dave@example.com", List.of("3000"));

    StubPropertyStatsDAO stubStats = new StubPropertyStatsDAO();

    NotificationService service = new NotificationService(
            new StubListingDAO(List.of()),
            new StubPropertyDAO(Map.of()),
            new StubPurchaserDAO(List.of(purchaser)),
            stubStats
    );

    // Simulate 3 searches — crosses the threshold of 3
    for (int i = 0; i < 3; i++) {
      stubStats.incrementPostcodeSearch("3000");
    }

    Map<Purchaser, List<String>> alerts = service.getTrendingPostcodeAlerts();

    assertNotNull(alerts.get(purchaser));
    assertEquals(1, alerts.get(purchaser).size());
    assertTrue(alerts.get(purchaser).get(0).contains("3000"));
  }
}