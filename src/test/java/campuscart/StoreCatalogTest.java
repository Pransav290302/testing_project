package campuscart;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class StoreCatalogTest {

    @Test
    void lookupPriceNull() {
        assertNull(StoreCatalog.lookupPrice(null));
    }

    @Test
    void lookupPriceKnownItemCaseInsensitive() {
        assertEquals(new BigDecimal("6.25"), StoreCatalog.lookupPrice("binder"));
        assertEquals(new BigDecimal("6.25"), StoreCatalog.lookupPrice("  BINDER  "));
    }

    @Test
    void lookupPriceUnknown() {
        assertNull(StoreCatalog.lookupPrice("not-a-real-sku"));
    }

    @Test
    void prettyMenuListsInventory() {
        String menu = StoreCatalog.prettyMenu();
        assertTrue(menu.contains("binder"));
        assertTrue(menu.contains("$6.25"));
        assertTrue(menu.contains("hoodie"));
        assertTrue(menu.contains("earplugs"));
        assertTrue(menu.contains(System.lineSeparator()));
    }

    @Test
    void lookupPriceEverySku() {
        assertEquals(new BigDecimal("13.40"), StoreCatalog.lookupPrice("flashdrive"));
        assertEquals(new BigDecimal("44.95"), StoreCatalog.lookupPrice("hoodie"));
        assertEquals(new BigDecimal("27.50"), StoreCatalog.lookupPrice("calculator"));
        assertEquals(new BigDecimal("11.09"), StoreCatalog.lookupPrice("sketchbook"));
        assertEquals(new BigDecimal("0.50"), StoreCatalog.lookupPrice("pencil"));
        assertEquals(new BigDecimal("2.75"), StoreCatalog.lookupPrice("highlighter"));
        assertEquals(new BigDecimal("8.99"), StoreCatalog.lookupPrice("waterbottle"));
        assertEquals(new BigDecimal("16.33"), StoreCatalog.lookupPrice("stapler"));
        assertEquals(new BigDecimal("3.10"), StoreCatalog.lookupPrice("lanyard"));
        assertEquals(new BigDecimal("4.49"), StoreCatalog.lookupPrice("earplugs"));
    }
}
