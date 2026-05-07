package campuscart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Cart {

    // key = lowercase item name so "Binder" and "binder" match
    private final Map<String, LineItem> rows = new LinkedHashMap<>();

    public int allPiecesCount() {
        int sum = 0;
        for (LineItem row : rows.values()) {
            sum += row.getHowMany();
        }
        return sum;
    }

    public BigDecimal goodsSubtotal() {
        BigDecimal sub = BigDecimal.ZERO;
        for (LineItem row : rows.values()) {
            sub = sub.add(row.lineSubtotal());
        }
        return Pricing.roundMoney(sub);
    }

    public List<LineItem> snapshotRows() {
        return new ArrayList<>(rows.values());
    }

    public int addPieces(String itemLabel, BigDecimal unitPrice, int qty) {
        String key = itemLabel.trim().toLowerCase();
        LineItem got = rows.get(key);
        if (got == null) {
            rows.put(key, new LineItem(itemLabel.trim(), unitPrice, qty));
        } else {
            got.setHowMany(got.getHowMany() + qty);
        }
        return allPiecesCount();
    }

    public boolean dropRow(String rawName) {
        String key = rawName.trim().toLowerCase();
        return rows.remove(key) != null;
    }

    public boolean changeQty(String rawName, int newQty) {
        String key = rawName.trim().toLowerCase();
        LineItem row = rows.get(key);
        if (row == null) {
            return false;
        }
        row.setHowMany(newQty);
        return true;
    }

    public LineItem lookup(String rawName) {
        return rows.get(rawName.trim().toLowerCase());
    }

    public void wipe() {
        rows.clear();
    }

    public boolean nothingInside() {
        return rows.isEmpty();
    }
}
