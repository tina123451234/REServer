package property;

import java.util.List;

public class Purchaser {

  public String purchaserID;
  public String name;
  public String email;
  public List<String> interestedPostcodes;

  // REQUIRED for JSON deserialization
  public Purchaser() {
  }

  public Purchaser(
          String purchaserID,
          String name,
          String email,
          List<String> interestedPostcodes
  ) {
    this.purchaserID = purchaserID;
    this.name = name;
    this.email = email;
    this.interestedPostcodes = interestedPostcodes;
  }
}