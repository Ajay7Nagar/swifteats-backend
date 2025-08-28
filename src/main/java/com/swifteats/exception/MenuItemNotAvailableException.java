package com.swifteats.exception;

/**
 * Exception thrown when menu item is not available
 */
public class MenuItemNotAvailableException extends SwiftEatsException {
    
    public MenuItemNotAvailableException(String itemName) {
        super("Menu item is not available: " + itemName, "MENU_ITEM_NOT_AVAILABLE");
    }
    
    public MenuItemNotAvailableException(Long itemId) {
        super("Menu item is not available with id: " + itemId, "MENU_ITEM_NOT_AVAILABLE");
    }
}

