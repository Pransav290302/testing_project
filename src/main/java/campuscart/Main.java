package campuscart;

import java.math.BigDecimal;
import java.util.Scanner;

public class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Scanner kb = new Scanner(System.in);

        System.out.println("Campus supply kiosk.");
        System.out.print("Name for the order: ");
        String buyer = kb.nextLine().trim();

        System.out.print("Home state (use 2 letters like IL / TX): ");
        String state = kb.nextLine().trim();

        Pricing.ShipSpeed ship = askShipping(kb);
        Cart bag = new Cart();

        loadingSpinner("Starting kiosk", 8);

        boolean quit = false;
        while (!quit) {
            paintMenu();
            System.out.print("Pick a menu number (1-8 only): ");
            String pickRaw = kb.nextLine();
            String pick = normalizeMenuPick(pickRaw);

            if (pick.isEmpty()) {
                System.out.println("You pressed Enter without a choice - type 1 through 8.");
            } else if (pick.equals("1")) {
                addFlow(kb, bag);
            } else if (pick.equals("2")) {
                printRunningTotal(state, ship, bag);
            } else if (pick.equals("3")) {
                peekBag(bag);
            } else if (pick.equals("4")) {
                tweakQty(kb, bag);
            } else if (pick.equals("5")) {
                tossLine(kb, bag);
            } else if (pick.equals("6")) {
                ship = askShipping(kb);
            } else if (pick.equals("7")) {
                wrapUp(bag);
            } else if (pick.equals("8")) {
                quit = true;
            } else {
                System.out.println("That is not a menu number. Use 1-8 only here (item names are for option 1).");
            }
        }

        System.out.println("Thanks for stopping by, " + buyer + ".");
    }

    private static void paintMenu() {
        System.out.println();
        System.out.println("----- MENU -----");
        System.out.println("1) Add something to the cart");
        System.out.println("2) Get current total (tax + shipping counted in)");
        System.out.println("3) See what is in the cart");
        System.out.println("4) Edit quantity on a line");
        System.out.println("5) Remove a line from the cart");
        System.out.println("6) Switch shipping (Standard vs next day)");
        System.out.println("7) Checkout");
        System.out.println("8) Quit");
        System.out.println("Things we actually sell right now:");
        System.out.print(StoreCatalog.prettyMenu());
    }

    private static Pricing.ShipSpeed askShipping(Scanner kb) {
        while (true) {
            System.out.print("Shipping - type STANDARD or NEXT (next day): ");
            String words = kb.nextLine().trim().toLowerCase();
            if (words.equals("standard") || words.equals("std") || words.equals("s")) {
                return Pricing.ShipSpeed.STANDARD;
            }
            if (words.equals("next") || words.equals("next_day") || words.equals("next day")
                    || words.equals("overnight")) {
                return Pricing.ShipSpeed.NEXT_DAY;
            }
            System.out.println("I only understood STANDARD or NEXT. Give it another shot.");
        }
    }

    private static void addFlow(Scanner kb, Cart bag) {
        System.out.print("Which item name from the list: ");
        String name = kb.nextLine();
        BigDecimal price = StoreCatalog.lookupPrice(name);
        if (price == null) {
            System.out.println("Never heard of that sku - copy the name exactly.");
            return;
        }

        System.out.print("How many (whole numbers only): ");
        String rawQty = kb.nextLine().split("\\s+")[0].trim();
        Integer qty = readWholeQty(rawQty);
        if (qty == null) {
            System.out.println("Error: quantity has to be an integer that is at least 1.");
            return;
        }

        BigDecimal wouldBe = bag.goodsSubtotal().add(price.multiply(BigDecimal.valueOf(qty)));
        wouldBe = Pricing.roundMoney(wouldBe);
        if (!Pricing.okPurchaseSize(wouldBe)) {
            System.out.println("Error: order size before tax/shipping must land between "
                    + Pricing.MIN_ORDER + " and " + Pricing.MAX_ORDER + ".");
            return;
        }

        int pieces = bag.addPieces(name, price, qty);
        loadingSpinner("Syncing cart", 6);
        System.out.println("Added. You are now carrying " + pieces + " item(s) total in the cart.");
    }

    private static void printRunningTotal(String state, Pricing.ShipSpeed ship, Cart bag) {
        if (bag.nothingInside()) {
            System.out.println("---------- CURRENT TOTAL ----------");
            System.out.println("Cart is empty - nothing to total yet (subtotal is $0.00).");
            System.out.println("(Add items with menu 1. Checkout clears the cart.)");
            System.out.println("-----------------------------------");
            return;
        }
        BigDecimal goods = bag.goodsSubtotal();
        if (!Pricing.okPurchaseSize(goods)) {
            System.out.println("Error: goods subtotal must stay between "
                    + Pricing.MIN_ORDER + " and " + Pricing.MAX_ORDER + ".");
            return;
        }

        BigDecimal tax = Pricing.salesTax(state, goods);
        BigDecimal delivery = Pricing.shipFee(ship, goods);
        BigDecimal all = Pricing.orderTotal(state, ship, goods);

        loadingSpinner("Loading totals", 10);

        System.out.println("---------- CURRENT TOTAL ----------");
        System.out.println("Current total (tax and shipping included): $" + all.toPlainString());
        System.out.printf(
                "(goods $%s + tax $%s + shipping $%s)%n",
                goods.toPlainString(),
                tax.toPlainString(),
                delivery.toPlainString());
        System.out.println("-----------------------------------");
    }

    private static void peekBag(Cart bag) {
        if (bag.nothingInside()) {
            System.out.println("---------- CART CONTENTS ----------");
            System.out.println("Nothing in the bag yet.");
            System.out.println("-----------------------------------");
            return;
        }
        System.out.println("---------- CART CONTENTS ----------");
        for (LineItem row : bag.snapshotRows()) {
            System.out.printf(
                    " - %s  x%d  @ $%s  ->  $%s%n",
                    row.getLabel(),
                    row.getHowMany(),
                    row.getUnitPrice().toPlainString(),
                    row.lineSubtotal().toPlainString());
        }
        System.out.println("(subtotal of goods before tax/shipping)  $" + bag.goodsSubtotal().toPlainString());
        System.out.println("-----------------------------------");
    }

    private static void tweakQty(Scanner kb, Cart bag) {
        System.out.print("Which line do you want to fix: ");
        String name = kb.nextLine();
        LineItem row = bag.lookup(name);
        if (row == null) {
            System.out.println("That item is not sitting in the cart.");
            return;
        }

        System.out.print("New count: ");
        String rawQty = kb.nextLine().split("\\s+")[0].trim();
        Integer qty = readWholeQty(rawQty);
        if (qty == null) {
            System.out.println("Error: quantity has to be an integer that is at least 1.");
            return;
        }

        BigDecimal without = bag.goodsSubtotal().subtract(row.lineSubtotal());
        BigDecimal trial = Pricing.roundMoney(without.add(row.getUnitPrice().multiply(BigDecimal.valueOf(qty))));
        if (!Pricing.okPurchaseSize(trial)) {
            System.out.println("Error: order size before tax/shipping must land between "
                    + Pricing.MIN_ORDER + " and " + Pricing.MAX_ORDER + ".");
            return;
        }

        bag.changeQty(name, qty);
        System.out.println("Quantity updated.");
    }

    private static void tossLine(Scanner kb, Cart bag) {
        System.out.print("Name to delete: ");
        String name = kb.nextLine();
        if (bag.dropRow(name)) {
            System.out.println("Gone.");
        } else {
            System.out.println("Wasn't in there to begin with.");
        }
    }

    private static void wrapUp(Cart bag) {
        loadingSpinner("Checkout loading", 12);
        System.out.println("transaction completed");
        bag.wipe();
    }

    private static void sleepBrief(long ms) {
        if (Boolean.getBoolean("campuscart.fast.tests")) {
            return;
        }
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private static void loadingSpinner(String label, int ticks) {
        String[] frames = {"|", "/", "-", "\\"};
        StringBuilder pad = new StringBuilder();
        int wide = Math.max(40, label.length() + 24);
        for (int i = 0; i < wide; i++) {
            pad.append(' ');
        }
        for (int i = 0; i < ticks; i++) {
            String frame = frames[i % frames.length];
            System.out.print("\r[" + frame + "] " + label + " ..." + pad);
            System.out.flush();
            sleepBrief(110);
        }
        System.out.println("\r[done] " + label + " ... done" + pad);
    }

    private static Integer readWholeQty(String token) {
        try {
            if (token.contains(".") || token.toLowerCase().contains("e")) {
                return null;
            }
            int v = Integer.parseInt(token);
            if (v < 1) {
                return null;
            }
            return v;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String normalizeMenuPick(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return "";
        }
        s = fullWidthDigitsToAscii(s).trim().toLowerCase();

        if (s.equals("total") || s.equals("sum") || s.equals("owe")) {
            return "2";
        }
        if (s.equals("cart") || s.equals("contents") || s.equals("show") || s.equals("list")) {
            return "3";
        }
        if (s.equals("add")) {
            return "1";
        }
        if (s.equals("checkout") || s.equals("pay")) {
            return "7";
        }
        if (s.equals("quit") || s.equals("exit") || s.equals("done")) {
            return "8";
        }

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '1' && c <= '8') {
                return String.valueOf(c);
            }
        }

        return s;
    }

    private static String fullWidthDigitsToAscii(String input) {
        StringBuilder b = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (ch >= '\uFF10' && ch <= '\uFF19') {
                b.append((char) ('0' + (ch - '\uFF10')));
            } else {
                b.append(ch);
            }
        }
        return b.toString();
    }
}
