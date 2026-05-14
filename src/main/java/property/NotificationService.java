package property;

import java.util.*;

public class NotificationService {

  private final ListingDAO listingDAO;
  private final PropertyDAO propertyDAO;
  private final PurchaserDAO purchaserDAO;

  public NotificationService(ListingDAO listingDAO,
                             PropertyDAO propertyDAO,
                             PurchaserDAO purchaserDAO) {
    this.listingDAO = listingDAO;
    this.propertyDAO = propertyDAO;
    this.purchaserDAO = purchaserDAO;
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
}