package property;

import java.util.List;
import java.util.ArrayList;

public class Purchaser {
  public String purchaserId;
  public String name;
  public String email;
  public String phone;
  public List<String> postcodes = new ArrayList<>(); // max 5

  public Purchaser(String name, String email, String phone) {
    this.name = name;
    this.email = email;
    this.phone = phone;
  }
}