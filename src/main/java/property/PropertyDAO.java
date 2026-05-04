package property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class PropertyDAO {

    // List to hold test data
    private List<Property> properties = new ArrayList<>();


    public PropertyDAO() {
        // create some test data
        Random rand = new Random();
        for (int i = 1; i < 51; i++) {
            String postcode = String.valueOf(rand.nextInt(301) + 2000);   // 2000–2300
            String price = String.valueOf(rand.nextInt(3500001) + 500000); // 500000–4000000
            properties.add(new Property(String.valueOf(i), postcode, price));
        }
    }

    public boolean newProperty(Property property) {
        properties.add(property);
        return true;
    }

    // returns Optional wrapping a Property if id is found, empty Optional otherwise
    public Optional<Property> getPropertyById(String propertyID) {
        for (Property p : properties) {
            if (p.propertyID.equals(propertyID)) {
                System.out.println("id found ");
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }

    // returns a List of properties in a given postCode
    public List<Property> getPropertiesByPostCode(String postCode) {
        System.out.println("postcode is: " + postCode);
        List<Property> tmp = new ArrayList<>();
        for (Property p : properties) {
            if (p.postcode.equals(postCode)) {
                tmp.add(p);
                System.out.println("postcode found ");
            }
        }
        return tmp == null ? Collections.emptyList() : tmp;
    }

    // returns the individual prices for all properties. Potentially large
    public List<String> getAllPropertyPrices() {
        return properties.stream()
                .map(e -> e.propertyPrice)
                .collect(Collectors.toList());
    }

    // returns all properties. Potentially large
    public List<Property> getAllProperties() {
        return properties.stream().collect(Collectors.toList());
    }

    // returns properties where propertyPrice is within [minPrice, maxPrice]
    public List<Property> getPropertiesByPriceRange(long minPrice, long maxPrice) {
        return properties.stream()
                .filter(p -> {
                    long price = Long.parseLong(p.propertyPrice);
                    return price >= minPrice && price <= maxPrice;
                })
                .collect(Collectors.toList());
    }
}
