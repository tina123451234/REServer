package property;

public class ListingPrice {
  public String propertyID;
  public String price;
  public String priceDate;

  public ListingPrice(String propertyID, String price, String priceDate) {
    this.propertyID = propertyID;
    this.price = price;
    this.priceDate = priceDate;
  }
}