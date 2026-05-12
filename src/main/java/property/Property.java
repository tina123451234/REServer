package property;

public class Property {
    public String propertyID;
    public String postcode;
    public String propertyPrice;
    public boolean forSale;
    public String address;
    public String councilName;
    public String propertyType;
    public String contractDate;
    public String settlementDate;

    public Property() {}

    public Property(String propertyID, String postcode, String propertyPrice,
                    String address, String councilName, String propertyType,
                    String contractDate, String settlementDate) {
        this.propertyID = propertyID;
        this.postcode = postcode;
        this.propertyPrice = propertyPrice;
        this.forSale = false;
        this.address = address;
        this.councilName = councilName;
        this.propertyType = propertyType;
        this.contractDate = contractDate;
        this.settlementDate = settlementDate;
    }
}