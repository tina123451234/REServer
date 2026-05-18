package property;

import java.util.*;

public class NotificationService {

  private final ListingDAO listingDAO;
  private final PropertyDAO propertyDAO;
  private final PurchaserDAO purchaserDAO;
  private final PropertyStatsDAO statsDAO;

  public NotificationService(ListingDAO listingDAO,
                             PropertyDAO propertyDAO,
                             PurchaserDAO purchaserDAO,
                             PropertyStatsDAO statsDAO) {
    this.listingDAO = listingDAO;
    this.propertyDAO = propertyDAO;
    this.purchaserDAO = purchaserDAO;
    this.statsDAO = statsDAO;
  }

  // Returns map of purchaser -> list of matching [propertyId, price] pairs
  public Map<Purchaser, List<String[]>> getNotifications() {
    Map<Purchaser, List<String[]>> result = new LinkedHashMap<>();

    List<Listing> listings = listingDAO.getAllListings();
    List<Purchaser> purchasers = purchaserDAO.getAllPurchasers();

    for (Purchaser purchaser : purchasers) {
      List<String[]> matches = new ArrayList<>();

      for (Listing listing : listings) {
        Optional<Property> prop = propertyDAO.getPropertyById(listing.propertyId);
        if (prop.isEmpty()) continue;

        String postcode = prop.get().postcode;
        String latestPrice = listing.prices.isEmpty()
                ? "N/A"
                : listing.prices.get(listing.prices.size() - 1).price;

        if (purchaser.interestedPostcodes != null
                && purchaser.interestedPostcodes.contains(postcode)) {
          matches.add(new String[]{listing.propertyId, latestPrice});
        }
      }

      result.put(purchaser, matches);
    }

    return result;
  }
  private static final int VIEW_THRESHOLD = 5;
  private static final int SEARCH_THRESHOLD = 3;

  // Notify purchasers about properties that have been viewed heavily
  public Map<Purchaser, List<String>> getHotPropertyAlerts() {
    Map<Purchaser, List<String>> alerts = new LinkedHashMap<>();
    List<Listing> listings = listingDAO.getAllListings();
    List<Purchaser> purchasers = purchaserDAO.getAllPurchasers();

    for (Purchaser purchaser : purchasers) {
      List<String> hotMatches = new ArrayList<>();

      for (Listing listing : listings) {
        int views = statsDAO.getPropertyViewCount(listing.propertyId);
        if (views < VIEW_THRESHOLD) continue;

        Optional<Property> prop = propertyDAO.getPropertyById(listing.propertyId);
        if (prop.isEmpty()) continue;

        String postcode = prop.get().postcode;
        if (purchaser.interestedPostcodes != null
                && purchaser.interestedPostcodes.contains(postcode)) {
          hotMatches.add("Property " + listing.propertyId
                  + " in " + postcode
                  + " has been viewed " + views + " times!");
        }
      }

      if (!hotMatches.isEmpty()) {
        alerts.put(purchaser, hotMatches);
      }
    }
    return alerts;
  }

  // Notify purchasers about postcodes that are being searched heavily
  public Map<Purchaser, List<String>> getTrendingPostcodeAlerts() {
    Map<Purchaser, List<String>> alerts = new LinkedHashMap<>();
    List<Purchaser> purchasers = purchaserDAO.getAllPurchasers();

    for (Purchaser purchaser : purchasers) {
      List<String> trending = new ArrayList<>();

      if (purchaser.interestedPostcodes == null) continue;

      for (String postcode : purchaser.interestedPostcodes) {
        int searches = statsDAO.getPostcodeSearchCount(postcode);
        if (searches >= SEARCH_THRESHOLD) {
          trending.add("Postcode " + postcode
                  + " is trending — searched " + searches + " times!");
        }
      }

      if (!trending.isEmpty()) {
        alerts.put(purchaser, trending);
      }
    }
    return alerts;
  }

}