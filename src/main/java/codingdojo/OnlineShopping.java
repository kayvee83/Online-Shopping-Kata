package codingdojo;

import java.util.ArrayList;

/**
 * The online shopping company owns a chain of Stores selling
 * makeup and beauty products.
 * <p>
 * Customers using the online shopping website can choose a Store then
 * can put Items available at that store into their Cart.
 * <p>
 * If no store is selected, then items are shipped from
 * a central warehouse.
 */
public class OnlineShopping {

    private Session session;

    public OnlineShopping(Session session) {
        this.session = session;
    }

    /**
     * This method is called when the user changes the
     * store they are shopping at in the online shopping
     * website.
     *
     */
    public void switchStore(Store storeToSwitchTo) {
        Cart cart = (Cart) session.get("CART");
        DeliveryInformation deliveryInformation = (DeliveryInformation) session.get("DELIVERY_INFO");
        if (storeToSwitchTo == null) {
            if (cart != null) {
                for (Item item : cart.getItems()) {
                    if ("EVENT".equals(item.getType())) {
                        cart.markAsUnavailable(item);
                    }
                }
            }
            if (deliveryInformation != null) {
                deliveryInformation.setType("SHIPPING");
                deliveryInformation.setPickupLocation(null);
            }
        } else {
            if (cart != null) {
                ArrayList<Item> newItems = new ArrayList<>();
                for (Item item : cart.getItems()) {
                    if ("EVENT".equals(item.getType())) {
                        Store currentStore = (Store) session.get("STORE");
                        if (storeToSwitchTo.hasItem(item)) {
                            cart.markAsUnavailable(item);
                            newItems.add(storeToSwitchTo.getItem(item.getName()));
                        } else {
                            cart.markAsUnavailable(item);
                            ((StoreEvent) item).setLocation(currentStore);
                        }
                    } else if (!storeToSwitchTo.hasItem(item)) {
                        cart.markAsUnavailable(item);
                    }
                }

                Store currentStore = (Store) session.get("STORE");
                if (deliveryInformation != null
                        && deliveryInformation.getType() != null
                        && "HOME_DELIVERY".equals(deliveryInformation.getType())
                        && deliveryInformation.getDeliveryAddress() != null) {
                    if (!((LocationService) session.get("LOCATION_SERVICE")).isWithinDeliveryRange(storeToSwitchTo, deliveryInformation.getDeliveryAddress())) {
                        deliveryInformation.setType("PICKUP");
                        deliveryInformation.setPickupLocation(storeToSwitchTo);
                    } else {
                        deliveryInformation.setPickupLocation(currentStore);
                    }
                } else {
                    if (deliveryInformation != null && deliveryInformation.getDeliveryAddress() != null) {
                        if (((LocationService) session.get("LOCATION_SERVICE")).isWithinDeliveryRange(storeToSwitchTo, deliveryInformation.getDeliveryAddress())) {
                            deliveryInformation.setType("HOME_DELIVERY");
                            deliveryInformation.setPickupLocation(currentStore);
                            long weight = 0;
                            for (Item item : cart.getItems()) {
                                weight += item.getWeight();
                            }
                            deliveryInformation.setTotalWeight(weight);
                        }
                    }
                }
                for (Item item : newItems) {
                    cart.addItem(item);
                }
            }
        }
        session.put("STORE", storeToSwitchTo);
        session.saveAll();
    }
}
