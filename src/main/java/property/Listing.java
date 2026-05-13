package property;

import java.util.List;
import java.util.ArrayList;

public class Listing {
  public String propertyId;
  public String dateListed;
  public String initialPrice;
  public List<ListingPrice> prices = new ArrayList<>();

  public Listing(String propertyId, String dateListed, String initialPrice) {
    this.propertyId = propertyId;
    this.dateListed = dateListed;
    this.initialPrice = initialPrice;
  }

  public Listing() {
  }

}

