package property;

import java.util.List;
import java.util.ArrayList;

public class Listing {
  public String propertyId;
  public String dateListed;
  public List<ListingPrice> prices = new ArrayList<>();

  public Listing(String propertyId, String dateListed) {
    this.propertyId = propertyId;
    this.dateListed = dateListed;
  }
}