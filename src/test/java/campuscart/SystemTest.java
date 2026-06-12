package campuscart;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SystemTest {

    @Test
    void fullShoppingSessionWithShippingChange() {
        String out = MainIoHelper.runMain(
                "Morgan", "NY", "standard",
                "1", "hoodie", "1",
                "1", "flashdrive", "1",
                "6", "next_day",
                "2",
                "3",
                "7",
                "8");

        assertTrue(out.contains("hoodie"));
        assertTrue(out.contains("flashdrive"));
        assertTrue(out.contains("Switch shipping"));
        assertTrue(out.contains("shipping $25.00"));
        assertTrue(out.contains("transaction completed"));
    }

    @Test
    void allCatalogSkusArePurchasable() {
        String[] skus = {
                "binder", "flashdrive", "hoodie", "calculator", "sketchbook",
                "pencil", "highlighter", "waterbottle", "stapler", "lanyard", "earplugs"
        };
        for (String sku : skus) {
            assertNotNull(StoreCatalog.lookupPrice(sku), "missing sku: " + sku);
        }
        assertTrue(StoreCatalog.prettyMenu().contains("earplugs"));
    }

    @Test
    void standardShippingFreeAboveFiftyDollars() {
        String out = MainIoHelper.runMain(
                "Quinn", "IL", "standard",
                "1", "hoodie", "2",
                "2",
                "8");

        assertTrue(out.contains("shipping $0.00"));
    }

    @Test
    void menuWordAliasesRouteCorrectly() {
        String out = MainIoHelper.runMain(
                "Sky", "IL", "standard",
                "1", "stapler", "1",
                "owe",
                "show",
                "contents",
                "quit");

        assertTrue(out.contains("CURRENT TOTAL"));
        assertTrue(out.contains("CART CONTENTS"));
        assertTrue(out.contains("Thanks for stopping by, Sky."));
    }

    @Test
    void shippingAliasesAtStartup() {
        String next = MainIoHelper.runMain("A", "IL", "next", "8");
        String nextDay = MainIoHelper.runMain("B", "IL", "next_day", "8");
        assertTrue(next.contains("Thanks for stopping by, A."));
        assertTrue(nextDay.contains("Thanks for stopping by, B."));
    }
}
