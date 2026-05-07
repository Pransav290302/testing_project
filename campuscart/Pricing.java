package campuscart;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Handles cents rounding plus the tax/shipping rules from the project brief.
 */
public class Pricing {

    public static final BigDecimal MIN_ORDER = new BigDecimal("1.00");
    public static final BigDecimal MAX_ORDER = new BigDecimal("99999.99");

    private static final BigDecimal TAX_RATE = new BigDecimal("0.06");
    private static final String[] TAXED = {"IL", "CA", "NY"};

    private static final BigDecimal STD_SHIP = new BigDecimal("10.00");
    private static final BigDecimal STD_CUT = new BigDecimal("50.00");
    private static final BigDecimal NEXT_SHIP = new BigDecimal("25.00");

    public enum ShipSpeed {
        STANDARD,
        NEXT_DAY
    }

    public static BigDecimal roundMoney(BigDecimal n) {
        return n.setScale(2, RoundingMode.HALF_UP);
    }

    public static boolean okPurchaseSize(BigDecimal goodsBeforeTax) {
        BigDecimal g = roundMoney(goodsBeforeTax);
        return g.compareTo(MIN_ORDER) >= 0 && g.compareTo(MAX_ORDER) <= 0;
    }

    public static BigDecimal salesTax(String state, BigDecimal goodsBeforeTax) {
        if (state == null || goodsBeforeTax == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        String code = taxStateCode(state);
        if (code == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return roundMoney(goodsBeforeTax.multiply(TAX_RATE));
    }

    public static BigDecimal shipFee(ShipSpeed how, BigDecimal goodsBeforeTax) {
        if (how == null || goodsBeforeTax == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (how == ShipSpeed.NEXT_DAY) {
            return NEXT_SHIP.setScale(2, RoundingMode.HALF_UP);
        }
        // STANDARD: free only when raw cart is strictly over $50
        if (goodsBeforeTax.compareTo(STD_CUT) > 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return STD_SHIP.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal orderTotal(String state, ShipSpeed how, BigDecimal goodsBeforeTax) {
        BigDecimal tax = salesTax(state, goodsBeforeTax);
        BigDecimal ship = shipFee(how, goodsBeforeTax);
        return roundMoney(goodsBeforeTax.add(tax).add(ship));
    }

    /**
     * Accepts IL/CA/NY or the full names from the project PDF so totals do not look "wrong"
     * if someone types Illinois instead of IL.
     */
    private static String taxStateCode(String raw) {
        String s = raw.trim().toUpperCase().replace(".", "");
        if (s.equals("IL") || s.equals("ILLINOIS")) {
            return "IL";
        }
        if (s.equals("CA") || s.equals("CALIFORNIA")) {
            return "CA";
        }
        if (s.equals("NY") || s.equals("NEW YORK") || s.equals("NEWYORK")) {
            return "NY";
        }
        if (inList(TAXED, s)) {
            return s;
        }
        return null;
    }

    private static boolean inList(String[] xs, String v) {
        for (String x : xs) {
            if (x.equals(v)) {
                return true;
            }
        }
        return false;
    }
}
