package campuscart;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void quitAfterStartup() {
        String out = MainIoHelper.runMain("Alex", "IL", "standard", "8");
        assertTrue(out.contains("Campus supply kiosk."));
        assertTrue(out.contains("Thanks for stopping by, Alex."));
        assertTrue(out.contains("----- MENU -----"));
    }

    @Test
    void askShippingAcceptsAliasesAndRetries() {
        String out = MainIoHelper.runMain("Sam", "TX", "nope", "s", "8");
        assertTrue(out.contains("I only understood STANDARD or NEXT"));
        assertTrue(out.contains("Thanks for stopping by, Sam."));
    }

    @Test
    void askShippingNextDayVariants() {
        String out = MainIoHelper.runMain("Pat", "NY", "overnight", "8");
        assertTrue(out.contains("Thanks for stopping by, Pat."));
    }

    @Test
    void askShippingStdAndNextAliases() {
        String out = MainIoHelper.runMain("Jo", "CA", "std", "6", "next day", "8");
        assertTrue(out.contains("Switch shipping"));
    }

    @Test
    void emptyMenuPickShowsHint() {
        String out = MainIoHelper.runMain("Kim", "IL", "standard", "", "8");
        assertTrue(out.contains("You pressed Enter without a choice"));
    }

    @Test
    void invalidMenuNumberRejected() {
        String out = MainIoHelper.runMain("Lee", "IL", "standard", "9", "8");
        assertTrue(out.contains("That is not a menu number"));
    }

    @Test
    void wordShortcutsWork() {
        String out = MainIoHelper.runMain(
                "Max", "IL", "standard",
                "add", "binder", "1",
                "total",
                "cart",
                "checkout",
                "done");
        assertTrue(out.contains("Added. You are now carrying 1 item(s)"));
        assertTrue(out.contains("CURRENT TOTAL"));
        assertTrue(out.contains("CART CONTENTS"));
        assertTrue(out.contains("transaction completed"));
        assertTrue(out.contains("Thanks for stopping by, Max."));
    }

    @Test
    void fullWidthDigitMenuPick() {
        String out = MainIoHelper.runMain("Rae", "IL", "standard", "\uFF13", "8");
        assertTrue(out.contains("CART CONTENTS"));
        assertTrue(out.contains("Nothing in the bag yet."));
    }

    @Test
    void menuDigitEmbeddedInText() {
        String out = MainIoHelper.runMain("Val", "IL", "standard", "option 2", "8");
        assertTrue(out.contains("Cart is empty"));
    }

    @Test
    void addItemSuccess() {
        String out = MainIoHelper.runMain("Ann", "IL", "standard", "1", "binder", "2", "8");
        assertTrue(out.contains("Added. You are now carrying 2 item(s)"));
        assertTrue(out.contains("[done] Syncing cart"));
    }

    @Test
    void addUnknownSkuRejected() {
        String out = MainIoHelper.runMain("Ben", "IL", "standard", "1", "widget", "8");
        assertTrue(out.contains("Never heard of that sku"));
    }

    @Test
    void addInvalidQuantityRejected() {
        String out = MainIoHelper.runMain("Cal", "IL", "standard", "1", "binder", "0", "8");
        assertTrue(out.contains("quantity has to be an integer"));
        String out2 = MainIoHelper.runMain("Cal", "IL", "standard", "1", "binder", "abc", "8");
        assertTrue(out2.contains("quantity has to be an integer"));
        String out3 = MainIoHelper.runMain("Cal", "IL", "standard", "1", "binder", "1.5", "8");
        assertTrue(out3.contains("quantity has to be an integer"));
    }

    @Test
    void addQuantityParsesFirstToken() {
        String out = MainIoHelper.runMain("Dan", "IL", "standard", "1", "binder", "2 extra", "8");
        assertTrue(out.contains("Added. You are now carrying 2 item(s)"));
    }

    @Test
    void addOverMaxOrderRejected() {
        String out = MainIoHelper.runMain("Eve", "IL", "standard", "1", "hoodie", "3000", "8");
        assertTrue(out.contains("order size before tax/shipping must land between"));
    }

    @Test
    void totalEmptyCart() {
        String out = MainIoHelper.runMain("Fin", "IL", "standard", "2", "8");
        assertTrue(out.contains("Cart is empty - nothing to total yet"));
    }

    @Test
    void totalWithItemsShowsBreakdown() {
        String out = MainIoHelper.runMain(
                "Gus", "IL", "standard",
                "1", "binder", "2",
                "2",
                "8");
        assertTrue(out.contains("Current total (tax and shipping included): $23.25"));
        assertTrue(out.contains("(goods $12.50 + tax $0.75 + shipping $10.00)"));
        assertTrue(out.contains("[done] Loading totals"));
    }

    @Test
    void totalSubtotalOutOfRangeShowsError() throws Exception {
        Cart bag = new Cart();
        injectOversizedLine(bag);
        String out = runMainWithPreloadedCart("Hal", "IL", "standard", bag, "2", "8");
        assertTrue(out.contains("goods subtotal must stay between"));
    }

    @Test
    void peekEmptyAndFilledCart() {
        String out = MainIoHelper.runMain(
                "Ian", "IL", "standard",
                "3",
                "1", "pencil", "3",
                "3",
                "8");
        assertTrue(out.contains("Nothing in the bag yet."));
        assertTrue(out.contains(" - pencil  x3"));
        assertTrue(out.contains("(subtotal of goods before tax/shipping)  $1.50"));
    }

    @Test
    void tweakQtySuccessAndFailures() {
        String out = MainIoHelper.runMain(
                "Jay", "IL", "standard",
                "1", "binder", "1",
                "4", "ghost", "4", "binder", "0", "4", "binder", "2",
                "8");
        assertTrue(out.contains("That item is not sitting in the cart."));
        assertTrue(out.contains("quantity has to be an integer"));
        assertTrue(out.contains("Quantity updated."));
    }

    @Test
    void tweakQtyOverMaxRejected() {
        String out = MainIoHelper.runMain(
                "Kay", "IL", "standard",
                "1", "hoodie", "1",
                "4", "hoodie", "3000",
                "8");
        assertTrue(out.contains("order size before tax/shipping must land between"));
    }

    @Test
    void removeLineSuccessAndFailure() {
        String out = MainIoHelper.runMain(
                "Lou", "IL", "standard",
                "1", "stapler", "1",
                "5", "stapler",
                "5", "stapler",
                "8");
        assertTrue(out.contains("Gone."));
        assertTrue(out.contains("Wasn't in there to begin with."));
    }

    @Test
    void checkoutClearsCart() {
        String out = MainIoHelper.runMain(
                "Mia", "IL", "standard",
                "1", "lanyard", "1",
                "7",
                "3",
                "8");
        assertTrue(out.contains("transaction completed"));
        assertTrue(out.contains("[done] Checkout loading"));
        assertTrue(out.contains("Nothing in the bag yet."));
    }

    @Test
    void readWholeQtyRejectsScientificNotation() throws Exception {
        Method m = Main.class.getDeclaredMethod("readWholeQty", String.class);
        m.setAccessible(true);
        assertNull(m.invoke(null, "1e2"));
        assertEquals(5, m.invoke(null, "5"));
        assertNull(m.invoke(null, "-1"));
    }

    @Test
    void normalizeMenuPickHandlesNullAndWords() throws Exception {
        Method m = Main.class.getDeclaredMethod("normalizeMenuPick", String.class);
        m.setAccessible(true);
        assertEquals("", m.invoke(null, (Object) null));
        assertEquals("", m.invoke(null, "   "));
        assertEquals("2", m.invoke(null, "sum"));
        assertEquals("2", m.invoke(null, "owe"));
        assertEquals("3", m.invoke(null, "list"));
        assertEquals("3", m.invoke(null, "show"));
        assertEquals("3", m.invoke(null, "contents"));
        assertEquals("1", m.invoke(null, "add"));
        assertEquals("7", m.invoke(null, "pay"));
        assertEquals("7", m.invoke(null, "checkout"));
        assertEquals("8", m.invoke(null, "exit"));
        assertEquals("foo", m.invoke(null, "foo"));
    }

    @Test
    void fullWidthDigitsConverted() throws Exception {
        Method m = Main.class.getDeclaredMethod("fullWidthDigitsToAscii", String.class);
        m.setAccessible(true);
        assertEquals("123", m.invoke(null, "\uFF11\uFF12\uFF13"));
        assertEquals("a1b", m.invoke(null, "a\uFF11b"));
        assertEquals("abc", m.invoke(null, "abc"));
        assertEquals("", m.invoke(null, ""));
    }

    @Test
    void sleepBriefRunsDelayWhenNotFastMode() throws Exception {
        String prev = System.getProperty("campuscart.fast.tests");
        System.setProperty("campuscart.fast.tests", "false");
        try {
            Method sleepBrief = Main.class.getDeclaredMethod("sleepBrief", long.class);
            sleepBrief.setAccessible(true);
            long start = System.nanoTime();
            sleepBrief.invoke(null, 60L);
            assertTrue(System.nanoTime() - start > 40_000_000L);
        } finally {
            if (prev == null) {
                System.clearProperty("campuscart.fast.tests");
            } else {
                System.setProperty("campuscart.fast.tests", prev);
            }
        }
    }

    @Test
    void sleepBriefHandlesInterrupt() throws Exception {
        String prev = System.getProperty("campuscart.fast.tests");
        System.setProperty("campuscart.fast.tests", "false");
        try {
            Method sleepBrief = Main.class.getDeclaredMethod("sleepBrief", long.class);
            sleepBrief.setAccessible(true);
            AtomicBoolean interrupted = new AtomicBoolean(false);
            CountDownLatch started = new CountDownLatch(1);
            Thread t = new Thread(() -> {
                started.countDown();
                try {
                    sleepBrief.invoke(null, 5000L);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                interrupted.set(Thread.currentThread().isInterrupted());
            });
            t.start();
            assertTrue(started.await(2, TimeUnit.SECONDS));
            t.interrupt();
            t.join(3000);
            assertTrue(interrupted.get());
        } finally {
            if (prev == null) {
                System.clearProperty("campuscart.fast.tests");
            } else {
                System.setProperty("campuscart.fast.tests", prev);
            }
        }
    }

    private static void injectOversizedLine(Cart bag) throws Exception {
        Field rowsField = Cart.class.getDeclaredField("rows");
        rowsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, LineItem> rows = (Map<String, LineItem>) rowsField.get(bag);
        rows.put("big", new LineItem("big", new BigDecimal("100000.00"), 1));
    }

    private static String runMainWithPreloadedCart(
            String name, String state, String ship, Cart unused, String... rest) throws Exception {
        Method printTotal = Main.class.getDeclaredMethod(
                "printRunningTotal", String.class, Pricing.ShipSpeed.class, Cart.class);
        printTotal.setAccessible(true);

        Cart bag = new Cart();
        injectOversizedLine(bag);

        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try {
            System.setOut(new PrintStream(capture, true, java.nio.charset.StandardCharsets.UTF_8));
            printTotal.invoke(null, state, Pricing.ShipSpeed.STANDARD, bag);
        } finally {
            System.setOut(original);
        }
        return capture.toString(java.nio.charset.StandardCharsets.UTF_8);
    }
}
