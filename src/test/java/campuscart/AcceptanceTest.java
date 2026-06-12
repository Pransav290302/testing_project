package campuscart;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AcceptanceTest {

    @Test
    void customerCanPurchaseWithTaxAndStandardShipping() {
        String out = MainIoHelper.runMain(
                "Jordan", "IL", "standard",
                "1", "calculator", "1",
                "2",
                "7",
                "8");

        assertTrue(out.contains("Added. You are now carrying 1 item(s)"));
        assertTrue(out.contains("Current total (tax and shipping included): $39.15"));
        assertTrue(out.contains("transaction completed"));
        assertTrue(out.contains("Thanks for stopping by, Jordan."));
    }

    @Test
    void customerInTexasPaysNoTax() {
        String out = MainIoHelper.runMain(
                "Riley", "TX", "standard",
                "1", "binder", "10",
                "2",
                "8");

        assertTrue(out.contains("(goods $62.50 + tax $0.00 + shipping $0.00)"));
    }

    @Test
    void nextDayShippingAlwaysCostsTwentyFive() {
        String out = MainIoHelper.runMain(
                "Casey", "CA", "next",
                "1", "sketchbook", "5",
                "2",
                "8");

        assertTrue(out.contains("(goods $55.45 + tax $3.33 + shipping $25.00)"));
    }

    @Test
    void invalidQuantityShowsError() {
        String out = MainIoHelper.runMain(
                "Drew", "NY", "standard",
                "1", "highlighter", "-3",
                "8");

        assertTrue(out.contains("quantity has to be an integer"));
    }

    @Test
    void orderBelowMinimumDollarRejected() {
        String out = MainIoHelper.runMain(
                "Emery", "IL", "standard",
                "1", "pencil", "1",
                "8");

        assertTrue(out.contains("order size before tax/shipping must land between"));
    }

    @Test
    void cartEditAndRemoveWorkflow() {
        String out = MainIoHelper.runMain(
                "Frankie", "IL", "standard",
                "1", "waterbottle", "2",
                "4", "waterbottle", "1",
                "3",
                "5", "waterbottle",
                "3",
                "8");

        assertTrue(out.contains("Quantity updated."));
        assertTrue(out.contains(" - waterbottle  x1"));
        assertTrue(out.contains("Gone."));
        assertTrue(out.contains("Nothing in the bag yet."));
    }
}
