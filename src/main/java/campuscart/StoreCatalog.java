package campuscart;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class StoreCatalog {

    private StoreCatalog() {
    }

    private static final Map<String, BigDecimal> TAG_TO_PRICE = new LinkedHashMap<>();

    static {
        TAG_TO_PRICE.put("binder", new BigDecimal("6.25"));
        TAG_TO_PRICE.put("flashdrive", new BigDecimal("13.40"));
        TAG_TO_PRICE.put("hoodie", new BigDecimal("44.95"));
        TAG_TO_PRICE.put("calculator", new BigDecimal("27.50"));
        TAG_TO_PRICE.put("sketchbook", new BigDecimal("11.09"));
        TAG_TO_PRICE.put("pencil", new BigDecimal("0.50"));
        TAG_TO_PRICE.put("highlighter", new BigDecimal("2.75"));
        TAG_TO_PRICE.put("waterbottle", new BigDecimal("8.99"));
        TAG_TO_PRICE.put("stapler", new BigDecimal("16.33"));
        TAG_TO_PRICE.put("lanyard", new BigDecimal("3.10"));
        TAG_TO_PRICE.put("earplugs", new BigDecimal("4.49"));
    }

    public static BigDecimal lookupPrice(String typed) {
        if (typed == null) {
            return null;
        }
        return TAG_TO_PRICE.get(typed.trim().toLowerCase());
    }

    public static String prettyMenu() {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String, BigDecimal> e : TAG_TO_PRICE.entrySet()) {
            b.append("   * ")
                    .append(e.getKey())
                    .append(" ... $")
                    .append(e.getValue())
                    .append(System.lineSeparator());
        }
        return b.toString();
    }
}
